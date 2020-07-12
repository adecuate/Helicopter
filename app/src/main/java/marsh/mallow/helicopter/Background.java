package marsh.mallow.helicopter;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Background {

    private Bitmap bitmapImage;
    private int x, y, dx;

    public Background(Bitmap bitmapImage)
    {
        this.bitmapImage = bitmapImage;
        dx = GamePanel.MOVESPEED;
    }

    public void update(){
        x+=dx;
        if (x<-GamePanel.WIDTH){ x=0;}
    }

    public void draw(Canvas canvas){
        canvas.drawBitmap(bitmapImage, x,y,null);
        if(x<0){
            canvas.drawBitmap(bitmapImage, x+GamePanel.WIDTH, y,null);
        }
    }

}
