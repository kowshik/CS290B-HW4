package api;

import java.rmi.RemoteException;
import java.util.List;

import system.Computer;
import system.Shared;

/**
 * Models any task that can be executed on a remote machine using <a
 * href="http://en.wikipedia.org/wiki/Divide_and_conquer_algorithm">Divide and
 * conquer algorithm</a>. Every task in the Divide and Conquer tree is
 * identified by an ID. The parent of each task is identified by a parentID.
 * 
 * @author Manasa Chandrasekhar
 * @author Kowshik Prakasam
 */
public interface Task<T> {

	/**
	 * Enum to describe the status of the task during the Divide and the Conquer
	 * phase. If the task's status is set to DECOMPOSE, then it is said to be in
	 * the divide phase of execution where the current task is being split into
	 * smaller subtasks for parallelization on a cluster. If the task's status
	 * is set to COMPOSE, then it is said to be in the conquer phase of
	 * execution where results from smaller tasks are being aggregated.
	 */
	enum Status {
		DECOMPOSE, COMPOSE
	};

	/**
	 * This method models the 'Divide' phase of the <a
	 * href="http://en.wikipedia.org/wiki/Divide_and_conquer_algorithm">Divide
	 * and conquer algorithm</a>. Any subclass implementing this method should
	 * divide the existing task into smaller subtasks, pack the subtasks in a
	 * {@link api.Result Result} object and return them. The returned subtasks
	 * are consumed by a {@link api.Client2Space Space} object by queueing them
	 * again for execution on different machine in the cluster. If the base case
	 * of recursion has been reached during the process of decomposition, then
	 * the value can be returned in the {@link api.Result Result} object,
	 * instead of subtasks.
	 * 
	 * @return A {@link api.Result Result} object containing either subtasks
	 *         that have been created during this decomposition stage, or value
	 *         of a base case in the underlying recursion
	 */
	Result<?> decompose();

	/**
	 * This method models the 'Conquer' phase of the <a
	 * href="http://en.wikipedia.org/wiki/Divide_and_conquer_algorithm">Divide
	 * and conquer algorithm</a>. Any subclass implementing this method should
	 * compose results of smaller subtasks (passed as a parameter) that were
	 * earlier generated by this task during the 'Divide' phase, into a single
	 * value that can be consumed by the parent of this task in the overall
	 * recursion tree.
	 * 
	 * @return A {@link api.Result Result} object containing a value obtained by
	 *         composing results of smaller subtasks that were generated by this
	 *         task in the 'Divide' phase
	 * @param list
	 *            <?> A java.util.List containing results obtained from the
	 *            smaller subtasks that were originally generated by the same
	 *            task
	 */

	Result<?> compose(List<?> list);

	/**
	 * @return Returns the status of this task
	 * 
	 * 
	 */
	Task.Status getStatus();

	/**
	 * Sets the status of this task. This method can be used by a
	 * {@link api.Client2Space Space} object to switch the status of a subtask
	 * after the from DECOMPOSE (after the 'Divide' phase) into COMPOSE (to
	 * start the 'Conquer' phase).
	 * 
	 * 
	 */

	void setStatus(Task.Status s);

	/**
	 * 
	 * @return ID of the parent task in the recursion tree
	 */
	String getParentId();

	/**
	 * 
	 * Sets the ID of the parent task in the recursion tree
	 */
	void setParentId(String id);

	/**
	 * 
	 * @return Unique ID representing this task in the recursion tree
	 */

	String getId();

	/**
	 * 
	 * Sets a unique ID representing this task in the recursion tree
	 */

	void setId(String taskId);

	/**
	 * 
	 * @return Number of smaller subtasks generated by this task at the end of
	 *         the DECOMPOSE ('Divide') phase.
	 */

	int getDecompositionSize();

	/**
	 * 
	 * @return Unique IDs of all child tasks generated by this task at the end
	 *         of the DECOMPOSE ('Divide') phase
	 */
	List<String> getChildIds();

	Object getShared() throws RemoteException;

	void setShared(Shared<?> shared) throws RemoteException;

	void setComputer(Computer computer);
}
