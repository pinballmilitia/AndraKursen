package elevator.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import elevator.rmi.*;

/**
 * 
 * This is the main controller. It runs as a thread and listens
 * for button presses from floor panels. 
 * On creation it constructs elevatorHandlers, one for each 
 * elevator. Also created is shared objects for communication between
 * main controller and elevatorHandler communication.
 *
 */
public class Controller implements Runnable, ActionListener{

	private final String FLOOR_BUTTON = "b";
	private ElevatorController[] worker;
	private ElevatorSharedWIP[] workerData;
	private int numFloors;
	private int numElevators;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Thread(new Controller()).start();
	}

	private void init() throws Exception{
		MakeAll.init();
		numFloors = MakeAll.getNumberOfFloors();
		numElevators = MakeAll.getNumberOfElevators();
		workerData = new ElevatorSharedWIP[numElevators];
		worker = new ElevatorController[numElevators];

		for(int i = 0; i < numElevators; i++){
			workerData[i] = new ElevatorSharedWIP(numFloors);
			worker[i] = new ElevatorController(MakeAll.getElevator(i+1), workerData[i]);
			worker[i].setPrecision(0.01f);
			MakeAll.addInsideListener((i+1), worker[i]);
			MakeAll.addPositionListener((i+1), worker[i]);
			new Thread(worker[i]).start();
		}
		MakeAll.addFloorListener(this);
	}

	@Override
	public void run() {
		try {
			init();
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {

		//Split command
		String command[] = actionEvent.getActionCommand().split("\\s");

		if(command.length == 3) {
			
			//If command is from floor button (should not be anything else)
			if(command[0].equals(FLOOR_BUTTON)) {

				//COMMAND: b <floor> <direction>

				int floor = -1;
				int direction = -1;
				try {
					floor = Integer.parseInt(command[1]);
					direction = Integer.parseInt(command[2]); 

				} catch (NumberFormatException e) {
					System.err.println("Main controller: ERROR PARSING COMMAND.\nIgnoring command\n");
					return;
				}

				//Try to find a sleeping elevator already on floor
				int index = findSleepingElevatorOnFloor(floor);
				if(index != -1) {
					System.out.println("Elevator #" + (index + 1) + " is already on floor " + floor);
					workerData[index].setFloorRequestAtIndex(floor, true);
					return;
				}

				//Try to find an elevator on its way to floor moving in same direction as caller wish to travel
				index = findElevatorGoingToFloorInDirection(floor, direction);
				if(index != -1) {
					System.out.println("Elevator #" + index + " is already on its way to floor " + floor);
					return;
				}

				//Try to find an elevator that is moving towards floor in same direction as caller wish to travel
				index = findElevatorPassingFloorInDirection(floor, direction);
				if(index != -1) {
					//ask elevator to stop at caller floor so he can get on
					System.out.println("Elevator #" + index + " is asked to stop on its way");
					workerData[index].setFloorRequestAtIndex(floor, true);
					return;
				}

				//Try to find the closest sleeping (still) elevator
				index = findNearestSleepingElevator(floor);
				if(index != -1) {
					workerData[index].setFloorRequestAtIndex(floor, true);
					return;
				}
			}
		}
	}

	//Returns index of elevator sleeping on floor or -1 if no elevator is available
	private int findSleepingElevatorOnFloor(int floor) {		
		for (int i = 0; i < workerData.length; i++) {
			if(workerData[i].getFloor() == floor) {
				return i;
			}
		}
		return -1;
	}


	//Returns index of elevator going to 'floor' or -1 if no such
	//elevator is found
	private int findElevatorGoingToFloorInDirection(int floor, int direction) {

		for (int i = 0; i < workerData.length; i++) {
			if(workerData[i].getDirection() == direction) {
				if((direction == ElevatorSharedWIP.DOWN && workerData[i].getPosition() > floor) || 
						(direction == ElevatorSharedWIP.UP && workerData[i].getPosition() < floor)) {
					if(workerData[i].getFloorRequestAtIndex(floor)) {
						return i;
					}
				}
			}
		}

		return -1;
	}

	//Returns index of elevator that will pass floor and move in same direction
	//as callers wants to move in
	private int findElevatorPassingFloorInDirection(int floor, int direction) {

		for (int i = 0; i < workerData.length; i++) {
			if(workerData[i].getDirection() == direction) {
				if((direction == ElevatorSharedWIP.DOWN && workerData[i].getPosition() > floor) || 
						(direction == ElevatorSharedWIP.UP && workerData[i].getPosition() < floor)) {					
					return i;
				}
			}
		}

		return -1;
	}

	//Return index of the nearest elevator without work or -1 if none found
	private int findNearestSleepingElevator(int floor) {

		//Array that will contain distances from floor to elevator
		double distances[] = new double[workerData.length];

		/*
		 * For each elevator, calculate the distance from the elevators
		 * current floor to the floor call is made from.
		 */
		for (int i = 0; i < workerData.length; i++) {
			distances[i] = Math.abs(floor - workerData[i].getPosition());
		}

		//Find shortest distance == the closest elevator
		int min_index = 0;
		for (int i = 1; i < distances.length; i++) {
			if(distances[min_index] > distances[i])
				min_index = i;
		}

		//this check should be made MUCH earlier
		if(workerData[min_index].getDirection() == ElevatorSharedWIP.STILL)
			return min_index;

		//default : none found
		return -1;
	}
}
