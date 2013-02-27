package elevator.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import elevator.rmi.Elevator;

public class ElevatorController implements Runnable, ActionListener {
	
	private final Elevator elevator;
	private boolean floorRequest[];
	
	public ElevatorController(Elevator elevator, int numFloors){
		this.elevator = elevator;
		this.floorRequest = new boolean[numFloors];
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
