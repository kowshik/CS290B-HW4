package system;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

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
		this.shared = new TspShared(TspShared.INFINITY);
	}

	
	/**
	 * @see api.Task Task
	 */
	@Override
	public Result<?> execute(Task<?> t) {
		t.setComputer(this);
		return t.execute();
	}

	@Override
	public void setShared(Shared<?> shared) {
		this.shared=shared;
		
	}

	
	

	@Override
	public synchronized boolean broadcast(Shared<?> proposedShared) throws RemoteException{

		if (proposedShared.isNewerThan(shared)) {
			shared = proposedShared;
			space.broadcast(new Broadcast(this.shared, this.getId()));
			return true;
		}
		return false;
	}

	
	@Override
	public synchronized Shared<?> getShared() {
		return this.shared;
	}

	
	@Override
	public String getId() {
		
		return this.id;
	}

	
	@Override
	public void setId(String id) {
		System.out.println("Got ID : "+id);
		this.id=id;
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
			System.err.println("ComputerImpl Remote exception : ");
			e.printStackTrace();

		} catch (MalformedURLException e) {
			System.err.println("ComputerImpl Malformed exception : ");
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.err.println("ComputerImpl NotBound exception : ");
			e.printStackTrace();
		}
	}


}
