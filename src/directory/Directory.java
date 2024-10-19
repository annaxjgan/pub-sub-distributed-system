/**
 * Name: Anna Gan, Student ID: 1579818
 */
package directory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import remote.IRemoteTopic;
import remote.IRemoteDir;

/**
 * Directory service for a distributed publish-subscribe system. Allows brokers
 * to register themselves, query available brokers, and retrieve broker details.
 */
public class Directory {
	
	// Main method to create a directory registry and bind the topic remote object to it.
	public static void main(String args[]) {
		
		if (args.length!=2) {
			System.out.println("Invalid argument length. Usage example \"java -jar directory.jar ip port\"");
			System.exit(0);
		}
	
		try {
			
			String ip = args[0];
			InetAddress inetAddress = InetAddress.getByName(ip);
			int port = Integer.parseInt(args[1]);
			
			Registry registry = LocateRegistry.createRegistry(port);
			IRemoteTopic topicList = new RemoteTopic(); 

		    registry.bind("topics", topicList);
		    IRemoteDir directory = new RemoteDir(); 
		    registry.bind("directory", directory);
		    System.out.println("Directory service started at " + ip + ":" + port);
		    
		} catch (NumberFormatException e) {
    		System.err.println("Error: Invalid format for port number or worker pool size");
            System.exit(1);
		} catch (RemoteException e) {
            System.out.println("RemoteException: " + e.getMessage());
        } catch (UnknownHostException e) {
            System.out.println("Exception: " + e.getMessage());
        } catch (AlreadyBoundException e) {
        	System.out.println("Exception: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
		
	}
}
