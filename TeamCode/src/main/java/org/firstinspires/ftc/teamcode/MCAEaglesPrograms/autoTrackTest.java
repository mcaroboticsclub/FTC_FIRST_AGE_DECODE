package org.firstinspires.ftc.teamcode.MCAEaglesPrograms;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;

import java.util.List;

@TeleOp(name = "AutoTrack Test", group = "MCA EAGLES PROGRAMS")
public class autoTrackTest extends LinearOpMode {

    DcMotor turret;
    Limelight3A limelight;

    int targetAprilTagID = 24;

    double kP = 0.015;
    double kD = 0.002;
    double lastTx = 0;
    double deadband = 5.0;

    @Override
    public void runOpMode() {

        turret = hardwareMap.dcMotor.get("Turret");
        turret.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        turret.setDirection(DcMotorSimple.Direction.FORWARD);

        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.pipelineSwitch(0);
        limelight.start();

        waitForStart();

        while (opModeIsActive()) {

            LLResult result = limelight.getLatestResult();

            if (result != null && result.isValid()) {
                List<LLResultTypes.FiducialResult> fiducials =
                        result.getFiducialResults();

                LLResultTypes.FiducialResult target = null;

                for (LLResultTypes.FiducialResult f : fiducials) {
                    if (f.getFiducialId() == targetAprilTagID) {
                        target = f;
                        break;
                    }
                }

                if (target != null) {
                    double tx = target.getTargetXDegrees();
                    double error = -tx;
                    double derivative = tx - lastTx;

                    double power = (error * kP) - (derivative * kD);
                    power = Math.max(-0.4, Math.min(0.4, power));

                    if (Math.abs(tx) > deadband) {
                        turret.setPower(power);
                    } else {
                        turret.setPower(0);
                    }

                    lastTx = tx;

                    telemetry.addData("Target", "FOUND");
                    telemetry.addData("tx (deg)", "%.2f", tx);
                    telemetry.addData("Turret Power", "%.2f", power);
                } else {
                    turret.setPower(0);
                    telemetry.addData("Target", "NOT VISIBLE");
                }
            } else {
                turret.setPower(0);
                telemetry.addData("Limelight", "NO DATA");
            }

            telemetry.update();
        }

        limelight.stop();
    }
}
