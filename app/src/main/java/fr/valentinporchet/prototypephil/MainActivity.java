package fr.valentinporchet.prototypephil;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity implements View.OnTouchListener {
    // main constants
    private final float RADIUS_DP = 15f;
    private final float PRESSURE = 1f;
    private final int[] COLORS = {
            0xFF33B5E5, 0xFFAA66CC, 0xFF99CC00, 0xFFFFBB33, 0xFFFF4444,
            0xFF0099CC, 0xFF9933CC, 0xFF669900, 0xFFFF8800, 0xFFCC0000
    };
    private final int BACKGROUND_COLOR = 0xFFFFFFFF;
    private final int ANIMATION_SPEED = 1000; // paths per second

    // main variables
    private DrawPanel dp;
    private ArrayList<Path> pointsToDraw = new ArrayList<Path>();
    private ArrayList<Path> savePoints = new ArrayList<Path>();
    private Paint mPaint;
    private Path path;
    private Path segment = new Path();
    private PathMeasure pathMeasure;
    private boolean isAnimationDrawing = false;
    private long chrono = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        // initialize the drawing panel
        dp = new DrawPanel(this);
        dp.setOnTouchListener(this);

        // parameters for window
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        // initialize the paint
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(30);

        FrameLayout fl = new FrameLayout(this);
        fl.addView(dp);
        setContentView(fl);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        dp.pause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        dp.resume();
    }

    public class DrawPanel extends SurfaceView implements Runnable {

        Thread t = null;
        SurfaceHolder holder;
        boolean isItOk = false;

        public DrawPanel(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
            holder = getHolder();
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (isItOk) {
                if (!holder.getSurface().isValid()) {
                    continue;
                }

                Canvas c = holder.lockCanvas();
                c.drawRGB(255,255, 255); // background color
                onDraw(c); // see if we can do in another way
                holder.unlockCanvasAndPost(c);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            super.onDraw(canvas);

            if (isAnimationDrawing) {
                long currentTime = java.lang.System.currentTimeMillis();
                int numberOfPathsToDraw = 1 + (int)(((currentTime - chrono) * ANIMATION_SPEED) / 1000);
                float length = (int) pathMeasure.getLength();

                // we draw the right amount of paths
                if (numberOfPathsToDraw < length) {
                    segment = new Path();
                    pathMeasure.getSegment(0, numberOfPathsToDraw, segment, true);
                    canvas.drawPath(segment, mPaint);
                } else { // else we draw everything
                    for (Path path: savePoints) {
                        canvas.drawPath(path, mPaint);
                    }
                }
            } else {
                synchronized (pointsToDraw) {
                    for (Path path : pointsToDraw) {
                        canvas.drawPath(path, mPaint);
                    }
                }
            }
        }

        public void pause(){
            isItOk = false;
            while(true){
                try{
                    t.join();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                break;
            }
            t = null;
        }

        public void resume(){
            isItOk = true;
            t = new Thread(this);
            t.start();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent me) {
        // TODO Auto-generated method stub
        synchronized(pointsToDraw)
        {
            if (me.getAction() == MotionEvent.ACTION_DOWN) {
                // if an animation is ongoing, we stop it
                isAnimationDrawing = false;

                pointsToDraw.clear();
                path = new Path();

                // new color for the path
                int random = new Random().nextInt();
                random = Math.max(random, -1*random);
                mPaint.setColor(COLORS[random % COLORS.length]);

                path.moveTo(me.getX(), me.getY());
                //path.lineTo(me.getX(), me.getY());
                pointsToDraw.add(path);
            } else if(me.getAction() == MotionEvent.ACTION_MOVE) {
                path.lineTo(me.getX(), me.getY());
            } else if(me.getAction() == MotionEvent.ACTION_UP) {
                // do something in the future ?
                savePoints = (ArrayList<Path>) pointsToDraw.clone();
                pathMeasure = new PathMeasure(savePoints.get(0), false);
                pointsToDraw.clear();

                // we start the animation
                isAnimationDrawing = true;
                chrono = java.lang.System.currentTimeMillis();
            }
        }
        return true;
    }
}