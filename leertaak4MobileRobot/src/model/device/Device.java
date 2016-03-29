package model.device;

/**
 * Title    :   The Mobile Robot Explorer Simulation Environment v2.0
 * Copyright:   GNU General Public License as published by the Free Software Foundation
 * Company  :   Hanze University of Applied Sciences
 *
 * @author Dustin Meijer        (2012)
 * @author Alexander Jeurissen  (2012)
 * @author Davide Brugali       (2002)
 * @version 2.0
 */

import model.environment.Environment;
import model.environment.Position;
import model.robot.MobileRobot;

import java.awt.Polygon;
import java.awt.Color;

import java.io.PrintWriter;

import java.util.ArrayList;
import java.awt.geom.Point2D;
import java.awt.Graphics;
import java.awt.Graphics2D;

public abstract class Device implements Runnable {



	// A final object to make sure the lock cannot be overwritten with another Object
	private final Object lock = new Object();
  	private final String name;                    // the name of this device
	private final Polygon shape;                  // the device's shape in local coords

    // a reference to the environment
    protected final Environment environment;
   	// a reference to the robot
	protected final MobileRobot robot;
	// origin of the device reference frame with regards to the robot frame
	protected final Position localPosition;
    // the robot current position
    protected Position robotPosition;
    // the arrayList with all the commands
    protected final ArrayList<String> commands;
    // the colors of the devices
	protected Color backgroundColor = Color.red;
	protected Color foregroundColor = Color.blue;

    // Is the device running?
	protected boolean running;
    // Is the device executingCommand a command?
	protected boolean executingCommand;

	private PrintWriter output;

	protected long delay = 5;

	// the constructor
	protected Device(String name, MobileRobot robot, Position local, Environment environment) {
		this.name = name;
		this.robot = robot;
		this.localPosition = local;
		this.environment = environment;


		this.shape = new Polygon();
		this.robotPosition = new Position();

		this.running = true;
		this.executingCommand = false;

		this.commands = new ArrayList<String>();
		this.output = null;
		robot.readPosition(this.robotPosition);

	}

	// this method is invoked when the geometric shape of the device is defined
	protected void addPoint(int x, int y) {
		shape.addPoint(x, y);
	}


	public boolean sendCommand(String command) {
		commands.add(command);
		synchronized (lock) {
			// Notify the tread that is waiting for commands.
			lock.notify();
		}
		return true;

	}

	protected synchronized void writeOut(String data) {
		if (output != null) {
			output.println(data);
		} else {
			System.out.println(this.name + " output not initialized");
		}
	}


	public void setOutput(PrintWriter output) {
		this.output = output;
	}

	public void run() {
		System.out.println("Device " + this.name + " running");

		do {
			try {
				if (executingCommand) {
					// pause before the next step
					synchronized (this) {
						Thread.sleep(MobileRobot.delay);
					}
				} else if (commands.size() > 0) {
					// extracts the the next command and executes it
					String command = commands.remove(0);
					executeCommand(command);
				} else {
					// waits for a new command
					synchronized (lock) {
						// Wait to be notified about a new command (in sendCommand()).
						lock.wait();
					}
				}
				// processes a new step

				nextStep();

			} catch (InterruptedException ie) {
				System.err.println("Device : Run was interrupted.");
			}
		} while (this.running);
	}

	public Position getRobotPosition() {
		return robotPosition;
	}

	public Position getLocalPosition() {
		return localPosition;
	}

	public Polygon getShape() {
		return shape;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public Color getForegroundColor() {
		return foregroundColor;
	}

	public String getName() {
		return name;
	}

	protected abstract void executeCommand(String command);

	protected abstract void nextStep();

	public void paint(Graphics g) {
		// reads the robot's current position
		robot.readPosition(robotPosition);
		// draws the shape
		Polygon globalShape = new Polygon();
		Point2D point = new Point2D.Double();
		for(int i=0; i < shape.npoints; i++) {
			point.setLocation(shape.xpoints[i], shape.ypoints[i]);
			// calculates the coordinates of the point according to the local position
			localPosition.rotateAroundAxis(point);
			// calculates the coordinates of the point according to the robot position
			robotPosition.rotateAroundAxis(point);
			// adds the point to the global shape
			globalShape.addPoint((int)Math.round(point.getX()),(int)Math.round(point.getY()));
		}
		((Graphics2D) g).setColor(backgroundColor);
		((Graphics2D) g).fillPolygon(globalShape);
		((Graphics2D) g).setColor(foregroundColor);
		((Graphics2D) g).drawPolygon(globalShape);
	}

}
