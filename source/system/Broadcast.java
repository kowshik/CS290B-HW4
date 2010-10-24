/**
 * 
 */
package system;

import java.io.Serializable;

/**
 * @author Manasa Chandrasekhar
 * @author Kowshik Prakasam
 *
 */
public class Broadcast implements Serializable {
	
	private static final long serialVersionUID = 8867562356400662337L;
	private Shared<?> shared;
	private String computerId;

	public Broadcast(Shared<?> shared, String computerId){
		this.shared=shared;
		this.computerId=computerId;
	}
	
	public Shared<?> getShared(){
		return this.shared;
	}
	public String getComputerId(){
		return computerId;
	}
}
