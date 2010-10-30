package system;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import api.Result;
import api.Task;

/**
 * Defines a computer that can execute a {@link api.Task Task}. using <a
 * href="http://en.wikipedia.org/wiki/Divide_and_conquer_algorithm">Divide and
 * conquer algorithm</a>.
 * 
 * @author Manasa Chandrasekhar
 * @author Kowshik Prakasam
 */
public interface Computer extends Remote {

	/**
	 * @param t
	 *            Executes the divide phase of a generic <a
	 *            href="http://en.wikipedia.org/wiki/Divide_and_conquer_algorithm"
	 *            >Divide and conquer</a> task on a remote machine
	 * @return Returns the object returned by the task's decompose method.
	 *         Clients should look at implementations of the abstract class :
	 *         {@link client.Job Job} to understand the types of
	 *         {@link api.Result Result} objects that can be returned by this
	 *         method.
	 * @throws java.rmi.RemoteException
	 */
	Result<?> decompose(Task<?> t) throws RemoteException;

	/**
	 * @param t
	 *            Executes the conquer phase of a generic <a
	 *            href="http://en.wikipedia.org/wiki/Divide_and_conquer_algorithm"
	 *            >Divide and conquer</a> task on a remote machine
	 * @param list
	 *            List of values that needs to be composed during the conquer
	 *            phase of this task
	 * @return Returns the object returned by the task's compose method. Clients
	 *         should look at implementations of the abstract class :
	 *         {@link client.Job Job} to understand the types of
	 *         {@link api.Result Result} objects that can be returned by this
	 *         method.
	 * @throws java.rmi.RemoteException
	 */
	Result<?> compose(Task<?> t, List<?> list) throws RemoteException;

	/**
	 * Sends a new value of the shared object to the compute space
	 * 
	 * @param proposedShared
	 *            A new proposed value of the ({@link system.Shared Shared})
	 *            object
	 * @throws RemoteException
	 */
	boolean broadcast(Shared<?> proposedShared) throws RemoteException;

	/**
	 * Sets the internal shared object which is present in each computer
	 * 
	 * @param proposedShared
	 *            New shared object
	 * @throws RemoteException
	 */
	void setShared(Shared<?> proposedShared) throws RemoteException;

	/**
	 * 
	 * @return The shared object stored by the Computer
	 * @throws RemoteException
	 */
	Shared<?> getShared() throws RemoteException;

	/**
	 * 
	 * @return ID of the computer
	 * @throws RemoteException
	 */
	String getId() throws RemoteException;

	/**
	 * 
	 * @param id
	 *            Sets the ID of the computer
	 * @throws RemoteException
	 */
	void setId(String id) throws RemoteException;

}
