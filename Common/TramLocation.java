package tramsimulate;

/** Wrapper class for providing a standard return value for methods that 
 *  return tram (route, stop) location tuples *
 *  
 *  This class is immutable and has no functionality of its own */
public class TramLocation {
    // Default value for when a field is not used by a function's return value
    public static final int UNUSED = -1;
    
    // (Route, stop) location tuple. 
    // Public as they are immutable
    public final int route;
    public final int stop;
    
    // Constructor for (route, stop) location tuples
    public TramLocation(int route, int stop) {
        this.route = route;
        this.stop = stop;
    }
    
    // Constructor for stop-only location return values
    public TramLocation(int stop) {
        this.route = UNUSED;
        this.stop = stop;
    }
    
    // Constructor for blank location data, used to indicate failures
    public TramLocation() {
        this.route = UNUSED;
        this.stop = UNUSED;
    }
}
