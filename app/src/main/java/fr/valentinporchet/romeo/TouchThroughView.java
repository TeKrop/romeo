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

import java.util.ArrayList;

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
    private TTClientThread mClientThread;
    private Thread mClientSocketThread;
    private String mServerIP;
    private Vibrator mVibrator;
    private ArrayList<TTData> mPositions;
    private ArrayList<TTData> mOtherPositions;

    public TouchThroughView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            initialisePaint();
            initialisePositions();
            mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        }
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
        mPositions = new ArrayList<>();
        mOtherPositions = new ArrayList<>();
    }

    public void setServerIP(String serverIP) {
        mServerIP = serverIP;
    }

    /***** EVENT FUNCTIONS *****/
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        mPositions.clear();

        // for all touch points
        for (int i=0; i < pointerCount; i++) {
            int action = event.getActionMasked();
            float x = event.getX(i);
            float y = event.getY(i);

            /*
            * Switch on the action. The action is extracted from the event by
            * applying the MotionEvent.ACTION_MASK.
            */
            switch (action & MotionEvent.ACTION_MASK) {

                // Do subfunctions for each in the future
                case MotionEvent.ACTION_DOWN: {
                    Log.i("TouchDisplayView", "Touching down...");
                    this.onTouchDown(x, y);
                    break;
                }

                case MotionEvent.ACTION_MOVE: {
                    Log.v("TouchDisplayView", "Moving on " + x + "," + y);
                    this.onMoveTouch(x, y);
                    break;
                }
            }
        }
        this.sendData(); // send the array list of positions
        // trigger redraw on UI thread
        this.postInvalidate();
        return true;
    }

    private void onTouchDown(float x, float y) {
        mPositions.add(new TTData(x, y));
    }

    private void onMoveTouch(float x, float y) {
        mPositions.add(new TTData(x, y));
    }

    private void sendData() {
        Log.v("TouchThroughView", "Sending position data : " + mPositions + " to " + mServerIP);
        mClientThread = new TTClientThread(mPositions, mServerIP);
        // then we start it
        mClientSocketThread = new Thread(mClientThread);
        mClientSocketThread.start(); // we start the thread
    }

    /***** DRAWING FUNCTIONS *****/

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // if the two circles are here, check if collision
        /*if (mPositionX != -1000 && mPositionY != -1000) {
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
        }*/

        if (mOtherPositions.isEmpty()) {
            for (TTData p : mPositions) {
                mPaint.setColor(DEFAULT_COLOR);
                drawCircle(p, canvas);
            }
        } else {
            if (mPositions.isEmpty()) {
                for (TTData p : mOtherPositions) {
                    mPaint.setColor(DEFAULT_OTHER_COLOR);
                    drawCircle(p, canvas);
                }
            } else {
                boolean collision = false;
                int collisionIntensity = 0;
                TTData p1, p2;

                // List of collisions, in order to draw the circles
                // in the right color
                ArrayList<Boolean> collisions = new ArrayList<>();
                ArrayList<Boolean> collisionsOther = new ArrayList<>();
                for (int i=0; i < mPositions.size(); i++) { collisions.add(false); }
                for (int i=0; i < mOtherPositions.size(); i++) { collisionsOther.add(false); }

                for (int i=0; i < mPositions.size(); i++) {
                    for (int j=0; j < mOtherPositions.size(); j++) {
                        p1 = mPositions.get(i); p2 = mOtherPositions.get(j);

                        if (collisionBetween(p1.getX(), p1.getY(), p2.getX(), p2.getY())) {
                            mPaint.setColor(DEFAULT_COLLISION_COLOR);
                            drawCircle(p1, canvas); drawCircle(p2, canvas);
                            collision = true; collisionIntensity += 200;

                            collisions.set(i, true); collisionsOther.set(j, true);
                        } else {
                            if (collisions.get(i)) {
                                mPaint.setColor(DEFAULT_COLLISION_COLOR);
                            } else {
                                mPaint.setColor(DEFAULT_COLOR);
                            }
                            drawCircle(p1, canvas);

                            if (collisionsOther.get(j)) {
                                mPaint.setColor(DEFAULT_COLLISION_COLOR);
                            } else {
                                mPaint.setColor(DEFAULT_OTHER_COLOR);
                            }
                            drawCircle(p2, canvas);
                        }
                    }
                }

                if (collision) {
                    mVibrator.vibrate(collisionIntensity);
                }
            }
        }
    }

    private void drawCircle(TTData p, Canvas canvas) {
        canvas.drawOval(new RectF(p.getX() - WIDTH_RADIUS, p.getY() - HEIGHT_RADIUS,
                                  p.getX() + WIDTH_RADIUS, p.getY() + HEIGHT_RADIUS), mPaint);
    }

    public void getOtherPosition(ArrayList<TTData> mReceived) {
        Log.i("TouchThroughView", "Data received : " + mReceived);
        mOtherPositions = mReceived;
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
    private boolean collisionBetween(float aX, float aY, float bX, float bY) {
        float radius = (WIDTH_RADIUS + HEIGHT_RADIUS) / 2; // approximation for collision
        double x = Math.abs(aX-bX);
        double y = Math.abs(aY-bY);
        double distance = Math.sqrt(x*x + y*y);
        return distance < 2*radius;
    }
}
