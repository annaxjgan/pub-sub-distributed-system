/**
 * Name: Anna Gan, Student ID: 1579818
 */
package directory;

import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import remote.IRemoteSub;
import remote.IRemoteTopic;

/**
 * The RemoteTopicList class implements the IRemoteTopicList interface, providing methods for managing topics and 
 * their subscribers in a distributed publish-subscribe system. This class handles operations such as adding and 
 * deleting topics, managing subscribers, and retrieving topic information.
 */
public class RemoteTopic extends UnicastRemoteObject implements IRemoteTopic{
	
	private static List<Topic> topics = new ArrayList<>();
	private static Map<Integer,Topic> topicIdList = new HashMap<>();
	private static Map<Integer, List<String>> topicSubscriber = new HashMap<>(); // to keep track of remote subscribers for message broadcasting
	private static Map<String, Set<Integer>> subTopicList = new HashMap<>();

	 /**
     * Constructs a RemoteTopicList object, exporting it for RMI.
     * @throws Exception If an error occurs during remote object construction.
     */
	protected RemoteTopic() throws Exception {
		super();
	}
	
	/**
     * Adds a new topic to the list and updates the relevant mappings.
     * @param topic The topic to be added.
     */
	@Override
	public void addTopic(Topic topic) {
		topics.add(topic);
		topicIdList.put(topic.getId(),topic);
	    System.out.println("Topic added for publisher "+ topic.getPubName());
		
	}
	
	/**
	 * Deletes a topic from the list and updates the relevant mappings.
	 * 
	 * @param topicId The ID of the topic to be deleted.
	 */
	@Override
	public void deleteTopic(int topicId) {
		Topic topic = topicIdList.get(topicId);
		topics.remove(topic);
		topicIdList.remove(topicId);
	    topicSubscriber.remove(topicId);
		for (Set<Integer> topics : subTopicList.values()) {
			if (topics.contains(topicId)) {
				topics.remove(topicId);
			}
		}
	    System.out.println("Topic id " + topicId + " deleted");
	}
	
	/**
	 * Adds a new subscriber to a topic and updates the relevant mappings.
	 * 
	 * @param topicId  The ID of the topic to subscribe to.
	 * @param username The name of the subscriber.
	 */
	@Override
	public void addSubscriber(int topicId, String username) {
		List<String> subscriberNames = topicSubscriber.getOrDefault(topicId, new ArrayList<String>());
		subscriberNames.add(username);
		topicSubscriber.put(topicId, subscriberNames);
		System.out.println("Subscriber added to topic id " + topicId);
		
		Set<Integer> subscribedTopics = subTopicList.getOrDefault(username, new HashSet<Integer>());
		subscribedTopics.add(topicId);
		subTopicList.put(username, subscribedTopics);
		
		Topic topic = topicIdList.get(topicId);
		topic.addSubCount();
		
	}
	
	/**
     * Adds a subscriber to a specific topic.
     *
     * @param topicId The ID of the topic.
     * @param username The username of the subscriber.
     */
	@Override
	public void removeSubscriber(int topicId, String subName) {
		List<String> subscriberNames = topicSubscriber.get(topicId);

		// Null check in case the topic does not have any subscribers
		if (subscriberNames != null) {
			subscriberNames.remove(subName);
		}

		// Decrease the subscriber count for the topic
		Topic topic = topicIdList.get(topicId);
		if (topic != null) {
			topic.decreaseSubCount();
		}

		// Remove the topic from the subscriber's list of subscribed topics
		Set<Integer> subscribedTopics = subTopicList.getOrDefault(subName, new HashSet<>());
		subscribedTopics.remove(topicId);
	}
	
	/**
	 * Disconnects a subscriber from all topics.
	 * 
	 * @param subName The name of the subscriber to disconnect.
	 */
	@Override
	public void disconnectSubscriber(String subName) {
		Set<Integer> topics = subTopicList.get(subName);
	    
	    // Null check to avoid any issues if subscriber is not found
	    if (topics == null || topics.isEmpty()) {
	        System.out.println("No topics found for subscriber: " + subName);
	        return;
	    }
	    
	    for (int topicId : topics) {
	        removeSubscriber(topicId, subName);
	    }
	    subTopicList.remove(subName);
	    System.out.println("Subscriber " + subName + " has been disconnected from all topics.");
	}
	
	/**
	 * Retrieves a list of all remote subscribers for a given topic.
	 * 
	 * @param topicId The ID of the topic.
	 * @return A list of all remote subscribers for the topic.
	 */
	@Override
	public List<String> getAllRemoteSubscribers(int topicId) {
		if(topicSubscriber.containsKey(topicId)){
			for (Map.Entry<Integer, List<String>> entry : topicSubscriber.entrySet()) {
				System.out.println("Subscribers: " + entry.getValue());
			}
			return topicSubscriber.get(topicId);
			
		}
		return null;
	}
	
	/**
	 * Retrieves a list of all topics.
	 * 
	 * @return A list of all topics.
	 */
	@Override
	public List<Topic> getAllTopics() {
		return topics;
	}
	
	/**
     * Retrieves a map of all topics subscribed to by each subscriber.
     * 
     * @return A map of all topics subscribed to by each subscriber.
     */
	@Override
	public Map<String, Set<Integer>> getSubTopicList() {
		return subTopicList;
	}
	
	/**
     * Retrieves the details of a specific topic by its ID.
     *
     * @param id The ID of the topic.
     * @return The topic object if found; otherwise, null.
     */
	@Override
	public Topic getTopic(int id) {
		if(topicIdList.containsKey(id)){
			return topicIdList.get(id);
		}
		else {
            return null;
        }
	}
	
	
}
