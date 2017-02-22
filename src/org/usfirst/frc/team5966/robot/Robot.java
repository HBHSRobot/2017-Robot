package org.usfirst.frc.team5966.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.usfirst.frc.team5966.robot.commands.ExampleCommand;
import org.usfirst.frc.team5966.robot.subsystems.ExampleSubsystem;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot implements Runnable {

	final double SPEED = 0.32;
	final int NUM_MOTORS = 3;
	final int NUM_OF_PWM_SLOTS = 9;
	final int TRIGGER_AXIS = 3;
	final int TIMER = 3 * 1000;
	final boolean teleOp = false;
	
	static boolean autoMode2 = false;
	
	public static final ExampleSubsystem exampleSubsystem = new ExampleSubsystem();
	public static OI oi;

	Command autonomousCommand;
	SendableChooser<Command> chooser = new SendableChooser<>();
	
	CameraServer cameraServer;
	
	//array because there are three motors per side
	RobotDrive[] robotDrives;
	Joystick driveStick;
	VictorSP[] leftMotors;
	VictorSP[] rightMotors;
	VictorSP winchMotor;
	int autoCount = 3;
	static boolean autoMode = false;



	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		oi = new OI();
		chooser.addDefault("Default Auto", new ExampleCommand());
		// chooser.addObject("My Auto", new MyAutoCommand());
		SmartDashboard.putData("Auto mode", chooser);
		//left motors are PWM 0, 1, 2
		//right motors are PWM 3, 4, 5
		leftMotors = new VictorSP[NUM_MOTORS];
		rightMotors = new VictorSP[NUM_MOTORS];
		robotDrives = new RobotDrive[NUM_MOTORS];
		for (int i = 0; i < NUM_MOTORS; i++)
		{
			leftMotors[i] = new VictorSP(NUM_OF_PWM_SLOTS - i);
			rightMotors[i] = new VictorSP(i);
			
			robotDrives[i] = new RobotDrive(leftMotors[i],
					 rightMotors[i]);
			leftMotors[i].setInverted(true);
			rightMotors[i].setInverted(true);
		}
		winchMotor = new VictorSP(5);
		//XBOX Controller
		driveStick = new Joystick(0);
		//Camera Server
		cameraServer = CameraServer.getInstance();
		cameraServer.startAutomaticCapture();
		System.out.println("Robot Initialization Complete");
	}
	

	/**
	 * This function is called once each time the robot enters Disabled mode.
	 * You can use it to reset any subsystem information you want to clear when
	 * the robot is disabled.
		
	 */
	@Override
	public void disabledInit() {

	}

	@Override
	public void disabledPeriodic() {
		Scheduler.getInstance().run();
	}

	/**
		
		
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString code to get the auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional commands to the
	 * chooser code above (like the commented example) or additional comparisons
	 * to the switch structure below with additional strings & commands.
	 */
	@Override
	public void autonomousInit() {
		System.out.println("Autonomous Mode Initialized");
		autonomousCommand = chooser.getSelected();

		/*RobotDrive
		 * String autoSelected = SmartDashboard.getString("Auto Selector",
		 * "Default"); switch(autoSelected) 
		{ case "My Auto": autonomousCommand
		 * = new MyAutoCommand(); break; case "Default Auto": default:
		 * autonomousCommand = new ExampleCommand(); break; }
		 */ 

		// schedule the autonomous command (example)
		if (autonomousCommand != null)
			autonomousCommand.start();
		autoMode = true;
		autoMode2 = false;
		//autoCount = 3;
	}

	public void run(){
		System.out.println("Started Autonomous Timer Thread");
		try
		{
			Thread.sleep(TIMER);
			autoMode = false;
			/*autoMode2 = true;
			Thread.sleep(TIMER);
			autoMode2 = false;
			*/
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**NUM_MOTORS
	 * This function is called periodically during autonomous
	 */
	@Override
	public void autonomousPeriodic() {
		(new Thread(new Robot())).start();
		while(autoMode == true)
		{
			for(int i = 0; i < NUM_MOTORS; i++){
				robotDrives[i].drive(-SPEED, 0);
				//robotDrives[i].drive(SPEED, 3);
			}
		}
		/*while(autoMode2 == true)
		{
			for(int i = 0; i < NUM_MOTORS; i++){
				//robotDrives[i].drive(SPEED, 0);
				robotDrives[i].drive(-SPEED, 0);
			}
		}*/
	}

	@Override
	
	public void teleopInit() {
		// This makes sure that the autonomous stops running when
		// teleop starts running. If you want the autonomous to
		// continue until interrupted by another command, remove
		// this line or comment it out.
		if (autonomousCommand != null)
			autonomousCommand.cancel();
		System.out.println("Teleop Initialized");
	}

	/**
	 * This function is called periodically during operator control
	 */
	@Override
	public void teleopPeriodic() {
		Scheduler.getInstance().run();
		System.out.println("Teleop Periodic");
		while (isOperatorControl() && isEnabled())
		{
			//drive motor sets 1, 2, and 3
			for (int i = 0; i < NUM_MOTORS; i++)
			{
				robotDrives[i].arcadeDrive(driveStick);
			}
			//winch controls
			double triggerData = driveStick.getRawAxis(TRIGGER_AXIS);
			if (triggerData > 0.1)
			{
				winchMotor.setSpeed(triggerData);
			}
			else
			{
				winchMotor.setSpeed(0.0);
			}
			//fine robot control
			double rightStickXData = driveStick.getRawAxis(4);
			double rightStickYData = driveStick.getRawAxis(5);
			if (rightStickXData > 0.1 || rightStickXData < -0.1 || rightStickYData > 0.1 || rightStickYData < -0.1)
			{
				for (int i = 0; i < NUM_MOTORS; i++)
				{
					robotDrives[i].drive((-1 * rightStickYData) / 2, (-1 * rightStickXData) / 2);
				}
			}
			Timer.delay(0.01);
		}
	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {
		LiveWindow.run();
	}
}