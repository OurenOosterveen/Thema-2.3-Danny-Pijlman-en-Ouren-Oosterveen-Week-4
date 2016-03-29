package model.robot;

import model.virtualmap.OccupancyMap;

import java.io.PipedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.PipedOutputStream;
import java.io.IOException;

import java.util.StringTokenizer;

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

public class MobileRobotAI implements Runnable {

	private final OccupancyMap map;
	private final MobileRobot robot;
    private final int MIN_DISTANCE_FROM_WALL = 15;
    private final int MAX_DISTANCE_PER_STEP = 20;
    private final double SCAN_DISTANCE = 100.0;

	private boolean running;
    private boolean wallFound;

	public MobileRobotAI(MobileRobot robot, OccupancyMap map) {
		this.map = map;
		this.robot = robot;

	}

	/**
	 * In this method the gui.controller sends commands to the robot and its devices.
	 * At the moment all the commands are hardcoded.
	 * The exercise is to let the gui.controller make intelligent decisions based on
	 * what has been discovered so far. This information is contained in the OccupancyMap.
	 */
	public void run() {
		String result;
        this.wallFound = false;

        double distance;
        int degree;

		double position[] = new double[3];
		double measures[] = new double[360];
        double sonarMeasures[] = new double[360];
		while (map.mapDiscovered()) {
			try {

				PipedInputStream pipeIn = new PipedInputStream();
				BufferedReader input = new BufferedReader(new InputStreamReader(pipeIn));
				PrintWriter output = new PrintWriter(new PipedOutputStream(pipeIn), true);

				robot.setOutput(output);

//      ases where a variable value is never used after its assignment, i.e.:
				System.out.println("intelligence running");

				getPosition(input, position);
				scanPosition(input, measures, position);

                //robot.sendCommand("S1.SONARSCAN");
                //result = input.readLine();
                //parseSonarMeasures(result, sonarMeasures);
                //map.drawLaserScan(position, measures);

                if(!wallFound)
                {
                    distance = SCAN_DISTANCE;
                    degree = 0;
                    // look at all the degree and save if the measurement is closer than
                    // the scanner is looking at
                    for(int i=0; i<360;i++)
                    {
                        if(measures[i]<distance)
                        {
                            distance = measures[i];
                            degree = i;
                        }
                    }
                    //if no obstacle found, just move forward
                    if(distance == SCAN_DISTANCE)
                    {
                        robot.sendCommand("P1.MOVEFW 100");
                        result = input.readLine();
                    }
                    else
                    {
                        // else turn right to the degree
                        int obstacle = (int) distance;

                        robot.sendCommand("P1.ROTATERIGHT " + degree);
                        result = input.readLine();

                        robot.sendCommand("P1.MOVEFW " + (obstacle - 15));
                        result = input.readLine();

                        robot.sendCommand("P1.ROTATELEFT 90");
                        result = input.readLine();

                        wallFound = true;
                    }
                }
                else
                {
                    if(measures[0] < 2 && measures[90] != SCAN_DISTANCE)
                    {
                        robot.sendCommand("P1.ROTATELEFT 90");
                        result = input.readLine();
                    }
                    else if(measures[90] != SCAN_DISTANCE)
                    {
                        int temp = (int) measures[0];
                        if(measures[0] > 30)
                            temp = 30;

                        robot.sendCommand("P1.MOVEFW " + (temp - 1));
                        result = input.readLine();

                    }
                    else if(measures[90] == SCAN_DISTANCE)
                    {
                        if(measures[0] < 30)
                        {
                            int temp = (int) measures[0];
                            if(temp > 30){ temp = 30; }
                            robot.sendCommand("P1.MOVEFW " + (temp - 1));
                            result = input.readLine();

                        }
                        else
                        {
                            robot.sendCommand("P1.MOVEFW " + MAX_DISTANCE_PER_STEP);
                            result = input.readLine();
                        }

                        robot.sendCommand("P1.ROTATERIGHT 90");
                        result = input.readLine();

                        int temp = (int) measures[270];
                        if(temp > 30) { temp = 30; }
                        robot.sendCommand("P1.MOVEFW " + (temp - 1));
                        result = input.readLine();
                    }
                }
			} catch (IOException ioe) {
				System.err.println("execution stopped");
				running = false;
			}
		}

	}

	private void parsePosition(String value, double position[]) {
		int indexInit;
		int indexEnd;
		String parameter;

		indexInit = value.indexOf("X=");
		parameter = value.substring(indexInit + 2);
		indexEnd = parameter.indexOf(' ');
		position[0] = Double.parseDouble(parameter.substring(0, indexEnd));

		indexInit = value.indexOf("Y=");
		parameter = value.substring(indexInit + 2);
		indexEnd = parameter.indexOf(' ');
		position[1] = Double.parseDouble(parameter.substring(0, indexEnd));

		indexInit = value.indexOf("DIR=");
		parameter = value.substring(indexInit + 4);
		position[2] = Double.parseDouble(parameter);
	}

	private void parseMeasures(String value, double measures[]) {
        for (int i = 0; i < 360; i++) {
            measures[i] = 100.0;
        }
        if (value.length() >= 5) {
            value = value.substring(5);  // removes the "SCAN " keyword

            StringTokenizer tokenizer = new StringTokenizer(value, " ");

            double distance;
            int direction;
            while (tokenizer.hasMoreTokens()) {
                distance = Double.parseDouble(tokenizer.nextToken().substring(2));
                direction = (int) Math.round(Math.toDegrees(Double.parseDouble(tokenizer.nextToken().substring(2))));
                if (direction == 360) {
                    direction = 0;
                }
                measures[direction] = distance;
                // Printing out all the degrees and what it encountered.
                //System.out.println("direction = " + direction + " distance = " + distance);
            }
        }
        for (int i = 0; i < 360; i ++) {
            System.out.println("direction = " + i + " Distance = " + measures[i]);
        }
    }

    private void parseSonarMeasures(String value, double sonarMeasures[]) {
        for (int i = 0; i < 360; i++) {
            sonarMeasures[i] = 100.0;
        }
        if (value.length() >= 0) {
            value = value.substring(0);  // removes the "SCAN " keyword

            StringTokenizer tokenizer = new StringTokenizer(value, " ");

            double distance;
            int direction;
            while (tokenizer.hasMoreTokens()) {
                distance = Double.parseDouble(tokenizer.nextToken().substring(2));
                direction = (int) Math.round(Math.toDegrees(Double.parseDouble(tokenizer.nextToken().substring(2))));
                if (direction == 360) {
                    direction = 0;
                }
                sonarMeasures[direction] = distance;
                // Printing out all the degrees and what it encountered.
                //System.out.println("direction = " + direction + " distance = " + distance);
            }
        }
        for (int i = 0; i < 360; i ++) {
            System.out.println("direction = " + i + " Distance = " + sonarMeasures[i]);
        }
    }

    private void getPosition(BufferedReader input, double position[])
    {
        try {
            robot.sendCommand("R1.GETPOS");
            String result = input.readLine();
            parsePosition(result, position);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void scanPosition(BufferedReader input, double[] measures, double[] position)
    {
        try {
            robot.sendCommand("L1.SCAN");
            String result = input.readLine();
            parseMeasures(result, measures);
            map.drawLaserScan(position, measures);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
