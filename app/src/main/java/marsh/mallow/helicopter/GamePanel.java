package marsh.mallow.helicopter;


import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


import com.example.helicopter.R;

import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {

    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int MOVESPEED = -10;
    private MainThread mainThread;
    private Background bg;
    private Player player;
    private ArrayList<SmokePuff> smokePuffs;
    private long smokeStartTime;
    private long missileStartTime;
    private  ArrayList<Missiles> missiles;
    private  ArrayList<TopBorder> topBorders;
    private  ArrayList<BottomBorder> bottomBorders;
    private Random random = new Random();
    private int maxBorderHeight;
    private int minBorderHeight;
    private boolean topDown = true;
    private boolean botDown = true;
    private boolean newGameCreated;

    // increase to slaw down difficulty progression, decrease speed up difficulty progression
    private int progressDenom = 20;

    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean dissapear;
    private boolean started;
    private int best;


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        int counter =0;
        while (retry && counter<1000){
            counter++;
            try {
                mainThread.setRunning(false);
                mainThread.join();
                retry = false;
                mainThread = null;
            }catch (InterruptedException e) {e.printStackTrace(); }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.grassbg1));
        //instantiate player

        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.helicopter), 65, 25 , 3);
        smokePuffs = new ArrayList<SmokePuff>();
        smokeStartTime = System.nanoTime();
        missiles = new ArrayList<Missiles>();
        missileStartTime = System.nanoTime();

        topBorders = new ArrayList<TopBorder>();
        bottomBorders = new ArrayList<BottomBorder>();

        mainThread = new MainThread(getHolder(), this);

        // start game
        mainThread.setRunning(true);
        mainThread.start();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction()  == MotionEvent.ACTION_DOWN){
            if (!player.isPlaying() && newGameCreated && reset){
                player.setPlaying(true);
                player.setUp(true);
            }

            if (player.isPlaying()){
                if (!started){
                    started = true;
                }
                    reset = false;
                    player.setUp(true);
            }

            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            player.setUp(false);
            return true;
        }

            return super.onTouchEvent(event);
    }



    public GamePanel(Context context) {
        super(context);



        // add the callback to SurfaceHolder for intercept events
        getHolder().addCallback(this);

        // make gamePanel focusable so it can handle events
        setFocusable(true);
    }

    public void update() {
        if (player.isPlaying()) {

            if(bottomBorders.isEmpty()){
                player.setPlaying(false);
                return;
            }

            if (topBorders.isEmpty()){
                player.setPlaying(false);
                return;
            }


        bg.update();
        player.update();

        //calculate the threshold of height border can have based on the score
        //max and min border heart
        // border switched direction when either max or min is reached
            maxBorderHeight = 30+player.getScore()/progressDenom;

            //cap max border height so that borders can only take up a total off 1/2 the screeen
            if(maxBorderHeight>HEIGHT/4){
                maxBorderHeight= HEIGHT/4;
            }

            minBorderHeight = 5+player.getScore()/progressDenom;

        // check top border collision
            for (int i = 0; i < topBorders.size(); i++) {
                if (collision(topBorders.get(i), player)) {
                    player.setPlaying(false);
                }
            }
        // check bottom border collision
            for (int i = 0; i < bottomBorders.size(); i++) {
                if (collision(bottomBorders.get(i), player)) {
                    player.setPlaying(false);
                }
            }
            
            
        //update top border
        this.updateTopBorder();

        //update bottom border
        this.updateBottomBorder();

        //ms
        long elapsed = (System.nanoTime() - smokeStartTime)/1000000;
        if (elapsed>120) {
            smokePuffs.add(new SmokePuff(player.getX(), player.getY() + 10));
            smokeStartTime = System.nanoTime();
        }

        for (int i =0; i<smokePuffs.size(); i++){
            smokePuffs.get(i).update();
            if(smokePuffs.get(i).getX() < -10){
                smokePuffs.remove(i);
            }
        }

        }
        else {
            player.resetDY();
            if(!reset){
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                dissapear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(), R.drawable.explosion),
                        player.getX(),
                        player.getY()-30,
                        100,
                        100,
                        25);

            }

            explosion.update();

            long resetElapsed = (System.nanoTime()-startReset)/1000000;

            if (resetElapsed>2500 && !newGameCreated){
                newGame();
            }

            if (!newGameCreated) {
                newGame();
            }
        }
    }

    public boolean collision(GameObject a, GameObject b){
        if (Rect.intersects(a.getRect(), b.getRect())){
            return true;
        }
        return false;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        final float scaleFactorX = getWidth() / (WIDTH*1.f);
        final float scaleFactorY = getHeight() / (HEIGHT*1.f);
        if (canvas != null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            //  super.draw(canvas);
            bg.draw(canvas);

            if(!dissapear) {
                player.draw(canvas);
            }

            //add missiles on timer
            long missileElapsed = (System.nanoTime()- missileStartTime)/1000000;
            if (missileElapsed>(2000 - player.getScore()/4)){
                // first missile allways goes down from the middle
                    if (missiles.size() == 0){
                        missiles.add(new Missiles(BitmapFactory.decodeResource(
                                getResources(), R.drawable.missile),
                                WIDTH+10, HEIGHT/2, 45, 15, player.getScore(), 13));
                    } else {
                            missiles.add(new Missiles(BitmapFactory.decodeResource(getResources(),
                                    R.drawable.missile),
                                    WIDTH+10,
                                    (int) (random.nextDouble()* (HEIGHT - (maxBorderHeight * 2)) + maxBorderHeight),
                                    45,
                                    15,
                                     player.getScore(), 13));
                    }

                    // reset timer
                    missileStartTime = System.nanoTime();
            }

            // loop for every missile
            for (int i = 0; i < missiles.size(); i++) {
                missiles.get(i).update();
                if (collision(missiles.get(i),player)){
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;
                }

                //remove missile if it is off the screen
                if (missiles.get(i).getX()<-100){
                    missiles.remove(i);
                    break;
                }
            }

            //add smoke puffs on timer
            for (SmokePuff sm: smokePuffs){
                sm.draw(canvas);
            }

            // draw missiles
            for (Missiles m: missiles
                 ) {
                m.draw(canvas);

            }

            //draw top border
            for (TopBorder tb: topBorders
                 ) {
                tb.draw(canvas);
            }

            //draw bottom border
            for (BottomBorder bb: bottomBorders
            ) {
                bb.draw(canvas);
            }

            // draw explosion
            if (started){
                explosion.draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState);
        }
    }
        public void updateTopBorder(){
            // every 50 points, insert randomly placed top blocks that breaks pattern
            if (player.getScore()%50 == 0){
                topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                        R.drawable.brick), topBorders.get(topBorders.size()-1).getX()+20,
                        0, (int) (random.nextDouble()*maxBorderHeight)+1));
            }


            for (int i = 0; i < topBorders.size(); i++) {
                topBorders.get(i).update();
                if(topBorders.get(i).getX()<-20){
                    topBorders.remove(i);
                    // remove element of arrayList, replace it  by adding a new one

                    // calculate topdown which determines directon the border  is moving (up or down)
                    if(topBorders.get(topBorders.size()-1).getHeight()>=maxBorderHeight){
                        topDown = false;
                    }

                    if(topBorders.get(topBorders.size()-1).getHeight()<=minBorderHeight){
                        topDown = true;
                    }

                    // new border added will have larger height
                    if (topDown){
                        topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                                R.drawable.brick),
                                topBorders.get(topBorders.size()-1).getX()+20,
                                    0,
                                    topBorders.get(topBorders.size()-1).getHeight()+1));
                    }
                    // new border added will have smaller height
                    else {
                        topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                                R.drawable.brick),
                                topBorders.get(topBorders.size()-1).getX()+20,
                                0,
                                topBorders.get(topBorders.size()-1).getHeight()-1));

                    }
                }
            }
        }

        public void updateBottomBorder(){
            // every 40 points, insert randomly placed bottom blocks that breaks pattern
            if (player.getScore()%40 == 0){
                bottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(),
                        R.drawable.brick),
                        bottomBorders.get(bottomBorders.size()-1).getX()+20,
                        (int) ((random.nextDouble()*maxBorderHeight)+(HEIGHT-maxBorderHeight))));
            }

            //update bottom border
            for (int i = 0; i < bottomBorders.size(); i++) {
                bottomBorders.get(i).update();

                // if border is moving off screen, remove it and add a corresponding new one
                if (bottomBorders.get(i).getX() < -20) {
                    bottomBorders.remove(i);


                    // determine if border will be moving up or down
                    if (bottomBorders.get(bottomBorders.size() - 1).getY() <= HEIGHT - maxBorderHeight) {
                        botDown = true;
                    }

                    if (bottomBorders.get(bottomBorders.size() - 1).getY() >= HEIGHT - minBorderHeight) {
                        botDown = false;
                    }

                    if (botDown) {
                        bottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(),
                                R.drawable.brick),
                                bottomBorders.get(bottomBorders.size() - 1).getX() + 20,
                                bottomBorders.get(bottomBorders.size() - 1).getY() + 1));
                    } else {
                        bottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(),
                                R.drawable.brick),
                                bottomBorders.get(bottomBorders.size() - 1).getX() + 20,
                                bottomBorders.get(bottomBorders.size() - 1).getY() - 1));
                    }
                }

            }
    }

    public void newGame() {

        dissapear = false;

        bottomBorders.clear();
        topBorders.clear();
        missiles.clear();
        smokePuffs.clear();

        minBorderHeight = 5;
        maxBorderHeight = 30;


        if (player.getScore()> best){
            best = player.getScore();
        }

        player.resetDY();
        player.resetScore();
        player.setY(HEIGHT / 2);


        //create initial borders

        //initial top border
        for (int i = 0; i * 20 < WIDTH + 40; i++) {

            //first top border create
            if (i == 0) {
                topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                        (R.drawable.brick)),
                        i * 20,
                        0,
                        10));
            } else {
                topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                        (R.drawable.brick)),
                        i * 20,
                        0,
                        topBorders.get(i - 1).getHeight() + 1));
            }
        }

        //initial bottom border

        for (int i = 0; i * 20 < WIDTH + 40; i++) {

            //first top bottom border create
            if (i == 0) {
                topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20,
                        0,
                        10));
            } else {
                topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20,
                        0,
                        topBorders.get(i-1).getHeight()+1));
            }
        }

        //inicial bottom border

        for (int i = 0; i*20< WIDTH+40; i++) {
            if (i==0){
                bottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),
                        i*20,
                        HEIGHT-minBorderHeight));
            }
            //adding borders until initial screen is filed
            else {
                bottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),
                        i*20,
                        bottomBorders.get(i-1).getY()-1));
            }

            newGameCreated = true;

        }


        }

        public void drawText(Canvas canvas){
            Paint paint = new Paint();
            paint.setColor(Color.GREEN);
            paint.setTextSize(30);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
            canvas.drawText("DISTANCE: "+(player.getScore()*3),
                    10,
                    HEIGHT-10,
                    paint);
            canvas.drawText("BEST: "+best*3,
                    WIDTH - 215,
                    HEIGHT - 10,
                    paint);

            if   (!player.isPlaying() && newGameCreated && reset){
                Paint paint1 = new Paint();
                paint1.setTextSize(40);
                paint1.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
                canvas.drawText("PRESS TO START",WIDTH/2-50,
                        HEIGHT/2,
                        paint1);
                paint1.setTextSize(20);
                canvas.drawText("PRESS AND HOLD TO GO UP",WIDTH/2-50,
                        HEIGHT/2 +20,
                        paint1);
                canvas.drawText("RELEASE TO GO DOWN",WIDTH/2-50,
                        HEIGHT/2 +40,
                        paint1);
            }
        }
    }