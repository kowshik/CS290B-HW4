package system;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import api.Result;
import api.Task;

/**
 * Defines the remote server which is accessed by the client for execution of
 * objects of type {@link api.Task Task}
 * 
 * @author Manasa Chandrasekhar
 * @author Kowshik Prakasam
 */

public class ComputerImpl extends UnicastRemoteObject implements Computer {

	private static final long serialVersionUID = -4634299253959618077L;
	private Shared<?> shared;
	private Computer2Space space;
	private String id;
	
	/**
	 * Sets up the server for execution
	 * 
	 * @throws RemoteException
	 */
	public ComputerImpl(Computer2Space space) throws RemoteException {
		super();
		this.space=space;
	}

	@Override
	/**
	 * @see api.Task Task
	 */
	public Result<?> decompose(Task<?> t) {
		t.setComputer(this);
		return t.decompose();
	}

	/**
	 * @param shared
	 */
	public synchronized void setShared(Shared<?> shared) {
		this.shared=shared;
		
	}

	/**
	 * 
	 * Register Computer objects to the compute space
	 */
	public static void main(String[] args) {
		String computeSpaceServer = args[0];
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		try {

			Computer2Space space = (Computer2Space) Naming.lookup("//"
					+ computeSpaceServer + "/" + Computer2Space.SERVICE_NAME);
			
			ComputerImpl comp = new ComputerImpl(space);
			space.register(comp);
			
			System.out.println("Computer ready");
		} catch (RemoteException e) {
			System.err.println("ComputerImpl exception : ");
			e.printStackTrace();

		} catch (MalformedURLException e) {
			System.err.println("ComputerImpl exception : ");
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.err.println("ComputerImpl exception : ");
			e.printStackTrace();
		}
	}

	@Override
	/**
	 * @see api.Task Task
	 */
	public Result<?> compose(Task<?> t, List<?> list) throws RemoteException {
		return t.compose(list);
	}

	/**
	 * 
	 */
	public synchronized void broadcast(Shared<?> proposedShared) throws RemoteException{

		if (proposedShared.isNewerThan(shared)) {
			shared = proposedShared;
			space.broadcast(new Broadcast(this.shared, this.getId()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see system.Computer#getShared()
	 */
	@Override
	public synchronized Shared<?> getShared() {
		return this.shared;
	}

	/* (non-Javadoc)
	 * @see system.Computer#getId()
	 */
	@Override
	public String getId() {
		return this.id;
	}

	/* (non-Javadoc)
	 * @see system.Computer#setId(java.lang.String)
	 */
	@Override
	public void setId(String id) {
		this.id=id;
	}

}
