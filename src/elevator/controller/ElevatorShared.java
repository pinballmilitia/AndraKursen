package elevator.controller;

import java.util.Arrays;

/**
 * The class ElevatorShared is a shared object used for communication
 * between the main Controller and a ElevatorController.
 * Synchronized is used for synchronization.
 *
 */
public class ElevatorShared {
	
	static final int UP = 1;
	static final int DOWN = -1;
	static final int STILL = 0;
	
	private int direction, prioDirection;
	private int floor;
	private double position;
	private boolean floorRequest[];
	
	/*
	ElevatorSharedWIP(){
		direction = STILL;
		floor = 0;
		position = 0;
	}
	*/
	/**
	 * Constructor for an ElevatorShared object with numFloors floors
	 * 
	 * @param numFloors Number of floors of the elevator
	 */
	ElevatorShared(int numFloors){
		direction = STILL;
		prioDirection = STILL;
		floor = 0;
		position = 0;
		floorRequest = new boolean[numFloors];
	}
	
	/**
	 * Sets the direction of the elevator
	 * @param direction Direction of the elevator
	 */
	synchronized void setDirection(int direction){
		this.direction = direction;
	}
	
	/**
	 * Return the direction of the elevator
	 * @return direction of the elevator
	 */
	synchronized int getDirection(){
		return this.direction;
	}

	/**
	 * @return the prioDirection
	 */
	synchronized int getPrioDirection() {
		return prioDirection;
	}

	/**
	 * @param prioDirection the prioDirection to set
	 */
	synchronized void setPrioDirection(int prioDirection) {
		this.prioDirection = prioDirection;
	}

	/**
	 * Sets the current floor of the elevator
	 * @param floor current floor
	 */
	synchronized void setFloor(int floor) {
		this.floor = floor;
	}
	
	/**
	 * Return the current floor of the elevator
	 * @return current floor
	 */
	synchronized int getFloor() {
		return floor;
	}

	/**
	 * Return the current position of the elevator
	 * @return current position
	 */
	synchronized double getPosition() {
		return position;
	}

	/**
	 * Sets the current position of the elevator
	 * @param position position of the elevator
	 */
	synchronized void setPosition(double position) {
		this.position = position;
	}
	
	/**
	 * Return if the the button at floor index if pressed
	 * @param index floor to check
	 * @return true if button is pressed
	 */
	synchronized boolean getFloorRequestAtIndex(int index){
		if(index > -1)
			return floorRequest[index];
		else
			return false;
	}
	
	/**
	 * Sets the button of the floor index to pressed or unpressed
	 * @param index floor
	 * @param bool is the button pressed
	 */
	synchronized void setFloorRequestAtIndex(int index, boolean bool){
		this.floorRequest[index] = bool;
		this.notify(); // wake up threads waiting for new input
	}
	
	/**
	 * Returns a copy of the floorRequest array
	 * @return Array with button states
	 */
	synchronized boolean[] getFloorRequestSnapshot(){
		boolean copy[] = new boolean[floorRequest.length];
		for(int i = 0; i < floorRequest.length; i++){
			copy[i] = floorRequest[i];
		}
		return copy;
	}
	
	/**
	 * Returns the number of floor of this elevator
	 * @return number of floors
	 */
	int getNumberOfFloors(){
		return floorRequest.length;
	}
	
	@Override
	public String toString() {
		//StringBuffer sb = new StringBuffer();
		
		return "Floor: " + floor + " Position: " + position + "\n" + 
				"Direction: " + direction + " PrioDirection: " + prioDirection + "\n" +
				"FloorRequests: " + Arrays.toString(floorRequest) + "\n";
	}
}
