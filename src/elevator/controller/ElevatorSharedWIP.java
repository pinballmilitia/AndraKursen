package elevator.controller;

public class ElevatorSharedWIP {
	
	static final int UP = 1;
	static final int DOWN = -1;
	static final int STILL = 0;
	
	private int direction;
	private int floor;
	
	ElevatorSharedWIP(){
		direction = 0;
		floor = 0;
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
}
