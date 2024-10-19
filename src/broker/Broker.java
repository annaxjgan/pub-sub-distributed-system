/**
 * Name: Anna Gan, Student ID: 1579818
 */
package broker;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import directory.RemoteTopic;
import remote.IRemoteBroker;
import remote.IRemoteDir;
import remote.IRemoteSub;
import remote.IRemoteBPub;
import remote.IRemoteBSub;
import remote.IRemoteTopic;

/**
 * The Broker class serves as the main entry point for a broker in a distributed publish-subscribe system. It initializes the broker, 
 * registers it with the directory service, and binds remote objects for publishing and subscribing to topics.
 */
public class Broker {
	
	private static String ip;
	private static List<IRemoteBroker> connectedBrokers = new ArrayList<>();
	private static Map<String, IRemoteSub> connectedSubscribers = new HashMap<>();
	private static Map<String, Set<Integer>> pubTopicList = new HashMap<>();
	static IRemoteTopic topicList; 
	
	/**
     * The main method initializes the broker, registers it with the directory service, and binds remote interfaces for publishing  and subscribing to topics.
     *
     * @param args Command-line arguments: directory port, broker IP, and broker port.
     */
	public static void main(String[] args) {
		if (args.length!=4){
			System.out.println("Invalid argument length. Usage example \"java -jar broker.jar directory_ip directory_port ip port\"");
			System.exit(0);
		}
		
		try {
			String dirIP = args[0];
			InetAddress dirAddress = InetAddress.getByName(dirIP);
			int dirPort = Integer.parseInt(args[1]);
			ip = args[2];
			InetAddress inetAddress = InetAddress.getByName(ip);
			int brokerPort = Integer.parseInt(args[3]);
			
			Registry registry = LocateRegistry.createRegistry(brokerPort);
			Registry dirRegistry = LocateRegistry.getRegistry(dirPort);
			IRemoteDir directory = (IRemoteDir) dirRegistry.lookup("directory");
			topicList = (IRemoteTopic) dirRegistry.lookup("topics");
			
			List<Integer> activeBrokers = directory.registerBroker(ip, brokerPort);
			if(!activeBrokers.isEmpty()) {
				for (Integer port:activeBrokers) {
					Registry otherBrokerRegistry = LocateRegistry.getRegistry(port);
					IRemoteBroker otherBroker = (IRemoteBroker) otherBrokerRegistry.lookup("remoteBroker");
					connectedBrokers.add(otherBroker);
				}
				System.out.println();
			}
				
		    
		    RemoteBroker brokerObj = new RemoteBroker(connectedBrokers, topicList, connectedSubscribers, pubTopicList);
		    brokerObj.startPubHeartbeatMonitor();
		    brokerObj.startSubHeartbeatMonitor();

		    IRemoteBPub remoteObjPub = brokerObj;
		    IRemoteBSub remoteObjSub = brokerObj;
		    IRemoteBroker remoteObjBroker = brokerObj;
		    
		    registry.bind("remotePub", remoteObjPub);
		    registry.bind("remoteSub", remoteObjSub);
		    registry.bind("remoteBroker", remoteObjBroker);
		    
			
			// Notify other brokers of connection, and send remote object over
			if (!connectedBrokers.isEmpty()) {
				for (IRemoteBroker connectedBroker : connectedBrokers) {
                    connectedBroker.receiveConnection(brokerPort);
                }
            } 
			    
		    System.out.println("Broker server ready");
		    System.out.println("Remote interfaces bound to port: " + brokerPort);
			
		} catch (NumberFormatException e) {
			System.out.println("Error: Invalid format for directory or broker port.");
		} catch (UnknownHostException e) {
			System.out.println("Unknown host exception occurred. " + e.getMessage());
		} catch (RemoteException re) {
		    System.out.println("Remote exception occurred. " + re.getMessage().split(";")[0] + " OR Incorrect directory port.");
		} catch (AlreadyBoundException e) {
		    System.out.println("Remote object already bound.");
		} catch (NotBoundException e) {
			System.out.println("Remote object not found.");
		} 

	}
}
