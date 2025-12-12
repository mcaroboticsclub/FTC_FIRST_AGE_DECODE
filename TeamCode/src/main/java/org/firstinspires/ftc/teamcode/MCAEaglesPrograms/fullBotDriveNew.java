// Import Required Files
package org.firstinspires.ftc.teamcode.MCAEaglesPrograms;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

//Limelight
import com.qualcomm.hardware.limelightvision.*;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;

import java.util.List;

// Send the code and the operating mode to the robot with descriptions.
@TeleOp(name = "Full Bot Drive", group = "MCA EAGLES PROGRAMS")
public class fullBotDriveNew extends LinearOpMode {

    // Define the speedfactor variable to be used to control the max percent of speed.
    double speedFactor = 1.0;

    double spindexer_cpr = 1425.1; //counts per revolution for spindexer motor, val from https://www.gobilda.com/5202-series-yellow-jacket-planetary-gear-motor-50-9-1-ratio-24mm-length-6mm-d-shaft-117-rpm-36mm-gearbox-3-3-5v-encoder/
    int spindexer120RotTicks = (int)((120.0/360.0)*spindexer_cpr);


    // Define all of the motors.
    DcMotor frontLeft = null;
    DcMotor frontRight = null;
    DcMotor backLeft = null;
    DcMotor backRight = null;
    DcMotor intake = null;
    DcMotor spindexer = null;

    Limelight3A limelight = null;

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

        //Setup Limelight
        limelight = hardwareMap.get(Limelight3A.class, "limelight");

        telemetry.setMsTransmissionInterval(11); //why 11?

        limelight.pipelineSwitch(0);
        limelight.start();


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
            // General idea for spindexer 120 deg rotation
            //Use x for continuous, y for discrete 120

            //This continuous code is pretty dumb but should be fine
//            if(gamepad2.x){
//                spindexer.setPower(0.7)
//            }else{
//                spindexer.setPower(0);
//            }
            if (gamepad2.y) {
                spindexer.setTargetPosition(spindexer.getCurrentPosition() + spindexer120RotTicks);
                spindexer.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                spindexer.setPower(0.7);
            }

            //Camera stuff
            LLStatus status = limelight.getStatus();
            telemetry.addData("Name", "%s",
                    status.getName());
            telemetry.addData("LL", "Temp: %.1fC, CPU: %.1f%%, FPS: %d",
                    status.getTemp(), status.getCpu(),(int)status.getFps());
            telemetry.addData("Pipeline", "Index: %d, Type: %s",
                    status.getPipelineIndex(), status.getPipelineType());


            if(gamepad2.x){
                List<FiducialResult> fiducials = result.getFiducialResults();
                int closestFidId = -1;
                double closestFidDist = 1000000;
                double xDegrees = -1;
                for (FiducialResult fiducial : fiducials) {
                    int id = fiducial.getFiducialId(); // The ID number of the fiducial
                    double x = detection.getTargetXDegrees(); // Where it is (left-right)
//                    double y = detection.getTargetYDegrees(); // Where it is (up-down)
                    double strafe_3d = fiducial.getRobotPoseTargetSpace().getY();
                    double straight_3d = fiducial.getRobotPoseTargetSpace().getX();
                    double distance = Math.sqrt(strafe_3d*strafe_3d + straight_3d*straight_3d);

                    if(closestFidDist > distance){
                        closestFidDist = distance;
                        closestFidId = id;
                        xDegrees = x;
                    }

                    telemetry.addData("Fiducial " + id, "is " + distance + " meters away at "+x+" deg");
                }
                telemetry.addData("Closest fiducial "+closestFidId+" is "+closestFidDist+"m away at "+xDegrees+" deg");

                //rotate to orientation based on xDegrees
            }

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
