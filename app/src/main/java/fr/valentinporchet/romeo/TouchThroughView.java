package fr.valentinporchet.romeo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Valentin on 25/01/2016.
 */
public class TouchThroughView extends View {
    /**
     * Private constants
     */
    private final float DENSITY = getResources().getDisplayMetrics().density;
    private final float WIDTH_RADIUS = 20.f;
    private final float HEIGHT_RADIUS = 25.f;
    private final int DEFAULT_COLOR = 0xFFC31D40; // red
    private final int DEFAULT_OTHER_COLOR = 0xFF272F80; // blue
    private final int DEFAULT_COLLISION_COLOR = 0xFF2EC196; // green

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
        mPaint.setStyle(Paint.Style.FILL); // style of circle
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
        Log.v("TouchThroughView", "Sending position data : " + x + "," + y + " to " + mServerIP);
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
            if (collision(mPositionX, mPositionY, mOtherPositionX, mOtherPositionY)) {
                mPaint.setColor(DEFAULT_COLLISION_COLOR);
                drawMyCircle(canvas); drawOtherCircle(canvas);
                mVibrator.vibrate(300);
            } else {
                mPaint.setColor(DEFAULT_COLOR); // color of circle
                drawMyCircle(canvas);
                mPaint.setColor(DEFAULT_OTHER_COLOR); // color of other circle
                drawOtherCircle(canvas);
            }
        } else {
            // we draw the circle of the current user
            if (mPositionX != -1000) {
                mPaint.setColor(DEFAULT_COLOR); // color of circle
                drawMyCircle(canvas);
            }
            // and the other one if there is any
            if(mOtherPositionX != -1000) {
                mPaint.setColor(DEFAULT_OTHER_COLOR); // color of other circle
                drawOtherCircle(canvas);
            }
        }
    }

    private void drawMyCircle(Canvas canvas) {
        canvas.drawOval(new RectF(mPositionX - WIDTH_RADIUS, mPositionY - HEIGHT_RADIUS,
                                  mPositionX + WIDTH_RADIUS, mPositionY + HEIGHT_RADIUS), mPaint);
    }

    private void drawOtherCircle(Canvas canvas) {
        canvas.drawOval(new RectF(mOtherPositionX - WIDTH_RADIUS, mOtherPositionY - HEIGHT_RADIUS,
                                  mOtherPositionX + WIDTH_RADIUS, mOtherPositionY + HEIGHT_RADIUS), mPaint);
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
     * @return true if collision
     */
    private boolean collision(float aX, float aY, float bX, float bY) {
        float radius = (WIDTH_RADIUS + HEIGHT_RADIUS) / 2; // approximation for collision
        double x = Math.abs(aX-bX);
        double y = Math.abs(aY-bY);
        double distance = Math.sqrt(x*x + y*y);
        return distance < 2*radius;
    }
}
