package tramsimulate;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import tramsimulate.RPCMessage.MessageType;

/** Stub for packaging and sending communications to/from the client. 
 *  Sanitizes and validates input from clients before passing them to server functions **/
public class ServerCommsStub implements TramCommsInterface {
            
    // Driver object for the tram location system
    private RouteManager routeManager;
    
    // Return value indicating a failure of a subroutine
    public static final short SUBROUTINE_ERR = -1;
    
    // Constants indicating the position of arguments within csv fields
    private static final short ARGS_ROUTE = 0;
    private static final short ARGS_STOP = 1;
    private static final short ARGS_PREV_STOP = 2;
    private static final short ARGS_TRAMID = 2;
    
    // Number of csv arguments that are of the long type for each operation
    private static final short LONG_ARGS[] = {0, 1};

    // Indicates the expected number of integer csv arguments for each procedure
    private static final int[] OARGS_LENGTH = {3, 2};
    
    // Set of transaction IDs for checking if continued transactions exist
    private Set<Long> activeTransactions;

    
    // Initializes the stub with the passed tram location system (routeManager) object
    public ServerCommsStub(RouteManager routeManager) {
        this.routeManager = routeManager;
        activeTransactions = new HashSet<Long>();
    }

    /*** Unwraps the passed serialized request and performs validation.
     *** Passes the request on to server procedures if appropriate, and returns 
     *** the serialized response. ***/
    public Message processMessage(Message request) throws IOException {
        short status = FLAG_SUCCESS; // Status error indicator. 
        String csvResponse = ""; // String for building the csv server response
        RPCMessage unpacked = null; // Stores the deserialized message
        
        int[] args = null; // Stores the client integer csv arguments
        Long tramID = 0L; // Stores the tramID client argument
        int procedure = 0; // Indicates the procedure requested by the client
        
        try {
            // Attempt to deserialize the client request and retrieve its arguments
            unpacked = request.unmarshal();
            args = getCsvArgs(unpacked);
            
            // Retrieve the requested procedure ID
            procedure = unpacked.getProcedureID();
            
            // Get the tramID argument if necessary
            if (procedure == UPDATE_LOCATION) {
                tramID = Long.parseLong(unpacked.getCsv_data().split(",")[ARGS_TRAMID]);
            }
        
        // If a format exception occurred in parsing, the csv arguments were incorrect
        } catch (NumberFormatException e) {
            status = FLAG_CORRUPT_CSV;
        }
        
        // If no parsing errors were encountered...
        if (status == FLAG_SUCCESS) {
            // Perform general validation on the request, taking the result
            status = checkGeneralParams(unpacked, args);
        }
        
        // If no errors were encountered in general parameter checking...
        if (status == FLAG_SUCCESS) {
            
            // Choose which procedure to run based on the procedure flag
            switch (procedure) {

            // Get next tram stop procedure
            case GET_NEXT_STOP: 
                // Get the next stop number
                int nextStop = routeManager.getNextStop(
                        args[ARGS_ROUTE], args[ARGS_STOP], args[ARGS_PREV_STOP]);
                // Stop number of -1 indicates an error where the tram path is invalid
                if (nextStop == SUBROUTINE_ERR) {
                    status = FLAG_NO_ROUTE_SEQ;
                    csvResponse += SUBROUTINE_ERR;
                } else {
                    // Build the csv data response with the next stop value
                    csvResponse += nextStop;
                    // Record the transaction as in progress (waiting for update location)
                    activeTransactions.add(unpacked.getTransactionID());
                }
                break;
            
             // Update tram location procedure
            case UPDATE_LOCATION:
                // Attempt to update the location
                if (!routeManager.updateTramLocation
                        (tramID, args[ARGS_ROUTE], args[ARGS_STOP])) {
                    // Failure means the tram is taking an invalid path
                    status = FLAG_INVALID_UPDATE;
                } else {
                    // Record the transaction as complete
                    activeTransactions.remove(unpacked.getTransactionID());
                }
                break;
            }
        }
        
        // If there was an error, print it
        if (status != 0) {
            printServerError(status, unpacked.getProcedureID());
        }
        
        // Generate the reply based on the original message
        RPCMessage reply = new RPCMessage(unpacked, csvResponse, status);
        
        // Serialize and return the reply
        Message serialReply = new Message();
        serialReply.marshal(reply);
        return serialReply;
    }


    // Prints an appropriate message from the provided request procedure type
    // and status error flag
    private void printServerError(short status, short procedureID) {
       String error = "Unable to process ";
       if (procedureID < 3 && procedureID > 0) {
           error += PROCEDURE_NAMES[procedureID] + " ";
       }
       error += "request: " + ERROR_MESSAGES[status];
       
       System.out.println(error);

    }

    // Retrieves the integer csv arguments from a request
    private int[] getCsvArgs(RPCMessage unpacked) throws IOException, NumberFormatException {
        String[] args = unpacked.getCsv_data().split(",");
        
        int intArgsLength = args.length; 
        // Decrement the expected length if one of the arguments is a long
        intArgsLength -= LONG_ARGS[unpacked.getProcedureID()];
        
        // Attempt to parse the integer arguments
        int[] otherArgs = new int[intArgsLength];
        for (int i = 0; i < intArgsLength; i++) {
            otherArgs[i] = Integer.parseInt(args[i]);
        }
        
        return otherArgs;
    }
    
    // Performs basic checks on a received client request. Returns the error status value
    private short checkGeneralParams(RPCMessage unpacked, int[] args) {
        short status = 0; // Error status value
        int procedure = unpacked.getProcedureID();
        
        /*** Perform various basic checks. Set the error flag if a problem is found ***/
        
        // Check if the message type is correct (a request, rather than a reply)
        if (unpacked.getMessageType() != MessageType.REQUEST) {
            status = FLAG_NOT_REQUEST;
        }
        // Ensure the procedure is one of the valid procedures (0-1)
        else if (procedure != 0 && procedure != 1) {
            status = FLAG_NO_PROCEDURE;
        }
        // Ensure the number of csv arguments is correct
        else if (args.length != OARGS_LENGTH[procedure]) {
            status = FLAG_CORRUPT_CSV;
        }
        // ensure the tram route exists in the system
        else if (!routeManager.routeExists(args[ARGS_ROUTE])) {
            status = FLAG_NO_ROUTE;
        }
        // ensure the current stop argument exists in the system 
        else if (!routeManager.stopExists(args[ARGS_ROUTE], args[ARGS_STOP])) {
            status = FLAG_NO_STOP;
        }

        
        return status;
    }


}
