// Import Required Files
package org.firstinspires.ftc.teamcode.MCAEaglesPrograms;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

//Limelight
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;

import java.util.List;

// Send the code and the operating mode to the robot with descriptions.
@TeleOp(name = "Manual Turret Control", group = "MCA EAGLES PROGRAMS")
public class fullBotDrive extends LinearOpMode {

    // Define the speedfactor variable to be used to control the max percent of speed.
    double speedFactor = 1.0;

    double spindexer_cpr = 1425.1; //counts per revolution for spindexer motor
    int spindexer120RotTicks = (int) ((120.0 / 360.0) * spindexer_cpr);

    // Define all of the motors.
    DcMotor frontLeft = null;
    DcMotor frontRight = null;
    DcMotor backLeft = null;
    DcMotor backRight = null;
    DcMotor intake = null;
    DcMotor spindexer = null;
    DcMotor turret = null;
    DcMotor flywheel = null;

    // TURRET POSITION LIMITS (adjust after testing)
    int turretMinPosition = -1000;   // LEFT limit
    int turretMaxPosition = 1000;    // RIGHT limit
    boolean enforceLimits = true;    // Set false for calibration

    Limelight3A limelight = null;

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

        // Setup turret
        turret.setDirection(DcMotorSimple.Direction.FORWARD); // Change to REVERSE if it aims wrong way
        turret.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turret.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        // Reset spindexer encoder
        spindexer.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //Setup Limelight (optional - for camera viewing only)
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        telemetry.setMsTransmissionInterval(11);
        limelight.pipelineSwitch(0);
        limelight.start();

        telemetry.addData("🎮 MANUAL MODE", "Ready");
        telemetry.addData("Turret Control", "Gamepad2 Bumpers");
        telemetry.addData("Limits", enforceLimits ? "ENABLED" : "DISABLED");
        telemetry.update();

        // Wait for the start button to be pushed before starting the run loop.
        waitForStart();

        while (opModeIsActive()) {

            // ========== DRIVETRAIN CONTROL ==========
            frontLeft.setPower((-gamepad1.left_stick_y + gamepad1.left_stick_x + gamepad1.right_stick_x) * speedFactor);
            backLeft.setPower((-gamepad1.left_stick_y - gamepad1.left_stick_x + gamepad1.right_stick_x) * speedFactor);
            frontRight.setPower((-gamepad1.left_stick_y - gamepad1.left_stick_x - gamepad1.right_stick_x) * speedFactor);
            backRight.setPower((-gamepad1.left_stick_y + gamepad1.left_stick_x - gamepad1.right_stick_x) * speedFactor);

            // ========== INTAKE CONTROL ==========
            intake.setPower(gamepad2.left_trigger - gamepad2.right_trigger);

            // ========== SPINDEXER CONTROL ==========
            if (gamepad2.y) {
                spindexer.setTargetPosition(spindexer.getCurrentPosition() + spindexer120RotTicks);
                spindexer.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                spindexer.setPower(0.7);
            }

            // ========== TURRET MANUAL CONTROL (BUMPERS) ==========

            double turretPower = 0;

            // Left bumper = turn LEFT, Right bumper = turn RIGHT
            if (gamepad2.left_bumper) {
                turretPower = -0.5;  // Turn left at 50% speed
            } else if (gamepad2.right_bumper) {
                turretPower = 0.5;   // Turn right at 50% speed
            }

            // ===== ENFORCE POSITION LIMITS =====
            if (enforceLimits) {
                int currentPos = turret.getCurrentPosition();

                // Check if at limits
                if (currentPos <= turretMinPosition && turretPower < 0) {
                    // At LEFT limit, trying to go MORE left → STOP
                    turretPower = 0;
                    telemetry.addData("⚠️ LIMIT", "At LEFT limit!");
                } else if (currentPos >= turretMaxPosition && turretPower > 0) {
                    // At RIGHT limit, trying to go MORE right → STOP
                    turretPower = 0;
                    telemetry.addData("⚠️ LIMIT", "At RIGHT limit!");
                }
            }

            turret.setPower(turretPower);

            // Display turret controls
            telemetry.addData("🎮 TURRET CONTROL", "Bumpers");
            telemetry.addData("Left Bumper", "Turn LEFT");
            telemetry.addData("Right Bumper", "Turn RIGHT");

            // ========== CAMERA STATUS (Optional - just for viewing) ==========
            LLStatus status = limelight.getStatus();
            telemetry.addData("Camera", "%s", status.getName());
            telemetry.addData("LL Stats", "Temp: %.1fC, CPU: %.1f%%, FPS: %d",
                    status.getTemp(), status.getCpu(), (int) status.getFps());

            // Optional: Show if camera sees any AprilTags
            LLResult result = limelight.getLatestResult();
            if (result != null && result.isValid()) {
                List<LLResultTypes.FiducialResult> fiducials = result.getFiducialResults();
                if (!fiducials.isEmpty()) {
                    telemetry.addData("AprilTags Visible", fiducials.size());
                    for (LLResultTypes.FiducialResult fiducial : fiducials) {
                        telemetry.addData("  ID " + fiducial.getFiducialId(),
                                "%.1f° @ %.2fm",
                                fiducial.getTargetXDegrees(),
                                Math.sqrt(
                                        Math.pow(fiducial.getRobotPoseTargetSpace().getPosition().x, 2) +
                                                Math.pow(fiducial.getRobotPoseTargetSpace().getPosition().y, 2)
                                ));
                    }
                }
            }

            // ========== MOTOR TELEMETRY ==========
            telemetry.addData("Front Left Motor Power:", frontLeft.getPower());
            telemetry.addData("Front Right Motor Power:", frontRight.getPower());
            telemetry.addData("Back Left Motor Power:", backLeft.getPower());
            telemetry.addData("Back Right Motor Power:", backRight.getPower());
            telemetry.addData("Intake Motor Power:", intake.getPower());
            telemetry.addData("Spindexer Motor Power:", spindexer.getPower());
            telemetry.addData("Turret Motor Power:", turret.getPower());
            telemetry.addData("Turret Position:", turret.getCurrentPosition());
            telemetry.addData("Speed Factor:", speedFactor);

            // Position limit info
            telemetry.addData("Turret Limits:", enforceLimits ? "ENABLED" : "DISABLED");
            telemetry.addData("Limit Range:", turretMinPosition + " to " + turretMaxPosition);

            // Update the telemetry.
            telemetry.update();
        }

        // Stop limelight when program ends
        limelight.stop();
    }
}