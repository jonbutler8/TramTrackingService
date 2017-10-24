package tramsimulate;

import java.util.HashMap;
import java.util.Map;

/*** Class for managing a single, linear tram route using a linked list ***/
public class TramRoute {
    private int maxTrams; // Maximum number of trams allowed on the route
    private Map<Long, TramStop> trams; // trams in the route and their stop locations
    private TramStop head; // first stop in the tram stop list 
    private TramStop tail; // last stop in the tram stop list
    private Map<Integer, TramStop> stopMap; // map for easily locating stops by their ids
    
    // Constructs the new route, given a route number, a maximum tram limit,
    // and an array representing the tram stops and their ordering
    public TramRoute(int[] stopsArray, int maxTrams) {
        this.maxTrams = maxTrams;
        
        // Create the (stop number -> tram stop) mapping
        stopMap = new HashMap<Integer, TramStop>();

        // Create the tram stop linked list
        TramStop previousStop = null;
        for (int num : stopsArray) {
            // Create a new stop, setting its number and previous stop link
            TramStop newStop = new TramStop(num, previousStop);
            
            // Add it to the map for finding stops by their IDs
            stopMap.put(num, newStop);

            // Link the previous stop with the new stop
            if (previousStop != null) {
                previousStop.setNext(newStop);
            }
            
            // When there is no previous stop, this is the first. Set it as the list head
            else {
                head = newStop;
            }
            
            // If this is the last stop in the route, set it as the tail
            if (num == stopsArray[stopsArray.length - 1]) {
                tail = newStop;
            }
            
            // Set the new previous for the next iteration
            previousStop = newStop;
        }
        
        // Create the maps for finding trams locations by their ids
        trams = new HashMap<Long, TramStop>();
            
    }
    
    // Check if a tram exists on the route
    public boolean tramExists(long tramID) {
        return trams.containsKey(tramID);
    }
    
    // Check if the route is at maximum capacity (no new trams allowed)
    public boolean routeFull() {
        return trams.size() >= maxTrams;
    }
    
    
    /** Returns the next stop in the route for a tram, given the current stop 
    /*  of the tram and the previous stop. The previous stop is used to determine
     *  the direction in which the tram is heading
     */
    public int getNextStop(int currStopNum, int prevStopNum) {
            //throws TramRouteException 
       
        TramStop thisStop = head; // Get the first stop
        
        // Traverse the list to find the current stop
        boolean error = false; // Terminates the loop if an error is found
        while (thisStop != null && !error) {
            
            // If the correct stop has been found...
            if (thisStop.getNum() == currStopNum) {
                
                // Check if the tram is going backward and if so return the next stop
                if (thisStop.getNextStopForward().getNum() == prevStopNum ||
                 // Tram is also going backward if it at the end, and there is no previous stop
                        prevStopNum == 0 && currStopNum == tail.getNum()) {
                    return thisStop.getNextStopBackward().getNum();
                }
                
                // Check if the tram is going forward and if so return the next stop
                else if (thisStop.getNextStopBackward().getNum() == prevStopNum ||
                // Tram is also going forward if it at the start, and there is no previous stop
                        prevStopNum == 0 && currStopNum == head.getNum()) {
                    return thisStop.getNextStopForward().getNum();
                }
                else {
                    // Otherwise, the stop request is not correct. End the loop
                    error = true;
                }
            }
            
            // Keep repeating for each stop until the right stop is found
            thisStop = thisStop.getNext();
        }
        
        // If this point has been reached, then the request was incorrect
        // Inform the caller. 
        return ServerCommsStub.SUBROUTINE_ERR;
       
    }
    
    /*** Updates a tram on the route 
     *** This function can allow the route capacity to be exceeded, but the
     *** checking of this restriction is disabled as it would require 
     *** cross-communication between RMs. */
    public boolean updateTram(long tramID, int newStopID)  {
        boolean updateSuccess = true;
        
        // Get the new stop
        TramStop newStop = stopMap.get(newStopID);
        
        // If the new stop exists on the route, set it as the tram's location
        if (newStop != null) {
            trams.put(tramID, newStop);
        }
        
        // Return the boolean value of whether the update was a success
        return updateSuccess;
        
    }
    
    public boolean stopExists(int stopID) {
        return stopMap.containsKey(stopID);
    }
    
    
}
