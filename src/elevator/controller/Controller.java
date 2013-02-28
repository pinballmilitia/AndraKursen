package elevator.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import elevator.rmi.*;

public class Controller implements Runnable, ActionListener{

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
		try{
			init();

		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}

	}

	/**
	 * Floor button actionlistener
	 * 	 
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		//System.out.println(e.getActionCommand());

		//Split command
		String command[] = e.getActionCommand().split("\\s");

		//If command is from floor button
		if(command[0].equals("b")) {

			//COMMAND: b <floor> <direction>

			//Caller floor level
			int floor = Integer.parseInt(command[1]);

			//Wanted direction
			int direction = Integer.parseInt(command[2]); 

			System.out.println("Elevator XXXXX " + floor);
			/**1**/
			//If there is a sleeping elevator already on <floor> -> open doors
			int index = findSleepingElevatorOnFloor(floor);
			if(index != -1) {
				System.out.println("Elevator #" + (index + 1) + " is already on floor " + floor);
				workerData[index].setFloorRequestAtIndex(floor, true);
				return;
			}

			/**2**/
			//if there is an elevator going to <floor> AND MOVING IN CORRECT DIRECTION - wait for that elevator
			//Find index of elevator going to floor
			index = findElevatorGoingToFloorInDirection(floor, direction);
			if(index != -1) {
				//Elevator FOUND!
				System.out.println("Elevator #" + index + " is already on its way to floor " + floor);
				//elevator will move to floor without our help
				return;
			}

			/**3**/
			//If there is an elevator that will pass <floor> and move in <direction>, ask it to stop at <floor>
			//switch(direction)
			index = findElevatorPassingFloorInDirection(floor, direction);
			if(index != -1) {
				//ask elevator to stop at caller floor so he can get on
				System.out.println("Elevator #" + index + " is asked to go all the way to on its way to floor " + floor);
				workerData[index].setFloorRequestAtIndex(floor, true);
				return;
			}

			/**4**/ 
			//calculate closest <sleeping> elevator and call for it
			index = findNearestSleepingElevator(floor);
			if(index != -1) {
				workerData[index].setFloorRequestAtIndex(floor, true);
				return;
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

		switch (direction) {
		case ElevatorSharedWIP.DOWN:
			for (int i = 0; i < workerData.length; i++) {
				if(workerData[i].getDirection() == ElevatorSharedWIP.DOWN) {
					if(workerData[i].getPosition() > floor) {
						if(workerData[i].getFloorRequestAtIndex(floor)) {
							return i;
						}
					}
				}
			}
			break;

		case ElevatorSharedWIP.UP:
			for (int i = 0; i < workerData.length; i++) {
				if(workerData[i].getDirection() == ElevatorSharedWIP.UP) {
					if(workerData[i].getPosition() < floor) {
						if(workerData[i].getFloorRequestAtIndex(floor)) {
							return i;
						}
					}
				}
			}
			break;

		default:
			break;
		}


		//Check if there is an elevator going to 'floor' or if there is one already there

		//if elevator already at floor and still, we are done

		//FOR ALL ELEVATORS§
		//if direction == up
		//check if elevator direction is up
		//check if elevator is below floor 
		//check if there is floor request for 'floor'
		//if yes to all above -> setPressed()

		return -1;
	}

	//Returns index of elevator that will pass floor and move in same direction
	//as callers wants to move in
	private int findElevatorPassingFloorInDirection(int floor, int direction) {

		switch (direction) {
		case ElevatorSharedWIP.DOWN:
			for (int i = 0; i < workerData.length; i++) {
				if(workerData[i].getDirection() == ElevatorSharedWIP.DOWN) {
					if(workerData[i].getPosition() > floor) {
						return i;
					}
				}
			}
			break;

		case ElevatorSharedWIP.UP:
			for (int i = 0; i < workerData.length; i++) {
				if(workerData[i].getDirection() == ElevatorSharedWIP.UP) {
					if(workerData[i].getPosition() < floor) {
						return i;
					}
				}
			}
			break;

		default:
			break;
		}

		return -1;
	}

	//Return index of the nearest elevator without work or -1 if none found
	private int findNearestSleepingElevator(int floor) {
		
		int distances[] = new int[workerData.length];
		
		for (int i = 0; i < workerData.length; i++) {
			distances[i] = Math.abs(floor - workerData[i].getFloor());
		}
		
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
