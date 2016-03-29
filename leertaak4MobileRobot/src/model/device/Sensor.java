package model.device;

import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import model.environment.Environment;
import model.environment.Position;
import model.robot.MobileRobot;

public abstract class Sensor extends model.device.Device {

    int orientation = 1;      // 1: clockwise  -1: otherwise
    double rotStep = 1.0;     // one degree
    double numSteps = 0;
    double maxSteps = 0;

    // JB: The use of the booleans detect and scan (and Device.running) makes the code very complex
    // and easy to break. See executeCommand() and nextStep(). This could do with a decent refactoring!
    boolean detect = false;
    boolean scan = false;
    int range = 100;          // maximum range

    SonarMeasurement detectMeasure = null;
    ArrayList<SonarMeasurement> scanMeasures = new ArrayList<SonarMeasurement>();

    public Sensor(String name, MobileRobot robot, Position localPos, Environment environment) {
        super(name, robot, localPos, environment);

    }

    //only difference between scanners:
    public abstract double read(boolean first);

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
    }

    public double pointToObstacle(Polygon polygon, Point2D centre, Point2D front, boolean first) {
        int j = 0;
        double minDistance = -1.0;
        double dist = -1.0;
        double px, py;
        double x1, y1, x2, y2;
        double m1, q1, m2, q2;
        Line2D.Double beam = new Line2D.Double(centre, front);

        for(int i=0; i < polygon.npoints; i++) {
            j = i+1;
            if(j == polygon.npoints)
                j = 0;
            x1 = polygon.xpoints[i];
            y1 = polygon.ypoints[i];
            x2 = polygon.xpoints[j];
            y2 = polygon.ypoints[j];
            if(beam.intersectsLine(x1, y1, x2, y2)) {
                // calculates the intersection point
                if(centre.getX() == front.getX()) {
                    px = centre.getX();
                    py = (y2 - y1) / (x2 - x1) * (px - x1) + y1;
                }
                else
                if(x1 == x2) {
                    px = x1;
                    py = (front.getY()-centre.getY()) / (front.getX()-centre.getX()) * (px - centre.getX()) + centre.getY();
                }
                else {
                    m1 = (y2 - y1) / (x2 - x1);
                    q1 = y1 - m1 * x1;
                    m2 = (front.getY()-centre.getY()) / (front.getX()-centre.getX());
                    q2 = centre.getY() - m2 * centre.getX();
                    px = (q2 - q1) / (m1 - m2);
                    py = m1 * px + q1;
                }
                // calculates the distance between (cx, cy) and the intersection point
                dist = Point2D.Double.distance(centre.getX(), centre.getY(), px, py);
                if(minDistance == -1.0 || minDistance > dist)
                    minDistance = dist;
                if(first && minDistance > 0.0)
                    return minDistance;
            }
        }
        return minDistance;
    }

    public void executeCommand(String command) {
        if(command.indexOf("ROTATETO") > -1) {
            rotStep = 4.0;
            double direction = Math.abs(Double.parseDouble(command.trim().substring(9).trim()));
            while(direction < 0.0)
                direction+=360.0;
            while(direction > 360.0)
                direction-=360.0;
            double dirDiff = direction - Math.toDegrees(localPosition.getT());   // ??????????????
            if(dirDiff >= 0.0 && dirDiff <= 180.0) {
                numSteps = dirDiff / rotStep;
                maxSteps = numSteps;
                orientation = 1;
            }
            else if(dirDiff >= 0.0 && dirDiff > 180.0) {
                numSteps = (360.0 - dirDiff) / rotStep;
                maxSteps = numSteps;
                orientation = -1;
            }
            else if(dirDiff < 0.0 && -dirDiff <= 180.0) {
                numSteps = -dirDiff / rotStep;
                maxSteps = numSteps;
                orientation = -1;
            }
            else if(dirDiff < 0.0 && -dirDiff > 180.0) {
                numSteps = (360.0 + dirDiff) / rotStep;
                maxSteps = numSteps;
                orientation = 1;
            }
            running = true;
        }
        else if(command.equalsIgnoreCase("READ")) {
            writeOut("t=" + Double.toString(this.localPosition.getT()) + " d=" + Double.toString(this.read(true)));
        }
        else if(command.equalsIgnoreCase("SONARSCAN")) {

            rotStep = 1.0;
            scanMeasures.clear();
            numSteps = 360.0 / rotStep;
            maxSteps = numSteps;
            orientation = 1;
            scan = true;
            // send the list of measures
            commands.add("GETMEASURES");
            running = true;
        }
        else if(command.equalsIgnoreCase("GETMEASURES")) {
            SonarMeasurement measure = null;
            String measures = "SCAN";
            for(int i=0; i < scanMeasures.size(); i++) {
                measure = scanMeasures.get(i);
                measures += " d=" + measure.distance + " t=" + measure.direction;
            }
            writeOut(measures);
        }
        else if(command.equalsIgnoreCase("DETECT")) {
            detect = true;
            rotStep = 8.0;
            if(detectMeasure != null) {
                writeOut("SONAR DETECT d=" + detectMeasure.distance + " t=" + detectMeasure.direction);
                detectMeasure = null;
            }
            else if(localPosition.getT() == Math.toRadians(45.0)) {   // ?????????????
                // move the laser to the left position
                commands.add("ROTATETO 315");
                // repeats this command
                commands.add("DETECT");
            }
            else if(localPosition.getT() == Math.toRadians(315.0)) {  // ??????????????
                // move the laser to the right position
                commands.add("ROTATETO 45");
                // repeats this command
                commands.add("DETECT");
            }
            else {
                // move the laser to the right position
                commands.add("ROTATETO 45");
                // repeats this command
                commands.add("DETECT");
            }
        }
        else
            writeOut("DECLINED");
    }


    public void nextStep() {
        if (running && numSteps > 0.0) {
            if (numSteps < 1.0) {
                localPosition.rotateAroundAxis(0.0, 0.0, orientation * numSteps * rotStep);
            } else {
                localPosition.rotateAroundAxis(0.0, 0.0, orientation * rotStep);
            }
            environment.processEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            numSteps -= 1.0;
            running = true;
        } else if (running) {
            running = false;
            if (!detect && !scan) {
                writeOut("SENSOR ARRIVED");
            }
        }
        if (detect) {
            double distance = this.read(true);
            if (distance > -1.0) {
                if (detectMeasure == null)
                    detectMeasure = new SonarMeasurement(distance, localPosition.getT());  // ?????????????
                else if (detectMeasure.distance > distance)
                    detectMeasure.set(distance, localPosition.getT());  // ????????????
            }
        } else if (scan) {
            double distance = this.read(false);
            if (distance > -1.0) {
                scanMeasures.add(new SonarMeasurement(distance, localPosition.getT()));  // ??????????????
            }
        }

    }
}