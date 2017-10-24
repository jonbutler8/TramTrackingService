package tramsimulate;
import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException; 
import java.rmi.registry.LocateRegistry; 
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


/*** Front-end server class that registers itself as a remote server object
 *   on its own registry and forwards requests from tram clients to the 
 *   replication servers that are active. Functions as long as at least one
 *   replication server is running. 
 *   */
public class ReplicationDriver implements TramServer {
    // Hardcoded RMI url for retrieving the tram server object
    public static String url = "rmi://localhost/s3438465/TramServer/";
    public static int selfPort = 8464;
    public static int[] ports = {8465, 8466, 8467}; // Ports to use for RMI communications
    public static int NUM_TRAMS = 5;
    // Singleton instance of the class
    private static final ReplicationDriver instance = new ReplicationDriver();
    
    
    private ReplicationCommsManager comms;
    
    // Private constructor for singleton pattern
    private ReplicationDriver() {
        comms = new ReplicationCommsManager(ports, url);
    }
    
    // Method for retrieving the singleton instance
    public static ReplicationDriver getInstance() {
        return instance;
    }
    
    /*** FE server entry method. Retrieves the singleton instance if it 
     * exists and registers it as a remote object in the RMI registry so that
     * it can receive requests from remote clients */
    public static void main(String[] args) {
        try {
            // Kill the registry if it was created by a previous terminated instance
            try {
                UnicastRemoteObject.unexportObject(LocateRegistry.getRegistry(selfPort), true);
                System.out.println("Existing registry object deleted");
            }
            catch (NoSuchObjectException e){
                System.out.println("No existing registry object.");
            }
            
            // Get the server instance
            TramServer server = getInstance();
            
            // Register the front end for RMI usage
            UnicastRemoteObject.exportObject((TramServer)server, selfPort); 
            
            Registry registry = LocateRegistry.createRegistry(selfPort);
            
            // Bind the server to the hardcoded url
            registry.rebind(url, server);
            
            // Print the success message
            System.out.println("Front end bound to: " + url);
            
            
    
        // Exit if the registry can't be reached        
        } catch (RemoteException ex) {
            System.err.println("Couldn't contact rmiregistry.");
            ex.printStackTrace(); 
            System.exit(1);     
        }
    }

    /*** Implementation of TramServer interface which replicates messages
     *   from remote clients to three replication managers 
     *   @param request - Marshalled RPCMessage containing request parameters
     */
    public Message makeRequest(Message request) throws RemoteException {
        Message reply = null;
        
        // Attempt to replicate the request
        try {
            reply = comms.replicateRequest(request);
        }
        // Throws remote exceptions back to client so they are not interpreted
        // as IOException marshalling failures by the check below
        catch (RemoteException e) {
            System.out.println(e.getMessage());
            throw e;
        }
        // Other IOException indicates unmarshalling failure of request, sent
        // request is invalid/corrupted
        catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RemoteException("Unmarshalling failure");
        }

        
        return reply;
    }

}
