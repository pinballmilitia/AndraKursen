package elevator.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import elevator.rmi.*;

public class Controller implements Runnable, ActionListener{
	
	private int topFloor, numElevators;
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	@Override
	public void run() {
		try{
			MakeAll.init();
			MakeAll.addFloorListener(this);
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
