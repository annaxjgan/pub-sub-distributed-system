/**
 * Name: Anna Gan, Student ID: 1579818
 */
package remote;

import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Remote interface for directory services in a distributed publish-subscribe system.
 * Provides methods for brokers to register themselves, query available brokers, and retrieve broker details.
 */
public interface IRemoteDir extends Remote{
	
	public List<Integer> registerBroker(String brokerIp, int brokerPort) throws RemoteException, UnknownHostException;
	
	public String queryBroker() throws RemoteException;
	
	public String[] getBrokerDetails(int brokerId) throws RemoteException;
}
