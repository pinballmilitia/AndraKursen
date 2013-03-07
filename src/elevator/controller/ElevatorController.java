package elevator.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.concurrent.Semaphore;

import elevator.rmi.Elevator;

/**
 * The class ElevatorController controls an elevator via RMI
 * It checks if any internal button has been pressed or if the main Controller
 * has push any button.
 * If a button has been pressed this controller will move the elevator to that floor and
 * open the doors.  
 * Communication with the main Controller goes through a shared ElevatorShared object.
 * 
 */
public class ElevatorController implements Runnable, ActionListener {
	
	private static final double DEFPRECISION = 0.001;

	// Semaphore barrier to tell if the elevator is moving or not.
	private final Semaphore notMoving =  new Semaphore(1);

	private final Elevator elevator;
	private ElevatorShared shared;
	private double precision;

	/**
	 * Constructs an ElevatorController with a specified Elevator and shared object
	 * @param elevator RMI interface to an elevator
	 * @param shared ElevatorShared object shared with the main controller
	 * @throws RemoteException thrown if there is any RMI problem.
	 */
	public ElevatorController(Elevator elevator, ElevatorShared shared) throws RemoteException{
		this.elevator = elevator;
		//this.floorRequest = new boolean[numFloors+1];
		this.shared = shared;
		this.precision = DEFPRECISION;
		shared.setFloor(elevator.getScalePosition());
		shared.setPosition(elevator.whereIs());
	}
	/*
	void setPressed(int floor){
		this.shared.getFloorRequestAtIndex(floor] = true;
		//System.out.println("Move to " + floor);
		//this.notify();
	}
	 */

	@Override
	/**
	 * Receives an ActionEvent with a message containing the current position of the elevator
	 * or a button that has been pressed.
	 * 
	 * If a message with the position is received it will check if the levator is at a floor
	 * and if it should stop at that floor.
	 * 
	 * If a button message is received it will add that floor to the floorRequest shared
	 * variable. 
	 */
	public void actionPerformed(ActionEvent ae) {
		try{
			String[] command = ae.getActionCommand().split(" ");
			//System.out.println(command[0] + " " + command[2]);
			if(command[0].equals("f")){
				float position = Float.parseFloat(command[2]);
				shared.setPosition(position);
				int currentFloor = 0;

				currentFloor = (int)(position+0.5);
				double diff = position - currentFloor; 
				//System.out.println(diff);
				if(diff < precision && diff > -precision){
					elevator.setScalePosition(currentFloor);
					//System.out.println("Stop on floor " + currentFloor + "? " + shared.getFloorRequestAtIndex(currentFloor]);
					if(shared.getFloorRequestAtIndex(currentFloor)){
						shared.setFloor(currentFloor);
						elevator.stop();
						notMoving.release();
					}
				}else{
					shared.setFloor(-1); // moving
				}
				// counter deadlock at extreme positions
				if(position == (shared.getNumberOfFloors()-1) || position == 0){
					shared.setDirection(ElevatorShared.STILL);
					shared.setPrioDirection(ElevatorShared.STILL);
					if(notMoving.availablePermits() == 0)
						notMoving.release();
				}
			}
			// Internal elevator button presses
			else if(command[0].equals("p")){
				int floor = Integer.parseInt(command[2]);
				if(floor == 32000){ // STOP the elevator
					elevator.stop();
					for(int i = 0; i < shared.getNumberOfFloors(); i++){
						shared.setFloorRequestAtIndex(i,false);
					}
					shared.setDirection(ElevatorShared.STILL);
					if(notMoving.availablePermits() == 0)
						notMoving.release();
				}else{ // Add to floor request
					if(floor > shared.getNumberOfFloors())
						throw new IllegalArgumentException();
					shared.setFloorRequestAtIndex(floor, true);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	/**
	 * A loop running forever looks if there is any floorRequest and if there is none the thread will sleep
	 * If there is a request it will figure out in which direction the elevator should move and then tells 
	 * the elevator to move in that direction.
	 * If the elevator is moving the while loop is suspended. 
	 */
	public void run() {
		try {
			while(true){
				// Do not run code while moving elevator
				notMoving.acquire();
				
				// Check if there is the elevator if requested to move
				if(gotWork()){
					// If the elevator is on a floor where it supposed to stop at
					// open the doors
					if(shared.getFloorRequestAtIndex(shared.getFloor())){
						elevator.open();
						shared.setFloorRequestAtIndex(elevator.getScalePosition(),false);
						if(!gotWork()){
							shared.setDirection(ElevatorShared.STILL);
							shared.setPrioDirection(ElevatorShared.STILL);
						}
						Thread.sleep(3000);
						elevator.close();
						notMoving.release();
					}
					// Otherwise find which way the elevator is supposed to move
					else{
						findDirection();
					}
					// Move the elevator if necessary
					if(shared.getDirection() != ElevatorShared.STILL){
						if(shared.getDirection() == ElevatorShared.DOWN){
							elevator.down();
						}else{
							elevator.up();
						}

					}
				}
				// If no request sleep until there is one
				else{
					shared.setDirection(ElevatorShared.STILL);
					notMoving.release();

					// Suspend the thread while no new input
					synchronized(shared){
						shared.wait();
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Looks through the floorRequest snapshot to see it there is a request
	 * to move the elevator
	 * 
	 * @return true if any in the floorRequest is true;
	 */
	private boolean gotWork(){
		boolean work = false;
		boolean requests[] = shared.getFloorRequestSnapshot();
		for(int i = 0; i < requests.length; i++){
			work = work || requests[i];
			if(work)
				break;
		}
		return work;
	}

	/**
	 * 
	 */
	private void findDirection() throws RemoteException{
		if(shared.getDirection() == ElevatorShared.STILL){
			for(int i = 0; i < shared.getNumberOfFloors(); i++){
				if(shared.getFloorRequestAtIndex(i)){
					if(i < elevator.whereIs()){
						shared.setDirection(ElevatorShared.DOWN);
					}else{
						shared.setDirection(ElevatorShared.UP);
					}
					break;
				}
			}
		}else if(shared.getDirection() == ElevatorShared.UP){
			boolean higher = false;
			for(int i = (elevator.getScalePosition()); i < shared.getNumberOfFloors(); i++){
				if(shared.getFloorRequestAtIndex(i)){
					higher = true;
					break;
				}
			}
			if(!higher){
				if(gotWork()){
					shared.setDirection(ElevatorShared.DOWN);
				}else{
					shared.setDirection(ElevatorShared.STILL);
					notMoving.release();
				}
			}
		}else{
			boolean lower = false;
			for(int i = (elevator.getScalePosition()-1); i >= 0; i--){
				if(shared.getFloorRequestAtIndex(i)){
					lower = true;
					break;
				}
			}
			if(!lower){
				if(gotWork()){
					shared.setDirection(ElevatorShared.UP);
				}else{
					shared.setDirection(ElevatorShared.STILL);
					notMoving.release();
				}
			}
		}
	}

	/**
	 * Return the precison of this elevator controller
	 * 
	 * @return
	 */
	double getPrecision() {
		return precision;
	}

	/**
	 * 
	 * @param precision
	 */
	void setPrecision(double precision) {
		this.precision = precision;
	}

}
