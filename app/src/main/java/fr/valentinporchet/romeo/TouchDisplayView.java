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
import android.widget.ProgressBar;
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
    private final float DEFAULT_THICKNESS = 15.f;
    private final float MAX_TIME_THICKNESS = 1.f;
    private final long MOVE_TIME_MINIMUM = 30; // minimum time between two moves (in ms)

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
    private long mChrono = 0, mTouchChrono = 0, mMoveChrono = 0; // general chrono for animation, and chrono for thickness of the path
    private ProgressBar mDrawingProgressBar;
    private long mMinProgress, mCurrentProgress, mMaxProgress; // used for progressBar. min is useful for responses
    private float mSegmentOfPathToDraw;
    private int mCount;
    private int mCurrentPath; // contains the current path ID

    // letter button
    private ImageButton mLetterButton;
    private ImageButton mLittleEnvelope;
    private PopupView mPopupView;

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
                // if an animation is ongoing, we wait until it's finished
                if (!mIsAnimationDrawing) {
                    this.onTouchDown(x, y);
                    // we initialize the move chrono
                    mMoveChrono = java.lang.System.currentTimeMillis();
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                Log.v("TouchDisplayView", "Moving on " + x + "," + y);
                // if an animation is ongoing, we wait until it's finished
                // else, if the has been done a certain time after the previous registered move
                long currentTime = java.lang.System.currentTimeMillis();
                if ((!mIsAnimationDrawing)&&(currentTime - mMoveChrono >= MOVE_TIME_MINIMUM)) {
                    this.onMoveTouch(x, y);
                    mMoveChrono = currentTime;
                }
                break;
            }

            case MotionEvent.ACTION_UP: {
                Log.i("TouchDisplayView", "Releasing...");
                // For debugging purpose, we display the tab
                Log.v("TouchDisplayView", "Data : " + mTouchData);
                if (!mIsAnimationDrawing) {
                    this.onTouchUp(x, y);
                }
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
        // if the touch data is empty
        if (mTouchData.isEmpty()) {
            // we reinitialize the touch data
            mTouchData.clear();
            mAnimationDone = false;

            // we initialize the chrono
            mChrono = java.lang.System.currentTimeMillis();
        }

        // we add a new touchdata
        mTouchData.add(new TouchData());

        // we apply the first position to the path
        mTouchData.get(mTouchData.size() - 1).mPath.moveTo(x, y);

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

    /**
     * Function called when we are touching up
     * @param x x touching coordinates
     * @param y y touching coordinates
     */
    private void onTouchUp(float x, float y) {
        // if we didn't move since the onTouchDown
        // event, we will draw a dot
        if (mIsFirstTimeMoving) {
            // we set the thickness as default
            mTouchData.get(mTouchData.size()-1).mPathThickness = DEFAULT_THICKNESS * DENSITY;

            // add a new little line
            mTouchData.get(mTouchData.size()-1).mPath.lineTo(x+1, y+1);
            mTouchData.get(mTouchData.size()-1).mTimeForPaths.add(java.lang.System.currentTimeMillis() - mChrono);
            mTouchData.get(mTouchData.size() - 1).mTempPathLengths.add(new PathMeasure(mTouchData.get(mTouchData.size() - 1).mPath, false).getLength());

            mIsFirstTimeMoving = false;
        } // else, don't do anything special
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
            long progress = java.lang.System.currentTimeMillis() - mChrono;
            TouchData currentPath = mTouchData.get(mCurrentPath);

            // we update the progressBar
            mCurrentProgress = (long)(((progress - mMinProgress) / (float)(mMaxProgress - mMinProgress)) * 100);
            mDrawingProgressBar.setProgress((int) mCurrentProgress);

            // if the time that passed is superior to the last amount we have to reach, then
            // we are in a new step of the movement : we update the segment to draw with the value
            // saved for this time in mTempPathLengths
            if ((currentPath.mTimeForPaths.size() > mCount) && (progress > currentPath.mTimeForPaths.get(mCount))) {
                mSegmentOfPathToDraw = currentPath.mTempPathLengths.get(mCount);
                mCount++;
            }

            // we draw the right amount of paths. Cast to int in order to avoid bugs
            // when the segment to draw is almost equal to the length of the path measure
            if ((int)mSegmentOfPathToDraw < (int)mPathMeasure.getLength()) {
                // first we draw the previous finished paths (if there are any)
                this.drawFinishedPaths(canvas);

                // we apply the current color and thickness of last path
                mPathPaint.setColor(currentPath.mPathColor);
                mPathPaint.setStrokeWidth(currentPath.mPathThickness);

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
                    // we hide the progressBar
                    mDrawingProgressBar.setProgress(0);
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
        // if an animation is running, just wait until it's
        // finished before deleting something
        // else, if there is data on screen
        if ((!mIsAnimationDrawing) && (!mTouchData.isEmpty())){
            // we remove the last data in the touch data array
            mTouchData.remove(mTouchData.size() - 1);
            // trigger the redraw
            this.postInvalidate();
        }
    }

    /**
     * Method called to erase all the paths on screen
     */
    public boolean eraseAllPaths() {
        // if an animation is running, just wait until it's
        // finished before deleting something
        if (!mIsAnimationDrawing) {
            // we empty the touch data
            mTouchData.clear();
            // trigger the redraw
            this.postInvalidate();
        }
        return true;
    }

    /**
     * Method called when we launch a classic animation process
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

            // and get the value of the max time for animation
            TouchData last = mTouchData.get(mTouchData.size()-1);
            mMinProgress = 0;
            mMaxProgress = last.mTimeForPaths.get(last.mTimeForPaths.size()-1);

            this.postInvalidate();
        }
    }

    /**
     * Method called when we received a response drawing, in order to animate it
     * @param data received data
     */
    public void launchResponseAnimation(ArrayList<TouchData> data) {
        Log.i("TouchDisplayView", "Launching animation !");

        // first, we check that the current data isn't the same that the one we received
        // if the last uuid of both paths is the same, it's the same draw
        if (!mTouchData.get(mTouchData.size()-1).uuid.equals(data.get(data.size()-1).uuid)) {
            mIsAnimationDrawing = true;
            // we set the current path to draw to the beginning of new drawing
            mCurrentPath = mTouchData.size();
            // then we update the stored data with the new one
            mTouchData = data;

            // we put the chrono to the current time - the time elapsed for the animation
            TouchData current = mTouchData.get(mCurrentPath);
            mChrono = java.lang.System.currentTimeMillis() - current.mTimeForPaths.get(0);
            mPathMeasure = new PathMeasure(current.mPath, false);

            // and initialize the number of paths to draw
            mCount = 0;
            mSegmentOfPathToDraw = current.mTempPathLengths.get(mCount);

            // and get the value of the max time for animation
            TouchData last = mTouchData.get(mTouchData.size()-1);
            mMinProgress = current.mTimeForPaths.get(0);
            mMaxProgress = last.mTimeForPaths.get(last.mTimeForPaths.size()-1);

            this.postInvalidate();
        }
    }

    /**
     * Method called to launch a received animation by network
     */
    public void launchReceivedAnimation(ArrayList<TouchData> data, boolean isUserActive) {
        Log.i("TouchDisplayView", "Analyzing received data...");
        // we just replace the current data by the received data, and launch the animation
        // if the current data is null. Else we will do some checking...
        if (mTouchData.isEmpty()) {
            // we check if the person is available. If so, we just display the animation.
            // Else, we display the envelope to indicate to the person that she received a message
            // and store the data in a temp variable
            if (isUserActive) {
                Log.i("TouchDisplayView", "Launching received data now !");
                mTouchData = data;
                launchAnimation();
            } else {
                Log.i("TouchDisplayView", "Storing data and displaying envelope...");
                mTempReceivedData = data;
                mLetterButton.setVisibility(VISIBLE);
            }
        } else {
            // we check if the received data corresponds to the actual displayed data + new data.
            // if so, just display the new data. Else, store the data in temp location
            int myDataLength = mTouchData.size();
            boolean isResponseMessage = true;

            // if the current data is longer than the received data, it's
            // obviously not a response
            if (myDataLength > data.size()) {
                isResponseMessage = false;
            } else { // else we will do some checks
                for (int i=0; i < myDataLength; i++) {
                    // if the two data aren't equals (not the same uuid), it's a brand new data,
                    // and not a response message, we store it
                    if (!mTouchData.get(i).uuid.equals(data.get(i).uuid)) {
                        isResponseMessage = false; break;
                    }
                }
            }

            if (isResponseMessage) {
                Log.i("TouchDisplayView", "It's a response data ! Displaying...");
                // we only animate the new data, and immediately display the saved data
                launchResponseAnimation(data);
            } else {
                Log.i("TouchDisplayView", "Brand new data, but board full : storing...");
                mTempReceivedData = data;
                mLittleEnvelope.setVisibility(VISIBLE);
            }
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
            // if the board is empty, just launch the animation on it
            if (mTouchData.isEmpty()) {
                mTouchData = new ArrayList<>(mTempReceivedData);
                mTempReceivedData.clear();
                launchAnimation();
            } else { // else display in a popup
                mPopupView.launchAnimation(mTempReceivedData);
            }
        }
    }

    public void setLetterButton(ImageButton letterButton) {
        mLetterButton = letterButton;
    }

    public void setDrawingProgressBar(ProgressBar drawingProgressBar) {
        mDrawingProgressBar = drawingProgressBar;
    }

    public void setLittleEnvelope(ImageButton littleEnvelope) {
        mLittleEnvelope = littleEnvelope;
    }

    public void setPopupView(PopupView popupView) {
        mPopupView = popupView;
    }
}