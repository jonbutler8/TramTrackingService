[Back to Project Summary](https://jonbutler8.github.io/ProjectSummary/)
#### Distributed Tram Simulation (Java RMI)
A distributed system where a client service registers trams to a front-end that are then tracked along a simulated route by replicated tram tracking servers. This was an individual student assignment completed to demonstrate an understanding of distributed computing concepts. Uses Java RMI.

## How to run 
* After compiling run the `ReplicationDriver` class in the Server directory. 
* Next, run the class `TramServerImpl` in the RM directory up to three times, providing the port as the first command line argument. Valid ports are: 8465, 8466 and 8467. 
* Finally, run the `TramServerClient` class in the Client directory. 
* Observe the command output of the three classes. The system will function as long as one TramServerImpl instance exists. Try terminating one or two instances to see the result. 

## System details
### Structure
The system is split into three components: the client, the replication manager and the front end server. There is also a set of common classes that can be utilized by all three components. Multiple replication managers can be run with a port number as an argument so that they each register themselves as RMI objects with the same URL but with different ports. These ports are hardcoded on the front end server, so should be provided as 8465, 8466 and 8467 for a total of three replication managers.

### Run process
When the front end is run, it first registers itself as an RMI object with its own port so that clients can make remote requests. 
When the client is run, it retrieves the front end object and passes it to multiple thread instances which simulate their own tram. By default, there are 5 tram instances. The tram clients randomly generate their own tramID and randomly decide on a start route and start at either the first stop or last stop in that route. They then pause for 10-20 seconds before making their requests for getNextStop() and updateTramLocation() to the front end. 

When the front end retrieves a request, it calls listTramService(), which first checks which replication managers are running by attempting to retrieve their remote objects with the hard-coded port values. If a replication manager’s registry cannot be retrieved or the replication manager object does not exist on the registry, then the front end server will mark the replication manager as non-functional. It then prints the result of this check, displaying which servers are on and which are off. 
After this check, the front end sends the request from the client to all the replication servers that are running. After retrieving all replies, it sends back to the client the first non-null reply that was received from a replication manager. 

This allows replication managers to be turned on and off, with the system remaining functional as long as at least one is running. If zero replication managers are running after a listTramService() request, the replication manager will send a RemoteException back to the client. The client will then wait 2 seconds before retrying the request. 

### Marshalling and unmarshalling
The `Message` class used by the service implements a manual marshalling/unmarshalling procedure on top of Java RMI. Note that this is redundant, as RPC encapsulates its own marshalling/unmarshalling procedures. However, a requirement of this assignment was to implement manual marshalling to demonstrate understanding. The service also performs its own redundant data validation for the same purpose.

[Back to Project Summary](https://jonbutler8.github.io/ProjectSummary/)
