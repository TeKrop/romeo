package fr.valentinporchet.romeo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * View that shows touch events and their history via animations.
 */
public class TouchDisplayView extends View {
    /**
     * Private constants
     */
    private final float DENSITY = getResources().getDisplayMetrics().density;
    private final float DEFAULT_THICKNESS = 25.f;
    private final float MAX_TIME_THICKNESS = 1.f;

    /**
     * Private variables
     */

    // main variables
    private boolean mIsAnimationDrawing = false;
    private boolean mAnimationDone = true;
    private boolean mIsFirstTimeMoving = false;

    // containers of paths to draw and to animate
    private ArrayList<TouchData> mTouchData = new ArrayList<>();
    private ArrayList<TouchData> mTempReceivedData = new ArrayList<>();

    // variables for the path
    private Paint mPathPaint = new Paint();
    private int mLastSelectedColor = 0xFF242424; // black by default

    // variables used for animation of paths
    private Path mSegment = new Path();
    private PathMeasure mPathMeasure = new PathMeasure();
    private long mChrono = 0, mTouchChrono = 0; // general chrono for animation, and chrono for thickness of the path
    private float mSegmentOfPathToDraw;
    private int mCount;
    private int mCurrentPath; // contains the current path ID

    // letter button
    private ImageButton mLetterButton;

    /**
     * Constructor of the TouchDisplayView class
     * @param context Context
     * @param attrs Attributes
     */
    public TouchDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialisePaint();
    }

    /**
     * Sets up the required {@link android.graphics.Paint} objects for the screen density of this
     * device.
     */
    private void initialisePaint() {
        // Initialize Path Paint
        mPathPaint.setStrokeWidth(DEFAULT_THICKNESS * DENSITY); // thickness of path

        mPathPaint.setDither(true);
        mPathPaint.setStyle(Paint.Style.STROKE); // style of path
        mPathPaint.setStrokeJoin(Paint.Join.ROUND); // jointures between elements of path
        mPathPaint.setStrokeCap(Paint.Cap.ROUND); // start and end of path
    }

    // Getter for touch data
    public ArrayList<TouchData> getTouchData() {
        return this.mTouchData;
    }

    /***************** EVENT FUNCTIONS *****************/

    /*
     * Main event handling function
     */
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
                // if an animation is ongoing, we stop it
                mIsAnimationDrawing = false;
                this.onTouchDown(x, y);

                break;
            }

            case MotionEvent.ACTION_MOVE: {
                Log.v("TouchDisplayView", "Moving on " + x + "," + y);
                this.onMoveTouch(x, y);
                break;
            }

            case MotionEvent.ACTION_UP: {
                Log.i("TouchDisplayView", "Releasing...");
                // For debugging purpose, we display the tab
                Log.v("TouchDisplayView", "Data : " + mTouchData);
                break;
            }
        }

        // trigger redraw on UI thread
        this.postInvalidate();
        return true;
    }

    /**
     * Function called when we are touching down
     * @param x x touching coordinates
     * @param y y touching coordinates
     */
    private void onTouchDown(float x, float y) {
        Log.v("TouchDisplayView", "TouchData : " + mTouchData);
        // if we did the animation or the touch data is empty
        if ((mAnimationDone)||(mTouchData.isEmpty())) {
            // we reinitialize the touch data
            mTouchData.clear();
            mAnimationDone = false;

            // we initialize the chrono
            mChrono = java.lang.System.currentTimeMillis();
        }

        // we add a new touchdata
        mTouchData.add(new TouchData());

        // we apply the first position to the path
        mTouchData.get(mTouchData.size()-1).mPath.moveTo(x, y);

        // and we apply the selected color
        mTouchData.get(mTouchData.size()-1).mPathColor = mLastSelectedColor;

        // for the thickness, we will wait until the first move event to determine it
        mTouchChrono = java.lang.System.currentTimeMillis();
        mIsFirstTimeMoving = true;
    }

    /**
     * Function called when moving and already touching the area
     * @param x x touching coordinates
     * @param y y touching coordinates
     */
    private void onMoveTouch(float x, float y) {
        // we check if it's the first move event since the action_down, in
        // order to determine the thickness of the path
        if (mIsFirstTimeMoving) {
            // we get the time elapsed
            long timeElapsed = java.lang.System.currentTimeMillis() - mTouchChrono;
            mIsFirstTimeMoving = false;

            // the coeff will be setted between 1 and 2 (0s --> 1, 1s and + --> 2)
            float seconds = (timeElapsed / 1000.f);
            float coeff = 1 + (seconds > MAX_TIME_THICKNESS ? MAX_TIME_THICKNESS : seconds);

            // debug
            Log.v("TouchDisplayView", "elapsed = " + timeElapsed + ", seconds = " + seconds + " et coeff = " + coeff);

            // we add the determined thickness
            mTouchData.get(mTouchData.size()-1).mPathThickness = DEFAULT_THICKNESS * DENSITY * coeff;
        }

        // and we update the current path

        // just add a line toward the new position
        mTouchData.get(mTouchData.size()-1).mPath.lineTo(x, y);
        // and the time of the event
        mTouchData.get(mTouchData.size()-1).mTimeForPaths.add(java.lang.System.currentTimeMillis() - mChrono);
        // and the current length of the path
        mTouchData.get(mTouchData.size()-1).mTempPathLengths.add(new PathMeasure(mTouchData.get(mTouchData.size()-1).mPath, false).getLength());
    }

    /***************** DRAWING FUNCTIONS *****************/

    /*
     * Main drawing function
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // if we are drawing an animation
        if (mIsAnimationDrawing) {
            // we calculate the number of paths to draw depending on when we started the animation
            long currentTime = java.lang.System.currentTimeMillis();

            // if the time that passed is superior to the last amount we have to reach, then
            // we are in a new step of the movement : we update the segment to draw with the value
            // saved for this time in mTempPathLengths
            if ((mTouchData.get(mCurrentPath).mTimeForPaths.size() > mCount) && (currentTime - mChrono > mTouchData.get(mCurrentPath).mTimeForPaths.get(mCount))) {
                mSegmentOfPathToDraw = mTouchData.get(mCurrentPath).mTempPathLengths.get(mCount);
                mCount++;
            }

            Log.v("TouchDisplayView", "mSegmentToDraw = " + mSegmentOfPathToDraw);
            Log.v("TouchDisplayView", "mCount = " + mCount);
            Log.v("TouchDisplayView", "mPathMeasure length = " + mPathMeasure.getLength());

            // we draw the right amount of paths. Cast to int in order to avoid bugs
            // when the segment to draw is almost equal to the length of the path measure
            if ((int)mSegmentOfPathToDraw < (int)mPathMeasure.getLength()) {
                // first we draw the previous finished paths (if there are any)
                this.drawFinishedPaths(canvas);

                // we apply the current color and thickness of last path
                mPathPaint.setColor(mTouchData.get(mCurrentPath).mPathColor);
                mPathPaint.setStrokeWidth(mTouchData.get(mCurrentPath).mPathThickness);

                // then we draw the segment of the current animated path
                mSegment.rewind(); // we empty the segment path
                mPathMeasure.getSegment(0, mSegmentOfPathToDraw, mSegment, true); // we add the right path to the segment
                mSegment.rLineTo(0, 0); // workaround for KITKAT and earlier versions, see the doc of getSegment()
                canvas.drawPath(mSegment, mPathPaint); // we draw it
            } else { // else the animation of the path is over
                // if it's not the last path, we will draw the next one next time
                if (mCurrentPath < mTouchData.size()-1) {
                    mCurrentPath++;
                    // then we reassign correct values to temp variables
                    mPathMeasure.setPath(mTouchData.get(mCurrentPath).mPath, false);
                    mSegmentOfPathToDraw = 0;
                    mCount = 0;
                    // we draw the finished paths anyway
                    this.drawFinishedPaths(canvas);
                } else { // else we finished to draw the whole animation
                    mIsAnimationDrawing = false;
                    mAnimationDone = true;
                    for (TouchData touchData: mTouchData) {
                        mPathPaint.setColor(touchData.mPathColor);
                        mPathPaint.setStrokeWidth(touchData.mPathThickness);
                        canvas.drawPath(touchData.mPath, mPathPaint);
                    }
                    Log.i("TouchDisplayView", "Animation done.");
                }
            }
            this.postInvalidate();
        } else { // else we just draw the paths
            if (!mTouchData.isEmpty()) {
                for (TouchData touchData : mTouchData) {
                    mPathPaint.setColor(touchData.mPathColor);
                    mPathPaint.setStrokeWidth(touchData.mPathThickness);
                    canvas.drawPath(touchData.mPath, mPathPaint);
                }
            }
        }
    }

    /**
     * Just draw the previous paths on the canvas (depending on current path)
     * @param canvas Canvas in which we draw
     */
    private void drawFinishedPaths(Canvas canvas) {
        for (int i=0; i < mCurrentPath; i++) {
            // we set the color of the current path, and then we draw it
            mPathPaint.setColor(mTouchData.get(i).mPathColor);
            mPathPaint.setStrokeWidth(mTouchData.get(i).mPathThickness);
            canvas.drawPath(mTouchData.get(i).mPath, mPathPaint);
        }
    }

    /***************** PUBLIC METHODS *****************/

    /**
     * Method called by the main activity in order to change the color to apply for the next path
     * @param color value of the new color
     */
    public void setSelectedColor(int color) {
        mLastSelectedColor = color;
    }

    /**
     * Method called to erase the last drawed path
     */
    public void eraseLastPath() {
        // if there is data on screen
        if (!mTouchData.isEmpty()) {
            // if an animation is running, we stop it
            mIsAnimationDrawing = false;
            // we remove the last data in the touch data array
            mTouchData.remove(mTouchData.size()-1);
            // trigger the redraw
            this.postInvalidate();
            // if the data is now empty, and we received animation, draw it
            if (mTouchData.isEmpty()) {
                launchTempStoredAnimation();
            }
        }
    }

    /**
     * Method called to erase all the paths on screen
     */
    public boolean eraseAllPaths() {
        // if an animation is running, we stop it
        mIsAnimationDrawing = false;
        // we empty the touch data
        mTouchData.clear();
        // trigger the redraw
        this.postInvalidate();
        // if we received data earlier, draw it
        launchTempStoredAnimation();
        return true;
    }

    /**
     * Method called when the button is pressed, in order to launch
     * the animation process
     */
    public void launchAnimation() {
        // if there is no drawing, we display an error notification
        if (mTouchData.isEmpty()) {
            Toast.makeText(getContext(), "Error : no drawing to animate", Toast.LENGTH_LONG).show();
        } else { // else we animate !
            Log.i("TouchDisplayView", "Launching animation !");
            mIsAnimationDrawing = true;

            // and we start the chrono for the animation
            mChrono = java.lang.System.currentTimeMillis();
            mPathMeasure = new PathMeasure(mTouchData.get(0).mPath, false);

            // and we initialize the number of paths to draw
            mSegmentOfPathToDraw = 0;
            mCount = 0;
            mCurrentPath = 0;

            this.postInvalidate();
        }
    }

    /**
     * Method called to launch a received animation by network
     */
    public void launchReceivedAnimation(ArrayList<TouchData> data, String status) {
        Log.i("TouchDisplayView", "Launching received data...");
        // we just replace the current data by the received data, and launch the animation
        // if the current data is null. Else, we store the received data in a temp location
        // in order to display it when the current message has been sent

        if (mTouchData.isEmpty()) {
            // we check if the person is available. If so, we just display the animation.
            // Else, we display the envelope to indicate to the person that she received a message
            // and store the data in a temp variable
            if (status.equals("available")) {
                mTouchData = data;
                launchAnimation();
            } else {
                mTempReceivedData = data;
                mLetterButton.setVisibility(VISIBLE);
            }
        } else {
            mTempReceivedData = data;
        }
    }

    /**
     * Clear the current touch data
     */
    public void clearCurrentTouchData() {
        mTouchData.clear();
        this.postInvalidate();
    }

    /**
     * Method called to launch animation with temp stored data
     */
    public void launchTempStoredAnimation() {
        // if the received data isn't empty
        if (!mTempReceivedData.isEmpty()) {
            mTouchData = new ArrayList<>(mTempReceivedData);
            mTempReceivedData.clear();
            launchAnimation();
        }
    }

    public void setLetterButton(ImageButton letterButton) {
        mLetterButton = letterButton;
    }
}