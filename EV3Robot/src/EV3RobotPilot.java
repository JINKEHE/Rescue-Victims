import lejos.hardware.motor.Motor;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;

public class EV3RobotPilot {
	
	private static final double WHEEL_DIAMETER = 3.3;
	
	private MovePilot pilot;
	
	public EV3RobotPilot(){
		/* Setup Pilot */
		Wheel leftWheel = WheeledChassis.modelWheel(Motor.B, WHEEL_DIAMETER).offset(-10.0);
		Wheel rightWheel = WheeledChassis.modelWheel(Motor.C, WHEEL_DIAMETER).offset(10.0);
		Chassis myChassis = new WheeledChassis( new Wheel[]{leftWheel, rightWheel}, WheeledChassis.TYPE_DIFFERENTIAL);
		pilot = new MovePilot(myChassis);
		pilot.setAngularSpeed(15);
		pilot.setLinearSpeed(15);
	}
	
	public MovePilot getPilot(){
		return pilot;
	}
}
