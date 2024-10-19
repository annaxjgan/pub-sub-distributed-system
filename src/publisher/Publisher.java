/**
 * Name: Anna Gan, Student ID: 1579818
 */

package publisher;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import remote.IRemoteBroker;
import remote.IRemoteDir;
import remote.IRemoteBPub;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * The Publisher class represents a publisher in a distributed publish-subscribe system.
 * Publishers can create topics, publish messages to topics, view subscriber counts, and delete topics.
 * This class interacts with a broker through RMI to perform these operations.
 */
public class Publisher {

    private String username;
    private final static Scanner input = new Scanner(System.in);
    private static IRemoteBPub remoteInterface;
    private final static int HEARTBEAT_INTERVAL = 5000;


	/**
	 * Constructs a Publisher object with the specified username, broker IP address,
	 * and broker port number.
	 *
	 * @param username   The username of the publisher.
	 * @param brokerIP   The IP address of the broker.
	 * @param brokerPort The port number of the broker.
	 * @throws RemoteException If a remote communication error occurs.
	 */
    public Publisher(String username, String brokerIP, int brokerPort) throws RemoteException {
        this.username = username;
    };

  
    //Prints the available commands for the publisher.
    private static void printCommandList(){
        System.out.println("Please select command: create, publish, show, delete.");
        System.out.println("1. Create topic (create topic_id topic_name)\n" +
                "2. Publish message for existing topic (publish topic_id message)\n" +
                "3. Show subscriber count for topic (show)\n" +
                "4. Delete topic (delete topic_id)\n" +
                "5. Quit\n");
    }
    
    //Processes the logic for publisher commands based on user input.
    private void processLogic(String[] selectedCommand) throws RemoteException{
    	String result = "";
    	switch(selectedCommand[0].toLowerCase()) {
			case "create":
				if (selectedCommand.length < 3) {
					System.out.println("Error: Invalid number of arguments for create command.");
					System.out.println();
					break;
				}
				try {
					int create_id = Integer.parseInt(selectedCommand[1]);
					String topic_name = "";
					for (int i = 2; i < selectedCommand.length; i++) {
						topic_name += selectedCommand[i] + " ";
					}
					result = remoteInterface.create(create_id, topic_name, username);
					System.out.println(result);
				} catch (NumberFormatException e) {
					System.out.println("Error: Invalid topic ID. Please enter a valid number.\n");
				}
				break;
			case "publish":
				if (selectedCommand.length < 3) {
					System.out.println("Error: Invalid number of arguments for publish command.");
					System.out.println();
					break;
				}
				try {
					int publish_id = Integer.parseInt(selectedCommand[1]);
					String message = selectedCommand[2];
					int i = 3;
					while (i < selectedCommand.length) {
						message += " " + selectedCommand[i];
						i++;
					}
					if (message.length() > 100) {
						System.out.println("Error: Message length should be less than 100 characters.");
						System.out.println();
						break;
					}
					result = remoteInterface.publish(publish_id, message, username);
					System.out.println(result);
					System.out.println();
				} catch (NumberFormatException e) {
					System.out.println("Error: Invalid topic ID. Please enter a valid number.\n");
					System.out.println();
				}
				break;
			case "show":
				result = remoteInterface.show(username);
				System.out.println(result);
				System.out.println();
				break;
			case "delete":
				if (selectedCommand.length != 2) {
					System.out.println("Error: Invalid number of arguments for delete command.");
					System.out.println();
					break;
				}
				try {
					int delete_id = Integer.parseInt(selectedCommand[1]);
					result = remoteInterface.delete(delete_id, username);
					System.out.println(result);
					System.out.println();
				} catch (NumberFormatException e) {
					System.out.println("Error: Invalid topic ID. Please enter a valid number.\n");
					System.out.println();
				}
				break;
			default:
				System.out.println("ERROR: Invalid command/input. Please try again.\n");
    	}
    }
    
    //Starts the heartbeat thread to send heartbeats to the broker
    private void startHeartbeat(String brokerIP, int brokerPort) {
        Thread heartbeatThread = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(HEARTBEAT_INTERVAL);
                    // Send heartbeat to broker
                    remoteInterface.sendPubHeartbeat(username);
                }
            } catch (InterruptedException | RemoteException e) {
                System.out.println("Failed to send heartbeat: " + e.getMessage());
            }
        });
        heartbeatThread.start();
    }

    //Main method for the Publisher class.
    public static void main(String[] args){
    	
    	if (args.length!=3){
    		System.out.println("Invalid argument length. Usage example \"java -jar publisher.jar username directory_ip directory_port\"");
    		System.exit(0);
    	}
        
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
			
			Publisher publisher = new Publisher(username, brokerIP, brokerPort);
			publisher.startHeartbeat(brokerIP, brokerPort);
	        Registry registry = LocateRegistry.getRegistry(brokerPort);
	        remoteInterface = (IRemoteBPub) registry.lookup("remotePub");
	        printCommandList();
	        System.out.print("Select command: ");
	        String[] selectedCommand = input.nextLine().split(" ");
	        
	        while(!selectedCommand[0].toLowerCase().equals("quit")){
            	publisher.processLogic(selectedCommand);
            	printCommandList();
            	System.out.print("Select command: ");
            	selectedCommand = input.nextLine().split(" ");	
	        }
	        
			remoteInterface.pubDisconnect(username);
	        System.out.println("Publisher exits.");
	        System.exit(0);
	        
		} catch(InputMismatchException e) {
			System.out.println("Invalid broker number. Please try again.");
		} catch (NumberFormatException e) {
    		System.err.println("Error: Invalid format for port number or ip address");
            System.exit(1);
		}catch (RemoteException e) {
			System.out.println("Remote exception occurred."+ e.getMessage().split(";")[0]);
		} catch (NotBoundException e) {
			System.out.println("Remote object not found: "+ e.getMessage());
		} catch (UnknownHostException e) {
			System.out.println("Unknown host exception occurred: "+ e.getMessage());
		}

    }
}