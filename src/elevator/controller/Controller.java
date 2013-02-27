package elevator.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import elevator.rmi.*;

public class Controller implements Runnable, ActionListener{
	
	private ElevatorController[] worker;
	private ElevatorSharedWIP[] workerData;
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Thread(new Controller()).start();
	}
	
	private void init() throws Exception{
		MakeAll.init();
		int numFloors = MakeAll.getNumberOfFloors();
		int numElevators = MakeAll.getNumberOfElevators();
		workerData = new ElevatorSharedWIP[numElevators];
		worker = new ElevatorController[numElevators];
		for(int i = 0; i < numElevators; i++){
			workerData[i] = new ElevatorSharedWIP();
			worker[i] = new ElevatorController(MakeAll.getElevator(i+1), numFloors);
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
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println(e.getActionCommand());
	}

}
