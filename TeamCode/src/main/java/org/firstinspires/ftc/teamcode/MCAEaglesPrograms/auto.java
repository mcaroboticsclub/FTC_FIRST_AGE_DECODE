// Import Required Files
package org.firstinspires.ftc.teamcode.MCAEaglesPrograms;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import dev.nextftc.control.ControlSystem;

// Send the code and the operating mode to the robot with descriptions.
@Autonomous(name = "Move Back", group = "MCA EAGLES PROGRAMS")
public class auto extends LinearOpMode {

    // Define the speedfactor variable to be used to control the max percent of speed.
    double speedFactor = 1.0;

    // Define all of the motors.
    DcMotor frontLeft = null;
    DcMotor frontRight = null;
    DcMotor backLeft = null;
    DcMotor backRight = null;
    DcMotor flywheel = null;
    Servo verticalPush = null;
    Servo horizontalPush = null;

    @Override
    public void runOpMode() throws InterruptedException {

        // Hardware map all of the motors.
        frontLeft = hardwareMap.dcMotor.get("Front_Left");
        frontRight = hardwareMap.dcMotor.get("Front_Right");
        backLeft = hardwareMap.dcMotor.get("Back_Left");
        backRight = hardwareMap.dcMotor.get("Back_Right");
        flywheel = hardwareMap.dcMotor.get("Flywheel");
        verticalPush = hardwareMap.servo.get("Vertical");
        horizontalPush = hardwareMap.servo.get("Horizontal");

        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        // Reverse the direction of one side of the robot's motors.
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);

        // Wait for the start button to be pushed before starting the run loop.
        waitForStart();

        while (opModeIsActive()) {



            double CPR = 537.7;
            double actDist = 10; //in
            double circumferenceIn = Math.PI * 4.09449;
            double revolutions = actDist/circumferenceIn;
            double ticks = CPR * revolutions;

            ticks = 300;

            int oldFrontLeftPos = (int) frontLeft.getCurrentPosition();
            int oldBackRightPos = (int)backLeft.getCurrentPosition();
            int oldFrontRightPos = (int)frontRight.getCurrentPosition();
            int oldBackLeftPos = (int)backRight.getCurrentPosition();

            frontLeft.setTargetPosition(oldFrontLeftPos - (int) ticks);
            frontRight.setTargetPosition(oldFrontRightPos - (int) ticks);
            backLeft.setTargetPosition(oldBackLeftPos - (int) ticks);
            backRight.setTargetPosition(oldBackRightPos - (int) ticks);

//            frontLeft.setTargetPosition((int) ticks);
//            frontRight.setTargetPosition( (int) ticks);
//            backLeft.setTargetPosition( (int) ticks);
//            backRight.setTargetPosition( (int) ticks);

            frontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            frontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            backLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            backRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            frontLeft.setPower(0.5);
            frontRight.setPower(0.5);
            backLeft.setPower(0.5);
            backRight.setPower(0.5);


            sleep(1800);

            frontLeft.setPower(0);
            frontRight.setPower(0);
            backLeft.setPower(0);
            backRight.setPower(0);

            flywheel.setPower(-0.8);
            // 1) push horizontally
            horizontalPush.setPosition(0.85);
            telemetry.update();
            sleep(500);

            // 2) then push vertically
            verticalPush.setPosition(1);
            sleep(1200);
            telemetry.update();
            horizontalPush.setPosition(1);
            sleep(700);

            // reset
            verticalPush.setPosition(0.11);
            horizontalPush.setPosition(0.43);


//            frontLeft.setPower(1);
//            frontRight.setPower(-1);
//            backLeft.setPower(-1);
//            backRight.setPower(1);
//
//            sleep(2500);
//
//            frontLeft.setPower(0);
//            frontRight.setPower(0);
//            backLeft.setPower(0);
//            backRight.setPower(0);

        }
    }
}