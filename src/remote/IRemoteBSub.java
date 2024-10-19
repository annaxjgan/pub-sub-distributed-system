/**
 * Name: Anna Gan, Student ID: 1579818
 */
package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for subscriber operations in a distributed publish-subscribe system.
 * Allows subscribers to list topics, subscribe/unsubscribe to topics, and manage their connections.
 */
public interface IRemoteBSub extends Remote{

	public String list() throws RemoteException;
	
	public String sub(int topicId, IRemoteSub remoteSub, String username) throws RemoteException;
	
	public String current(String subUsername) throws RemoteException;
	
	public String unsub(int topicId, String username) throws RemoteException;
	
	public void subDisconnect(String subName) throws RemoteException;
	
	public void sendSubHeartbeat(String subName) throws RemoteException;
}
