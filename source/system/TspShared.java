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
public class TspShared implements Shared<Double>, Serializable {

	private static final long serialVersionUID = 165386141205567783L;
	private double upperBound;
	public static final Double INFINITY = -1.0d;

	public TspShared(double distance) {
		this.upperBound = distance;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see system.Shared#isNewerThan(system.Shared)
	 */
	@Override
	public boolean isNewerThan(Shared<?> newShared) {
		if (newShared instanceof TspShared) {
			TspShared newTspShared = (TspShared) newShared;
			double newUpperBound = newTspShared.get();
			double existingUpperBound = this.get();
			if (newUpperBound != INFINITY
					&& (existingUpperBound == INFINITY || existingUpperBound > newUpperBound)) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see system.Shared#get()
	 */
	@Override
	public Double get() {
		return this.upperBound;
	}

}
