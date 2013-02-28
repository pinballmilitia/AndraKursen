package elevator.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.concurrent.Semaphore;

import elevator.rmi.Elevator;

public class ElevatorController implements Runnable, ActionListener {
	
	private final Semaphore notMoving =  new Semaphore(1);
	
	private final Elevator elevator;
	//private boolean shared.getFloorRequestAtIndex(];
	private ElevatorSharedWIP shared;
	private double precision;
	
	public ElevatorController(Elevator elevator, ElevatorSharedWIP shared) throws RemoteException{
		this.elevator = elevator;
		//this.floorRequest = new boolean[numFloors+1];
		this.shared = shared;
		this.precision = 0.1;
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
	public void actionPerformed(ActionEvent ae) {
		try{
		String[] command = ae.getActionCommand().split(" ");
		//System.out.println(command[0] + " " + command[2]);
		if(command[0].equals("f")){
			float position = Float.parseFloat(command[2]);
			shared.setPosition(position);
			int currentFloor = elevator.getScalePosition();
			
			currentFloor = (int)(position+0.5);
			double diff = position - currentFloor; 
			//System.out.println(diff);
			if(diff < precision && diff > -precision){
				elevator.setScalePosition(currentFloor);
				shared.setFloor(currentFloor);
				//System.out.println("Stop on floor " + currentFloor + "? " + shared.getFloorRequestAtIndex(currentFloor]);
				if(shared.getFloorRequestAtIndex(currentFloor)){
					elevator.stop();
					notMoving.release();
				}
			}
			// counter deadlock at extreme positions
			if(position == (shared.getNumberOfFloors()-1) || position == 0){
				shared.setDirection(ElevatorSharedWIP.STILL);
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
	public void run() {
		try {
			while(true){
				notMoving.acquire();
				if(gotWork()){
					if(shared.getFloorRequestAtIndex(elevator.getScalePosition())){
						elevator.open();
						shared.setFloorRequestAtIndex(elevator.getScalePosition(),false);
						if(!gotWork()){
							shared.setDirection(ElevatorSharedWIP.STILL);
						}
						Thread.sleep(3000);
						elevator.close();
						notMoving.release();
					}else{
						if(shared.getDirection() == ElevatorSharedWIP.STILL){
							for(int i = 0; i < shared.getNumberOfFloors(); i++){
								if(shared.getFloorRequestAtIndex(i)){
									if(i < elevator.getScalePosition()){
										shared.setDirection(ElevatorSharedWIP.DOWN);
									}else{
										shared.setDirection(ElevatorSharedWIP.UP);
									}
									break;
								}
							}
						}else if(shared.getDirection() == ElevatorSharedWIP.UP){
							boolean higher = false;
							for(int i = (elevator.getScalePosition()); i < shared.getNumberOfFloors(); i++){
								if(shared.getFloorRequestAtIndex(i)){
									higher = true;
									break;
								}
							}
							if(!higher){
								if(gotWork()){
									shared.setDirection(ElevatorSharedWIP.DOWN);
								}else{
									shared.setDirection(ElevatorSharedWIP.STILL);
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
									shared.setDirection(ElevatorSharedWIP.UP);
								}else{
									shared.setDirection(ElevatorSharedWIP.STILL);
									notMoving.release();
								}
							}
						}
					}
					if(shared.getDirection() != ElevatorSharedWIP.STILL){
						if(shared.getDirection() == ElevatorSharedWIP.DOWN){
							elevator.down();
						}else{
							elevator.up();
						}
						
					}
				}else{
					shared.setDirection(ElevatorSharedWIP.STILL);
					notMoving.release();
					//this.wait();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean gotWork(){
		boolean work = false;
		for(int i = 0; i <= shared.getNumberOfFloors()-1; i++){
			work = work || shared.getFloorRequestAtIndex(i);
			if(work)
				break;
		}
		return work;
	}

	double getPrecision() {
		return precision;
	}

	void setPrecision(double precision) {
		this.precision = precision;
	}

}
