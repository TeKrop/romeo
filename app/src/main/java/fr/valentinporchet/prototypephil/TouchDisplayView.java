package fr.valentinporchet.prototypephil;
/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * View that shows touch events and their history. This view demonstrates the
 * use of {@link #onTouchEvent(android.view.MotionEvent)} and {@link android.view.MotionEvent}s to keep
 * track of touch pointers across events.
 */
public class TouchDisplayView extends View {
    /**
     * Public variables and constants
     */
    public static float PATH_THICKNESS = 15f; // thickness (without DENSITY of screen)
    public static int ANIMATION_SPEED = 1000; // paths per second

    /**
     * Private constants
     */
    private final float DENSITY = getResources().getDisplayMetrics().density;
    private final int[] COLORS = {
            0xFF33B5E5, 0xFFAA66CC, 0xFF99CC00, 0xFFFFBB33, 0xFFFF4444,
            0xFF0099CC, 0xFF9933CC, 0xFF669900, 0xFFFF8800, 0xFFCC0000
    };
    private final int BACKGROUND_COLOR = 0xFFFFFFFF;

    /**
     * Private variables
     */

    // main variables
    private boolean mIsDrawing = false;
    private boolean mIsAnimationDrawing = false;
    private Date mDate = new Date();

    // containers of paths to draw and to animate
    private ArrayList<Path> mPathsToDraw = new ArrayList<Path>();

    // variables for the path
    private float mPathThickness; // thickness of the path
    private int mColor; // color of the path
    private Paint mPathPaint = new Paint();

    // temp variables
    private Path mPath = null, mSegment = null;

    // measure of path, need it in order to animate
    private PathMeasure mPathMeasure = null;
    private long mChrono = 0;

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
        // Calculate radius in px from dp based on screen density

        // Initialize Path Paint
        mPathThickness = PATH_THICKNESS * DENSITY;

        mPathPaint.setDither(true);
        mPathPaint.setStrokeWidth(mPathThickness); // thickness of path
        mPathPaint.setStyle(Paint.Style.STROKE); // style of path
        mPathPaint.setStrokeJoin(Paint.Join.ROUND); // jointures between elements of path
        mPathPaint.setStrokeCap(Paint.Cap.ROUND); // start and end of path
    }

    /*
     * Main event handling function
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final int action = event.getAction();

        /*
         * Switch on the action. The action is extracted from the event by
         * applying the MotionEvent.ACTION_MASK. Alternatively a call to
         * event.getActionMasked() would yield in the action as well.
         */
        switch (action & MotionEvent.ACTION_MASK) {

            // Do subfunctions for each in the future
            case MotionEvent.ACTION_DOWN: {
                // if an animation is ongoing, we stop it
                mIsAnimationDrawing = false;
                // first pressed gesture has started
                mIsDrawing = true;

                // we initialize the array and the path
                mPathsToDraw.clear();
                mPath = new Path();

                // we generate a random number (make sure it positive)
                int random = new Random().nextInt();
                random = Math.max(random, -1*random);
                // we determine the color
                mColor = COLORS[random % COLORS.length];
                // we apply the color to the circle and path paints
                mPathPaint.setColor(mColor);

                // we apply the new element to the path
                mPath.moveTo(event.getX(), event.getY());
                mPathsToDraw.add(mPath);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                mPath.lineTo(event.getX(), event.getY());
                break;
            }

            case MotionEvent.ACTION_UP: {
                // we stopped to draw and will start to animate
                mIsDrawing = false;
                mIsAnimationDrawing = true;

                // we create the pathMeasure for our path
                mPathMeasure = new PathMeasure(mPathsToDraw.get(0), false);
                // and we start the chrono
                mChrono = java.lang.System.currentTimeMillis();

                break;
            }
        }

        // trigger redraw on UI thread
        this.postInvalidate();
        return true;
    }

    /*
     * Main drawing function
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mIsAnimationDrawing) {
            long currentTime = java.lang.System.currentTimeMillis();
            int numberOfPathsToDraw = 1 + (int)(((currentTime - mChrono) * ANIMATION_SPEED) / 1000);
            float length = (int) mPathMeasure.getLength();

            // we draw the right amount of paths
            if (numberOfPathsToDraw < length) {
                mSegment = new Path();
                mPathMeasure.getSegment(0, numberOfPathsToDraw, mSegment, true);
                canvas.drawPath(mSegment, mPathPaint);
            } else { // else the animation is over
                mIsAnimationDrawing = false;
                // we draw the paths anyway
                for (Path path: mPathsToDraw) {
                    canvas.drawPath(path, mPathPaint);
                }
            }
            this.postInvalidate();
        } else {
            for (Path path: mPathsToDraw) {
                canvas.drawPath(path, mPathPaint);
            }
        }
    }

    /**
     * Triggered method when the application is paused
     */
    public void pause() {

    }

    /**
     * Triggered method when the application is resumed after paused
     */
    public void resume() {

    }

    /**
     * Method called by the main activity when changing the value on the seekbar
     * @param progress new thickness value
     */
    public void updatePathThickness(int progress) {
        PATH_THICKNESS = progress;
        mPathThickness = PATH_THICKNESS * DENSITY;
        mPathPaint.setStrokeWidth(mPathThickness);
    }

    /**
     * Method called by the main activity when changing the value of animation speed
     * @param progress new animation speed value
     */
    public void updateAnimationSpeed(int progress) {
        ANIMATION_SPEED = progress;
    }
}

