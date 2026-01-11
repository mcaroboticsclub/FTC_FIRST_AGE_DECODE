package org.firstinspires.ftc.teamcode.MCAEaglesPrograms;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import java.util.List;

@TeleOp(name = "Full Bot Drive with Limelight", group = "MCA EAGLES Programs")
public class fullBotDriveLimelight extends LinearOpMode {
    double speedFactor = 1.0;
    
    DcMotor frontLeft = null;
    DcMotor frontRight = null;
    DcMotor backLeft = null;
    DcMotor backRight = null;
    DcMotor intakeDirect = null;
    DcMotor intakeBoost = null;
    DcMotor turret = null;
    DcMotor flywheel = null;
    Servo pusher = null;
    Servo blocker = null;
    Limelight3A limelight = null;
    
    private boolean autoTrackingEnabled = false;
    private double turretKp = 0.02;
    private double targetDeadband = 2.0;
    
    @Override
    public void runOpMode() throws InterruptedException {
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
        
//        limelight = hardwareMap.get(Limelight3A.class, "limelight");
//        limelight.setIpAddress("172.29.0.24");

        limelight = hardwareMap.get(Limelight3A.class, "Ethernet Device"); // TEST
        
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
        
        telemetry.setMsTransmissionInterval(11);
        limelight.pipelineSwitch(0);
        limelight.start();
        
        telemetry.addData("Status", "Initialized");
        telemetry.addData("Controls", "Gamepad2 X: Toggle Auto-Tracking");
        telemetry.update();
        
        waitForStart();
        
        while (opModeIsActive()) {
            if (gamepad2.x) {
                autoTrackingEnabled = !autoTrackingEnabled;
                sleep(200);
            }
            
            frontLeft.setPower((-gamepad1.left_stick_y + gamepad1.left_stick_x + gamepad1.right_stick_x) * speedFactor);
            backLeft.setPower((-gamepad1.left_stick_y - gamepad1.left_stick_x + gamepad1.right_stick_x) * speedFactor);
            frontRight.setPower((-gamepad1.left_stick_y - gamepad1.left_stick_x - gamepad1.right_stick_x) * speedFactor);
            backRight.setPower((-gamepad1.left_stick_y + gamepad1.left_stick_x - gamepad1.right_stick_x) * speedFactor);
            
            intakeDirect.setPower(-gamepad2.left_stick_y * speedFactor);
            intakeBoost.setPower(-intakeDirect.getPower());
            
            double turretPower = 0;
            
            if (autoTrackingEnabled) {
                turretPower = calculateTurretTracking();
                telemetry.addData("Turret Mode", "AUTO-TRACKING");
            } else {
                turretPower = -gamepad2.right_stick_x * 0.3;
                telemetry.addData("Turret Mode", "MANUAL");
            }
            
            turret.setPower(turretPower);
            
            flywheel.setPower(gamepad2.left_trigger - gamepad2.right_trigger);
            
            if (gamepad2.right_bumper) { // SERVO POSITIONS WRONG
                blocker.setPosition(0.29);
            } else if (gamepad2.left_bumper) {
                blocker.setPosition(0.39);
            }
            
            if (gamepad2.dpad_down) {
                pusher.setPosition(0.2);
            } else if (gamepad2.dpad_up) {
                pusher.setPosition(0);
            }
            
            telemetry.update();
        }
        
        limelight.stop();
    }
    
    private double calculateTurretTracking() {
        LLResult result = limelight.getLatestResult();
        
        if (result != null && result.isValid()) {
            List<LLResultTypes.FiducialResult> fiducials = result.getFiducialResults();
            
            if (fiducials != null && !fiducials.isEmpty()) {
                int closestFidId = -1;
                double closestFidDist = 1000000;
                double xDegrees = 0;
                
                for (LLResultTypes.FiducialResult fiducial : fiducials) {
                    int id = fiducial.getFiducialId();
                    double x = fiducial.getTargetXDegrees();
                    
                    double strafe_3d = fiducial.getRobotPoseTargetSpace().getPosition().y;
                    double straight_3d = fiducial.getRobotPoseTargetSpace().getPosition().x;
                    double distance = Math.sqrt(strafe_3d * strafe_3d + straight_3d * straight_3d);
                    
                    if (closestFidDist > distance) {
                        closestFidDist = distance;
                        closestFidId = id;
                        xDegrees = x;
                    }
                    
                    telemetry.addData("Fiducial " + id, String.format("%.2fm away at %.1f°", distance, x));
                }
                
                telemetry.addData("Closest Target", String.format("ID %d: %.2fm at %.1f°", 
                    closestFidId, closestFidDist, xDegrees));
                
                if (Math.abs(xDegrees) > targetDeadband) {
                    double turretPower = -xDegrees * turretKp;
                    turretPower = Math.max(-0.3, Math.min(0.3, turretPower));
                    telemetry.addData("Turret Correction", String.format("%.3f", turretPower));
                    return turretPower;
                } else {
                    telemetry.addData("Status", "LOCKED ON TARGET");
                    return 0;
                }
            } else {
                telemetry.addData("Status", "No fiducials detected");
                return 0;
            }
        } else {
            telemetry.addData("Status", "No valid Limelight data");
            return 0;
        }
    }
}
