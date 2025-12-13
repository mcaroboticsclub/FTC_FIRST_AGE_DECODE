//package org.firstinspires.ftc.teamcode.MCAEaglesPrograms;
//
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.DcMotor;
//
//@TeleOp(name = "Spindexer Step Test (+400 per X)", group = "MCA EAGLES PROGRAMS")
//public class SpindexerStepTest extends LinearOpMode {
//
//
//    private int targetTicks = 0;
//    private boolean prevX = false;
//
//    private static final int STEP_TICKS = 400;
//    private static final double SPIN_POWER = 0.6;
//
//    @Override
//    public void runOpMode() {
//
//        spindexer = hardwareMap.dcMotor.get("Spindexer");
//        spindexer.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//
//        // Start at 0 ticks
//        spindexer.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//        targetTicks = 0;
//
//
//        waitForStart();
//
//        while (opModeIsActive()) {
//
//
//
////            boolean xNow = gamepad2.x;
////
////            // Rising-edge: triggers once per press
////            if (xNow && !prevX) {
////                targetTicks += STEP_TICKS;
////                spindexer.setTargetPosition(targetTicks);
////                spindexer.setMode(DcMotor.RunMode.RUN_TO_POSITION);
////                spindexer.setPower(SPIN_POWER);
////            }
////            prevX = xNow;
////
////            // Stop power when it reaches target
////            if (!spindexer.isBusy()) {
////                spindexer.setPower(0);
////            }
//
//            telemetry.addData("mode", spindexer.getMode());
//            telemetry.addData("power", spindexer.getPower());
//            telemetry.addData("cur", spindexer.getCurrentPosition());
//            telemetry.addData("tgt", spindexer.getTargetPosition());
//            telemetry.addData("busy", spindexer.isBusy());
//            telemetry.update();
//        }
//    }
//}
