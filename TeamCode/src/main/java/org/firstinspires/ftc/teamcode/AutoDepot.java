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

import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;

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

@Autonomous(name="AutoDepot", group="Pushbot")
//@Disabled
public class AutoDepot extends OpenCVLinearOpModeBase {



    /* Declare OpMode members. */
    private BotDawg robot = new BotDawg();   // Use a Pushbot's hardware
    private ElapsedTime runtime = new ElapsedTime();

    double leftBlueValue = imageProcessor.leftMineralBlue;
    double midBlueValue = imageProcessor.midMineralBlue;
    double rightBlueValue = imageProcessor.rightMineralBlue;
    double blueMin = 0.0;
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


        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        telemetry.addData("Path", "Complete");
        telemetry.update();
        Context context = hardwareMap.appContext;

        cameraManager.initialize(context, true, this);
        imageProcessor.initialize();

        waitForStart();

        while (opModeIsActive()) {
            imageProcessor.takePicture();
            while(opModeIsActive() && !imageProcessor.isFrameReady()){
                idle();
            }

        }
        cameraManager.stop(context);
        imageProcessor.stop();

        blueMin = min(leftBlueValue, midBlueValue, rightBlueValue);
        //Cant use a switch statement because these are floating points.
        if(blueMin == leftBlueValue){
            //Run method left
        }else if(blueMin == midBlueValue){
            //Run method center
        }else if(blueMin == rightBlueValue){
            //Run method right
        }else{
            //run method default
        }
    }

    /*
     *  Method to perfmorm a relative move, based on encoder counts.
     *  Encoders are not reset as the move is based on the current position.
     *  Move will stop if any of three conditions occur:
     *  1) Move gets to the desired position
     *  2) Move runs out of time
     *  3) Driver stops the opmode running.
     */

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

    public void encoderLift(double speed,
                             double yInches,
                             double timeoutS) {
        int newYTarget;


        // Ensure that the opmode is still active
        if (opModeIsActive()) {

            // Determine new target position, and pass to motor controller
            newYTarget = robot.liftMotor.getCurrentPosition() + (int)(yInches * COUNTS_PER_INCH);


            robot.liftMotor.setTargetPosition(newYTarget);

            // Turn On RUN_TO_POSITION
            robot.liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);


            // reset the timeout time and start motion.
            runtime.reset();
            robot.liftMotor.setPower(Math.abs(speed));


            while (opModeIsActive() &&
                    (runtime.seconds() < timeoutS) &&
                    (robot.liftMotor.isBusy())) {

                // Display it for the driver.
                telemetry.addData("Path2",  "Running at %7d :%7d",
                        robot.liftMotor.getCurrentPosition());
                telemetry.update();
            }
            // Stop all motion;
            robot.liftMotor.setPower(0);

            // Turn off RUN_TO_POSITION
            robot.liftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        }
    }





    // Look at AutonomousSudo text file for more info abouut these 3 methods
    public void left(){
        while(opModeIsActive()) {
            encoderLift(Lift_Speed, 5, 2);
            //Turn X Degrees??????????????//

            encoderDrive(DRIVE_SPEED, 3.1, 3.1, 2);

            //Turn -X Degrees????????????//

            encoderDrive(DRIVE_SPEED, 3.1, 3.1, 2);

            robot.markerServo.setPosition(1.0);

            //Turn 225 Degrees??????????????//

            encoderDrive(DRIVE_SPEED, 8, 8, 6.0);
        }
    }

    public void center(){
        while(opModeIsActive()) {
            encoderLift(Lift_Speed, 5, 2);
            encoderDrive(DRIVE_SPEED, 2, 2, 1.5);
            robot.markerServo.setPosition(1.0);

            //Turn 270 Degrees??????????????//

            encoderDrive(DRIVE_SPEED, 8, 8, 6.0);
        }
    }

    public void right(){
        while(opModeIsActive()) {
            encoderLift(Lift_Speed, 5, 2);
            //Turn Y Degrees??????????????//
            encoderDrive(DRIVE_SPEED, 3.1, 3.1, 2);

            //Turn -Y Degrees????????????//

            encoderDrive(DRIVE_SPEED, 3.1, 3.1, 2);
            robot.markerServo.setPosition(1.0);

            //Turn 315 Degrees??????????????//

            encoderDrive(DRIVE_SPEED, 8, 8, 6.0);
        }
    }

    public void defaultPath(){
        while(opModeIsActive()) {
            encoderLift(Lift_Speed, 5, 2);

            robot.leftFrontMotor.setPower(0);
            robot.leftBackMotor.setPower(0);
            robot.rightFrontMotor.setPower(0);
            robot.rightBackMotor.setPower(0);
        }
    }








//
//    public void jewel(double holdTime){
//        ElapsedTime holdTimer = new ElapsedTime(ElapsedTime.Resolution.SECONDS);
//        holdTimer.reset();
//        while(opModeIsActive() && holdTimer.time() < holdTime){
//            if (robot.colorSensor.blue() > robot.colorSensor.red() + 2) {
//                encoderDrive(DRIVE_SPEED, -3, -3, 2.0);
//                robot.armServo.setPosition(1.0);
//                encoderDrive(DRIVE_SPEED, 3, 3, 2.0);
//            } else if (robot.colorSensor.blue() < robot.colorSensor.red() - 2) {
//                encoderDrive(DRIVE_SPEED, 3, 3, 2.0);
//                robot.armServo.setPosition(1.0);
//                encoderDrive(DRIVE_SPEED, -3, -3, 2.0);
//            } else {
//                robot.leftBackMotor.setPower(0);
//                robot.leftFrontMotor.setPower(0);
//                robot.rightBackMotor.setPower(0);
//                robot.leftBackMotor.setPower(0);
//            }
//        }
//        robot.armServo.setPosition(1.0);
//    }
    String format(OpenGLMatrix transformationMatrix) {
        return (transformationMatrix != null) ? transformationMatrix.formatAsTransform() : "null";
    }
}