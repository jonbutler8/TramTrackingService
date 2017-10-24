package tramsimulate;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.io.IOException;

/** Main server class. Creates and registers a remote server object to 
 *  process requests from connecting clients **/
public class TramServerImpl implements TramServer{
    // Hardcoded RMI url of the server
    public static final String URL = "rmi://localhost/s3438465/TramServer/";
    private static int port;
    
    // Process client communications and returns replies
    private static ServerCommsStub handler; 
    
    // Singleton instance
    private static final TramServer instance = new TramServerImpl(port);
    
    
    private TramServerImpl(int port) {
        // Set the server.policy path for allowing RMI communications
        System.setProperty("java.security.policy","./server.policy");
        
        // Create the communications class, passing it a new tram system class
        handler = new ServerCommsStub(new RouteManager());	
    }
    
    public static TramServer getInstance() {
        return instance;
    }

    
    /** Main server entry method. Some code adapted from the Week 3 lab code **/
    public static void main(String[] args) {
    	try {
	        // Kill the registry if it was created by a previous terminated instance
	        try {
	            UnicastRemoteObject.unexportObject(LocateRegistry.getRegistry(port), true);
	            System.out.println("Existing registry object deleted");
	        }
	        catch (NoSuchObjectException e){
	            System.out.println("No existing registry object.");
	        }
	        
	        port = Integer.parseInt(args[0]);
	        

			TramServer server = getInstance();
			// Register the the tram server for RMI usage
			TramServer stub = (TramServer) UnicastRemoteObject.exportObject(server, port); 
			Registry registry = LocateRegistry.createRegistry(port);
			// Bind the server to the hardcoded url
			registry.rebind(URL, (TramServer) stub);
			
			// Print the success message
			System.out.println("Server bound to: " + URL);
	
    	// Exit if the registry can't be reached		
		} catch (RemoteException ex) {
			System.err.println("Couldn't contact rmiregistry.");
			ex.printStackTrace(); 
			System.exit(1);		
		}
    }

    // Method called by remote clients for communications
    public Message makeRequest(Message request) throws RemoteException {
        try {
            // Attempt to process a client request and return the serialized result
            return handler.processMessage(request);

        // If a fatal marshalling error occurs such that the client cannot be properly
        // responded to, print an error and throw a remote exception
        } catch (IOException e) {
            String error = "Could not process request. Failure in marshalling or unmarshalling data";
            e.printStackTrace();
            throw new RemoteException(error);
        }
    }
    
}
