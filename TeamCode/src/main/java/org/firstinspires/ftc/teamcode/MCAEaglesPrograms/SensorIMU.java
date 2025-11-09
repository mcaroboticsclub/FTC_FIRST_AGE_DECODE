//package org.firstinspires.ftc.teamcode.MCAEaglesPrograms;
//
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.IMU;
//import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
//import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
//import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
//
//@TeleOp(name = "Sensor: 9-Axis IMU", group = "Sensor")
//public class SensorIMU extends LinearOpMode {
//
//    @Override
//    public void runOpMode() {
//        IMU imu = hardwareMap.get(IMU.class, "imu");
//
//        telemetry.addLine("Initializing REV 9-Axis IMU...");
//        telemetry.update();
//
//        imu.initialize(new IMU.Parameters());
//
//        waitForStart();
//
//        while (opModeIsActive()) {
//            // Get orientation (gyro)
//            YawPitchRollAngles orientation = imu.getRobotYawPitchRollAngles();
//
//            // Get linear acceleration (accelerometer)
//            Acceleration accel = imu.getLinearAcceleration();
//
//            telemetry.addLine("Gyroscope (degrees)");
//            telemetry.addData("Yaw", "%.1f", orientation.getYaw(AngleUnit.DEGREES));
//            telemetry.addData("Pitch", "%.1f", orientation.getPitch(AngleUnit.DEGREES));
//            telemetry.addData("Roll", "%.1f", orientation.getRoll(AngleUnit.DEGREES));
//
//            telemetry.addLine("\nAccelerometer (m/s²)");
//            telemetry.addData("X", "%.2f", accel.xAccel);
//            telemetry.addData("Y", "%.2f", accel.yAccel);
//            telemetry.addData("Z", "%.2f", accel.zAccel);
//
//            telemetry.addLine("\nMagnetometer");
//            telemetry.addData("Heading", "%.1f°", imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES));
//
//            telemetry.update();
//        }
//    }
//}
