/**
 * Name: Anna Gan, Student ID: 1579818
 */
package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for subscriber operations in a distributed publish-subscribe system.
 * Allows brokers to send messages to subscribers.
 */
public interface IRemoteSub extends Remote{

	public void receiveMessage(String message) throws RemoteException;
		
}

