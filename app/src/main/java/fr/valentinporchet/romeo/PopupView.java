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
public class PopupView extends View {
    /**
     * Private constants
     */
    private final float DENSITY = getResources().getDisplayMetrics().density;
    private final float DEFAULT_THICKNESS = 15.f;

    /**
     * Private variables
     */

    // main variables
    private boolean mIsAnimationDrawing = false;
    private boolean mAnimationDone = true;

    private ProgressBar mDrawingProgressBar;
    private long mMinProgress, mCurrentProgress, mMaxProgress; // used for progressBar. min is useful for responses

    // containers of paths to draw and to animate
    private ArrayList<TouchData> mTouchData = new ArrayList<>();

    // variables for the path
    private Paint mPathPaint = new Paint();

    // variables used for animation of paths
    private Path mSegment = new Path();
    private PathMeasure mPathMeasure = new PathMeasure();
    private long mChrono = 0; // general chrono for animation
    private float mSegmentOfPathToDraw;
    private int mCount;
    private int mCurrentPath; // contains the current path ID

    /**
     * Constructor of the PopupView class
     * @param context Context
     * @param attrs Attributes
     */
    public PopupView(Context context, AttributeSet attrs) {
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
                Log.i("PopupView", "Touching down...");
                // if an animation is ongoing, we wait until it's finished
                if (!mIsAnimationDrawing) {
                    this.setVisibility(INVISIBLE);
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                Log.v("PopupView", "Moving on " + x + "," + y);
                break;
            }

            case MotionEvent.ACTION_UP: {
                Log.i("PopupView", "Releasing...");
                break;
            }
        }

        // trigger redraw on UI thread
        this.postInvalidate();
        return true;
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
                    Log.i("PopupView", "Animation done.");
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
     * Method called when we launch a classic animation process
     */
    public void launchAnimation(ArrayList<TouchData> data) {
        mTouchData = new ArrayList<>(data);
        // if there is no drawing, we display an error notification
        if (mTouchData.isEmpty()) {
            Toast.makeText(getContext(), "Error : no drawing to animate", Toast.LENGTH_LONG).show();
        } else { // else we animate !
            Log.i("PopupView", "Launching animation !");
            this.setVisibility(VISIBLE);
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

    public void setDrawingProgressBar(ProgressBar drawingProgressBar) {
        mDrawingProgressBar = drawingProgressBar;
    }
}