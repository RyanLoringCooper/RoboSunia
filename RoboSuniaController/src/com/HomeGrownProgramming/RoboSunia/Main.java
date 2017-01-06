package com.HomeGrownProgramming.RoboSunia;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.io.IOException;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Main extends Thread {
	
	// 4 bytes are sent to the robot
	// 1st byte is the direction byte for motor 1
	// 2nd byte is the direction byte for motor 2
	// 3rd byte is the PWM value for motor 1 (0-255)
	// 4th byte is the PWM value for motor 2 (0-255)
	
	private static final int MAX_PWM = 255;
	private static final int MIN_PWM = 40;
	private int limitPWM = 100;
	private int lmPWM = 0, rmPWM = 0;
	private int PWMInc = 30;
	private JLabel distanceLabel;
	
	private static WebTalker wt;
	private KeyAction ka;
	
	public Main() {
		makeFrame();
		wt = new WebTalker();
	}
	
	private void makeFrame() {
		JFrame frame = new JFrame("RoboSunia Control Window");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setSize(600,400);
		frame.setLocation(10,30);
		JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, MIN_PWM, MAX_PWM, limitPWM);
		speedSlider.setMajorTickSpacing(10);
		speedSlider.setMinorTickSpacing(5);
		speedSlider.setPaintTicks(true);
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put( new Integer( MIN_PWM ), new JLabel("Slow") );
		labelTable.put( new Integer( 100 ), new JLabel("Good") );
		labelTable.put( new Integer( 2*MAX_PWM/3 ), new JLabel("Fast") );
		labelTable.put( new Integer( MAX_PWM ), new JLabel("Dangerous") );
		speedSlider.setLabelTable( labelTable );
		speedSlider.setPaintLabels(true);
		Font font = new Font("Serif", Font.PLAIN, 15);
		speedSlider.setFont(font);
		speedSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
		        if (!source.getValueIsAdjusting()) {
		            limitPWM = (int)source.getValue();
		        }   
			}
		});
		JLabel sliderLabel = new JLabel("Speed Limit", JLabel.CENTER);
		sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		speedSlider.setFocusable(false);
		distanceLabel = new JLabel("Waiting for first reading", JLabel.CENTER);
		ka = new KeyAction();
		frame.addKeyListener(ka);
		frame.add(sliderLabel);
		frame.add(speedSlider);
		frame.add(distanceLabel);
		//frame.add(kaContainer);
		//frame.pack();
		frame.setVisible(true);
	}
	
	public static void terminate() throws IOException {
		// send terminating signal
		wt.send("rese");
		wt.close();
		System.exit(0);
	}
	
	// This will turn the boolean values about the motor states stored in ka into a string.
	private char[] getMotorStates() {
		if(ka.forwardPressed) {
			if(ka.leftPressed) {
				ka.lmState = 0;
				ka.rmState = 1;
			} else if(ka.rightPressed) {
				ka.lmState = 1;
				ka.rmState = 0;
			} else {
				ka.lmState = 1;
				ka.rmState = 1;
			}
		} else if(ka.backwardPressed) {
			if(ka.leftPressed) {
				ka.lmState = -1;
				ka.rmState = 0;
			} else if(ka.rightPressed) {
				ka.lmState = 0;
				ka.rmState = -1;
			} else {
				ka.lmState = -1;
				ka.rmState = -1;
			}
		} else if(ka.leftPressed) {
			ka.lmState = -1;
			ka.rmState = 1;
		} else if(ka.rightPressed) {
			ka.lmState = 1;
			ka.rmState = -1;
		}
		char[] b = new char[4];
		b[0] = (char) (ka.lmState == -1 ? 1 : 0);
		b[1] = (char) (ka.rmState == -1 ? 1 : 0);
		if(ka.lmState != 0) {
			lmPWM = lmPWM+PWMInc > limitPWM ? limitPWM : lmPWM+PWMInc;
		} else {
			lmPWM = lmPWM-PWMInc < 0 ? 0 : lmPWM-PWMInc;
		}
		if(ka.rmState != 0) {
			rmPWM = rmPWM+PWMInc >= limitPWM ? limitPWM : rmPWM+PWMInc;
		} else {
			rmPWM = rmPWM-PWMInc < 0 ? 0 : rmPWM-PWMInc;
		}
		b[2] = (char) lmPWM;
		b[3] = (char) rmPWM;
		return b;
	}
	
	@Override
	public void run() {
		for(;;) {
			try {
				wt.send(getMotorStates());
				distanceLabel.setText(wt.read());
				Thread.sleep(100);
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		new Main().start();
	}
}
