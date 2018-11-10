package org.firstinspires.ftc.teamcode;/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import android.content.Context;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.sql.Driver;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;


import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

import java.util.Locale;

/**
 * This file illustrates the concept of driving a path based on encoder counts.
 * It uses the common Pushbot hardware class to define the drive on the robot.
 * The code is structured as a LinearOpMode
 *
 * The code REQUIRES that you DO have encoders on the wheels,
 *   otherwise you would use: PushbotAutoDriveByTime;
 *
 *  This code ALSO requires that the drive Motors have been configured such that a positive
 *  power command moves them forwards, and causes the encoders to count UP.
 *
 *   The desired path in this example is:
 *   - Drive forward for 48 inches
 *   - Spin right for 12 Inches
 *   - Drive Backwards for 24 inches
 *   - Stop and close the claw.
 *
 *  The code is written using a method called: encoderDrive(speed, leftInches, rightInches, timeoutS)
 *  that performs the actual movement.
 *  This methods assumes that each movement is relative to the last stopping place.
 *  There are other ways to perform encoder based moves, but this method is probably the simplest.
 *  This code uses the RUN_TO_POSITION mode to enable the Motor controllers to generate the run profile
 *
 * Use Android Studios to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */

@Autonomous(name="backUpCrater", group="Pushbot")
//@Disabled
public class backUpCrater extends OpenCVLinearOpModeBase {

	// The IMU sensor object
	BNO055IMU imu;

	// State used for updating telemetry
	Orientation angles;
	Acceleration gravity;

	/* Declare OpMode members. */
	private BotDawg robot = new BotDawg();   // Use a Pushbot's hardware
	private ElapsedTime runtime = new ElapsedTime();

	private static final double     COUNTS_PER_MOTOR_REV    = 1440 ;    // eg: TETRIX Motor Encoder
	private static final double     DRIVE_GEAR_REDUCTION    = 1.0 ;     // This is < 1.0 if geared UP
	private static final double     WHEEL_DIAMETER_INCHES   = 4.0 ;     // For figuring circumference
	private static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
			(WHEEL_DIAMETER_INCHES * 3.1415);
	static final double     DRIVE_SPEED             = 0.7;
	static final double     TURN_SPEED              = 0.5;
	static final double     Lift_Speed              = 0.4;

	public static double min(double a, double b, double c) {
		return Math.min(Math.min(a, b), c);
	}
	@Override
	public void runOpMode(){
		robot.init(hardwareMap);

		// Send telemetry message to signify robot waiting;
		telemetry.addData("Status", "Resetting Encoders");
		telemetry.update();
		// Send telemetry message to indicate successful Encoder reset
		telemetry.addData("Path0",  "Starting at %7d :%7d",
				robot.leftFrontMotor.getCurrentPosition(),
				robot.rightFrontMotor.getCurrentPosition(),
				robot.leftBackMotor.getCurrentPosition(),
				robot.rightBackMotor.getCurrentPosition());
		telemetry.update();



		// Set up the parameters with which we will use our IMU. Note that integration
		// algorithm here just reports accelerations to the logcat log; it doesn't actually
		// provide positional information.
		BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
		parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
		parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
		parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample opmode
		parameters.loggingEnabled      = true;
		parameters.loggingTag          = "IMU";
		parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();


		// Initialize sensor parameters
		imu.initialize(parameters);

		// Wait for the game to start (driver presses PLAY)
		waitForStart();
		telemetry.addData("Path", "Complete");
		telemetry.update();
		Context context = hardwareMap.appContext;

		// Set up our telemetry dashboard
		composeTelemetry();

		waitForStart();

		// Start the logging of measured acceleration
		imu.startAccelerationIntegration(new Position(), new Velocity(), 1000);

		defaultPath();
	}

	/*
	 *  Method to perfmorm a relative move, based on encoder counts.
	 *  Encoders are not reset as the move is based on the current position.
	 *  Move will stop if any of three conditions occur:
	 *  1) Move gets to the desired position
	 *  2) Move runs out of time
	 *  3) Driver stops the opmode running.
	 */
	void composeTelemetry() {

		// At the beginning of each telemetry update, grab a bunch of data
		// from the IMU that we will then display in separate lines.
		telemetry.addAction(new Runnable() { @Override public void run()
		{
			// Acquiring the angles is relatively expensive; we don't want
			// to do that in each of the three items that need that info, as that's
			// three times the necessary expense.
			angles   = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
			gravity  = imu.getGravity();
		}
		});

		telemetry.addLine()
				.addData("status", new Func<String>() {
					@Override public String value() {
						return imu.getSystemStatus().toShortString();
					}
				})
				.addData("calib", new Func<String>() {
					@Override public String value() {
						return imu.getCalibrationStatus().toString();
					}
				});

		telemetry.addLine()
				.addData("heading", new Func<String>() {
					@Override public String value() {
						return formatAngle(angles.angleUnit, angles.firstAngle);
					}
				})
				.addData("roll", new Func<String>() {
					@Override public String value() {
						return formatAngle(angles.angleUnit, angles.secondAngle);
					}
				})
				.addData("pitch", new Func<String>() {
					@Override public String value() {
						return formatAngle(angles.angleUnit, angles.thirdAngle);
					}
				});

		telemetry.addLine()
				.addData("grvty", new Func<String>() {
					@Override public String value() {
						return gravity.toString();
					}
				})
				.addData("mag", new Func<String>() {
					@Override public String value() {
						return String.format(Locale.getDefault(), "%.3f",
								Math.sqrt(gravity.xAccel*gravity.xAccel
										+ gravity.yAccel*gravity.yAccel
										+ gravity.zAccel*gravity.zAccel));
					}
				});
	}

	String formatAngle(AngleUnit angleUnit, double angle) {
		return formatDegrees(AngleUnit.DEGREES.fromUnit(angleUnit, angle));
	}

	String formatDegrees(double degrees){
		return String.format(Locale.getDefault(), "%.1f", AngleUnit.DEGREES.normalize(degrees));
	}

	public void encoderDrive(double speed,
			double leftInches, double rightInches,
			double timeoutS) {
		int newLeftFrontTarget;
		int newRightFrontTarget;
		int newLeftBackTarget;
		int newRightBackTarget;

		// Ensure that the opmode is still active
		if (opModeIsActive()) {

			// Determine new target position, and pass to motor controller
			newLeftFrontTarget = robot.leftFrontMotor.getCurrentPosition() + (int)(leftInches * COUNTS_PER_INCH);
			newRightFrontTarget = robot.rightFrontMotor.getCurrentPosition() + (int)(rightInches * COUNTS_PER_INCH);
			newLeftBackTarget = robot.leftBackMotor.getCurrentPosition() + (int)(leftInches * COUNTS_PER_INCH);
			newRightBackTarget = robot.rightBackMotor.getCurrentPosition() + (int)(rightInches * COUNTS_PER_INCH);

			robot.leftFrontMotor.setTargetPosition(newLeftFrontTarget);
			robot.rightFrontMotor.setTargetPosition(newRightFrontTarget);
			robot.leftBackMotor.setTargetPosition(newLeftBackTarget);
			robot.rightBackMotor.setTargetPosition(newRightBackTarget);

			// Turn On RUN_TO_POSITION
			robot.leftFrontMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
			robot.rightFrontMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
			robot.leftBackMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
			robot.rightBackMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

			// reset the timeout time and start motion.
			runtime.reset();
			robot.leftFrontMotor.setPower(Math.abs(speed));
			robot.rightFrontMotor.setPower(Math.abs(speed));
			robot.leftBackMotor.setPower(Math.abs(speed));
			robot.rightBackMotor.setPower(Math.abs(speed));

			// keep looping while we are still active, and there is time left, and both motors are running.
			// Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits
			// its target position, the motion will stop.  This is "safer" in the event that the robot will
			// always end the motion as soon as possible.
			// However, if you require that BOTH motors have finished their moves before the robot continues
			// onto the next step, use (isBusy() || isBusy()) in the loop test.
			while (opModeIsActive() &&
					(runtime.seconds() < timeoutS) &&
					(robot.leftFrontMotor.isBusy() && robot.rightFrontMotor.isBusy())) {

				// Display it for the driver.
				telemetry.addData("Path2",  "Running at %7d :%7d",
						robot.leftFrontMotor.getCurrentPosition(),
						robot.rightFrontMotor.getCurrentPosition());
				telemetry.update();
			}
			// Stop all motion;
			robot.leftFrontMotor.setPower(0);
			robot.rightFrontMotor.setPower(0);
			robot.leftBackMotor.setPower(0);
			robot.rightBackMotor.setPower(0);
			// Turn off RUN_TO_POSITION
			robot.leftFrontMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
			robot.rightFrontMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

			//  sleep(250);   // optional pause after each move
		}
	}


	public void turn(float degrees) {
		while (opModeIsActive()) {
			degrees = degrees + angles.firstAngle;
			if ((angles.firstAngle != degrees) && (degrees < 0)) {
				robot.leftFrontMotor.setPower(-TURN_SPEED);
				robot.rightFrontMotor.setPower(Math.abs(TURN_SPEED));
				robot.leftBackMotor.setPower(Math.abs(-TURN_SPEED));
				robot.rightBackMotor.setPower(Math.abs(TURN_SPEED));
			}else if((angles.firstAngle != degrees) && (degrees > 0)){
				robot.leftFrontMotor.setPower(TURN_SPEED);
				robot.rightFrontMotor.setPower(Math.abs(-TURN_SPEED));
				robot.leftBackMotor.setPower(Math.abs(TURN_SPEED));
				robot.rightBackMotor.setPower(Math.abs(-TURN_SPEED));
			}else{
				robot.leftFrontMotor.setPower(0);
				robot.rightFrontMotor.setPower(Math.abs(0));
				robot.leftBackMotor.setPower(Math.abs(0));
				robot.rightBackMotor.setPower(Math.abs(0));

			}
		}
	}
	public void dropMarker(){
		robot.scoopMotor.setTargetPosition(288);
		sleep(1500);
		robot.scoopMotor.setTargetPosition(0);
		sleep(1500);
	}



	// Look at AutonomousSudo text file for more info about these 3 methods

	public void defaultPath(){
		while(opModeIsActive()) {
			encoderDrive(DRIVE_SPEED, 12,12, 2);
			turn(90);
			encoderDrive(DRIVE_SPEED, 18,18, 	2);
			turn(40);
			encoderDrive(DRIVE_SPEED, 20,20, 2);
			dropMarker();
			sleep(2000);
			turn(90);
			encoderDrive(DRIVE_SPEED, 48,48, 2);
		}
	}

	String format(OpenGLMatrix transformationMatrix) {
		return (transformationMatrix != null) ? transformationMatrix.formatAsTransform() : "null";
	}
}