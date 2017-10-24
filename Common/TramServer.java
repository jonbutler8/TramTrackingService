package tramsimulate;
import java.rmi.Remote; 

import java.rmi.RemoteException;

/*** Communication interface for the server stub ***/
public interface TramServer extends Remote{ 
    // Takes a serialized RPC message via the Message class as input
    public Message makeRequest(Message request) throws RemoteException; 
}
