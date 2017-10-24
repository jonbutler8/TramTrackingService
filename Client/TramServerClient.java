package tramsimulate;
import java.rmi.NotBoundException; 
import java.rmi.RemoteException; 
import java.rmi.registry.LocateRegistry; 
import java.rmi.registry.Registry;


/*** Multithreaded class that retrieves the remote server object and then
 *   creates NUM_TRAMS tram simulations, passing them the server object so 
 *   they can independently communicate with the server. This is the server-side
 *   top level class ***/
public class TramServerClient {
    // Hardcoded RMI url for retrieving the tram server object
    public static String url = "rmi://localhost/s3438465/TramServer/";
    public static int port = 8464; // Port to use for RMI communications
    public static int NUM_TRAMS = 5;
    
    public static void main(String[] args) {
        try{
            //Get the RMI registry created by the server
            Registry registry = LocateRegistry.getRegistry(null, port);
            // Retrieve the remote server object
            TramServer remoteServer = (TramServer) registry.lookup(url);
            
            // Create NUM_TRAMS tram simulation threads, passing the server
            for (int i = 0; i < NUM_TRAMS; i++) {
                new Tram(remoteServer);
            }
            
        // Exists if the registry can't be reached
        } catch (RemoteException ex) {
            System.err.println("Couldn't contact registry.");
            System.err.println(ex);
            System.exit(1);
        // Exits if the registry can be reached but the server object doesn't exist
        } catch (NotBoundException ex) {
            System.err.println("There is no object bound to " + url);
            System.exit(1);
        }
    }

}
