// Import Required Files
package org.firstinspires.ftc.teamcode.MCAEaglesPrograms;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.*;

// Limelight
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.limelightvision.LLResultTypes.*;

// Send the code and the operating mode to the robot with descriptions.
@TeleOp(name = "Manual Random Buttons Everything No Limelight", group = "MCA EAGLES PROGRAMS")
public class driveManual extends LinearOpMode {

    // Define the speedfactor variable to be used to control the max percent of speed.
    double speedFactor = 1.0;

    // Autotrack.
    boolean autoTrackEnabled = false;

    // ===== FIX #2: Turret soft limits (encoder ticks) =====
    // Use your tested values
    int turretMinPosition = -200;   // left limit
    int turretMaxPosition = 900;    // right limit
    boolean enforceTurretLimits = true;

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

        // ===== FIX #2: Turret encoder setup ONCE (no resets in loop) =====
        turret.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turret.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        // Setup Limelight
        Limelight3A limelight = hardwareMap.get(Limelight3A.class, "limelight");

        telemetry.setMsTransmissionInterval(11); // why 11?

        limelight.pipelineSwitch(0);
        limelight.start();

        // Wait for the start button to be pushed before starting the run loop.
        waitForStart();

        while (opModeIsActive()) {

            frontLeft.setPower((-gamepad1.left_stick_y + gamepad1.left_stick_x + gamepad1.right_stick_x) * speedFactor);
            backLeft.setPower((-gamepad1.left_stick_y - gamepad1.left_stick_x + gamepad1.right_stick_x) * speedFactor);
            frontRight.setPower((-gamepad1.left_stick_y - gamepad1.left_stick_x - gamepad1.right_stick_x) * speedFactor);
            backRight.setPower((-gamepad1.left_stick_y + gamepad1.left_stick_x - gamepad1.right_stick_x) * speedFactor);

            intake.setPower((gamepad1.left_trigger - gamepad1.right_trigger) * speedFactor);

            if (gamepad2.x) {
                spindexer.setPower(0.5 * speedFactor);
            } else if (gamepad2.y) {
                spindexer.setPower(-0.5 * speedFactor); // TODO: Add set positions to move to.
            } else {
                spindexer.setPower(0);
            }

            flywheel.setPower(gamepad2.left_trigger - gamepad2.right_trigger);

            if (gamepad2.left_bumper) {
                continue; // TODO: Add shortcut to auto shoot here.
            }

            if (gamepad2.right_bumper) {
                continue; // TODO: Toggle automatic aim or manual shoot.
            }

            if (gamepad1.y) {
                speedFactor = 0.5;
            } else if (gamepad1.x) {
                speedFactor = 1.0;
            }

            // Camera telemetry
            LLStatus status = limelight.getStatus();
            telemetry.addData("Name", "%s", status.getName());
            telemetry.addData("LL", "Temp: %.1fC, CPU: %.1f%%, FPS: %d",
                    status.getTemp(), status.getCpu(), (int) status.getFps());
            telemetry.addData("Pipeline", "Index: %d, Type: %s",
                    status.getPipelineIndex(), status.getPipelineType());

            LLResult result = limelight.getLatestResult();

            // ===== MANUAL TURRET MODE =====
            if (autoTrackEnabled == false) {
                if (gamepad2.dpad_left) {
                    turret.setPower(-0.5 * speedFactor);
                } else if (gamepad2.dpad_right) {
                    turret.setPower(0.5 * speedFactor);
                } else {
                    turret.setPower(0);
                }
            }

            // ===== AUTO TRACK MODE =====
            if (autoTrackEnabled == true) {

                // ===== FIX #1: Null/invalid guard to prevent crashes =====
                if (result == null || !result.isValid()) {
                    turret.setPower(0);
                    telemetry.addData("Turret Auto", "No valid Limelight result");
                } else {

                    List<FiducialResult> fiducials = result.getFiducialResults();

                    if (fiducials == null || fiducials.isEmpty()) {
                        turret.setPower(0);
                        telemetry.addData("Turret Auto", "No AprilTags visible");
                    } else {

                        int closestFidId = -1;
                        double closestFidDist = 1000000;
                        double xDegrees = 0;

                        for (FiducialResult fiducial : fiducials) {
                            int id = fiducial.getFiducialId(); // The ID number of the fiducial
                            double x = fiducial.getTargetXDegrees(); // Where it is (left-right)

                            double strafe_3d = fiducial.getRobotPoseTargetSpace().getPosition().y;
                            double straight_3d = fiducial.getRobotPoseTargetSpace().getPosition().x;
                            double distance = Math.sqrt(strafe_3d * strafe_3d + straight_3d * straight_3d);

                            if (closestFidDist > distance) {
                                closestFidDist = distance;
                                closestFidId = id;
                                xDegrees = x;
                            }

                            telemetry.addData("Fiducial " + id, "is " + distance + " meters away at " + x + " deg");
                        }

                        telemetry.addData("Closest fiducial ", closestFidId + " is " + closestFidDist + "m away at " + xDegrees + " deg");

                        // rotate to orientation based on xDegrees
                        if (closestFidId != -1) { // Only rotate if we found a fiducial
                            double kP = 0.012; // Lower proportional gain for gentler response
                            double minPower = 0.1; // Lower minimum power for smoother start
                            double maxPower = 0.5; // Conservative max power limit
                            double deadband = 0.5; // Tighter deadband for precision

                            double turretPower = 0;

                            if (Math.abs(xDegrees) > deadband) {
                                // Calculate turret rotation power (negative to rotate toward target)
                                turretPower = -xDegrees * kP;

                                // Apply minimum power threshold
                                if (Math.abs(turretPower) < minPower) {
                                    turretPower = Math.signum(turretPower) * minPower;
                                }

                                // Clamp to conservative max power
                                turretPower = Math.max(-maxPower, Math.min(maxPower, turretPower));

                                // ===== FIX #2: Enforce limits safely (NO encoder resets) =====
                                if (enforceTurretLimits) {
                                    int currentPos = turret.getCurrentPosition();
                                    if (currentPos <= turretMinPosition && turretPower < 0) {
                                        turretPower = 0;
                                        telemetry.addData("LIMIT", "At LEFT limit!");
                                    } else if (currentPos >= turretMaxPosition && turretPower > 0) {
                                        turretPower = 0;
                                        telemetry.addData("LIMIT", "At RIGHT limit!");
                                    }
                                }

                                telemetry.addData("Turret auto-aiming", "Error: %.2f°, Power: %.2f", xDegrees, turretPower);
                            } else {
                                telemetry.addData("Turret", "Locked on target!");
                            }

                            // ===== FIX #3: Set turret power ONCE (no overwriting) =====
                            turret.setPower(turretPower * speedFactor);
                        } else {
                            turret.setPower(0);
                        }
                    }
                }
            }

            // Toggle auto-track on D-Pad Up
            if (gamepad2.dpad_up) {
                autoTrackEnabled = !autoTrackEnabled;
                sleep(200);
            }

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
            telemetry.addData("Vertical Pusher Position:", verticalPush.getPosition());
            telemetry.addData("Horizontal Pusher Position:", horizontalPush.getPosition());
            telemetry.addData("Speed Factor:", speedFactor);
            telemetry.addData("Automatic Tracking Enabled:", autoTrackEnabled);

            telemetry.update();
        }
    }
}
