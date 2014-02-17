package cmsc433_p2;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * Customers are simulation actors that have two fields: a name, and a list
 * of Food items that constitute the Customer's order.  When running, an
 * customer attempts to enter the restaurant (only successful if the
 * restaurant has a free table), place its order, and then leave the 
 * restaurant when the order is complete.
 */
public class Customer implements Runnable {
	//JUST ONE SET OF IDEAS ON HOW TO SET THINGS UP...
	private final String name;
	private final List<Food> order;
	private final int orderNum;    
	
	private static Object counterLock = new Object();
	private static int runningCounter = 0;
	private static final int capacityTables = Simulation.capacity;
	private static final Semaphore TablesAvailable = new Semaphore(capacityTables);
	

	/**
	 * You can feel free modify this constructor.  It must take at
	 * least the name and order but may take other parameters if you
	 * would find adding them useful.
	 */
	public Customer(String name, List<Food> order) {
		this.name = name;
		this.order = order;
		synchronized(counterLock){
			this.orderNum = ++runningCounter;
		}
	}

	public String toString() {
		return name;
	}

	/** 
	 * This method defines what an Customer does: The customer attempts to
	 * enter the restaurant (only successful when the restaurant has a
	 * free table), place its order, and then leave the restaurant
	 * when the order is complete.
	 */
	public void run() {
		//YOUR CODE GOES HERE...
		Simulation.logEvent(SimulationEvent.customerStarting(this));
		try {
			//acquire lock with semaphore for number of table amount
			TablesAvailable.acquire();
			//enter coffee shop after beating the lock
			Simulation.logEvent(SimulationEvent.customerEnteredCoffeeShop(this));
			//code to place order with cooks
			synchronized(Simulation.orders){
				while(Simulation.orderNums.size()>=Simulation.cookAvail){
					try{Simulation.orders.wait();}catch(InterruptedException e) {}
				}
				//add order to shared hash
				Simulation.decrementCooks();
				Simulation.orders.put(orderNum, order);
				Simulation.orderNums.add(orderNum);
				Simulation.logEvent(SimulationEvent.customerPlacedOrder(this, order, orderNum));
				Simulation.orders.notifyAll();	
			}
			//code to eat food
			synchronized(Simulation.ordersDone){
				while(!Simulation.ordersDone.containsKey(orderNum)){
					try{Simulation.ordersDone.wait();}catch(InterruptedException e) {}
				}
				//received order
				Simulation.logEvent(SimulationEvent.customerReceivedOrder(this, Simulation.ordersDone.remove(orderNum), orderNum));
				Simulation.ordersDone.get(orderNum);
				Simulation.ordersDone.notifyAll();
			}
			//customer leaves and releases lock on table
			Simulation.logEvent(SimulationEvent.customerLeavingCoffeeShop(this));
			TablesAvailable.release();
			return;
		} catch (InterruptedException e) {System.out.println("Shit aint working on "+this.name);}		
	}
}