package tramsimulate;

/*** Provides standard constants to be used by the client and server 
 *   communication stubs ***/
public interface TramCommsInterface { 
    // Error flags and their meanings
    public static final short FLAG_NO_MARTIAL = -1;
    public static final short FLAG_SUCCESS = 0;
    public static final short FLAG_NOT_REQUEST = 1;
    public static final short FLAG_NO_PROCEDURE = 2;
    public static final short FLAG_CORRUPT_CSV = 3;
    
    public static final short FLAG_UNREG_TRAM = 4;
    public static final short FLAG_NO_ROUTE = 5;
    public static final short FLAG_ROUTES_FULL = 6;
    public static final short FLAG_NO_STOP = 7;
    public static final short FLAG_NO_ROUTE_SEQ = 8;
    public static final short FLAG_PREEXIST_TRAM = 9;
    public static final short FLAG_TRAM_ROUTE_MISMATCH = 10;
    public static final short FLAG_INVALID_UPDATE = 11;
    public static final short FLAG_NO_TRANSACTION = 12;
    
    // Error message corresponding to each error flag
    public static final String[] ERROR_MESSAGES = {"no error", 
                            "message was not a request.",
                           "invalid procedure number.",
                           "empty or unreadable csv data.",
                           "tram-only procedure access attempt by non-registered tram",
                           "route number specified does not exist", 
                           "all routes are full",
                           "stop number specified does not exist",
                           "invalid previous-current stop sequence specified",
                           "tram already exists", 
                           "the tram exists, but on a different route than specified",
                           "invalid next stop for current tram position", 
                           "no prior next stop request for this transaction"};
    
    // The names of the two server procedure types
    public static final String[] PROCEDURE_NAMES = {"get next stop", "update location"};
    
    
    // Values of the procedure ID for each server procedure type
    public static final short GET_NEXT_STOP = 0;
    public static final short UPDATE_LOCATION = 1;
}
