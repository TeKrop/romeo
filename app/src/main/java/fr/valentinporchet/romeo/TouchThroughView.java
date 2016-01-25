package fr.valentinporchet.romeo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Valentin on 25/01/2016.
 */
public class TouchThroughView extends View {
    /**
     * Private constants
     */
    private final float DENSITY = getResources().getDisplayMetrics().density;
    private final float DEFAULT_RADIUS = 25.f;
    private final int DEFAULT_COLOR = 0xFFFF0000; // red
    private final int DEFAULT_OTHER_COLOR = 0xFF0000FF; // blue
    private final int DEFAULT_COLLISION_COLOR = DEFAULT_COLOR + DEFAULT_OTHER_COLOR; // fusion

    /**
     * Private variables
     */
    private Paint mPaint = new Paint();
    private float mPositionX, mPositionY, mOtherPositionX, mOtherPositionY;
    private TTClientThread mClientThread;
    private Thread mClientSocketThread;
    private String mServerIP;
    private Vibrator mVibrator;

    public TouchThroughView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialisePaint();
        initialisePositions();
        mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * Sets up the required {@link android.graphics.Paint} objects for the screen density of this
     * device.
     */
    private void initialisePaint() {
        // Initialize Path Paint
        mPaint.setStrokeWidth(DEFAULT_RADIUS * DENSITY); // thickness of circle
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE); // style of circle
        mPaint.setStrokeJoin(Paint.Join.ROUND); // jointures between elements of circle
        mPaint.setStrokeCap(Paint.Cap.ROUND); // start and end of circle
    }

    private void initialisePositions() {
        mPositionX = mPositionY = mOtherPositionX = mOtherPositionY = -1000; // outside of view
    }

    public void setServerIP(String serverIP) {
        mServerIP = serverIP;
    }

    /***** EVENT FUNCTIONS *****/
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();

        /*
         * Switch on the action. The action is extracted from the event by
         * applying the MotionEvent.ACTION_MASK.
         */
        switch (action & MotionEvent.ACTION_MASK) {

            // Do subfunctions for each in the future
            case MotionEvent.ACTION_DOWN: {
                Log.i("TouchDisplayView", "Touching down...");
                this.onTouchDown(x, y);
                this.sendData(x, y);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                Log.v("TouchDisplayView", "Moving on " + x + "," + y);
                this.onMoveTouch(x, y);
                this.sendData(x, y);
                break;
            }

            case MotionEvent.ACTION_UP: {
                Log.i("TouchDisplayView", "Releasing...");
                this.onTouchUp();
                this.sendData(-1000, -1000);
                break;
            }
        }

        // trigger redraw on UI thread
        this.postInvalidate();
        return true;
    }

    private void onTouchDown(float x, float y) {
        mPositionX = x;
        mPositionY = y;

        // send notification to the other person
    }

    private void onMoveTouch(float x, float y) {
        mPositionX = x;
        mPositionY = y;
    }

    private void onTouchUp() {
        mPositionX = mPositionY = -1000;
    }

    private void sendData(float x, float y) {
        Log.v("TouchThroughView", "Sending position data : " + x + "," + y);
        mClientThread = new TTClientThread(new TTData(x, y), mServerIP);
        // then we start it
        mClientSocketThread = new Thread(mClientThread);
        mClientSocketThread.start(); // we start the thread
    }
    /***** DRAWING FUNCTIONS *****/

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // if the two circles are here, check if collision
        if (mPositionX != -1000 && mPositionY != -1000) {
            // if there is collision, we put the same color and vibrate
            if (collision(mPositionX, mPositionY, mOtherPositionX, mOtherPositionY, DEFAULT_RADIUS)) {
                mPaint.setColor(DEFAULT_COLLISION_COLOR);
                canvas.drawCircle(mPositionX, mPositionY, DEFAULT_RADIUS, mPaint);
                canvas.drawCircle(mOtherPositionX, mOtherPositionY, DEFAULT_RADIUS, mPaint);
                mVibrator.vibrate(300);
            } else {
                mPaint.setColor(DEFAULT_COLOR); // color of circle
                canvas.drawCircle(mPositionX, mPositionY, DEFAULT_RADIUS, mPaint);
                mPaint.setColor(DEFAULT_OTHER_COLOR); // color of other circle
                canvas.drawCircle(mOtherPositionX, mOtherPositionY, DEFAULT_RADIUS, mPaint);
            }
        } else {
            // we draw the circle of the current user
            if (mPositionX != -1000) {
                mPaint.setColor(DEFAULT_COLOR); // color of circle
                canvas.drawCircle(mPositionX, mPositionY, DEFAULT_RADIUS, mPaint);
            }
            // and the other one if there is any
            if(mOtherPositionX != -1000) {
                mPaint.setColor(DEFAULT_OTHER_COLOR); // color of other circle
                canvas.drawCircle(mOtherPositionX, mOtherPositionY, DEFAULT_RADIUS, mPaint);
            }
        }
    }

    public void getOtherPosition(TTData mReceived) {
        Log.i("TouchThroughView", "Data received : " + mReceived.posX + " " + mReceived.posY);
        mOtherPositionX = mReceived.posX;
        mOtherPositionY = mReceived.posY;
        this.postInvalidate();
    }

    /**
     * Collision between two circles
     * @param aX x coordinates of first circle
     * @param aY y coordinates of first circle
     * @param bX x coordinates of second circle
     * @param bY y coordinates of second circle
     * @param radius radius of circles
     * @return true if collision
     */
    private boolean collision(float aX, float aY, float bX, float bY, float radius) {
        double x = Math.abs(aX-bX);
        double y = Math.abs(aY-bY);
        double distance = Math.sqrt(x*x + y*y);
        return distance < 2*radius;
    }
}
