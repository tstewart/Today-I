package io.github.tstewart.todayi.events;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/* Listens for Swipe performed events from View.OnTouchListener */
public abstract class OnSwipePerformedListener implements View.OnTouchListener {

    GestureDetector mGestureDetector;
    Context mContext;

    protected OnSwipePerformedListener(Context context) {
        this.mGestureDetector = new GestureDetector(context, new SwipeGestureDetector(context, this));
        this.mContext = context;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    public abstract boolean onTouch(MotionEvent event);

    /**
     * Called when a swipe was detected
     *
     * @param direction Direction the swipe was performed (left/right)
     */
    public abstract void onSwipe(SwipeDirection direction);

    /* The direction a successful swipe was performed in */
    public enum SwipeDirection {
        LEFT,
        RIGHT
    }

    /* Handles onFling events and calculates if the fling was valid, and the fling direction */
    public static class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {

        /* Minimum percentage of the total supported fling velocity that counts as an adequate fling */
        private static final float MIN_SWIPE_VELOCITY_PERCENTAGE = 0.05f;
        /* Minimum percentage distance of the screen size that counts as an adequate fling */
        private static final float MIN_SWIPE_DISTANCE_PERCENTAGE = 0.3f;
        private final Context mContext;
        private final OnSwipePerformedListener mListener;

        public SwipeGestureDetector(Context context, OnSwipePerformedListener listener) {
            this.mContext = context;
            this.mListener = listener;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return mListener.onTouch(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mContext != null) {
                float flingStart = e1.getX();
                float flingEnd = e2.getX();

                /* Get the velocity of the fling as a relative percentage to the maximum fling velocity for the phone */
                float relativeVelocityPercent = getRelativeVelocityPercentage(velocityX);

                /* Get percentage of the overall screen width swiped */
                float screenSwipedPercent = getScreenDistanceSwiped(flingStart, flingEnd);

                /* If velocity and screen swipe distances are greater than minimum, then this is a valid fling. */
                if (Math.abs(relativeVelocityPercent) > MIN_SWIPE_VELOCITY_PERCENTAGE
                        && Math.abs(screenSwipedPercent) > MIN_SWIPE_DISTANCE_PERCENTAGE) {
                    /* If percentage of screen swiped was positive, user swiped right */
                    if (screenSwipedPercent > 0) mListener.onSwipe(SwipeDirection.RIGHT);
                        /* Otherwise, user swiped left */
                    else mListener.onSwipe(SwipeDirection.LEFT);
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        /**
         * Get the velocity of the fling as a relative percentage to the maximum fling velocity for the phone
         *
         * @param velocity Speed of the swipe
         * @return Speed relative to phone's maximum detectable velocity
         */
        private float getRelativeVelocityPercentage(float velocity) {
            /* Maximum velocity the user's phone uses to constitute a fling */
            float maxVelocity = ViewConfiguration.get(mContext).getScaledMaximumFlingVelocity();
            return velocity / maxVelocity;
        }

        /**
         * Get percentage of the overall screen width swiped
         *
         * @param xStart Position swipe started at
         * @param xEnd   Position swipe ended at
         * @return Distance swiped relative to total screen width
         */
        private float getScreenDistanceSwiped(float xStart, float xEnd) {
            /* Get information about the phone's display */
            DisplayMetrics display = mContext.getResources().getDisplayMetrics();
            /* Get display width */
            float width = display.widthPixels;

            /* Divide distance travelled by total width */
            return (Math.abs(xEnd) - Math.abs(xStart)) / width;
        }
    }

}
