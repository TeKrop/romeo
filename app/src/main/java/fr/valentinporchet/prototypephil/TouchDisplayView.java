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
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

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
     * Private constants
     */

    // radius of active touch circle in dp
    private static final float CIRCLE_RADIUS_DP = 15f;
    private static final float INACTIVE_BORDER_DP = 10f;
    private static final int INACTIVE_BORDER_COLOR = 0xFFFFD060;
    private static final float PRESSURE = 1f;
    private static final int[] COLORS = {
            0xFF33B5E5, 0xFFAA66CC, 0xFF99CC00, 0xFFFFBB33, 0xFFFF4444,
            0xFF0099CC, 0xFF9933CC, 0xFF669900, 0xFFFF8800, 0xFFCC0000
    };

    /**
     * Private variables
     */

    // main variables
    private boolean mHasTouch = false;
    private Date date = new Date();
    private ArrayList<TouchHistory> history = new ArrayList<TouchHistory>();
    private ArrayList<PathHistory> pathHistory = new ArrayList<PathHistory>();

    // calculated radius in px
    private float mCircleRadius;
    private int mColor;
    private float mRadius;
    private Paint mCirclePaint = new Paint();

    private float mPathWidth;
    private Paint mPathPaint = new Paint();

    private float mBorderWidth;
    private Paint mBorderPaint = new Paint();

    /**
     * Inner classes
     */
    final class TouchHistory {
        private float mX;
        private float mY;
        private long mTime;

        public TouchHistory(float x, float y, long elapsedTime) {
            mX = x; mY = y; mTime = elapsedTime;
        }

        public float getX() {
            return mX;
        }

        public float getY() {
            return mY;
        }

        public long getTime() {
            return mTime;
        }
    }

    final class PathHistory {
        private float mX, mY, mWidth, mHeight, mRotation;

        public PathHistory(float startX, float endX, float startY, float endY) {
            mX = startX;
            mY = startY - mRadius;
            mHeight = mRadius * 2;
            mWidth = (float)Math.sqrt(Math.abs(endX - startX) + Math.abs(endY - startY));
            mRotation = (float) Math.tan(Math.abs(endY - startY) / Math.abs(endX - startX));
            System.out.println("startX=" + startX + ", startY=" + startY + ", endX=" + endX + ", endY=" + endY + ", diffY=" + Math.abs(endY - startY) + ", diffX=" + Math.abs(endX - startX));
        }

        public float getX() {
            return mX;
        }

        public float getY() {
            return mY;
        }

        public float getWidth() {
            return mWidth;
        }

        public float getHeight() {
            return mHeight;
        }

        public float getRotation() {
            return mRotation;
        }

        @Override
        public String toString() {
            return "PathHistory{" +
                    "mX=" + mX +
                    ", mY=" + mY +
                    ", mWidth=" + mWidth +
                    ", mHeight=" + mHeight +
                    ", mRotation=" + mRotation +
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
                // first pressed gesture has started
                mHasTouch = true;
                // we generate a random number (make sure it positive)
                int random = new Random().nextInt();
                random = Math.max(random, -1*random);
                // we determine the color
                mColor = COLORS[random % COLORS.length];
                // we apply the color to the circle and path paints
                mCirclePaint.setColor(mColor);
                mPathPaint.setColor(mColor);
                // we add the first point
                history.add(new TouchHistory(event.getX(), event.getY(), date.getTime()));
                break;
            }

            case MotionEvent.ACTION_UP: {
                mHasTouch = false;
                // we empty the list
                history.clear();
                pathHistory.clear();
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                // we add the new event
                history.add(new TouchHistory(event.getX(), event.getY(), date.getTime()));

                // and the new path event
                TouchHistory beforeLast = history.get(history.size() - 2);
                PathHistory newPath = new PathHistory(beforeLast.getX(), beforeLast.getY(), event.getX(), event.getY());
                pathHistory.add(newPath);
                System.out.println(newPath);
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

        // draw inactive border
        canvas.drawRect(mBorderWidth, mBorderWidth, getWidth() - mBorderWidth, getHeight()
                - mBorderWidth, mBorderPaint);

        // Canvas background color depends on whether there is an active touch
        if (mHasTouch) {
            for (TouchHistory point : history) {
                canvas.drawCircle(point.getX(), point.getY(), mRadius, mCirclePaint);
            }

            for (PathHistory path : pathHistory) {
                canvas.save();
                canvas.rotate(path.getRotation() * (float) Math.PI / 180, path.getX(), path.getY() + mRadius);
                canvas.drawRect(path.getX(), path.getY(), path.getWidth(), path.getHeight(), mPathPaint);
                canvas.restore();
            }
        }
    }

    /*
     * Below are only helper methods and variables required for drawing.
     */

    /**
     * Sets up the required {@link android.graphics.Paint} objects for the screen density of this
     * device.
     */
    private void initialisePaint() {
        // Calculate radius in px from dp based on screen density

        // Initialize Circle Paint
        float density = getResources().getDisplayMetrics().density;
        mCircleRadius = CIRCLE_RADIUS_DP * density;
        mRadius = PRESSURE * mCircleRadius;

        // Initialize Path Paint
        mPathWidth = CIRCLE_RADIUS_DP * 2 * density;
        mPathPaint.setStrokeWidth(mPathWidth);
        mPathPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        // Setup paint for inactive border
        mBorderWidth = INACTIVE_BORDER_DP * density;
        mBorderPaint.setStrokeWidth(mBorderWidth);
        mBorderPaint.setColor(INACTIVE_BORDER_COLOR);
        mBorderPaint.setStyle(Paint.Style.STROKE);
    }
}

