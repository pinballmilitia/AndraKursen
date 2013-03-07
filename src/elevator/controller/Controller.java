package elevator.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

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
	private ElevatorShared[] workerData;
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
		workerData = new ElevatorShared[numElevators];
		worker = new ElevatorController[numElevators];

		for(int i = 0; i < numElevators; i++){
			workerData[i] = new ElevatorShared(numFloors);
			worker[i] = new ElevatorController(MakeAll.getElevator(i+1), workerData[i]);
			worker[i].setPrecision(0.01f);
			MakeAll.addInsideListener((i+1), worker[i]);
			MakeAll.addPositionListener((i+1), worker[i]);
			new Thread(worker[i]).start();
		}
		MakeAll.addFloorListener(this);
	}

	@Override
	/**
	 * Creates Elevator controllers, ElevatorShared object and gives references to elevator objects
	 * to the ElevatorController object.
	 * Registers the ElevatorControllers as PositionListener and InsideListener to their Elevator
	 * and start new thread from the ElevatorController objects.
	 * Then registers itself as a FloorListener.
	 */
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
	/**
	 * Receives floor button presses and calculates which elevator to send to that floor.
	 */
	public void actionPerformed(ActionEvent actionEvent) {

		System.out.println("event: " + actionEvent.getActionCommand());

		//Split command
		String command[] = actionEvent.getActionCommand().split("\\s");

		if(command.length == 3) {

			//If command is from floor button (should not be anything else)
			if(command[0].equals(FLOOR_BUTTON)) {

				//COMMAND: b <floor> <direction>

				int floor, direction = -1;

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

					//If elevator is below floor
					if(workerData[index].getPosition() < floor) {
						workerData[index].setDirection(ElevatorShared.UP);
					}

					else
						if(workerData[index].getPosition() > floor)
							workerData[index].setDirection(ElevatorShared.DOWN);

					//Set the direction caller intend to travel
					workerData[index].setPrioDirection(direction);
					return;
				}

				System.out.println("No elevator on floor " + floor + " found");

				//Try to find an elevator on its way to floor moving in same direction as caller wish to travel
				index = findElevatorGoingToFloorInDirection(floor, direction);
				if(index != -1) {
					System.out.println("Elevator #" + index + " is already on its way to floor " + floor);
					return;
				}
				System.out.println("No elevator going to floor " + floor + " in correct direction found");

				//Try to find an elevator that is moving towards floor in same direction as caller wish to travel
				index = findElevatorPassingFloorInDirection(floor, direction);
				if(index != -1) {
					//ask elevator to stop at caller floor so he can get on
					System.out.println("Elevator #" + index + " is asked to stop on its way");
					workerData[index].setFloorRequestAtIndex(floor, true);
					return;
				}

				System.out.println("No elevator passing floor found");

				//Try to find the closest sleeping (still) elevator
				index = findNearestSleepingElevator(floor);
				if(index != -1) {
					System.out.println("Nearest sleeping elevator is " + index);
					workerData[index].setFloorRequestAtIndex(floor, true);

					//If elevator is below floor
					if(workerData[index].getPosition() < floor) {
						workerData[index].setDirection(ElevatorShared.UP);
					}

					else
						if(workerData[index].getPosition() > floor)
							workerData[index].setDirection(ElevatorShared.DOWN);

					//Set the direction caller intend to travel
					workerData[index].setPrioDirection(direction);

					return;
				}

				System.out.println("No sleeping elevator found");

				//Try to find the closest sleeping (still) elevator
				index = findNearestWorkingElevator(floor);
				if(index != -1) {
					System.out.println("Nearest WORKING elevator is " + index);
					workerData[index].setFloorRequestAtIndex(floor, true);
					return;
				}

				System.out.println("Weird, no elevators in this building?");
			}
		}
	}

	//Returns index of elevator sleeping on floor or -1 if no elevator is available
	private int findSleepingElevatorOnFloor(int floor) {		
		for (int i = 0; i < workerData.length; i++) {
			//UPDATE
			if(workerData[i].getFloor() == floor && workerData[i].getDirection() == ElevatorShared.STILL && workerData[i].getPrioDirection() == ElevatorShared.STILL) {
				System.out.println("elevator " + i + " is on floor " + floor);
				return i;
			}
		}
		return -1;
	}


	//Returns index of elevator going to 'floor' or -1 if no such
	//elevator is found
	private int findElevatorGoingToFloorInDirection(int floor, int direction) {

		for (int i = 0; i < workerData.length; i++) {
			if(workerData[i].getPrioDirection() == direction) {
				if((direction == ElevatorShared.DOWN && workerData[i].getPosition() > floor) || 
						(direction == ElevatorShared.UP && workerData[i].getPosition() < floor)) {
					//UPDATE
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
				if((direction == ElevatorShared.DOWN && workerData[i].getPosition() > floor) || 
						(direction == ElevatorShared.UP && workerData[i].getPosition() < floor)) {					
					return i;
				}
			}
		}

		return -1;
	}

	//Return index of the nearest non-working elevator or -1 if none found
	private int findNearestSleepingElevator(int floor) {

		ArrayList<Integer> sleeping_elevators = new ArrayList<Integer>();

		//Find index of all sleeping/non-moving elevators that are available for work
		for (int i = 0; i < workerData.length; i++) {
			if(workerData[i].getDirection() == ElevatorShared.STILL && workerData[i].getPrioDirection() == ElevatorShared.STILL)
				//This check needs to be done or solution will not work
				if(workerData[i].getFloor() != -1) {
					sleeping_elevators.add(i);
				}
		}

		//If no sleeping elevator found
		if(sleeping_elevators.size() == 0)
			return -1;


		else //If only one elevator found, no need for further checks
			if(sleeping_elevators.size() == 1)
				return sleeping_elevators.get(0);

			//Otherwise we want to find the elevator CLOSEST to the callers floor
			else {
				//Array that will contain distances from floor to elevator
				double distances[] = new double[sleeping_elevators.size()];

				/*
				 * For each elevator, calculate the distance from the elevators
				 * current floor to the floor call is made from.
				 */
				for (int i = 0; i < distances.length; i++) {
					distances[i] = Math.abs(floor - workerData[sleeping_elevators.get(i)].getPosition());
					//System.out.println("distance: " + sleeping_elevators.get(i) + " = " + distances[i]);
				}

				//Find shortest distance == the closest elevator
				int min_index = 0;
				for (int i = 1; i < distances.length; i++) {
					if(distances[min_index] > distances[i]) {
						min_index = i;
					}
				}

				return sleeping_elevators.get(min_index);
			}
	}

	//This is the 'last resort'. We simply return index of a random
	//elevator
	private int findNearestWorkingElevator(int floor) {
		//Select random elevator
		return new Random().nextInt(workerData.length);
	}
}
