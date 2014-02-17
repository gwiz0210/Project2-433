package cmsc433_p2;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;


/**
 * Cooks are simulation actors that have at least one field, a name.
 * When running, a cook attempts to retrieve outstanding orders placed
 * by Eaters and process them.
 */
public class Cook implements Runnable {
	private final String name;

	/**
	 * You can feel free modify this constructor.  It must
	 * take at least the name, but may take other parameters
	 * if you would find adding them useful. 
	 *
	 * @param: the name of the cook
	 */
	public Cook(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	/**
	 * This method executes as follows.  The cook tries to retrieve
	 * orders placed by Customers.  For each order, a List<Food>, the
	 * cook submits each Food item in the List to an appropriate
	 * Machine, by calling makeFood().  Once all machines have
	 * produced the desired Food, the order is complete, and the Customer
	 * is notified.  The cook can then go to process the next order.
	 * If during its execution the cook is interrupted (i.e., some
	 * other thread calls the interrupt() method on it, which could
	 * raise InterruptedException if the cook is blocking), then it
	 * terminates.
	 */
	public void run() {

		Simulation.logEvent(SimulationEvent.cookStarting(this));
		try {
			while(true) {
				int orderNum;
				LinkedList<Food> order;
				LinkedList<Future<Food>> foodOrdered = new LinkedList<Future<Food>>();
				LinkedList<Food> foodDone = new LinkedList<Food>();
				//YOUR CODE GOES HERE...
				synchronized(Simulation.orders){
					while(Simulation.orderNums.size()==0){
						Simulation.orders.wait();
					}
					orderNum = Simulation.orderNums.remove();
					order = (LinkedList<Food>) Simulation.orders.get(orderNum);
					Simulation.orders.notifyAll();
				}
				if(order != null){
					//Cook received order
					Simulation.logEvent(SimulationEvent.cookReceivedOrder(this, order, orderNum));
					for(int i = 0; i < order.size(); i++){
						Food temp = order.get(i);
						Future<Food> done = null;
						Simulation.logEvent(SimulationEvent.cookStartedFood(this, temp, orderNum));
						done = Simulation.machines.get(temp).makeFood();
						foodOrdered.add(done);
					}
					while(foodOrdered.size()>0){
						for(int i = 0;i<foodOrdered.size();i++) {
							if(foodOrdered.get(i).isDone()){
								Food temp = (Food) foodOrdered.get(i).get();
								Simulation.logEvent(SimulationEvent.cookFinishedFood(this, temp, orderNum));
								foodDone.add(temp);
								foodOrdered.remove(i);
								i = 0;
							}
						}
					}
					synchronized(Simulation.orders){
						synchronized(Simulation.ordersDone){
							Simulation.logEvent(SimulationEvent.cookCompletedOrder(this, orderNum));
							Simulation.incrementCooks();
							Simulation.ordersDone.put(orderNum, foodDone);
							Simulation.ordersDone.notifyAll();
						}
						Simulation.orders.notifyAll();
					}
				}
					
			}
		}
		catch(InterruptedException e) {
			// This code assumes the provided code in the Simulation class
			// that interrupts each cook thread when all customers are done.
			// You might need to change this if you change how things are
			// done in the Simulation class.
			Simulation.logEvent(SimulationEvent.cookEnding(this));
		} catch (ExecutionException e) {} 
	}
}