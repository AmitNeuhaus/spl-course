package bgu.spl.mics;

import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * 
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {
	
	/**
	 * This should be the the only public constructor in this class.
	 */

	private boolean resolved;
	private T result;
	private long timeCounter;

	public Future() {
		result = null;
		resolved = false;
		timeCounter = System.nanoTime();
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     * @return return the result of type T if it is available, if not wait until it is available.
     */

	public synchronized T get() throws InterruptedException {
		while (!resolved){

				wait();

		}
		return result;
	}
	
	/**
     * Resolves the result of this Future object.
	 * @pre isDone == false;
	 * @post isDone == True;
	 * @post future.get() != null
     */
	public synchronized void resolve (T result) {
		resolved = true;
		this.result = result;
		notifyAll();
	}
	
	/**
     * @return true if this object has been resolved, false otherwise
	 * @return resolved;
     */
	public boolean isDone() {
		return resolved;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     * @param timeout 	the maximal amount of time units to wait for the result.
     * @param unit		the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not, 
     * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
     *         elapsed, return null.
	 *
	 * @post if (resolvedTime) - (startTime) <= timeout {@return} T.
	 * 		 else return null;
     */
	public T get(long timeout, TimeUnit unit) {
		long convertedTimeout = TimeUnit.MILLISECONDS.convert(timeout,unit);
		try{
			Thread.sleep(convertedTimeout);
		}catch(InterruptedException ignored){
			if (isDone()){
				return result;
			}
		}
		return result;
	}



}
