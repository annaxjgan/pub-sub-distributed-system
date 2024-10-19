/**
 * Name: Anna Gan, Student ID: 1579818
 */

package subscriber;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.InputMismatchException;
import java.util.Scanner;
import remote.IRemoteBSub;
import remote.IRemoteSub;
import remote.IRemoteDir;

/**
 * The Subscriber class represents a subscriber in a distributed publish-subscribe system.
 * This class handles subscriber actions such as listing topics, subscribing to topics, 
 * showing current subscriptions, and unsubscribing from topics. The class interacts with 
 * the broker to perform these operations via RMI.
 */
public class Subscriber {
	
	private String username;
	private IRemoteSub remoteSub;
    private final static Scanner input = new Scanner(System.in);
	private static IRemoteBSub remoteInterface;
	private final static int HEARTBEAT_INTERVAL = 5000;

	/**
     * Constructor for the Subscriber class, which initializes a new subscriber with the given username
     * and exports the remote subscriber object.
     *
     * @param username The name of the subscriber.
     * @throws RemoteException If a remote communication error occurs.
     */
	public Subscriber(String username) throws RemoteException {
		this.username = username;
		this.remoteSub = new RemoteSub();
	   
	}
	
	//Processes the logic for subscriber commands based on user input.
	private void processLogic(String[] command) throws RemoteException {
		String result = "";
		switch(command[0].toLowerCase()) {
			case "list":
				result = remoteInterface.list();
				System.out.println(result);
				break;
			case "sub":
				if (command.length != 2) {
					System.out.println("ERROR: Invalid number of arguments for sub command.\n");
					break;
				}
				try {
					int topicId = Integer.parseInt(command[1]);
					String username = this.username;
					result = remoteInterface.sub(topicId, remoteSub, username);
					System.out.println(result);
				} catch (NumberFormatException e) {
					System.out.println("ERROR: Invalid topic ID. Please enter a valid number.\n");
				}
				break;
			case "current":
				result = remoteInterface.current(this.username);
				System.out.println(result);
				break;
			case "unsub":
				if (command.length != 2) {
					System.out.println("ERROR: Invalid number of arguments for unsub command.\n");
					break;
				}
				try {
					int id = Integer.parseInt(command[1]);
					result = remoteInterface.unsub(id, this.username);
					System.out.println(result);
				} catch (NumberFormatException e) {
					System.out.println("ERROR: Invalid topic ID. Please enter a valid number.\n");
				}
				break;
			default:
				System.out.println("ERROR: Invalid command/input. Please try again.\n");
				break;
		
		}
	}
	
	//Prints the available commands for the subscriber.
    private static void printCommandList(){
        System.out.println("Please select command: list, sub, current, unsub");
        System.out.println("1. List all topics (list)\n" +
                "2. Subscribe to a topic (sub topic_id)\n" +
                "3. Show current subscriptions (current)\n" +
                "4. Unsubcribe from a topic (unsub topic_id)\n" +
                "5. Quit\n");
    }
    
    //Starts the heartbeat thread for the subscriber.
    private void startHeartbeat(String brokerIP, int brokerPort) {
        Thread heartbeatThread = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(HEARTBEAT_INTERVAL);
                    // Send heartbeat to broker
                    remoteInterface.sendSubHeartbeat(username);
                    
                }
            } catch (InterruptedException | RemoteException e) {
                System.out.println("Failed to send heartbeat: " + e.getMessage());
            }
        });
        heartbeatThread.start();
    }
   
	//Main method for the subscriber, which connects to the broker and processes user input.
	public static void main(String[] args) {
		
		if (args.length != 3) {
			System.out.println("Invalid argument length. Usage example \"java -jar subscriber.jar username directory_ip directory_port\"");
			System.exit(0);
		}
		
		Subscriber subscriber;
		try {
			
			String username = args[0];
			String dirIP = args[1];
			InetAddress inetAddress = InetAddress.getByName(dirIP);
			int dirPort = Integer.parseInt(args[2]);
			
			Registry dirRegistry = LocateRegistry.getRegistry("localhost",dirPort);
			IRemoteDir remoteDir = (IRemoteDir) dirRegistry.lookup("directory");
			System.out.println(remoteDir.queryBroker());
			System.out.print("Select broker number:");
			int brokerNum = input.nextInt();
			input.nextLine();
			String brokerIP = remoteDir.getBrokerDetails(brokerNum)[0];
			int brokerPort = Integer.parseInt(remoteDir.getBrokerDetails(brokerNum)[1]);
			
			subscriber = new Subscriber(username);
			subscriber.startHeartbeat(brokerIP, brokerPort);
			
			Registry registry = LocateRegistry.getRegistry(brokerIP, brokerPort);
			remoteInterface = (IRemoteBSub) registry.lookup("remoteSub");
			printCommandList();
			String[] selectedCommand = input.nextLine().split(" ");
	        
	        while(!selectedCommand[0].toLowerCase().equals("quit")){
            	subscriber.processLogic(selectedCommand);
            	printCommandList();
            	selectedCommand = input.nextLine().split(" ");	
	        }
	        
	        remoteInterface.subDisconnect(username);
	        System.out.println("Subscriber exits.");
	        System.exit(0);
		}catch(InputMismatchException e) {
			System.out.println("Invalid broker number. Please try again.");
		}catch (NumberFormatException e) {
    		System.err.println("Error: Invalid format for port number or ip address");
            System.exit(1);
		} catch (RemoteException e) {
			System.out.println("Remote exception occured. "+ e.getMessage().split(";")[0]);
		} catch (NotBoundException e) {
			System.out.println("Remote object not found. "+ e.getMessage());
		} catch (UnknownHostException e) {
			System.out.println("Unknown host exception occurred. " + e.getMessage());
		} 
		
	}
}
