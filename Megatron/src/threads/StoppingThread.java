package threads;

import java.io.IOException;

import lejos.hardware.Button;
import lejos.hardware.Keys;
import main.Megatron;

// this thread enables the user to turn off the robot at any moment by clicking the Escape  Button
public class StoppingThread extends Thread {
    private Megatron robot;

    public StoppingThread(Megatron robot) {
        this.robot = robot;
        this.setDaemon(true);
    }

    public void run() {
        while (true) {
            if (Button.getButtons() == Keys.ID_ESCAPE) {
                try {
                    robot.stop();
                } catch (IOException e) {
                    // this won't happen
                }
            }
        }
    }
}
