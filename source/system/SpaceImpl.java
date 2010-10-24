package system;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import api.Result;
import api.Client2Space;
import api.Task;

/**
 * Implementation of the Space Interface. Represents a raw computing resource
 * where tasks ({@link api.Task Task}) are automatically executed by registered
 * workers as soon as they are dropped in. If a worker crashes, the computation
 * would still continue (assuming there are other workers still running), since
 * each task is executed under a transaction, which would be rolled back after
 * the worker crashed, leaving the task in the space for another worker to pick
 * up. For more information, please refer <a
 * href="http://today.java.net/pub/a/today/2005/04/21/farm.html">How to build a
 * compute farm</a>.
 * 
 * The multithreading design implemented by this class is that of the <a
 * href="http://en.wikipedia.org/wiki/Cilk">Cilk</a> runtime. Please read the
 * architecture of Cilk to understand the class better.
 * 
 * @author Manasa Chandrasekhar
 * @author Kowshik Prakasam
 * 
 */
public class SpaceImpl extends UnicastRemoteObject implements Client2Space,
		Computer2Space, Runnable {

	private Thread t;
	private static final long serialVersionUID = 3093568798450948074L;
	private Map<String, Successor> waitingTasks;
	private LinkedBlockingQueue<Result<?>> results;
	private List<ComputerProxy> proxies;
	private static final int PORT_NUMBER = 2672;
	private Shared<?> shared;

	/**
	 * Default constructor
	 * 
	 * @throws RemoteException
	 */
	public SpaceImpl() throws RemoteException {

		this.waitingTasks = Collections
				.synchronizedMap(new HashMap<String, Successor>());
		this.results = new LinkedBlockingQueue<Result<?>>();
		this.proxies = Collections
				.synchronizedList(new Vector<ComputerProxy>());
		t = new Thread(this, "Space");
		t.start();
	}

	public boolean put(Task<?> aTask) throws RemoteException {
		if (proxies.size() > 0) {
			int random = new Random().nextInt(this.proxies.size());
			proxies.get(random).addTask(aTask);
			return true;
		}

		return false;
	}

	/**
	 * @see api.Client2Space#compute(Task, Shared) Client2Space.compute(Task,
	 *      Shared)
	 */

	public Result<?> compute(Task<?> aTask, Shared<?> shared)
			throws java.rmi.RemoteException {

		this.shared = shared;
		if (this.put(aTask)) {
			try {
				return results.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.err
				.println("Unable to register tasks due to absence of computer proxies");
		return null;
	}

	/**
	 * Used to add to the queue of {@link api.Result Result} objects in this
	 * compute space
	 * 
	 * @throws RemoteException
	 */
	public void putResult(Result<?> result) throws RemoteException {
		results.add(result);
	}

	/**
	 * Remote method for the computers to register to the compute space
	 * 
	 * @throws RemoteException
	 */
	@Override
	public synchronized void register(Computer computer) throws RemoteException {
		ComputerProxy aProxy = new ComputerProxy(computer, this);
		this.proxies.add(aProxy);
	}

	public synchronized void addProxy(ComputerProxy aProxy) {
		this.proxies.add(aProxy);
	}

	public synchronized void removeProxy(ComputerProxy aProxy) {
		this.proxies.remove(aProxy);
	}

	/**
	 * Starts the compute space and binds remote objects into the RMI registry
	 * 
	 * @param args
	 *            Command-line arguments can be passed (if any)
	 */
	public static void main(String[] args) {

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		try {

			Client2Space space = new SpaceImpl();
			Registry registry = LocateRegistry.createRegistry(PORT_NUMBER);
			registry.rebind(Client2Space.SERVICE_NAME, space);
			System.out.println("Space instance bound");
		} catch (Exception e) {
			System.err.println("SpaceImpl exception:");
			e.printStackTrace();

		}
	}

	/**
	 * Polls for {@link system.Successor Successor} threads to be execute once
	 * they move into READY status
	 */
	@Override
	public void run() {
		while (true) {
			synchronized (this) {
				Set<Entry<String, Successor>> successorSet = waitingTasks
						.entrySet();
				for (Entry<String, Successor> e : successorSet) {
					Successor s = e.getValue();
					if (s.getStatus() == Successor.Status.READY) {
						s.start();
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param s
	 *            Successor thread to be added to the queue
	 */
	public void addSuccessor(Successor s) {
		synchronized (this) {
			waitingTasks.put(s.getId(), s);
		}

	}

	/**
	 * 
	 * @param successorId
	 *            Successor thread to be removed from the queue
	 */
	public void removeSuccessor(String successorId) {
		synchronized (this) {
			waitingTasks.remove(successorId);
		}

	}

	/**
	 * 
	 * @param id
	 *            ID of the successor thread whose Closure object is required
	 * @return Gets the closure object corresponding to the Successor thread.
	 */
	public Successor.Closure getClosure(String id) {
		synchronized (this) {
			return waitingTasks.get(id).getClosure();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see system.Computer2Space#receiveBroadcast(system.Broadcast)
	 */
	@Override
	public synchronized void broadcast(Broadcast broadcast)
			throws RemoteException {
		Shared<?> newShared = broadcast.getShared();
		String computerId = broadcast.getComputerId();
		if (!shared.isNewerThan(newShared)) {
			this.setShared(newShared);
			for (ComputerProxy cp : proxies) {
				if (!cp.getId().equals(computerId)) {
					cp.setShared(newShared);
				}
			}
		}
	}

	/**
	 * @param newShared
	 */
	private synchronized void setShared(Shared<?> newShared) {
		this.shared=newShared;
	}

	public synchronized Shared<?> getShared() {
		return this.shared;
	}
}
