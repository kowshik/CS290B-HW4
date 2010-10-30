package system;

import java.io.Serializable;
import java.util.List;

import api.Result;
import api.Task;

/**
 * Implementation of the {@link api.Result Result} interface
 * 
 * @author Manasa Chandrasekhar
 * @author Kowshik Prakasam
 */
public class ResultImpl<T> implements Result<T>, Serializable {

	private static final long serialVersionUID = -7688137730920618986L;
	private T result;
	private List<Task<T>> subTasks;

	/**
	 * Default constructor
	 */
	
	public ResultImpl() {
		this.result = null;
		this.subTasks = null;
	}

	/**
	 * @return Returns the value computed by the task
	 * 
	 * 
	 */
	@Override
	public T getValue() {
		return this.result;
	}

	/**
	 * @return Returns sub tasks generated by the underlying computation using
	 *         divide and conquer
	 */
	@Override
	public List<Task<T>> getSubTasks() {
		return this.subTasks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see api.Result#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(T value) {
		this.result = value;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see api.Result#setSubTasks(java.util.List)
	 */
	@Override
	public void setSubTasks(List<Task<T>> subTasks) {
		this.subTasks = subTasks;
	}

}
