package model.device;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;

import model.environment.Environment;
import model.environment.Obstacle;
import model.environment.Position;
import model.robot.MobileRobot;

public class Sonar extends Sensor {


    public Sonar(String name, MobileRobot robot, Position local,
                 Environment environment) {
        super(name, robot, local, environment);
        backgroundColor = Color.blue;
        this.addPoint(0, 10);
        this.addPoint(10, 0);
        this.addPoint(0, -2);
        this.addPoint(-2, -0);

        this.delay = 3;
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        if(scan && numSteps > 0)
        {
            Point2D point = new Point2D.Double();
            point.setLocation(0, 0);
            localPosition.rotateAroundAxis(point);
            robotPosition.rotateAroundAxis(point);

            int radius = (int) Math.round(range - range*(numSteps/maxSteps));

            int diameter = radius * 2;
            int x = (int) Math.round(point.getX()) - radius;
            int y = (int) Math.round(point.getY()) - radius;
            g.drawOval(x, y, diameter, diameter);
        }
    }

    public double read(boolean first) {
        Point2D centre = new Point2D.Double(localPosition.getX(), localPosition.getY());
        Point2D front = new Point2D.Double(localPosition.getX() + range * Math.cos(localPosition.getT()),
                localPosition.getY() + range * Math.sin(localPosition.getT()));
        // reads the robot's position
        robot.readPosition(robotPosition);
        // center's coordinates according to the robot position
        robotPosition.rotateAroundAxis(centre);
        // front's coordinates according to the robot position
        robotPosition.rotateAroundAxis(front);

        double minDistance = -1.0;
        for(int i=0; i < environment.getObstacles().size(); i++) {
            // This is really dirty: the laser uses direct access to environment's obstacles
            Obstacle obstacle = environment.getObstacles().get(i);
            if (!obstacle.getOpaque()) {
                double dist = pointToObstacle(obstacle.getPolygon(), centre, front, first);
                if(minDistance == -1.0 || (dist > 0 && dist < minDistance)) {
                    minDistance = dist;
                    if(minDistance > -1 && first) {
                        return minDistance;
                    }
                }
            }
        }
        if(minDistance > 0)
            return minDistance;
        return -1.0;
    }



}