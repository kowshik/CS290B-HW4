package system;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import system.Successor.Closure;
import api.Result;
import api.Task;

/**
 * For every {@link system.Computer Computer} instance that registers with the
 * compute space ({@link api.Client2Space Space}), there is a proxy maintained
 * by the space. This allows the compute space to maintain multiple threads for
 * each instance of registered {@link system.Computer Computer} objects. This
 * class is responsible for execution of {@link api.Task Task} objects in the
 * registered remote computers.
 * 
 * Each proxy maintains a queue of tasks that need to be executed one after the
 * other on a remote machine. These tasks can either represent the Divide phase
 * or the Conquer phase in the <a
 * href="http://en.wikipedia.org/wiki/Divide_and_conquer_algorithm">Divide and
 * conquer algorithm</a>.
 * 
 * @author Manasa Chandrasekhar
 * @author Kowshik Prakasam
 * 
 */
public class ComputerProxy implements Runnable {
	private static final String LOG_FILE_PREFIX = "/cs/student/kowshik/computerproxy_";
	private Computer compObj;
	private SpaceImpl space;
	private Thread t;
	private LinkedBlockingQueue<Task<?>> tasks;
	private String id;
	private Logger logger;
	private Handler handler;

	/**
	 * 
	 * @param compObj
	 *            Computer registed with the compute space (
	 *            {@link api.Client2Space Space})
	 * @param space
	 *            Implementation of ({@link api.Client2Space Space}) which is
	 *            responsible for maintaining each instance of this class
	 * @throws RemoteException
	 */
	public ComputerProxy(Computer compObj, SpaceImpl space)
			throws RemoteException {
		this.compObj = compObj;
		this.space = space;
		this.tasks = new LinkedBlockingQueue<Task<?>>();
		this.id = new Random().nextInt() + "";
		compObj.setId(id);
		this.logger = Logger.getLogger("ComputerProxy" + id);
		this.logger.setUseParentHandlers(false);
		this.handler = null;
		try {
			this.handler = new FileHandler(LOG_FILE_PREFIX + id + ".log");

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.handler.setFormatter(new SimpleFormatter());
		logger.addHandler(handler);
		t = new Thread(this, "ComputerProxy " + getRandomProxyName());

		t.start();

	}

	public String getId() {
		return id;
	}

	public synchronized void setShared(Shared<?> newShared)
			throws RemoteException {
		compObj.setShared(newShared);
	}

	/**
	 * Loops infinitely and attempts to fetch a {@link api.Task Task} object
	 * from the proxy's queue and executes it. If the thread is interrupted, the
	 * task is returned to the compute space's queue. If the task execution is
	 * successful, then the {@link api.Result Result} produced is also added to
	 * compute space's queue of {@link api.Result Result} objects.
	 * 
	 * These tasks can either represent the Divide phase or the Conquer phase in
	 * the <a
	 * href="http://en.wikipedia.org/wiki/Divide_and_conquer_algorithm">Divide
	 * and conquer algorithm</a>. Divide tasks are represented by the DECOMPOSE
	 * status and Conquer tasks are represented by the CONQUER status. The proxy
	 * switches the status of the task to COMPOSE, immediately after the Divide
	 * phase is over.
	 */
	public void run() {
		boolean isAlive = true;
		while (isAlive) {
			if (!tasks.isEmpty()) {

				Task<?> aTask = null;
				try {
					aTask = tasks.take();
					System.out.println("Taken task :" + aTask.getId());
					Result<?> r = null;
					switch (aTask.getStatus()) {
					case DECOMPOSE:
						r = compObj.decompose(aTask);

						/*
						 * This task has generated child tasks, so a successor
						 * has to be created. Each child task is also added to
						 * the space.
						 */

						if (r.getSubTasks() != null) {
							Successor s = new Successor(aTask, space,
									aTask.getDecompositionSize());
							space.addSuccessor(s);

							for (Task<?> task : r.getSubTasks()) {
								space.put(task);
							}
						}
						/*
						 * There are no child tasks, but the DECOMPOSE stage has
						 * returned a value. It means that this is the base case
						 * of recursion. Base cases can be produced only during
						 * the COMPOSE stage if the entire recursion tree has
						 * more than one node. They can also be produced in the
						 * DECOMPOSE stage when the entire recursion tree has
						 * just one and only one node. For example, consider a
						 * case where the Client passes the Fibonacci task :
						 * F(0) or F(1) where the entire recursion tree has only
						 * one node.
						 */
						else if (r.getValue() != null
								&& (aTask.getId().equals(aTask.getParentId()))) {

							space.putResult(r);
							logger.info("Elapsed Time="
									+ (r.getEndTime() - r.getStartTime()));
						}
						/*
						 * If the DECOMPOSE stage has neither returned sub tasks
						 * nor values, then the node must have been pruned in
						 * branch-and-bound. So just pass on the null value to
						 * the parent's closure.
						 */
						else {

							Closure parentClosure = space.getClosure(aTask
									.getParentId());
							parentClosure.put(r.getValue());
							logger.info("Elapsed Time="
									+ (r.getEndTime() - r.getStartTime()));

						}

						aTask.setStatus(Task.Status.COMPOSE);
						break;
					case COMPOSE:
						Closure taskClosure = space.getClosure(aTask.getId());
						r = compObj.compose(aTask, taskClosure.getValues());

						/*
						 * When the parent ID is equal to the task's ID, then it
						 * represents the very first node of the recursion tree.
						 * It also means that the task has completed execution,
						 * and so the result is written into space.
						 */
						if (aTask.getId().equals(aTask.getParentId())) {
							space.putResult(r);
							Shared<?> proposedShared = compObj.getShared();
							if((Double) compObj.getShared().get() < (Double) (space.getShared().get())){
								space.setShared(proposedShared);
								Broadcast newBroadcast = new Broadcast(proposedShared,compObj.getId());
								space.broadcast(newBroadcast);
								System.out.println("Space shared Object value:" +space.getShared().get());
							}
							
						}
						/*
						 * Otherwise, this is just yet another COMPOSE stage in
						 * the recursion. So, get the closure of the parent
						 * thread from the space and write the result to it.
						 */
						else {
							Closure parentClosure = space.getClosure(aTask
									.getParentId());
							parentClosure.put(r.getValue());
						}

						/*
						 * Remove the successor thread that triggered this
						 * COMPOSE stage as its life is over.
						 */
						space.removeSuccessor(aTask.getId());
						logger.info("Elapsed Time="
								+ (r.getEndTime() - r.getStartTime()));
						break;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (RemoteException e) {
					System.err
							.println("ComputerProxy : RemoteException occured in thread : "
									+ this.t.getName());
					System.err.println("Reassigning task to task queue");
					try {
						space.put(aTask);
					} catch (RemoteException ex) {
						System.err
								.println("Unable to reassign task to task queue");
						ex.printStackTrace();
					}
					isAlive = false;
					space.removeProxy(this);
				}

			}
		}

	}

	/**
	 * 
	 * @param aTask
	 *            A task to be added to this proxy's queue
	 */
	public synchronized void addTask(Task<?> aTask) {
		try {
			this.tasks.put(aTask);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @return A random thread name made up of exactly three alphabets
	 */
	private static String getRandomProxyName() {
		char first = (char) ((new Random().nextInt(26)) + 65);
		char second = (char) ((new Random().nextInt(26)) + 65);
		char third = (char) ((new Random().nextInt(26)) + 65);
		return "" + first + second + third;
	}

}
