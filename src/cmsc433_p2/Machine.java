package cmsc433_p2;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;


/**
 * A Machine is used to make a particular Food.  Each Machine makes
 * just one kind of Food.  Each machine has a capacity: it can make
 * that many food items in parallel; if the machine is asked to
 * produce a food item beyond its capacity, the requester blocks.
 * Each food item takes at least item.cookTimeMS milliseconds to
 * produce.
 */
public class Machine {
	public final String machineName;
	public final Food machineFoodType;
	public final int capacity;
	public final Machine machine = this;
	public static LinkedList<OrderPair> foodQueue = new LinkedList<OrderPair>();
	private static final Semaphore cooking = new Semaphore(Simulation.machineLoad);
	//YOUR CODE GOES HERE...


	/**
	 * The constructor takes at least the name of the machine,
	 * the Food item it makes, and its capacity.  You may extend
	 * it with other arguments, if you wish.  Notice that the
	 * constructor currently does nothing with the capacity; you
	 * must add code to make use of this field (and do whatever
	 * initialization etc. you need).
	 */
	public Machine(String nameIn, Food foodIn, int capacityIn) {
		this.machineName = nameIn;
		this.machineFoodType = foodIn;
		this.capacity = capacityIn;
		//YOUR CODE GOES HERE...
		Simulation.logEvent(SimulationEvent.machineStarting(this, machineFoodType, capacity));
	}
	

	

	/**
	 * This method is called by a Cook in order to make the Machine's
	 * food item.  You can extend this method however you like, e.g.,
	 * you can have it take extra parameters or return something other
	 * than void.  It should block if the machine is currently at full
	 * capacity.  If not, the method should return, so the Cook making
	 * the call can proceed.  You will need to implement some means to
	 * notify the calling Cook when the food item is finished.
	 */
	public Future<Food> makeFood() throws InterruptedException {
		//YOUR CODE GOES HERE...
		FutureTask<Food> maker = new FutureTask<Food>(new CookAnItem(), machineFoodType);
		Thread operator = new Thread(maker);
		operator.start();
		return maker;
	}

	//THIS MIGHT BE A USEFUL METHOD TO HAVE AND USE BUT IS JUST ONE IDEA
	private class CookAnItem implements Runnable {
		public void run() {
			try {
				cooking.acquire();
				//YOUR CODE GOES HERE...
				Simulation.logEvent(SimulationEvent.machineCookingFood(machine, machineFoodType));
				Thread.sleep(machineFoodType.cookTimeMS);
				cooking.release();
			} catch(InterruptedException e) { }
			Simulation.logEvent(SimulationEvent.machineDoneFood(machine, machineFoodType));
			return;
		}
	}
 

	public String toString() {
		return machineName;
	}
}