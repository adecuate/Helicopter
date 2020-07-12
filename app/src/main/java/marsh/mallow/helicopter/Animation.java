package marsh.mallow.helicopter;

import android.graphics.Bitmap;

public class Animation {
    private Bitmap[] frames;
    private int currentFrame;
    private long startTime;
    private long delay;
    private boolean playedOnce;

    public void setFrames (Bitmap[] frames){
        this.frames = frames;
        currentFrame = 0;
        startTime = System.nanoTime();
    }

    public void setDelay (long d){
       delay = d;
    }

    public void setCurrentFrame(int currentFrame) {
        this.currentFrame = currentFrame;
    }

    public void update(){
        long elapsed = (System.nanoTime()-startTime)/1000000;
        if (elapsed > delay){
            currentFrame++;
            startTime = System.nanoTime();
        }

        //which image in array is going to return
        if (currentFrame == frames.length){
            currentFrame = 0;
            playedOnce = true;
        }
    }

    public Bitmap getImage(){
        return frames[currentFrame];
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    public boolean isPlayedOnce() {
        return playedOnce;
    }
}
