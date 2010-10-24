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
public class TspShared implements Shared<Integer>, Serializable {

	private static final long serialVersionUID = 165386141205567783L;
	private Integer upperBound;
	public static final Integer INFINITY = -1;

	public TspShared(int distance) {
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
			int newUpperBound = newTspShared.get();
			int existingUpperBound = this.get();
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
	public Integer get() {
		return this.upperBound;
	}

}
