/**
 * Name: Anna Gan, Student ID: 1579818
 */
package directory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import remote.IRemoteDir;

/**
 * The RemoteDir class represents a remote directory service in a distributed publish-subscribe system. 
 * This class provides methods for brokers to register themselves, query available brokers, and retrieve broker details. 
 * This class extends UnicastRemoteObject to allow for remote object transmission over RMI.
 */
public class RemoteDir extends UnicastRemoteObject implements IRemoteDir{
	
	private int brokerNumber;
	private Map<Integer, String> brokerIps = new HashMap<>();
	private Map<Integer, Integer> brokerPorts = new HashMap<>();
	
	/**
	 * Constructs a RemoteDir object, exporting it for RMI.
	 * 
	 * @throws RemoteException If an error occurs during remote object construction.
	 */
	public RemoteDir() throws RemoteException {
		super();
		this.brokerNumber = 0;
	}

	/**
     * Registers a new broker with the specified IP and port, returning a list of active broker ports.
     *
     * @param brokerIp The IP address of the broker.
     * @param brokerPort The port number of the broker.
     * @return A list of currently registered broker ports.
     * @throws RemoteException If a remote communication error occurs.
     * @throws UnknownHostException If the broker's IP address cannot be determined.
     */
	public List<Integer> registerBroker(String brokerIp, int brokerPort) throws RemoteException, UnknownHostException {
		List<Integer> res = new ArrayList<>();
		res.addAll(brokerPorts.values());
		int brokerId = ++brokerNumber;
		brokerIps.put(brokerId, InetAddress.getByName(brokerIp).getHostAddress());
		brokerPorts.put(brokerId, brokerPort);
		return res;
	}

	/**
	 * Queries the directory for a list of active brokers and their details.
	 *
	 * @return A string representation of the active brokers.
	 * @throws RemoteException If a remote communication error occurs.
	 */
	public String queryBroker() throws RemoteException {
		StringBuilder res = new StringBuilder();
		res.append("Active brokers (ip:port):\n");
		for (int i = 1; i <= brokerNumber; i++) {
			res.append( "[" + i + "] " + brokerIps.get(i) + " : " + brokerPorts.get(i) + "\n");
		}
		return res.toString();
	}
	
	/**
     * Retrieves the IP address and port of a specific broker by its ID.
     *
     * @param brokerId The ID of the broker.
     * @return An array containing the broker's IP address and port number.
     * @throws RemoteException If a remote communication error occurs.
     */
	public String[] getBrokerDetails(int brokerId) throws RemoteException {
		String[] res = new String[2];
		res[0] = brokerIps.get(brokerId);
		res[1] = String.valueOf(brokerPorts.get(brokerId));
		return res;
	}
}
