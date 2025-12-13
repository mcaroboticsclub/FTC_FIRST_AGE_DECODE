// Import Required Files
package org.firstinspires.ftc.teamcode.MCAEaglesPrograms;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.*;

//Limelight
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.limelightvision.LLResultTypes.*;

@TeleOp(name = "Full Bot Drive", group = "MCA EAGLES PROGRAMS")
public class driveManual extends LinearOpMode {

    // Drive
    double speedFactor = 1.0;

    // Turret limits (ticks). 0 = turret facing forward (ONLY true if you reset encoder while forward).
    static final int TURRET_MIN_TICKS = -385;
    static final int TURRET_MAX_TICKS =  632;

    // Autotrack + shooter
    boolean autoTrackEnabled = false;
    boolean triggerShoot = false;
    boolean prevDpadDown = false;
    boolean prevDpadUp = false;

    // Spindexer slots
    static final int SPIN_SLOT_0 = 0;
    static final int SPIN_SLOT_1 = 510;
    static final int SPIN_SLOT_2 = 955;
    static final int TOTAL_SPIN_SLOTS = 3;
    int currentSpindexerSlot = 0;
    boolean spindexerButtonLast = false;

    // Motors / servos
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

        // Hardware map
        frontLeft  = hardwareMap.dcMotor.get("Front_Left");
        frontRight = hardwareMap.dcMotor.get("Front_Right");
        backLeft   = hardwareMap.dcMotor.get("Back_Left");
        backRight  = hardwareMap.dcMotor.get("Back_Right");
        intake     = hardwareMap.dcMotor.get("Intake");
        flywheel   = hardwareMap.dcMotor.get("Flywheel");
        spindexer  = hardwareMap.dcMotor.get("Spindexer");
        turret     = hardwareMap.dcMotor.get("Turret");

        verticalPush   = hardwareMap.servo.get("Vertical");
        horizontalPush = hardwareMap.servo.get("Horizontal");

        // Initial servo positions (your "rest" positions)
        horizontalPush.setPosition(0.4);
        verticalPush.setPosition(0.1);

        // Brake
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        spindexer.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        turret.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Reverse one side
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);

        // ----------------------------
        // Spindexer encoder setup (Fix B)
        // MUST set target BEFORE RUN_TO_POSITION or you'll get TargetPositionNotSetException.
        // ----------------------------
        spindexer.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        spindexer.setTargetPosition(SPIN_SLOT_0);              // <-- FIX: set target first
        spindexer.setMode(DcMotor.RunMode.RUN_TO_POSITION);    // now it's safe
        spindexer.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        spindexer.setPower(0);                                 // don't move until start

        // Turret encoder setup
        // IMPORTANT: This makes "0 = forward" ONLY if the turret is physically pointing forward right now.
        turret.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turret.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        turret.setPower(0);

        // Limelight
        Limelight3A limelight = hardwareMap.get(Limelight3A.class, "limelight");
        telemetry.setMsTransmissionInterval(11);
        limelight.pipelineSwitch(0);
        limelight.start();

        waitForStart();

        // Start at slot 0 (this will apply power and ensure it goes where you want)
        goToSpindexerSlot(0);

        while (opModeIsActive()) {

            // Drive
            frontLeft.setPower((-gamepad1.left_stick_y + gamepad1.left_stick_x + gamepad1.right_stick_x) * speedFactor);
            backLeft.setPower((-gamepad1.left_stick_y - gamepad1.left_stick_x + gamepad1.right_stick_x) * speedFactor);
            frontRight.setPower((-gamepad1.left_stick_y - gamepad1.left_stick_x - gamepad1.right_stick_x) * speedFactor);
            backRight.setPower((-gamepad1.left_stick_y + gamepad1.left_stick_x - gamepad1.right_stick_x) * speedFactor);

            // Intake
            intake.setPower((gamepad1.left_trigger - gamepad1.right_trigger) * speedFactor);

            // Spindexer: press X to advance slot (rising-edge)
            boolean spindexerButtonNow = gamepad2.x;
            if (spindexerButtonNow && !spindexerButtonLast) {
                int nextSlot = (currentSpindexerSlot + 1) % TOTAL_SPIN_SLOTS;
                goToSpindexerSlot(nextSlot);
            }
            spindexerButtonLast = spindexerButtonNow;

            // Optional: stop power once it reaches target (won't actively hold)
            if (!spindexer.isBusy()) {
                spindexer.setPower(0);
            }

            // Flywheel
            flywheel.setPower(gamepad2.left_trigger - gamepad2.right_trigger);

            // Toggle autotrack (dpad_up rising-edge)
            boolean dpadUp = gamepad2.dpad_up;
            if (dpadUp && !prevDpadUp) {
                autoTrackEnabled = !autoTrackEnabled;
            }
            prevDpadUp = dpadUp;

            // Shooter: dpad_down (press once) runs the full sequence once
            boolean dpadDown = gamepad2.dpad_down;
            if (dpadDown && !prevDpadDown && !triggerShoot) {
                triggerShoot = true;
            }
            prevDpadDown = dpadDown;

            if (triggerShoot) {
                // 1) push horizontally
                horizontalPush.setPosition(0.85);
                sleep(500);

                // 2) then push vertically
                verticalPush.setPosition(1.0);
                sleep(1200);

                // extra horizontal push (your choice)
                horizontalPush.setPosition(1.0);
                sleep(700);

                // reset
                verticalPush.setPosition(0.1);
                horizontalPush.setPosition(0.4);

                triggerShoot = false;
            }

            // Turret control
            int turretPos = turret.getCurrentPosition();

            if (!autoTrackEnabled) {
                // Manual turret (dpad_left/right) with encoder limits
                double turretCmd = 0;
                if (gamepad2.dpad_left)  turretCmd = -0.5 * speedFactor;
                else if (gamepad2.dpad_right) turretCmd =  0.5 * speedFactor;

                // Block motion into the limits
                if ((turretCmd > 0 && turretPos >= TURRET_MAX_TICKS) ||
                    (turretCmd < 0 && turretPos <= TURRET_MIN_TICKS)) {
                    turretCmd = 0;
                }

                turret.setPower(turretCmd);

            } else {
                // Auto-aim (open-loop power) with encoder limits
                LLResult result = limelight.getLatestResult();
                double turretPower = 0;

                if (result != null) {
                    List<FiducialResult> fiducials = result.getFiducialResults();

                    int closestFidId = -1;
                    double closestFidDist = 1e9;
                    double xDegrees = 0;

                    if (fiducials != null) {
                        for (FiducialResult fiducial : fiducials) {
                            int id = fiducial.getFiducialId();
                            double x = fiducial.getTargetXDegrees();

                            double strafe_3d = fiducial.getRobotPoseTargetSpace().getPosition().y;
                            double straight_3d = fiducial.getRobotPoseTargetSpace().getPosition().x;
                            double distance = Math.sqrt(strafe_3d * strafe_3d + straight_3d * straight_3d);

                            if (distance < closestFidDist) {
                                closestFidDist = distance;
                                closestFidId = id;
                                xDegrees = x;
                            }

                            telemetry.addData("Fiducial " + id, distance + "m @ " + x + " deg");
                        }
                    }

                    telemetry.addData("Closest fiducial", closestFidId + " @ " + closestFidDist + "m, x=" + xDegrees);

                    // Convert xDegrees error -> turret power
                    if (closestFidId != -1) {
                        double kP = 0.012;
                        double minPower = 0.1;
                        double maxPower = 0.5;
                        double deadband = 0.5;

                        if (Math.abs(xDegrees) > deadband) {
                            turretPower = -xDegrees * kP;

                            if (Math.abs(turretPower) < minPower) {
                                turretPower = Math.signum(turretPower) * minPower;
                            }

                            turretPower = Math.max(-maxPower, Math.min(maxPower, turretPower));
                            turretPower *= speedFactor;
                        } else {
                            turretPower = 0;
                        }
                    }
                }

                // Enforce encoder limits (block motion into stops)
                turretPos = turret.getCurrentPosition();
                if ((turretPower > 0 && turretPos >= TURRET_MAX_TICKS) ||
                    (turretPower < 0 && turretPos <= TURRET_MIN_TICKS)) {
                    turretPower = 0;
                }

                turret.setPower(turretPower);
            }

            // Limelight telemetry
            LLStatus status = limelight.getStatus();
            telemetry.addData("LL Name", status.getName());
            telemetry.addData("LL", "Temp: %.1fC, CPU: %.1f%%, FPS: %d",
                    status.getTemp(), status.getCpu(), (int) status.getFps());
            telemetry.addData("Pipeline", "Index: %d, Type: %s",
                    status.getPipelineIndex(), status.getPipelineType());

            // Telemetry
            telemetry.addData("Turret Now", turret.getCurrentPosition());
            telemetry.addData("Turret Limits", "[" + TURRET_MIN_TICKS + ", " + TURRET_MAX_TICKS + "]");
            telemetry.addData("AutoTrack", autoTrackEnabled);

            telemetry.addData("Spindexer Slot", currentSpindexerSlot);
            telemetry.addData("Spindexer Now", spindexer.getCurrentPosition());
            telemetry.addData("Spindexer Target", spindexer.getTargetPosition());
            telemetry.addData("Spindexer Busy", spindexer.isBusy());

            telemetry.addData("Vertical Servo", verticalPush.getPosition());
            telemetry.addData("Horizontal Servo", horizontalPush.getPosition());

            telemetry.addData("Speed Factor", speedFactor);

            telemetry.update();
        }
    }

    // Spindexer slot function
    void goToSpindexerSlot(int slot) {
        currentSpindexerSlot = slot;

        int target;
        switch (slot) {
            case 0: target = SPIN_SLOT_0; break;
            case 1: target = SPIN_SLOT_1; break;
            case 2: target = SPIN_SLOT_2; break;
            default: target = SPIN_SLOT_0;
        }

        spindexer.setTargetPosition(target);
        spindexer.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        spindexer.setPower(0.5);
    }
}
