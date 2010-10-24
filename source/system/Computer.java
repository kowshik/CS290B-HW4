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
	 * @param shared
	 *            Shared object containing the latest broadcast message from the
	 *            compute space
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

	void broadcast(Shared<?> proposedShared) throws RemoteException;

	void setShared(Shared<?> proposedShared) throws RemoteException;

	Shared<?> getShared();
	
	String getId() throws RemoteException;
	void setId(String id) throws RemoteException;

}
