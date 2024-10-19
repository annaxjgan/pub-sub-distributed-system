/**
 * Name: Anna Gan, Student ID: 1579818
 */

package directory;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/**
 * The Topic class represents a topic in a publish-subscribe system.
 * Each topic has an ID, a name, a publisher name, and a count of current subscribers.
 * This class implements Serializable to allow for remote transmission over RMI.
 */
public class Topic implements Serializable{
	
	private int id;
	private String name;
	private String pubName;
	private int subCount;

	/**
	 * Constructs a Topic object with the specified ID, name, and publisher name.
	 * 
	 * @param id      The ID of the topic.
	 * @param name    The name of the topic.
	 * @param pubName The name of the publisher.
	 * @throws RemoteException If an error occurs during remote object construction.
	 */
	public Topic(int id, String name, String pubName) throws RemoteException{
		this.id = id;
		this.name = name;
		this.pubName = pubName;
		this.subCount = 0;

	}

	/**
	 * Returns the ID of the topic.
	 * 
	 * @return The ID of the topic.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the name of the topic.
	 * 
	 * @return The name of the topic.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the name of the publisher of the topic.
	 * 
	 * @return The name of the publisher.
	 */
	public String getPubName() {
		return pubName;
	}
	
	/**
	 * Increases the subscriber count of the topic by 1.
	 */
	public void addSubCount() {
		this.subCount++;
	}
	
	/**
	 * Decrements the subscriber count by one.
	 */
	public void decreaseSubCount() {
		this.subCount--;
	}
	
	/**
	 * Returns the current subscriber count for the topic.
	 *
	 * @return The number of subscribers.
	 */
	public int getSubCount() {
		return subCount;
	}


}
