package tramsimulate;


import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/*** Helper class for handling communications between replication managers ***/
public class ReplicationCommsManager {
    public static final int MAX_SEND_ATTEMPTS = 5;
    
    private TramServer[] replications; //Replication manager remote object array
    private String rmiUrl; // The rmi url all replication managers use
    private boolean operational = false; // Indicates whether at least one server is available
    private int[] rmiPorts; // Unique ports for each replication manager registry
    
    /*** Initializes communication variables with passed parameters 
     * @param ports - integer array of port numbers for each rmi instance
     * @param url   - string of the rmi url all replication managers use
     */
    public ReplicationCommsManager(int[] ports, String url) {
        // Set the server.policy path for allowing RMI communications
        System.setProperty("java.security.policy","./server.policy");
        replications = new TramServer[ports.length];
        rmiPorts = ports;
        rmiUrl = url;
    }

    /*** Replicates the passed client request by first checking which servers
     *   are active and then passing the message to all active servers
     * @param - Marshalled RPCMessage containing request parameters
     * @return - Marshalled RPCMessage containing reply parameters
     * @throws RemoteException thrown on communication failure with RMs
     * @throws IOException thrown on unmarshalling failure of request
     */
    public Message replicateRequest(Message request) throws RemoteException, IOException  {
        // Find which replication managers are active and print the result
        listTramService();
        Message reply = null;

        // Proceed only if at least one replication manager is active
        if (operational) {
            // Check if message can be unmarshalled correctly before attempting to pass to RMs
            request.unmarshal();
            
            // Get a reply from all the active replication managers, taking only one
            reply = getAllReplies(request);
            // Null result indicates no server could process request
            if (reply == null) {
                operational = false;
            }
        }
        // Throw appropriate exception when no replication managers are available
        if (!operational) {
            throw new RemoteException("Cannot process request: all replications down");
        }

        return reply;
    }
    
    /*** Attempts to communicate the passed request to each communication 
     *   manager. If a communication fails, the remote object is nulled in
     *   the replication manager array to indicate its down status.
     * @param request - Marshalled RPCMessage containing request parameters
     * @return Marshalled RPCMessage containing reply parameters, null on 
     *         failure
     */
    private Message getAllReplies(Message request) {
        // Initially assume the request failed
        Message reply = null;
        
        // Attempt to contact each replication
        for (int i = 0; i < replications.length; i++) {
            // Do not attempt if the replication manager is already down
            if (replications[i] != null) {
                Message thisReply = getOneReply(request, replications[i]);
                // Null result indicates replication manager is down
                if (thisReply == null) {
                    replications[i] = null;
                    System.out.printf("RM%d now down\n", i+1);
                }
                // Set the reply to the first non-null reply received
                else if (reply == null) {
                    reply = thisReply;
                }
            }
        }
        
        return reply;
    }
    
    /*** Attempts to communicate the passed request to a single communication 
     *   manager, returning the reply. If a communication fails, the return value is null.
     * @param request - Marshalled RPCMessage containing request parameters
     * @param server - Remote object of the replication manager to process the request
     * @return Marshalled RPCMessage containing reply parameters, null on 
     *         failure
     */
    private Message getOneReply(Message request, TramServer server) {
        Message reply = null;
        // Keep track of the number of retries
        short tryCount = 0;
        
        do {
            try {
                // Attempt to make the request
                reply = server.makeRequest(request);
            }
            // Print a message on failure
            catch (Exception e) {
                System.err.println("Error communicating with TramServer: " + e.getMessage());
            }
            finally {
                tryCount++;
            }
            // Continue looping until we receive a reply, or until we have reached
            // the maximum number of retries
        } while (reply == null && tryCount < MAX_SEND_ATTEMPTS);
        
        return reply;
    }
    
    /*** Checks the status of the replication managers, printing the result ***/
    private void listTramService() {
        // First check each server's status
        operational = checkReplicatedStatus();
        
        // Print the result
        for (int i = 0; i < replications.length; i++) {
            if (i != 0) {
                System.out.printf(" | ");
            }
            System.out.printf("RM%d %3s", i+1, replications[i] == null ? "off" : "on");
        }
        System.out.println();
    }

    /*** Checks the status of each replication manager, storing the status of 
     *   each in the replication manager array. Returns false if all replications
     *   are down
     * @return boolean value of whether at least one replication is active
     */
    private boolean checkReplicatedStatus() {
        boolean systemOperable = false;
        
        //Get the RMI registers created by the replication managers
        for (int i = 0; i < replications.length; i++) {
            try {
                Registry registry = LocateRegistry.getRegistry(null, rmiPorts[i]);
                // Retrieve the remote server object
                replications[i] = (TramServer) registry.lookup(rmiUrl);
                
                //If we reached this point without an exception, the system is operable
                systemOperable = true;

            } catch (RemoteException ex) {
                //System.err.println("Couldn't contact registry.");
                replications[i] = null;
            // If the registry can be reached but the server object doesn't exist
            } catch (NotBoundException ex) {
                replications[i] = null;
                //System.err.println("There is no object bound to " + rmiUrl);
            } catch (Exception e) {
                //System.err.println("Error communicating with RM: " + e.getMessage());
            }
        }

        return systemOperable;
    }
}
