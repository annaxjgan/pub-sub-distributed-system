/**
 * Name: Anna Gan, Student ID: 1579818
 */
package remote;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for broker-to-broker communication in a distributed publish-subscribe system.
 * Allows brokers to exchange messages and establish connections with each other.
 */
public interface IRemoteBroker extends Remote{
	
	void receiveMessageFromBroker(int topicId, String message) throws RemoteException;
	
	void receiveConnection(int otherBrokerPort) throws RemoteException, NotBoundException; //allow others to invoke method of other broker to send this.remoteObject to them when connected.
	
}
