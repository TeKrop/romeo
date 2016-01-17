package fr.valentinporchet.prototypephil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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

    /**
     * Private variables
     */

    // main variables
    private boolean mIsAnimationDrawing = false;
    private boolean mAnimationDone = true;
    private boolean mIsEraseModeOn = false;

    // containers of paths to draw and to animate
    private ArrayList<TouchData> mTouchData = new ArrayList<>();

    // variables for the path
    private Paint mPathPaint = new Paint();
    private int mLastSelectedColor = 0xFF000000; // black by default
    private float mLastSelectedThickness = 20.f; // normal by default

    // variables used for animation of paths
    private Path mSegment = new Path();
    private PathMeasure mPathMeasure = new PathMeasure();
    private long mChrono = 0;
    private float mSegmentOfPathToDraw;
    private int count;
    private int currentPath; // contains the current path ID

    /**
     * Public structures
     */

    /**
     * Class used to store data about touch events (one path)
     */
    class TouchData {
        public Path mPath = new Path(); // the path
        public ArrayList<Float> mTempPathLengths = new ArrayList<>(); // array containing path length at each move event
        public ArrayList<Long> mTimeForPaths = new ArrayList<>(); // array containing time elapsed at each move event
        public int mPathColor; // color of the path
        public float mPathThickness; // size of the path

        @Override
        public String toString() {
            return "TouchData{" +
                    "mPath=" + mPath +
                    ", mTempPathLengths=" + mTempPathLengths +
                    ", mTimeForPaths=" + mTimeForPaths +
                    ", mPathColor=" + mPathColor +
                    ", mPathThickness=" + mPathThickness +
                    '}';
        }
    }

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
        this.setWillNotDraw(false);

        // Initialize Path Paint
        mPathPaint.setStrokeWidth(mLastSelectedThickness * DENSITY); // thickness of path

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
                Log.i("TouchDisplayView", "Touching down...");
                // if an animation is ongoing, we stop it
                mIsAnimationDrawing = false;

                // if we are on erase mode, we detected if we touched a path
                if (mIsEraseModeOn) {
                    Log.i("TouchDisplayView", "... with eraser mode on.");
                    this.onTouchDownEraserOn(x, y);
                } else {
                    Log.i("TouchDisplayView", "... with drawer mode on.");
                    this.onTouchDownDrawerOn(x, y);
                }
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
     * Function called when we are touching down with eraser mode on
     * @param x x touching coordinates
     * @param y y touching coordinates
     */
    private void onTouchDownEraserOn(float x, float y) {
        int dataToErase = -1; // value used if we must delete a path
        // for all the drawed paths
        for (int i=0; i < mTouchData.size(); i++) {
            // if we touched the current path, we save the
            if (touchedPath(mTouchData.get(i).mPath, x, y)) {
                dataToErase = i;
                break; // we stop the for loop as we found a path to erase
            }
        }
        // if the value is different from -1, we found a path
        // to erase, we delete it from the list
        if (dataToErase != -1) {
            Log.i("TouchDisplayView", "Path found, erasing it...");
            mTouchData.remove(dataToErase);
        }
    }

    /**
     * Used to check if a touched point is in a path
     * @param path Path to check
     * @param eventX X pos of touch event
     * @param eventY Y pos of touch event
     * @return true if the point is in the path
     */
    private boolean touchedPath(Path path, float eventX, float eventY) {
        // we make a bounding rectangle
        RectF rectF = new RectF();
        path.computeBounds(rectF, true);

        // we make a region based on the path and contained in the rectangle
        Region r = new Region();
        r.setPath(path, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));

        // we make another region for the touching area
        Region touch = new Region();
        touch.set((int) eventX - 20, (int) eventY - 20, (int) eventX + 20, (int) eventY + 20);

        // we just return if the regions are colliding each other
        return r.op(touch, Region.Op.INTERSECT);
    }

    /**
     * Function called when we are touching down with drawer mode on
     * @param x x touching coordinates
     * @param y y touching coordinates
     */
    private void onTouchDownDrawerOn(float x, float y) {
        // if we did the animation
        if (mAnimationDone) {
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

        // and we apply the selected color and selected thickness
        mTouchData.get(mTouchData.size()-1).mPathColor = mLastSelectedColor;
        mTouchData.get(mTouchData.size()-1).mPathThickness = mLastSelectedThickness * DENSITY;
    }

    /**
     * Function called when moving and already touching the area
     * @param x x touching coordinates
     * @param y y touching coordinates
     */
    private void onMoveTouch(float x, float y) {
        // if we are not on mode eraser, we update the current path
        if (!mIsEraseModeOn) {
            // just add a line toward the new position
            mTouchData.get(mTouchData.size()-1).mPath.lineTo(x, y);
            // and the time of the event
            mTouchData.get(mTouchData.size()-1).mTimeForPaths.add(java.lang.System.currentTimeMillis() - mChrono);
            // and the current length of the path
            mTouchData.get(mTouchData.size()-1).mTempPathLengths.add(new PathMeasure(mTouchData.get(mTouchData.size()-1).mPath, false).getLength());
        }
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
            if ((mTouchData.get(currentPath).mTimeForPaths.size() > count) && (currentTime - mChrono > mTouchData.get(currentPath).mTimeForPaths.get(count))) {
                mSegmentOfPathToDraw = mTouchData.get(currentPath).mTempPathLengths.get(count);
                count++;
            }

            Log.v("TouchDisplayView", "mSegmentOfPathToDraw : " + mSegmentOfPathToDraw);
            Log.v("TouchDisplayView", "count : " + count);

            // we draw the right amount of paths
            if (mSegmentOfPathToDraw < mPathMeasure.getLength()) {
                // first we draw the previous finished paths (if there are any)
                this.drawFinishedPaths(canvas);

                // we apply the current color and thickness of last path
                mPathPaint.setColor(mTouchData.get(currentPath).mPathColor);
                mPathPaint.setStrokeWidth(mTouchData.get(currentPath).mPathThickness);

                // then we draw the segment of the current animated path
                mSegment.rewind(); // we empty the segment path
                mPathMeasure.getSegment(0, mSegmentOfPathToDraw, mSegment, true); // we add the right path to the segment
                canvas.drawPath(mSegment, mPathPaint); // we draw it
            } else { // else the animation of the path is over
                // if it's not the last path, we will draw the next one next time
                if (currentPath < mTouchData.size()-1) {
                    currentPath++;
                    // then we reassign correct values to temp variables
                    mPathMeasure.setPath(mTouchData.get(currentPath).mPath, false);
                    mSegmentOfPathToDraw = 0;
                    count = 0;
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
        for (int i=0; i < currentPath; i++) {
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
        mIsEraseModeOn = false; // we deactivate the erase mode
        mLastSelectedColor = color;
    }

    public void setSelectedThickness(int size) {
        mIsEraseModeOn = false; // we deactivate the erase mode
        mLastSelectedThickness = size; // thickness of path
        mPathPaint.setStrokeWidth(mLastSelectedThickness ); // applied with the density of the screen
    }

    public void activeEraseMode() {
        mIsEraseModeOn = true;
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
            mIsAnimationDrawing = true;

            // and we start the chrono for the animation
            mChrono = java.lang.System.currentTimeMillis();
            mPathMeasure = new PathMeasure(mTouchData.get(0).mPath, false);

            // and we initialize the number of paths to draw
            mSegmentOfPathToDraw = 0;
            count = 0;
            currentPath = 0;

            this.postInvalidate();
        }
    }
}