package elevator.controller;

public class ElevatorSharedWIP {
	
	static final int UP = 1;
	static final int DOWN = -1;
	static final int STILL = 0;
	
	private int direction;
	private int floor;
	private double position;
	private boolean floorRequest[];
	private int topFloor;
	
	ElevatorSharedWIP(){
		direction = STILL;
		floor = 0;
		position = 0;
	}
	
	ElevatorSharedWIP(int numFloors){
		direction = STILL;
		floor = 0;
		position = 0;
		floorRequest = new boolean[numFloors];
	}
	
	void setDirection(int direction){
		this.direction = direction;
	}
	
	int getDirection(){
		return this.direction;
	}

	void setFloor(int floor) {
		this.floor = floor;
	}
	
	int getFloor() {
		return floor;
	}

	double getPosition() {
		return position;
	}

	void setPosition(double position) {
		this.position = position;
	}
	
	boolean getFloorRequestAtIndex(int index){
		return floorRequest[index];
	}
	
	void setFloorRequestAtIndex(int index, boolean bool){
		this.floorRequest[index] = bool;
	}
	
	boolean[] getFloorRequestSnapshot(){
		boolean copy[] = floorRequest;
		return copy;
	}
	
	int getNumberOfFloors(){
		return floorRequest.length;
	}
}
