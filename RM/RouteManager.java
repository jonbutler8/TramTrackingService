package tramsimulate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/** Driver class for the server-side tram location system. Used by the 
 *  server communications stub to update and retrieve the locations of client trams ***/
public class RouteManager {
    // The maximum number of trams that are allowed on each single route
    public static final int MAX_ROUTE_TRAMS = 5;
    
    // Hardcoded route/stop values
    public static final int[] routeNums = {1, 96, 101, 109, 112};
    public static final int[][] stops = {{1, 2, 3, 4, 5},
            {23, 24, 2, 34, 22},
            {123, 11, 22, 34, 5, 4, 7},
            {88, 87, 85, 80, 9, 7, 2, 1},
            {110, 123, 11, 22, 34, 33, 29, 4}};
    
    // (Route ID -> Route) mapping
    private Map<Integer, TramRoute> routes;
    
    // Constructs the initial tram route system based on the hard-coded values
    public RouteManager() {
        routes = new HashMap<Integer, TramRoute>();
        
        // Create each hardcoded route, passing its stop numbers as arguments
        for (int i = 0 ; i < routeNums.length ; i++) {
            routes.put(routeNums[i], 
                    new TramRoute(stops[i], MAX_ROUTE_TRAMS));;
        }
    }
    
    // Returns the boolean value of whether the route with the corresponding
    // routeID exists in the system
    public boolean routeExists(int routeID) {
        return routes.containsKey(routeID);
    }
    
    // Returns the boolean value of whether the tram with the corresponding tramID
    // exists in the corresponding routeID
    public boolean tramExists(long tramID, int routeID) {
        TramRoute route = routes.get(routeID);
        if (route != null) {
            return route.tramExists(tramID);
        }
        return false;
        
    }
    
    // Returns the boolean value of whether the tram with the corresponding tramID
    // exists in any route
    public boolean tramExists(long tramID) {
        for (TramRoute route : routes.values()) {
            if (route.tramExists(tramID)) {
                return true;
            }
        }
        return false;
        
    }
    
    /*** Given the current stop and previous stop of a tram, returns the 
     *** calculates the direction in which the tram is going and thus
     *** the appropriate next stop. Returns -1 values on error ***/
    public int getNextStop(int routeID, int currStopNum, int prevStopNum) {
            //throws TramRouteException {
        TramRoute route = routes.get(routeID);
        
        // Delegate the request to the specific route and get the response
        return route.getNextStop(currStopNum, prevStopNum);
    }
    
  
    
    public boolean stopExists(int routeID, int stopID) {
        boolean exists = false;
        TramRoute route = routes.get(routeID);
        if (route != null) {
            exists = route.stopExists(stopID);
        }
        return exists;
    } 
    
    // Updates the the location of the tram with the passed tramID to the 
    // new stop number provided. Returns the boolean value of whether
    // the operation was a success.
    public boolean updateTramLocation(long tramID, int tramRoute, int newStopNum) {
        
        
        TramRoute route = routes.get(tramRoute);
        if (route != null) {
 
            
            // Delegate the request to the specific route and get the result
            boolean success = route.updateTram(tramID, newStopNum);
            // If successful, print the updated tram location
            if (success) {
                printPosition(tramID, tramRoute, newStopNum);
            }
            
            // Return result to caller
            return success;
        }
        
        // Returns false if the route doesn't exist.
        return false;
        
    }
    
    // Print tram status string
    private void printPosition(long tramID, int routeID, int currentStop) {
        LocalDateTime stamp = LocalDateTime.now();
        System.out.printf("%s Tram %17s on route %3d now at stop %3d\n",
                stamp.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:SS|")), 
                Long.toHexString(tramID), routeID, currentStop);
    }


}

