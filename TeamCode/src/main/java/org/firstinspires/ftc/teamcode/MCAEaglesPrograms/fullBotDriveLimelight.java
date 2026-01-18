package org.firstinspires.ftc.teamcode.MCAEaglesPrograms;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.util.Range;

import java.util.List;

@TeleOp(name = "Full Bot Drive - Limelight", group = "MCA EAGLES Programs")
public class fullBotDriveLimelight extends LinearOpMode {

    // Drive scaling
    private double speedFactor = 1.0;

    // Hardware
    private DcMotor frontLeft, frontRight, backLeft, backRight;
    private DcMotor intakeDirect, intakeBoost, turret, flywheel;
    private Servo pusher, blocker;
    private Limelight3A limelight;

    // Turret tracking
    private boolean autoTrackingEnabled = false;
    private boolean lastXState = false;
    private final double turretKp = 0.02;
    private final double targetDeadband = 2.0;

    @Override
    public void runOpMode() {

        // ---- Hardware Map ----
        frontLeft = hardwareMap.dcMotor.get("Front_Left");
        frontRight = hardwareMap.dcMotor.get("Front_Right");
        backLeft = hardwareMap.dcMotor.get("Back_Left");
        backRight = hardwareMap.dcMotor.get("Back_Right");

        intakeDirect = hardwareMap.dcMotor.get("Intake_Direct");
        intakeBoost = hardwareMap.dcMotor.get("Intake_Boost");
        flywheel = hardwareMap.dcMotor.get("Flywheel");
        turret = hardwareMap.dcMotor.get("Turret");

        pusher = hardwareMap.servo.get("Pusher");
        blocker = hardwareMap.servo.get("Blocker");

        limelight = hardwareMap.get(Limelight3A.class, "limelight");

        // ---- Motor Configuration ----
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        intakeDirect.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intakeBoost.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        turret.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        backRight.setDirection(DcMotorSimple.Direction.REVERSE);
        flywheel.setDirection(DcMotorSimple.Direction.REVERSE);

        // ---- Limelight ----
        telemetry.setMsTransmissionInterval(11);
        limelight.pipelineSwitch(0);
        limelight.start();

        telemetry.addData("Status", "Initialized");
        telemetry.addData("Controls", "Gamepad2 X: Toggle Auto-Tracking");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            // ---- Toggle auto-tracking (debounced) ----
            boolean xPressed = gamepad2.x;
            if (xPressed && !lastXState) {
                autoTrackingEnabled = !autoTrackingEnabled;
            }
            lastXState = xPressed;

            // ---- Mecanum Drive ----
            double y = -gamepad1.left_stick_y;
            double x = gamepad1.left_stick_x;
            double rx = gamepad1.right_stick_x;

            frontLeft.setPower(Range.clip((y + x + rx) * speedFactor, -1.0, 1.0));
            backLeft.setPower(Range.clip((y - x + rx) * speedFactor, -1.0, 1.0));
            frontRight.setPower(Range.clip((y - x - rx) * speedFactor, -1.0, 1.0));
            backRight.setPower(Range.clip((y + x - rx) * speedFactor, -1.0, 1.0));

            // ---- Intake ----
            intakeDirect.setPower(-gamepad2.left_stick_y * speedFactor);
            intakeBoost.setPower(-intakeDirect.getPower());

            // ---- Turret ----
            double turretPower;
            if (autoTrackingEnabled) {
                turretPower = calculateTurretTracking();
                telemetry.addData("Turret Mode", "AUTO");
            } else {
                turretPower = -gamepad2.right_stick_x * 0.3;
                telemetry.addData("Turret Mode", "MANUAL");
            }
            turret.setPower(turretPower);

            // ---- Flywheel ----
            flywheel.setPower(gamepad2.left_trigger - gamepad2.right_trigger);

            // ---- Servos ----
            if (gamepad2.right_bumper) {
                blocker.setPosition(0.29);
            } else if (gamepad2.left_bumper) {
                blocker.setPosition(0.39);
            }

            if (gamepad2.dpad_down) {
                pusher.setPosition(0.2);
            } else if (gamepad2.dpad_up) {
                pusher.setPosition(0.0);
            }

            telemetry.update();
        }

        limelight.stop();
    }

    // ---- Limelight Turret Tracking ----
    private double calculateTurretTracking() {

        LLResult result = limelight.getLatestResult();

        if (result == null || !result.isValid()) {
            telemetry.addData("Limelight", "No valid result");
            return 0;
        }

        List<LLResultTypes.FiducialResult> fiducials = result.getFiducialResults();
        if (fiducials == null || fiducials.isEmpty()) {
            telemetry.addData("Limelight", "No fiducials");
            return 0;
        }

        int closestId = -1;
        double closestDist = Double.MAX_VALUE;
        double xDegrees = 0;

        for (LLResultTypes.FiducialResult fid : fiducials) {

            double strafe = fid.getRobotPoseTargetSpace().getPosition().y;
            double forward = fid.getRobotPoseTargetSpace().getPosition().x;
            double distance = Math.hypot(strafe, forward);

            if (distance < closestDist) {
                closestDist = distance;
                closestId = fid.getFiducialId();
                xDegrees = fid.getTargetXDegrees();
            }

            telemetry.addData(
                    "Fiducial " + fid.getFiducialId(),
                    String.format("%.2fm @ %.1f°", distance, fid.getTargetXDegrees())
            );
        }

        telemetry.addData(
                "Closest Target",
                String.format("ID %d | %.2fm | %.1f°", closestId, closestDist, xDegrees)
        );

        if (Math.abs(xDegrees) <= targetDeadband) {
            telemetry.addData("Turret", "LOCKED");
            return 0;
        }

        double correction = Range.clip(-xDegrees * turretKp, -0.3, 0.3);
        telemetry.addData("Turret Correction", correction);
        return correction;
    }
}
