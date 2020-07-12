package marsh.mallow.helicopter;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class BottomBorder extends GameObject {

    private Bitmap image;

    public BottomBorder(Bitmap res, int x, int y) {
        this.image = res;
        height = 200;
        weight = 20;
        this.x = x;
        this.y = y;

        dx = GamePanel.MOVESPEED;

        image = Bitmap.createBitmap(res, 0,0,weight,height);
    }

    public void update(){
        x+=dx;
    }

    public void draw(Canvas canvas){
        try {
            canvas.drawBitmap(image, x,y, null);
        }catch (Exception e){};
    }
}
