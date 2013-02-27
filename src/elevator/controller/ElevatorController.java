package elevator.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import elevator.Elevator;

public class ElevatorController implements Runnable, ActionListener {
	
	private Elevator elevator;
	
	public ElevatorController(Elevator elevator, int numFloors){
		this.elevator = elevator;
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
