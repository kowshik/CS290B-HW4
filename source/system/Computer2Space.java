package system;

import java.rmi.RemoteException;

/**
 * Compute server's abstraction of the compute space ({@link api.Client2Space
 * Space})
 */
public interface Computer2Space extends java.rmi.Remote {

	String SERVICE_NAME = "Space";

	/**
	 * {@link system.Computer Computer} objects can use this method to register
	 * with a compute space ({@link api.Client2Space Space})
	 * 
	 * @param computer
	 *            Registers a remote computer with the resource allocator that
	 *            manages the cluster operations
	 * @throws java.rmi.RemoteException
	 */
	void register(Computer computer) throws RemoteException;

	void broadcast(Broadcast broadcast) throws RemoteException;
}