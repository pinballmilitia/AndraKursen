package elevator.controller;

public class ElevatorSharedWIP {
	
	static final int UP = 1;
	static final int DOWN = -1;
	static final int STILL = 0;
	
	private int direction;
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
	ElevatorSharedWIP(int numFloors){
		direction = STILL;
		floor = 0;
		position = 0;
		floorRequest = new boolean[numFloors];
	}
	
	synchronized void setDirection(int direction){
		this.direction = direction;
	}
	
	synchronized int getDirection(){
		return this.direction;
	}

	synchronized void setFloor(int floor) {
		this.floor = floor;
	}
	
	synchronized int getFloor() {
		return floor;
	}

	synchronized double getPosition() {
		return position;
	}

	synchronized void setPosition(double position) {
		this.position = position;
	}
	
	synchronized boolean getFloorRequestAtIndex(int index){
		if(index > -1)
			return floorRequest[index];
		else
			return false;
	}
	
	synchronized void setFloorRequestAtIndex(int index, boolean bool){
		this.floorRequest[index] = bool;
		this.notify(); // wake up threads waiting for new input
	}
	
	synchronized boolean[] getFloorRequestSnapshot(){
		boolean copy[] = new boolean[floorRequest.length];
		for(int i = 0; i < floorRequest.length; i++){
			copy[i] = floorRequest[i];
		}
		return copy;
	}
	
	int getNumberOfFloors(){
		return floorRequest.length;
	}
}
