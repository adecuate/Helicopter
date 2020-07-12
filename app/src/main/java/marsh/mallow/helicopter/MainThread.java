package marsh.mallow.helicopter;

import android.graphics.Canvas;
import android.view.SurfaceHolder;


public class MainThread extends Thread {
    private int fps = 30;
    private double avgFPS;
    private final SurfaceHolder surfaceHolder;
    private GamePanel gamePanel;
    private boolean running;
    public static Canvas canvas;

    public MainThread(SurfaceHolder surfaceHolder, GamePanel gamePanel) {
        super();
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;
    }

    @Override
    public void run() {
        long startTime;
        long timeMills;
        long waitTime;
        long totalTime = 0;
        int frameCount = 0;
        long targetTime = 1000/ fps;

        while (running) {
            startTime = System.nanoTime();

            canvas = null;

            // try locking the canvas for pixels editing
            try {
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    this.gamePanel.update();
                    this.gamePanel.draw(canvas);
                }
            } catch (Exception e) {
            }
            finally {
                if (canvas != null){
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e){e.printStackTrace();}
                }
            }
            timeMills = (System.nanoTime() - startTime) / 1000000;
            waitTime = targetTime - timeMills;
            try {
                this.sleep(waitTime);
            } catch (Exception e) {
            }

            totalTime += System.nanoTime() - startTime;
            frameCount++;

            if (frameCount == fps) {
                avgFPS = 1000 / ((totalTime/frameCount)/1000000);
                frameCount = 0;
                totalTime = 0;
                System.out.println(avgFPS);
            }
        }
        // super.run();
    }

    public void setRunning(boolean b) {
        running = b;
    }
}
