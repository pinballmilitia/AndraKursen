package elevator.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.concurrent.Semaphore;

import elevator.rmi.Elevator;

public class ElevatorController implements Runnable, ActionListener {
	
	private final Semaphore notMoving =  new Semaphore(1);
	
	private final Elevator elevator;
	private boolean floorRequest[];
	private ElevatorSharedWIP shared;
	private double precision;
	
	public ElevatorController(Elevator elevator, ElevatorSharedWIP shared, int numFloors) throws RemoteException{
		this.elevator = elevator;
		this.floorRequest = new boolean[numFloors+1];
		this.shared = shared;
		this.precision = 0.1;
		shared.setFloor(elevator.getScalePosition());
		shared.setPosition(elevator.whereIs());
	}
	
	void setPressed(int floor){
		this.floorRequest[floor] = true;
		System.out.println("Move to " + floor);
		//this.notify();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		try{
		String[] command = ae.getActionCommand().split(" ");
		System.out.println(command[0] + " " + command[2]);
		if(command[0].equals("f")){
			float position = Float.parseFloat(command[2]);
			shared.setPosition(position);
			int currentFloor = shared.getFloor();
			
			currentFloor = (int)(position+0.5);
			double diff = position - currentFloor; 
			System.out.println(diff);
			if(diff < precision && diff > -precision){
				elevator.setScalePosition(currentFloor);
				shared.setFloor(currentFloor);
				System.out.println("Stop on floor " + currentFloor + "? " + floorRequest[currentFloor]);
				if(floorRequest[currentFloor]){
					elevator.stop();
					notMoving.release();
				}
			}
			if(position == 5 || position == 0){
				shared.setDirection(ElevatorSharedWIP.STILL);
				if(notMoving.availablePermits() == 0)
					notMoving.release();
			}
		}
		else if(command[0].equals("p")){
			int floor = Integer.parseInt(command[2]);
			if(floor == 32000){
				elevator.stop();
				for(int i = 0; i < floorRequest.length; i++){
					floorRequest[i] = false;
				}
				if(notMoving.availablePermits() == 0)
					notMoving.release();
			}else{
				if(floor > floorRequest.length)
					throw new IllegalArgumentException();
				floorRequest[floor] = true;
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
					if(floorRequest[shared.getFloor()]){
						elevator.open();
						floorRequest[shared.getFloor()] = false;
						if(!gotWork()){
							shared.setDirection(ElevatorSharedWIP.STILL);
						}
						Thread.sleep(3000);
						elevator.close();
						notMoving.release();
					}else{
						if(shared.getDirection() == ElevatorSharedWIP.STILL){
							for(int i = 0; i < floorRequest.length; i++){
								if(floorRequest[i]){
									if(i < shared.getFloor()){
										shared.setDirection(ElevatorSharedWIP.DOWN);
									}else{
										shared.setDirection(ElevatorSharedWIP.UP);
									}
									break;
								}
							}
						}else if(shared.getDirection() == ElevatorSharedWIP.UP){
							boolean higher = false;
							for(int i = (shared.getFloor()); i < floorRequest.length; i++){
								if(floorRequest[i]){
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
							for(int i = (shared.getFloor()-1); i >= 0; i--){
								if(floorRequest[i]){
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
		for(int i = 0; i <= floorRequest.length-1; i++){
			work = work || floorRequest[i];
			if(work)
				break;
		}
		//System.out.println(work);
		return work;
	}

	double getPrecision() {
		return precision;
	}

	void setPrecision(double precision) {
		this.precision = precision;
	}

}
