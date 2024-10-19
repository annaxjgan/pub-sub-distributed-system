/**
 * Name: Anna Gan, Student ID: 1579818
 */
package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import directory.Topic;

/**
 * Remote interface for managing topics and subscribers in a distributed publish-subscribe system.
 * Provides methods for adding, removing, and retrieving topics and subscribers.
 */
public interface IRemoteTopic extends Remote{

	void addTopic(Topic topic) throws RemoteException;

	void deleteTopic(int topicId) throws RemoteException;
	
	void addSubscriber(int topicId, String username) throws RemoteException;

	void removeSubscriber(int topicId, String subName) throws RemoteException;

	List<String> getAllRemoteSubscribers(int topicId) throws RemoteException;

	List<Topic> getAllTopics() throws RemoteException;
	
	Map<String, Set<Integer>> getSubTopicList() throws RemoteException;

	Topic getTopic(int id) throws RemoteException;
	
	void disconnectSubscriber(String subName) throws RemoteException;


}

