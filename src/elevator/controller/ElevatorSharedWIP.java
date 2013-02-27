package elevator.controller;

public class ElevatorSharedWIP {
	
	public static int UP = 1;
	public static int DOWN = -1;
	public static int STILL = 0;
	
	private boolean floorPress[];
	private int direction;
	
	public ElevatorSharedWIP(int floors){
		floorPress = new boolean[floors];
		direction = 0;
	}

}
