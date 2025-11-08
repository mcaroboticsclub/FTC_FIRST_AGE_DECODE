// Import Required Files
package org.firstinspires.ftc.teamcode.MCAEaglesPrograms;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

// Send the code and the operating mode to the robot with descriptions.
@TeleOp(name = "Full Bot Drive", group = "MCA EAGLES PROGRAMS")
public class fullBotDrive extends LinearOpMode {

    // Define the speedfactor variable to be used to control the max percent of speed.
    double speedFactor = 1.0;

    // Define all of the motors.
    DcMotor frontLeft = null;
    DcMotor frontRight = null;
    DcMotor backLeft = null;
    DcMotor backRight = null;
    DcMotor intake = null;
    DcMotor spindexer = null;

    @Override
    public void runOpMode() throws InterruptedException {

        // Hardware map all of the motors.
        frontLeft = hardwareMap.dcMotor.get("Front_Left");
        frontRight = hardwareMap.dcMotor.get("Front_Right");
        backLeft = hardwareMap.dcMotor.get("Back_Left");
        backRight = hardwareMap.dcMotor.get("Back_Right");
        intake = hardwareMap.dcMotor.get("Intake");
        spindexer = hardwareMap.dcMotor.get("Spindexer");

        // Set all of the motors to brake when not powered.
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE); // TODO: Potentially set to not brake or another mode.
        spindexer.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Reverse reverse the direction of one side of the robot's motors.
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);

        // Reset spindexer encoder
        spindexer.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        double spindexer_cpr = 12345; //counts per revolution for spindexer motor, idk what the val is
        int spindexer120RotTicks = (int)((120.0/360.0)*spindexer_cpr);

        // General idea for spindexer 120 deg rotation
        if (gamepad2.y) {
            spindexer.setTargetPosition(spindexer.getCurrentPosition() + spindexer120RotTicks);
            spindexer.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            spindexer.setPower(0.7);
        }


        // Wait for the start button to be pushed before starting the run loop.
        waitForStart();

        while (opModeIsActive()) {

            // TODO: Power management.

            // Set drivetrain to move based off of the inputs of the left and right sticks of gamepad 1.
            frontLeft.setPower((-gamepad1.left_stick_y + gamepad1.left_stick_x + gamepad1.right_stick_x) * speedFactor);
            backLeft.setPower((-gamepad1.left_stick_y - gamepad1.left_stick_x + gamepad1.right_stick_x) * speedFactor);
            frontRight.setPower((-gamepad1.left_stick_y - gamepad1.left_stick_x - gamepad1.right_stick_x) * speedFactor);
            backRight.setPower((-gamepad1.left_stick_y + gamepad1.left_stick_x - gamepad1.right_stick_x) * speedFactor);

            // Set intake controls to move based off of the inputs of the left and right triggers of gamepad2.
            intake.setPower(gamepad2.left_trigger - gamepad2.right_trigger);

            // Set spindexer controls to move based off of the inputs of the x and y buttons of gamepad 2.


            // Add telemetry data for all parts of the robot, for all motors and servos, for power and position, and for the current speed factor.
            telemetry.addData("Front Left Motor Power: ", frontLeft.getPower());
            telemetry.addData("Front Left Motor Position:", frontLeft.getCurrentPosition());
            telemetry.addData("Front Right Motor Power:", frontRight.getPower());
            telemetry.addData("Front Right Motor Position:", frontRight.getCurrentPosition());
            telemetry.addData("Back Left Motor Power:", backLeft.getPower());
            telemetry.addData("Back Left Motor Position:", backLeft.getCurrentPosition());
            telemetry.addData("Back Right Motor Power:", backRight.getPower());
            telemetry.addData("Back Right Motor Position:", backRight.getCurrentPosition());
            telemetry.addData("Intake Motor Power:", intake.getPower());
            telemetry.addData("Intake Motor Position:", intake.getCurrentPosition());
            telemetry.addData("Spindexer Motor Power: ", spindexer.getPower());
            telemetry.addData("Spindexer Motor Positon:", spindexer.getCurrentPosition());
            telemetry.addData("Speed Factor", speedFactor);

            // Update the telemetry.
            telemetry.update();
        }

        // Update the telemetry.
        telemetry.update(); // TODO: Remove one of these telemetry updates. (?)
    }
}