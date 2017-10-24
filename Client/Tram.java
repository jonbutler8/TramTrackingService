package tramsimulate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;


/** Class for simulating a single tram instance in a thread 
 *  Upon creation, registers itself with the passed server stub and retrieves 
 *  a start stop and route and then repeatedly makes two request types: 
 *  getting the next stop and updating its location on the server. 
 *  Sleeps for 10-20 seconds (randomly chosen at every sleep interval) after
 *  successfully processing both request types.  */
public class Tram implements Runnable {
    public static int TEN_SECONDS_MILLIS = 10000; // Constant for sleep calculation
    
    // Hard-coded route values for choosing initial location
    public static final int[] ROUTES = {1, 96, 101, 109, 112};
    public static final int[] FIRST_STOPS = {1, 23, 123, 88, 110};
    public static final int[] LAST_STOPS = {5, 22, 7, 1, 4};
    
    private long tramID;
    private int currentStop; // Number ID of the tram's current stop
    private int previousStop; // Number ID of the tram's previous stop
    private int routeID;  // Number ID of the tram's route

    private TramCommsStub stub; // Client stub for server communication
    
    
    // Constructs the tram object and then begins a new thread
    // Uses the passed remote server to make requests
    public Tram(TramServer server) {
        
        // Create a client stub, passing the server object for communications
        this.stub = new TramCommsStub(server);
        
        // Random tram ID. Assumed to be unique across tram instances.
        tramID = ThreadLocalRandom.current().nextLong();
        
        previousStop = 0;
        
        // Randomly choose a route
        int routeIndex = ThreadLocalRandom.current().nextInt(ROUTES.length);
        routeID = ROUTES[routeIndex];
        
        // Randomly choose whether to begin at the left or right side of the route
        currentStop = ThreadLocalRandom.current().nextBoolean() ? 
                FIRST_STOPS[routeIndex] : LAST_STOPS[routeIndex];
        
       
        // Print the initial position
        printPosition();
        
        // Begin the tram simulation in a new thread        
        new Thread(this).start();
    }
    
    // Print tram status string
    public void printPosition() {
        LocalDateTime stamp = LocalDateTime.now();
        System.out.printf("%s Tram %17s on route %3d now at stop %3d\n",
                stamp.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:SS|")), 
                Long.toHexString(tramID), routeID, currentStop);
    }
    
    // Update the tram's position to the new stop and print the new location
    private void updateStop(int newStop) {
        previousStop = currentStop;
        currentStop = newStop;
        printPosition();
    }

    // Thread wrapper for tram simulation
    public void run() {
       try {
           simulateTram();
        } catch (InterruptedException e) {
            // Error upon thread interruption
            System.out.println("Simulation of tram " + tramID + "interrupted");
            e.printStackTrace();
        }
    }
    
    // Method for simulating the tram instance. Repeatedly polls the location
    // server in order to retrieve its next location and update to that location
    private void simulateTram() throws InterruptedException {
        while (true) {   
            
            // Gives 10-20 seconds. Simulates 
            // the time it takes a tram to go from stop to stop.
            int sleepTime = ThreadLocalRandom.current().nextInt
                    (TEN_SECONDS_MILLIS) + TEN_SECONDS_MILLIS;
            
            // Sleep for the randomly decided sleep time
            Thread.sleep(sleepTime);

            
            // Retrieve the new stop from the server
            int newStop = stub.retrieveNextStop(tramID, routeID, currentStop, previousStop);
            
            //Update local location data
            updateStop(newStop);
            
            //Inform the server to update its location data
            stub.updateTramLocation(tramID, routeID, newStop);
        }
    }
}
