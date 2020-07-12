package marsh.mallow.helicopter;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Random;

public class Missiles extends GameObject {

    private int score;
    private int speed;
    private Random random = new Random();
    private Animation animation = new Animation();
    private Bitmap bitmap;

    public Missiles (Bitmap res, int x, int y, int w, int h, int s, int numFrames){
        super.x = x;
        super.y = y;

        weight = w;
        height = h;

        score = s;
        speed = 7 + (int) (random.nextDouble()*score/30);

        // missile speed
        if (speed>=40) {
            speed = 40;
        }

        Bitmap[] image = new Bitmap[numFrames];

        bitmap = res;

        for (int i=0; i<image.length; i++){
            image[i] = Bitmap.createBitmap(bitmap, 0, i*height, weight, height);
        }

        animation.setFrames(image);
        animation.setDelay(100 - speed);
    }

    public void update(){
        x-=speed;
        animation.update();
    }

    public void draw(Canvas canvas){
        try {
            canvas.drawBitmap(animation.getImage(), x,y,null);
        } catch (Exception e){
        }
    }

    //offset slightly for more realistic collision detection
    @Override
    public int getWeight() {
        return weight-10;
    }
}
