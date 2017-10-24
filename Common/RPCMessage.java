package tramsimulate;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

/** Unserailized RPC message that is serialized with the Message class. 
 *  Provides a standard form for communications between the tram server and clients
 */
public class RPCMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    // Type for indicating whether the message is a request or a reply
    public enum MessageType{REQUEST, REPLY};
    
    private MessageType messageType;
    // ID that is unique to each transaction
    private long transactionID;
    // ID that is unique to each message
    private long RPCID;
    // Counter that indicates the request number within a certain tram client
    private long requestID;
    // Indicates the server procedure that the request/reply is relevant to
    private short procedureID;
    // Any extra data for the request/reply, stored as a csv string
    private String csv_data;
    // Server-side error status indicator. A value of 0 indicates no error
    private short status;
    
    // Message constructor for a pre-existing transaction (client-side)
    // Takes a pre-stored transaction ID instead of generating a new one
    public RPCMessage(long transactionID, long requestID, String csv_data, 
            short procedureID) {
        this.transactionID = transactionID;
        this.csv_data = csv_data;
        this.procedureID = procedureID;
        this.requestID = requestID;
        
        // Generate the unique ID for the message
        RPCID = ThreadLocalRandom.current().nextLong();
       
        messageType = MessageType.REQUEST;
        
    }
    
    // Message constructor for start of transaction (client-side)
    // Generates a random new transaction ID
    public RPCMessage(long requestID, String csv_data, 
            short procedureID) {
        this(ThreadLocalRandom.current().nextLong(), requestID, csv_data,
                procedureID);
    }
    
    // Message constructor for a reply (server-side)
    // Copies fields from the request to the reply for validation by the client
    public RPCMessage(RPCMessage clientRequest, String csv_data, 
            short status) {
        messageType = MessageType.REPLY;
        this.csv_data = csv_data;
        this.status = status;
        
        // Copy the relevant information from the client request
        transactionID = clientRequest.getTransactionID();
        RPCID = clientRequest.getRPCID();
        requestID = clientRequest.getRequestID();
        procedureID = clientRequest.getProcedureID();
    }
    
    // De-serialization constructor. Not to be used for creating requests/replies,
    // only unpacking them through the Message class
    public RPCMessage(MessageType messageType, long transactionID, long RPCID, 
            long requestID, short procedureID, String csv_data, short status) {
        this.messageType = messageType;
        this.transactionID = transactionID;
        this.RPCID = RPCID;
        this.requestID = requestID;
        this.procedureID = procedureID;
        this.csv_data = csv_data;
        this.status = status;
    }
    
    // Constructor to be used for replicating a registration request
    public RPCMessage (RPCMessage replyToReplicate) {
        
        this.messageType = MessageType.REPLY;
        this.transactionID = replyToReplicate.transactionID;
        this.RPCID = replyToReplicate.RPCID;
        this.requestID = replyToReplicate.requestID;
        this.procedureID = replyToReplicate.procedureID;
        this.csv_data = replyToReplicate.csv_data;
        this.status = 0;
 
    }

    public MessageType getMessageType() {
        return messageType;
    }
    public long getTransactionID() {
        return transactionID;
    }
    public long getRPCID() {
        return RPCID;
    }
    public long getRequestID() {
        return requestID;
    }
    public short getProcedureID() {
        return procedureID;
    }
    public String getCsv_data() {
        return csv_data;
    }
    public short getStatus() {
        return status;
    }

    

}
