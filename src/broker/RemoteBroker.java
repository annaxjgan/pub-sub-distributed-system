/**
 * Name: Anna Gan, Student ID: 1579818
 */
package broker;

import remote.IRemoteBPub;
import remote.IRemoteBSub;
import remote.IRemoteBroker;
import remote.IRemoteSub;
import remote.IRemoteTopic;

import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import directory.Topic;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * The RemoteObjectBroker class implements the IRemoteBPub, IRemoteBSub, and
 * IRemoteBroker interfaces, providing methods for managing topics and
 * subscribers in a distributed publish-subscribe system. This class handles
 * operations such as adding and deleting topics, managing subscribers, and
 * retrieving topic information.
 */
public class RemoteBroker extends UnicastRemoteObject implements IRemoteBPub, IRemoteBSub, IRemoteBroker {
	
	private static final int HEARTBEAT_INTERVAL = 5000;
	private IRemoteTopic topicList;
    private List<IRemoteBroker> connectedBrokers;  // List of connected remote brokers
    private Map<String, IRemoteSub> connectedSubscribers;
	private Map<String, Set<Integer>> pubTopicList;
	private Map<String, Long> publisherHeartbeat = new HashMap<>();
	private Map<String, Long> subscriberHeartbeat = new HashMap<>();


	/**
     * Constructs a RemoteObjectBroker with the specified broker list, topic list, connected subscribers,and publisher topic list.
     *
     * @param brokerList A list of connected brokers.
     * @param topicList A reference to the remote topic list.
     * @param connectedSubscribers A map of connected subscribers.
     * @param pubTopicList A map of publisher topics.
     * @throws RemoteException If a remote communication error occurs.
     */
    protected RemoteBroker(List<IRemoteBroker> brokerList, IRemoteTopic topicList, Map<String, IRemoteSub> connectedSubscribers, Map<String, Set<Integer>> pubTopicList) throws RemoteException {
        super();
        this.topicList = topicList;
		if (brokerList == null) {
			connectedBrokers = new ArrayList<>();
		} else {
			connectedBrokers = brokerList;
		}
		this.connectedSubscribers = connectedSubscribers;
		this.pubTopicList = pubTopicList;
    }

    /**
     * Receives messages from another broker and forwards them to subscribers.
     *
     * @param topicId The ID of the topic.
     * @param message The message content.
     * @throws RemoteException If a remote communication error occurs.
     */
    @Override
    public void receiveMessageFromBroker(int topicId, String message) throws RemoteException {
        sendMessage(topicId, message);
    }
    
    /**
     * Establishes a connection with another broker.
     *
     * @param otherBrokerPort The port of the other broker.
     * @throws RemoteException If a remote communication error occurs.
     * @throws NotBoundException If the remote object is not bound.
     */
    @Override
    public void receiveConnection(int otherBrokerPort) throws RemoteException, NotBoundException {
    	Registry otherBrokerRegistry = LocateRegistry.getRegistry(otherBrokerPort);
		IRemoteBroker otherBroker = (IRemoteBroker) otherBrokerRegistry.lookup("remoteBroker");
        connectedBrokers.add(otherBroker);
        System.out.println("Connection established with broker port "+ otherBrokerPort);
    };

    /**
     * Creates a new topic.
     *
     * @param id The ID of the new topic.
     * @param topicName The name of the new topic.
     * @param pubUsername The username of the publisher.
     * @return A success message or an error message if the topic ID already exists.
     * @throws RemoteException If a remote communication error occurs.
     */
	@Override
	public synchronized String create(int id, String topicName, String pubUsername) throws RemoteException {
		if (topicList.getTopic(id) == null) {
			Topic topic = new Topic(id, topicName, pubUsername);
			topicList.addTopic(topic);
			Set<Integer> topics = pubTopicList.getOrDefault(pubUsername, new HashSet<Integer>());
			topics.add(id);
			pubTopicList.put(pubUsername, topics);
			return "SUCCESS: Topic " + id + " created.\n";
		}	
		return "ERROR: Topic ID already exists. Please choose a new ID\n";
		
	}
	
	/**
     * Publishes a message to a specific topic.
     *
     * @param id The ID of the topic.
     * @param message The message to publish.
     * @return A success message or an error message if the topic does not exist.
     * @throws RemoteException If a remote communication error occurs.
     */
	@Override
	public synchronized String publish(int id, String message, String username) throws RemoteException {
		if (topicList.getTopic(id) == null) {
			return "ERROR: Topic with id " + id + " does not exists.\n";
		}
		Set<Integer> topics = pubTopicList.get(username);
		if (topics.contains(id)) {
			Topic topic = topicList.getTopic(id);
			String messageContent =  id + ":" + topic.getName() + ": " + message;
			sendMessage(id, messageContent);
			broadcastMessage(id, messageContent);
			return "SUCCESS: Message published for topic " + id + ".\n";
		}
		else {
			return "ERROR: Topic id " + id + " not found in publisher's topic list.\n";
		}
		
	}
	
	/**
     * Displays the current topics published by the specified publisher.
     *
     * @param pubUsername The username of the publisher.
     * @return A string containing the current topics or an error message if none exist.
     * @throws RemoteException If a remote communication error occurs.
     */
	@Override
	public String show(String pubUsername) throws RemoteException {

		StringBuilder result = new StringBuilder();
		
		if (!pubTopicList.containsKey(pubUsername)) {
            return "Topic list for " + pubUsername + " is currently empty. Please create a new topic.\n";
		}
		Set<Integer> topicIds = pubTopicList.get(pubUsername);
		result.append("Current published topics (Topic ID: Topic Name: Subscriber count) :\n");
		for (Integer id: topicIds) {
			Topic topic = topicList.getTopic(id);
			result.append(topic.getId() + " : " + topic.getName() + " : " + topic.getSubCount() + "\n");
		}
		return result.toString();
	}
	
	/**
     * Deletes a topic.
     *
     * @param id The ID of the topic to delete.
     * @param pubUsername The username of the publisher requesting the deletion.
     * @return A confirmation message indicating the topic has been deleted.
     * @throws RemoteException If a remote communication error occurs.
     */
	@Override
	public synchronized String delete(int id, String pubUsername) throws RemoteException {
		Topic topic = topicList.getTopic(id);
		if (!pubTopicList.containsKey(pubUsername)) {
			return "ERROR: Topic list is empty.\n";
		}
		if (topic == null) {
			return "ERROR: Topic with id " + id + " does not exist.\n";
		}
		String result = removeTopicFromPub(pubUsername, id);
		return result;
	}

	/**
     * Lists all available topics.
     *
     * @return A string representation of all topics or an error message if none exist.
     * @throws RemoteException If a remote communication error occurs.
     */
	@Override
	public String list() throws RemoteException{
		StringBuilder result = new StringBuilder();
		
		if (topicList.getAllTopics().isEmpty()) {
			return "No topics available.\n";
		}
		
		result.append("List of topics available (Topic ID : Topic Name : Publisher) :\n");
		for (Topic topic : topicList.getAllTopics()) {
			result.append( topic.getId() + " : " + topic.getName() + " : " + topic.getPubName() + "\n");
		}
		return result.toString();
	};
	
	/**
     * Subscribes a user to a topic.
     *
     * @param topicId The ID of the topic to subscribe to.
     * @param remoteSub The remote subscriber object.
     * @param username The username of the subscriber.
     * @return A success message or an error message if the topic does not exist or the user is already subscribed.
     * @throws RemoteException If a remote communication error occurs.
     */
	@Override
	public synchronized String sub(int topicId, IRemoteSub remoteSub, String username) throws RemoteException{
		if (topicList.getTopic(topicId) == null) {
			return "ERROR: Topic does not exist.\n";
		}
		if (! topicList.getSubTopicList().isEmpty()){
			if ( topicList.getSubTopicList().get(username) != null &&  topicList.getSubTopicList().get(username).contains(topicId)) {
				return "ERROR: Already subscribed to topic " + topicId + ".\n";
			}
		}
		topicList.addSubscriber(topicId, username);
		connectedSubscribers.put(username, remoteSub);
		return "SUCCESS: Succesfully subscribed to topic " + topicId + ".\n";
	};
	
	/**
     * Displays the current subscriptions of a subscriber.
     *
     * @param subUsername The username of the subscriber.
     * @return A string representation of the subscriber's current subscriptions or an error message if none exist.
     * @throws RemoteException If a remote communication error occurs.
     */
	@Override
	public String current(String subUsername) throws RemoteException{
		StringBuilder result = new StringBuilder();
		if (! topicList.getSubTopicList().containsKey(subUsername) ||  topicList.getSubTopicList().get(subUsername).isEmpty()) {
            return "No current subscription(s), previously subscribed topics may have been deleted.\n";
         }
		Set<Integer> topicIds =  topicList.getSubTopicList().get(subUsername);
		result.append("Current subscriptions (Topic ID : Topic Name : Publisher) : \n");
		for (Integer topicId : topicIds) {
			Topic topic = topicList.getTopic(topicId);
			result.append(topic.getId() + " : " + topic.getName() + " : " + topic.getPubName() + "\n");
		}
		return result.toString();
	};
	
	/**
	 * Unsubscribes a user from a topic.
	 *
	 * @param topicId  The ID of the topic to unsubscribe from.
	 * @param username The username of the subscriber.
	 * @return A success message or an error message if the user is not subscribed
	 *         to the topic.
	 * @throws RemoteException If a remote communication error occurs.
	 */
	@Override
	public synchronized String unsub(int topicId, String username) throws RemoteException{
		if (!topicList.getSubTopicList().containsKey(username)) {
			return "ERROR: No existing subscription to unsubscribe from.\n";
		}
		if (!topicList.getSubTopicList().get(username).contains(topicId)) {
			return "ERROR: No existing subscription to topic " + topicId + " / Topic has been deleted.\n";
		}
		topicList.removeSubscriber(topicId, username);
		connectedSubscribers.remove(username);
		return "SUCCESS: Succesfully unsubscribed to topic " + topicId + ".\n";
	};
	
	/**
	 * Disconnects a subscriber from the broker.
	 *
	 * @param subName The name of the subscriber to disconnect.
	 * @throws RemoteException If a remote communication error occurs.
	 */
	@Override
	public void subDisconnect(String subName) throws RemoteException {
		topicList.disconnectSubscriber(subName);
		connectedSubscribers.remove(subName);
	};
	
	/**
	 * Disconnects a publisher from the broker.
	 *
	 * @param pubName The name of the publisher to disconnect.
	 * @throws RemoteException If a remote communication error occurs.
	 */
	@Override
	public void pubDisconnect(String pubName) throws RemoteException {
	    // Get the list of topic IDs the publisher is responsible for
	    Set<Integer> topics = pubTopicList.get(pubName);
	    
	    // Check if the publisher has topics; if not, simply return or log the error
	    if (topics == null) {
	        System.out.println("No topics found for publisher: " + pubName + ". Nothing to delete.");
	        return;
	    }

	    // Iterate through the topics and remove them
	    for (int topicId : topics) {
	        sendMessage(topicId, "Publisher " + pubName + " has disconnected. Topic " + topicId + " is no longer available and has been removed from your subscription list.");
	        broadcastMessage(topicId, "Publisher " + pubName + " has disconnected. Topic " + topicId + " is no longer available and has been removed from your subscription list.");
	        topicList.deleteTopic(topicId);  // Remove topic from the system
	    }

	    // Remove the publisher from the pubTopicList after cleaning up
	    pubTopicList.remove(pubName);

	    System.out.println("Publisher " + pubName + " has been disconnected and all their topics have been removed.");
	};
	
	 
	// Remove topic from publisher list
	private String removeTopicFromPub(String username, int id) throws RemoteException {
		Set<Integer> topics = pubTopicList.get(username);
		if (topics.contains(id)) {
			Topic topic = topicList.getTopic(id);
			String message = " Topic " + id + " has been deleted and removed from your subscription list.";
			String messageContent =  id + ":" + topic.getName() + ": " + message;
			sendMessage(id, messageContent);
			broadcastMessage(id, messageContent);
			topics.remove(id);
			topicList.deleteTopic(id);
			System.out.println("Topic id " + id + " removed from publisher list");
			return "Topic id " + id + " successfully deleted.\n";
		}
		else {
			return "ERROR: Topic id " + id + " not found in publisher list.\n";
		}

    }
	
	// Broadcast message to all connected brokers
	private synchronized void broadcastMessage(int topicId, String message) throws RemoteException {
		if(!connectedBrokers.isEmpty()) {
			for (IRemoteBroker broker : connectedBrokers) {
				broker.receiveMessageFromBroker(topicId, message);
			}
		}
	}
	
	// Send message to all connected subscribers of a topic on this broker
	private synchronized void sendMessage(int topicId, String message) throws RemoteException {
		List<String> subscribers = topicList.getAllRemoteSubscribers(topicId);
        if (subscribers != null) {
            for (String subscriberName : subscribers) {
            	if(connectedSubscribers.containsKey(subscriberName)){
            		IRemoteSub subObj = connectedSubscribers.get(subscriberName);
                	subObj.receiveMessage(message);  // Notify each subscriber
            	}
            }
        }
	}
	
	/**
     * Updates the heartbeat timestamp for a publisher.
     *
     * @param pubName the name of the publisher
     * @throws RemoteException if a remote communication error occurs
     */
	@Override
	public void sendPubHeartbeat(String pubName) throws RemoteException {
	    publisherHeartbeat.put(pubName, System.currentTimeMillis());
	}
	
	/**
     * Updates the heartbeat timestamp for a subscriber.
     *
     * @param subName the name of the subscriber
     * @throws RemoteException if a remote communication error occurs
     */
	@Override
	public void sendSubHeartbeat(String subName) throws RemoteException {
	    subscriberHeartbeat.put(subName, System.currentTimeMillis());
	}


	/**
     * Checks if any publishers have crashed by comparing the current time 
     * with the last received heartbeat. If a publisher has not sent a heartbeat
     * within the defined interval, it is considered crashed.
     *
     * @throws RemoteException if a remote communication error occurs
     */
	private void checkForCrashedPub() throws RemoteException {
	    long currentTime = System.currentTimeMillis();
	    List<String> crashedPublishers = new ArrayList<>();

	    // Check if any publiRemoteshers have stopped sending heartbeats
	    for (String pubName : publisherHeartbeat.keySet()) {
	        long lastHeartbeat = publisherHeartbeat.get(pubName);
	        if (currentTime - lastHeartbeat > HEARTBEAT_INTERVAL ) {
	            crashedPublishers.add(pubName);
	        }
	    }

	    // Handle each crashed publisher
	    for (String pubName : crashedPublishers) {
	    	System.out.println("Publisher " + pubName + " has crashed.");
	    	pubDisconnect(pubName); // Remove publisher and delete its topics
	    	publisherHeartbeat.remove(pubName); // Remove from heartbeat map
	    }
	}
	
	/**
	 * Checks if any subscribers have crashed by comparing the current time with the
	 * last received heartbeat. If a subscriber has not sent a heartbeat within the
	 * defined interval, it is considered crashed.
	 *
	 * @throws RemoteException if a remote communication error occurs
	 */
	private void checkForCrashedSub() throws RemoteException {
	    long currentTime = System.currentTimeMillis();
	    List<String> crashedSubscribers = new ArrayList<>();

	    // Check if any subscribers have stopped sending heartbeats
	    for (String subName : subscriberHeartbeat.keySet()) {
	        long lastHeartbeat = subscriberHeartbeat.get(subName);
	        if (currentTime - lastHeartbeat > HEARTBEAT_INTERVAL) {
	            System.out.println("Subscriber " + subName + " has stopped sending heartbeats.");
	            crashedSubscribers.add(subName);
	        }
	    }

	    // Handle each crashed subscriber
	    for (String subName : crashedSubscribers) {
	        System.out.println("Disconnecting crashed subscriber: " + subName);
	        subDisconnect(subName);  // Unsubscribe the subscriber
	        subscriberHeartbeat.remove(subName);  // Remove from heartbeat map
	        System.out.println("Subscriber " + subName + " removed from heartbeat map.");
	    }
	}

	// Start a thread to periodically check for crashed subscribers
	public void startSubHeartbeatMonitor() {
	    Thread monitorThread = new Thread(() -> {
	        while (true) {
	            try {
	                Thread.sleep(HEARTBEAT_INTERVAL ); //double the sleep interval to account for network latency
	                checkForCrashedSub();
	            } catch (InterruptedException | RemoteException e) {
	                System.out.println("Error checking for crashed subscribers: " + e.getMessage());
	            }
	        }
	    });
	    monitorThread.start();
	}

	// Method to periodically check for crashed clients
	public void startPubHeartbeatMonitor() {
		Thread monitorThread = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(HEARTBEAT_INTERVAL);
					checkForCrashedPub();
				} catch (InterruptedException | RemoteException e) {
					System.out.println("Error checking for crashed publishers: " + e.getMessage());
				}
			}
		});
		monitorThread.start();
	}


}
