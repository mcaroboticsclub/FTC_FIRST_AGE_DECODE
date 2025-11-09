// Import Required Files
package org.firstinspires.ftc.teamcode.MCAEaglesPrograms;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

// Send the code and the operating mode to the robot with descriptions.
@TeleOp(name = "Manual Random Buttons Everything No Limelight", group = "MCA EAGLES PROGRAMS")
public class driveManual extends LinearOpMode {

    // Define the speedfactor variable to be used to control the max percent of speed.
    double speedFactor = 1.0;

    // Define all of the motors.
    DcMotor frontLeft = null;
    DcMotor frontRight = null;
    DcMotor backLeft = null;
    DcMotor backRight = null;
    DcMotor intake = null;
    DcMotor spindexer = null;
    DcMotor turret = null;
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
        intake = hardwareMap.dcMotor.get("Intake");
        flywheel = hardwareMap.dcMotor.get("Flywheel");
        spindexer = hardwareMap.dcMotor.get("Spindexer");
        turret = hardwareMap.dcMotor.get("Turret");

        verticalPush = hardwareMap.servo.get("Vertical");
        horizontalPush = hardwareMap.servo.get("Horizontal");

        // Set all of the motors to brake when not powered.
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        spindexer.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        turret.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Reverse the direction of one side of the robot's motors.
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);

        // Wait for the start button to be pushed before starting the run loop.
        waitForStart();

        while (opModeIsActive()) {

            frontLeft.setPower((-gamepad1.left_stick_y + gamepad1.left_stick_x + gamepad1.right_stick_x) * speedFactor);
            backLeft.setPower((-gamepad1.left_stick_y - gamepad1.left_stick_x + gamepad1.right_stick_x) * speedFactor);
            frontRight.setPower((-gamepad1.left_stick_y - gamepad1.left_stick_x - gamepad1.right_stick_x) * speedFactor);
            backRight.setPower((-gamepad1.left_stick_y + gamepad1.left_stick_x - gamepad1.right_stick_x) * speedFactor);

            intake.setPower(gamepad1.left_trigger - gamepad1.right_trigger);

            if (gamepad2.x) {
                spindexer.setPower(0.5);
            } else if (gamepad2.y) {
                spindexer.setPower(-0.5);
            } else {
                spindexer.setPower(0);
            }

            if (gamepad2.dpad_left) {
                turret.setPower(-0.5);
            } else if(gamepad2.dpad_right) {
                turret.setPower(0.5);
            } else {
                turret.setPower(0);
            }

            flywheel.setPower(gamepad2.left_trigger - gamepad2.right_trigger);

            if (gamepad2.dpad_down) {
                verticalPush.setPosition(0.47);
            }

            else if (gamepad2.dpad_up)
                verticalPush.setPosition(0.53);

            telemetry.addData("Front Left Motor Power:", frontLeft.getPower());
            telemetry.addData("Front Left Motor Position:", frontLeft.getCurrentPosition());
            telemetry.addData("Front Right Motor Power:", frontRight.getPower());
            telemetry.addData("Front Right Motor Position:", frontRight.getCurrentPosition());
            telemetry.addData("Back Left Motor Power:", backLeft.getPower());
            telemetry.addData("Back Left Motor Position:", backLeft.getCurrentPosition());
            telemetry.addData("Back Right Motor Power:", backRight.getPower());
            telemetry.addData("Back Right Motor Position:", backRight.getCurrentPosition());
            telemetry.addData("Intake Motor Power:", intake.getPower());
            telemetry.addData("Intake Motor Position Now:", intake.getCurrentPosition());
            telemetry.addData("Intake Motor Position Target:", intake.getTargetPosition());
            telemetry.addData("Spindexer Motor Power:", spindexer.getPower());
            telemetry.addData("Spindexer Motor Position Now:", spindexer.getCurrentPosition());
            telemetry.addData("Spindexer Motor Position Target:", spindexer.getTargetPosition());
            telemetry.addData("Turret Motor Power:", turret.getPower());
            telemetry.addData("Turret Motor Position Now:", turret.getCurrentPosition());
            telemetry.addData("Turret Motor Position Target:", turret.getCurrentPosition());
            telemetry.addData("Flywheel Motor Power:", flywheel.getPower());
            telemetry.addData("Flywheel Motor Position Now:", flywheel.getCurrentPosition());
            telemetry.addData("Flywheel Motor Position Target:", flywheel.getTargetPosition());
            telemetry.addData("Speed Factor:", speedFactor);
            
            // Update the telemetry.
            telemetry.update();
        }
    }
}