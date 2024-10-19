/**
 * Name: Anna Gan, Student ID: 1579818
 */

package subscriber;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import remote.IRemoteSub;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of the IRemoteSub interface, representing a remote subscriber
 * that can receive messages from a broker in a distributed publish-subscribe system.
 * When a message is received, the message and the timestamp are printed to the console.
 */
public class RemoteSub extends UnicastRemoteObject implements IRemoteSub{
	
	/**
     * Constructor for the RemoteSub class, which exports the object for RMI.
     *
     * @throws RemoteException If a remote communication error occurs.
     */
	protected RemoteSub() throws RemoteException {
		super();
	}

	/**
	 * Receives a message from a broker and prints it to the console.
	 * 
	 * @param topicId the ID of the topic the message is associated with
	 * @param message the message to be printed
	 */
	@Override
	public void receiveMessage(String message) throws RemoteException {
		
        LocalDateTime now = LocalDateTime.now();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm:ss");
        
        String formattedDateTime = now.format(formatter);
                
		System.out.println(formattedDateTime + " " + message + "\n");
	}
	

}
