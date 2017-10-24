package tramsimulate;

import java.io.IOException;
import java.rmi.RemoteException;

import tramsimulate.RPCMessage.MessageType;

/** Stub for packaging and sending communications to/from the client. 
 *  Interacts with the server to produce its results **/
public class TramCommsStub implements TramCommsInterface {
    private TramServer server; // Server interface instance
    private long transactionID;
    private long requestID; 
    private static final String MALFORM = "Malformed server response: "; // Basic error message prefix
    // Expected number of comma separated values from different server requests
    private static final int[] RESPONSE_LENGTHS = {1, 1};
    public static final int RETRY_DELAY = 1000;
    
    // Initializes the instance, taking a remote server object as input
    public TramCommsStub(TramServer server) {
        this.server = server;
               
        transactionID = 0;
        requestID = 0;
    }
    
    // Generalizes behavior for the three request types. Continually requests 
    // a response from the server until a correct response is obtained
    private TramLocation genericLocationRequest(short procedure, String requestCsv) 
        throws InterruptedException {
        boolean success = false; 
        RPCMessage request;  // Unserialized client-side RPC message
        TramLocation responseData = null; // Return data to give to the caller

        // Repeats until the request was a success
        while (!success) {
            // Create a new RPC message. If the request type is a new transaction,
            // use the new transaction constructor and save the new transaction ID
            // for reuse.
            if (procedure == GET_NEXT_STOP) {
                request = new RPCMessage(
                        requestID++, requestCsv, procedure);
                transactionID = request.getTransactionID();
            }
            // If the request type is a continued transaction, reuse the current
            // transaction ID.
            else {
                request = new RPCMessage(transactionID, requestID++, requestCsv, procedure);
            }
            
            // Process and send the request the server, getting the response
            responseData = processRequest(request);
            // End the request loop if the request data is error-free
            if (responseData != null) {
                success = true;
            }
            else {
                Thread.sleep(RETRY_DELAY);
            }
        }
        
        return responseData;
    }
    
    
    // Wrapper interface method for retrieving the next stop of a tram
    public int retrieveNextStop(long tramID, int routeID, int currentStopNum, 
            int previousStopNum) throws InterruptedException  {
        // Build the request csv data
        String requestCsv = routeID + "," + currentStopNum + "," + previousStopNum;
        
        // Only the stop data of the response is relevant, take it as the result
        TramLocation responseData = genericLocationRequest(GET_NEXT_STOP, requestCsv);

        return responseData.stop;
    }
    
    /// Wrapper interface method for updating the tram location server-side
    public void updateTramLocation(long tramID, int routeID, int stopNum) throws 
        InterruptedException {
        // Build the request csv data
        String requestCsv =  routeID +  "," + stopNum + "," + tramID;
        
        genericLocationRequest(UPDATE_LOCATION, requestCsv);
        
    }
    
    // Prints an appropriate message from the provided request procedure type
    // and status error flag
    private void printStatusError(short procedure, short status) {
        System.out.println(PROCEDURE_NAMES[procedure] + 
                " request failed: received error flag from server:");
        System.out.println("\t" + ERROR_MESSAGES[status]);
    }
    
    // Performs the actual communication with the server, handling any errors that occur
    private TramLocation processRequest(RPCMessage request) {
        TramLocation returnData = null;
        
        Message serialReq = new Message(); // Will be used to serialize the RPC request
        Message serialReply = null; // Stores the serial reply from the server
        RPCMessage reply; // Stores the deserialized reply
         
        try {
            // Serialize the request data
            serialReq.marshal(request); 
            // Send the request to the server, storing the response
            serialReply = server.makeRequest(serialReq); 
            // Deserialize the response so its correctness can be checked
            reply = serialReply.unmarshal();
            
            // Get the csv data from the server response, if any
            String csvString = reply.getCsv_data();
            String csvData[] = csvString.split(",");
            
            /*** Check all possible error modes, printing appropriate responses ***/
            // If the server set an error flag, print its corresponding error message
            if (reply.getStatus() != 0) {
                printStatusError(request.getProcedureID(), reply.getStatus());
            }
            // Server sent a reply of the wrong type
            else if (reply.getMessageType() != MessageType.REPLY) {
                System.out.println(MALFORM + "message is not a reply type");
            }
            // The transaction ID of the reply doesn't match
            else if (reply.getTransactionID() != request.getTransactionID()) {
                System.out.println(MALFORM + "transaction ID mismatch");
            }
            // The procedure ID of the reply doesn't match
            else if (reply.getProcedureID() != request.getProcedureID()) {
                System.out.println(MALFORM + "procedure ID mismatch");
            }
            // RPC ID of the reply doesn't match
            else if (reply.getRPCID() != request.getRPCID()) {
                System.out.println(MALFORM + "RPCID mismatch");
            }
            // Request ID of the reply doesn't match
            else if (reply.getRequestID() != request.getRequestID()) {
                System.out.println(MALFORM + "request ID mismatch");
            }
            // The reply's csv data doesn't match the expected format for the procedure
            else if (csvData.length != RESPONSE_LENGTHS[request.getProcedureID()] ||
                    request.getProcedureID() == UPDATE_LOCATION && !csvString.isEmpty()) {
                System.out.println(MALFORM + "invalid csv reply");
            }
            
            /*** if no errors occurred, return the data based on the procedure type ***/
            // Get stop operation reply
            else if (request.getProcedureID() == GET_NEXT_STOP) {
                int nextStop = Integer.parseInt(reply.getCsv_data());
                returnData = new TramLocation(nextStop);
            }
            // Update tram location on the server operation reply
            else if (request.getProcedureID() == UPDATE_LOCATION) {
                return new TramLocation();
            }
       
        // Handles RPC communication failure
        } catch (RemoteException e) {
            System.out.println("Error receiving request from server:" + e.getMessage());
            ;
        // Handles marshalling/unmarshalling failure
        } catch (IOException e) {
            String error = serialReply == null ? "Error marshalling client request: " :
                "Error unmarshalling server reply: ";
            System.out.println(error + e.getMessage());
        // This failure mode indicates that the csv data had non-numeric values
        } catch (NumberFormatException e) {
            System.out.println(MALFORM + "tram location data not numeric");
        }
        
        return returnData;
    }
    
    
}
