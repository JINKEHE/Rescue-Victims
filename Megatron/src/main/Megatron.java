package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import threads.StoppingThread;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.Sound;

public class Megatron {
    // for the robot itself
    private Brick ev3;
    // for sensors
    private EV3UltrasonicSensor uSensor;
    private EV3ColorSensor cSensor;
    private SampleProvider colourSP, ultrasonicDistSP;
    private float[] colourSample, ultrasonicDistSample;
    private NXTRegulatedMotor uSensorMotor;
    // for calibrations
    private static final double IDEAL_DISTANCE = 14;
    private static final double ADJUST_ANGLE_THRESHOLD = 55;
    private static final double MINIMAL_ANGLE_TO_ADJUST = 5;
    private static final double MOVE_DISTANCE_TO_ADJUST_DISTANCE = 13;
    private static final double MOVE_DISTANCE_TO_ADJUST_ANGLE = 5;
    private static final double MOVE_BACK_DISTANCE = 2;
    private boolean firstStepFinished = false;
    // for pilot setting (optimized through experiments)
    private MovePilot pilot;
    private static final double ANGULAR_SPEED = 50;
    private static final double ANGULAR_ACCELERATION = 200;
    private static final double LINEAR_SPEED = 10;
    private static final double LINEAR_ACCELERATION = 60;
    private static final double DIAMETER = 4.4;
    private static final double OFFSET = 5.5;
    // shorter one in this assignment
    private static final double LEFT_RIGHT_DISTANCE = 10;
    // longer one in this assignment
    private static final double UP_DOWN_DISTANCE = 12;
    private static final double FIXED_LENGTH_DISTANCE = 12;
    // for stopping thread (the thread used to shut down the robot)
    private StoppingThread stoppingThread;
    // for scanning
    private static final int SCAN_DELAY = 40;
    private static final int REPEAT_SCAN_TIMES = 10;
    private static final double SCAN_STABLE_THRESHOLD = 0.5;
    private static final float OCCUPIED_THRESHOLD = 30;
    private static final double RED_THRESHOLD = 0.1;
    private static final double BLUE_THRESHOLD = 0.1;
    private static final double GREEN_THRESHOLD = 0.08;
    private static final double BLACK_THRESHOLD = 0.08;
    private static final String BLACK = "black";
    private static final String GREEN = "green";
    private static final String RED = "red";
    private static final String BLUE = "blue";
    private static final String WHITE = "white";
    // for socket connection
    private static final int PORT = 18888;
    private Socket sock;
    private ServerSocket server;
    private OutputStream outputStream;
    private PrintWriter output;
    private InputStream inputStream;
    private BufferedReader input;
    private static final String WRONG = "wrong";
    private static final String DONE = "done";
    // relative headings
    private static final String RELATIVE_FRONT = "0";
    private static final String RELATIVE_BACK = "1";
    private static final String RELATIVE_LEFT = "2";
    private static final String RELATIVE_RIGHT = "3";
    // headings
    private static final String UNKNOWN = "unknown";
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    
    /* set up sensors */

    // set up ultrasonic sensor
    private void setupUltrasonicSensor() {
        uSensorMotor = Motor.C;
        uSensor = new EV3UltrasonicSensor(ev3.getPort("S3"));
        ultrasonicDistSP = uSensor.getDistanceMode();
        ultrasonicDistSample = new float[ultrasonicDistSP.sampleSize()];
    }

    // set up the color sensor -> using RGB mode
    private void setupColorSensor() {
        cSensor = new EV3ColorSensor(ev3.getPort("S2"));
        colourSP = cSensor.getRGBMode();
        colourSample = new float[colourSP.sampleSize()];
    }

    /* sensor methods */

    // get raw data from the color sensor (RGB mode)
    private float[] getRawColorData() {
        colourSP.fetchSample(colourSample, 0);
        return colourSample;
    }

    // get the color scanned
    private String getColorName() {
        float[] colorData = getRawColorData();
        float r = colorData[0];
        float g = colorData[1];
        float b = colorData[2];
        if (r < BLACK_THRESHOLD && g < BLACK_THRESHOLD && b < BLACK_THRESHOLD) {
            return BLACK;
        } else if (r > RED_THRESHOLD && g > BLUE_THRESHOLD && b > BLUE_THRESHOLD) {
            return WHITE;
        } else if (r > RED_THRESHOLD) {
            return RED;
        } else if (b > BLUE_THRESHOLD) {
            return BLUE;
        } else if (g > GREEN_THRESHOLD) {
            return GREEN;
        }
        return WRONG;
    }

    // get one single distance (gurantee a non-INFINITY return)
    private float getOneDistance() {
        try {
            Thread.sleep(SCAN_DELAY);
        } catch (InterruptedException e) {
            // this won't happen
        }
        // ask ultrasonic sensor for raw data
        ultrasonicDistSP.fetchSample(ultrasonicDistSample, 0);
        float distance = ultrasonicDistSample[0] * 100;
        // if the distance is INIFINITY, try to move back or rotate a little bit
        // to avoid it
        int count = 1;
        // repeat the following actions until a non-INIFINITY number is returned
        while (Float.isInfinite(distance)) {
            // move back and scan again
            if (count == 1) {
                pilot.travel(-1);
                ultrasonicDistSP.fetchSample(ultrasonicDistSample, 0);
                distance = ultrasonicDistSample[0] * 100;
                count = -1;
                // if moving back does not work, rotate one degree and scan
                // again
            } else if (count == -1) {
                pilot.rotate(1);
                ultrasonicDistSP.fetchSample(ultrasonicDistSample, 0);
                distance = ultrasonicDistSample[0] * 100;
                pilot.rotate(-1);
                count = 1;
            }
        }
        return distance;
    }

    // get a stable and accruate distance
    private double getAccurateDistance() {
        double average = 0;
        while (true) {
            // get a set of data by scanning and compute an average
            average = 0;
            float[] distances = new float[REPEAT_SCAN_TIMES];
            for (int i = 0; i <= REPEAT_SCAN_TIMES - 1; i++) {
                distances[i] = getOneDistance();
                average += distances[i] / REPEAT_SCAN_TIMES;
            }
            // if each distance we got is close to the average, then this set of
            // data is reliable
            // if any distance in the data set is very different from the
            // average, then this set
            // of data will be considered unreliable and will be scanned again
            for (int i = 0; i <= REPEAT_SCAN_TIMES - 1; i++) {
                if (Math.abs(average - distances[i]) > SCAN_STABLE_THRESHOLD) {
                    continue;
                }
            }
            break;
        }
        return average;
    }

    /* moving related methods */

    // set up pilot
    private void setupPilot() {
        Wheel leftWheel = WheeledChassis.modelWheel(Motor.B, DIAMETER).offset(-OFFSET);
        Wheel rightWheel = WheeledChassis.modelWheel(Motor.D, DIAMETER).offset(OFFSET);
        Chassis myChassis = new WheeledChassis(new Wheel[] { leftWheel, rightWheel }, WheeledChassis.TYPE_DIFFERENTIAL);
        pilot = new MovePilot(myChassis);
        pilot.setAngularSpeed(ANGULAR_SPEED);
        pilot.setAngularAcceleration(ANGULAR_ACCELERATION);
        pilot.setLinearSpeed(LINEAR_SPEED);
        pilot.setLinearAcceleration(LINEAR_ACCELERATION);
    }

    // if we don't know our heading, just travel for a fixed length
    // if we know, then we can move more accurately
    private void goForward(double distance) {
        pilot.forward();
        while (true) {
            if (getColorName().equals(BLACK)) {
                pilot.stop();
                pilot.travel(distance);
                break;
            }
        }          
    }

    // move according to the relative target direction to the robot
    // for example, if the relative direction is front, then just go forward
    private String moveRelatively(String relativeDirection, String heading) {
        System.out.println("move: " + relativeDirection);
        doCalibration(relativeDirection);
        switch (relativeDirection) {
        case RELATIVE_FRONT:
            break;
        case RELATIVE_BACK:
            pilot.rotate(180);
            break;
        case RELATIVE_LEFT:
            pilot.rotate(-90);
            break;
        case RELATIVE_RIGHT:
            pilot.rotate(90);
            break;
        }
        // if after rotation, heading is up or down -> then move the longer distance
        // if after rotation, heading is left or right -> then move the short distance
        // if we still don't know the heading, then move fixed length distance
        double distance = 0;
        if (heading.equals(UNKNOWN)) {
            distance = FIXED_LENGTH_DISTANCE;   
        } else {
            if (relativeDirection.equals(RELATIVE_LEFT) || relativeDirection.equals(RELATIVE_RIGHT)) {
                if (heading.equals(LEFT) || heading.equals(RIGHT)) {
                    distance = UP_DOWN_DISTANCE;
                } else {
                    distance = LEFT_RIGHT_DISTANCE;
                }
            } else {
                if (heading.equals(LEFT) || heading.equals(RIGHT)) {
                    distance = LEFT_RIGHT_DISTANCE;
                } else {
                    distance = UP_DOWN_DISTANCE;
                } 
            }
        }
        goForward(distance);
        return DONE;
    }    
    
    /* calibration related methods */

    // do calibration
    private void doCalibration(String direction) {
        int sensorToRotate = 0;
        boolean reverse = false;
        if (!firstStepFinished || direction.equals(RELATIVE_FRONT)) {
            return;
        } else if (direction.equals(RELATIVE_BACK) || direction.equals(RELATIVE_RIGHT)) {
            // according to the left obstacle
            sensorToRotate = 90;
            reverse = true;
        } else if (direction.equals(RELATIVE_LEFT)) {
            // according to the right obstacle
            sensorToRotate = -90;
            reverse = false;
        }
        pilot.travel(-MOVE_BACK_DISTANCE);
        uSensorMotor.rotate(sensorToRotate);
        double distance = getAccurateDistance();
        if (distance < OCCUPIED_THRESHOLD) {
            adjustAngle(reverse);
        }
        if (distance < IDEAL_DISTANCE) {
            adjustDistance(reverse);
        }
        uSensorMotor.rotate(-sensorToRotate);
        pilot.travel(MOVE_BACK_DISTANCE);
    }

    // adjust the angle between the wall
    private void adjustAngle(boolean reverse) {
        pilot.travel(-MOVE_DISTANCE_TO_ADJUST_ANGLE);
        double firstDist = getAccurateDistance();
        pilot.travel(MOVE_DISTANCE_TO_ADJUST_ANGLE);
        double secondDist = getAccurateDistance();
        double tantheta = (secondDist - firstDist) / MOVE_DISTANCE_TO_ADJUST_ANGLE;
        if (Math.abs(secondDist - firstDist) > 10) {
            return;
        }
        double theta = Math.atan(tantheta) * 180 / Math.PI;
        if (Math.abs(theta) > ADJUST_ANGLE_THRESHOLD)
            theta = 0;
        theta *= reverse ? -1 : 1;
        pilot.rotate(theta);
    }

    // adjust the distance between the wall
    private void adjustDistance(boolean reverse) {
        double currentDistance = getAccurateDistance();
        double diff = IDEAL_DISTANCE - currentDistance;
        double sintheta = diff / MOVE_DISTANCE_TO_ADJUST_DISTANCE;
        if (sintheta > 1)
            return; // in case unexpected thing happened
        double theta = Math.asin(sintheta) * 180 / Math.PI;
        if (theta > 80)
            theta = ADJUST_ANGLE_THRESHOLD;
        theta *= reverse ? -1 : 1;
        if (Math.abs(theta) > MINIMAL_ANGLE_TO_ADJUST) {
            pilot.rotate(theta);
            pilot.travel(-MOVE_DISTANCE_TO_ADJUST_DISTANCE);
            pilot.rotate(-theta);
            pilot.travel(MOVE_DISTANCE_TO_ADJUST_DISTANCE);
        }
        adjustAngle(reverse);
    }

    // stop the robot
    public void stop() throws IOException {
        pilot.stop();
        Sound.beepSequenceUp();
        sock.close();
        server.close();
        System.exit(0);
    }

    // set up the server
    private void setupServer() {
        try {
            server = new ServerSocket(PORT);
            boolean connected = false;
            while (!connected) {
                try {
                    sock = server.accept();
                    connected = true;
                    Sound.beep();
                } catch (Exception e) {

                }
            }
            outputStream = sock.getOutputStream();
            output = new PrintWriter(outputStream, true);
            inputStream = sock.getInputStream();
            input = new BufferedReader(new InputStreamReader(inputStream));
        } catch (Exception e) {

        }
    }

    // constructor of Megatron, also the main loop
    public Megatron() {
        ev3 = BrickFinder.getDefault();
        setupPilot();
        setupColorSensor();
        setupUltrasonicSensor();
        setupServer();
        stoppingThread = new StoppingThread(this);
        stoppingThread.start();
        while (true) {
            try {
                String command = input.readLine();
                System.out.println("Command: " + command);
                if (command.equals("get(color)")) {
                    output.println(getColorName());
                } else if (command.equals("get(occupied)")) {
                    output.println(getOccupiedInfo());
                } else if (command.startsWith("move")) {
                    String[] parameters = getParameters(command);
                    output.println(moveRelatively(parameters[0],parameters[1]));
                    firstStepFinished = true;
                } else if (command.equals("end")) {
                    Sound.beepSequence();
                    Sound.beepSequenceUp();
                    Sound.beep();
                    output.println(DONE);
                }
                output.flush();
            } catch (IOException e) {
            }
        }
    }

    // extract parameters from a command
    // for example, say(hello,world,nihao) -> ["hello","world","nihao"]
    private String[] getParameters(String command) {
        String[] info = command.split(",");
        String[] parameters = new String[info.length];
        for (int i = 0; i <= parameters.length - 1; i++) {
            if (info[i].contains("(")) {
                info[i] = info[i].substring(info[i].indexOf("(") + 1, info[i].length());
            }
            if (info[i].contains(")")) {
                info[i] = info[i].substring(0, info[i].indexOf(")"));
            }
            parameters[i] = info[i];
        }
        return parameters;
    }

    // get occupancy information around the robot
    // the return would be a binary string
    private String getOccupiedInfo() {
        pilot.travel(-MOVE_BACK_DISTANCE);
        char[] results = new char[4];
        String str = "";
        // get front first
        boolean front = getAccurateDistance() < OCCUPIED_THRESHOLD;
        results[0] = front ? '1' : '0';
        // sensor rotate right 90 get right
        uSensorMotor.rotate(-90);
        boolean right = getAccurateDistance() < OCCUPIED_THRESHOLD;
        results[3] = right ? '1' : '0';
        // sensor rotate left 180
        uSensorMotor.rotate(180);
        boolean left = getAccurateDistance() < OCCUPIED_THRESHOLD;
        results[2] = left ? '1' : '0';
        if (firstStepFinished == true) {
            results[1] = '0';
        } else {
            pilot.rotate(-90);
            results[1] = getAccurateDistance() < OCCUPIED_THRESHOLD ? '1' : '0';
            pilot.rotate(90);
        }
        uSensorMotor.rotate(-90);
        str = new String(results);
        System.out.println(str);
        pilot.travel(MOVE_BACK_DISTANCE);
        return str;
    }

    public static void main(String[] args) {
        new Megatron();
    }
}
