package tramsimulate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

import tramsimulate.RPCMessage.MessageType;

/** Class for serializing and encapsulating communication between the tram server
 *  and the tram client. The only communications sent between the two entities
 *  are done via passing objects of this type */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    protected byte data[] = null; // Stores serialized data
    protected int length = 0; // Records the expected length of the serialized data
    
    
    // Returns the boolean value of whether the length of the serialized data
    // matches the length that was recorded when it was serialized.
    // A fail on this check indicates corrupted data.
    public boolean hasValidLength() {
        return data.length == length;
    }
    
    /*** Serializes the passed RPC message and stores the result, as well of the 
     *   length of the serialized data ***/
    public void marshal(RPCMessage rpcMessage) throws IOException {
        // Create the streams to serialize the message contents
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream dataWriter = new DataOutputStream(bytes);

        // Write each long field to the streams, keeping track of the byte length
        dataWriter.writeLong(rpcMessage.getTransactionID());
        dataWriter.writeLong(rpcMessage.getRPCID());
        dataWriter.writeLong(rpcMessage.getRequestID());
        length += Long.SIZE * 3;
        
        // Write each short field
        dataWriter.writeShort(rpcMessage.getStatus());
        dataWriter.writeShort(rpcMessage.getProcedureID());
        dataWriter.writeShort(rpcMessage.getMessageType().ordinal());
        length += Short.SIZE * 3;
        
        // Write the csv data. 
        // Use the length of the string to calculate the  byte size
        String csv = rpcMessage.getCsv_data();
        dataWriter.writeChars(csv);
        length += Character.SIZE * csv.length();
        
        // Retrieve the final byte array
        dataWriter.close();
        data = bytes.toByteArray();
        bytes.close();
        
        //Update the to be in bytes, rather than bits
        length = length / Byte.SIZE;
    }

    
    /*** Unmarshals the stored serialized message and performs basic validation.
     *** Returns the deserialized result */
    public RPCMessage unmarshal() throws IOException {
        
        // If the data is not the expected length, some corruption has occured.
        // Indicate to the caller.
        if (length != data.length) {
            throw new IOException("Data length mismatch.");
        }
        
        // Create the streams to unmarshal the message contents
        ByteArrayInputStream bytes = new ByteArrayInputStream(data);
        DataInputStream dataReader = new DataInputStream(bytes);
        
        // Read the serialized fields from the array
        long transactionID = dataReader.readLong();
        long RPCID = dataReader.readLong();
        long requestID = dataReader.readLong();
        
        short status = dataReader.readShort();
        short procedureID = dataReader.readShort();
        MessageType type = MessageType.values()[dataReader.readShort()];
        
        // The remaining bytes make up the csv string. Read them in
        String csv = "";
        while (dataReader.available() >= Character.SIZE / Byte.SIZE) {
            csv += dataReader.readChar();
        }
        
        //Reconstruct and return the RPCMessage
        return new RPCMessage(
                type, transactionID, RPCID, requestID, procedureID, csv, status);
    }
}
