/**
 * Name: Anna Gan, Student ID: 1579818
 */

package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * Remote interface for publisher operations in a distributed publish-subscribe system.
 * Defines methods for creating topics, publishing messages, showing subscribers, and deleting topics.
 * Extends {@link java.rmi.Remote} to support RMI communication.
 */
public interface IRemoteBPub extends Remote{
	
	
	public String create(int id, String name,String pubUsername) throws RemoteException;

	public String publish(int id, String message, String pubUsername) throws RemoteException;
	
	public String show(String pubUsername) throws RemoteException;
	
	public String delete(int id, String pubUsername) throws RemoteException;
	
	public void pubDisconnect(String pubName) throws RemoteException;
	
	public void sendPubHeartbeat(String pubName) throws RemoteException;
	

}
