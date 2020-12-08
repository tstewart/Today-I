package io.github.tstewart.todayi.events;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/* Listens for Swipe performed events from View.OnTouchListener */
public class OnSwipePerformedListener implements View.OnTouchListener {

    GestureDetector mGestureDetector;
    Context mContext;

    public OnSwipePerformedListener(Context context) {
        this.mGestureDetector = new GestureDetector(context, new SwipeGestureDetector(context,this));
        this.mContext = context;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.performClick();
        return mGestureDetector.onTouchEvent(event);
    }

    public void onSwipe(SwipeDirection direction) {}

    /* Handles onFling events and calculates if the fling was valid, and the fling direction */
    public static class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {

        private final Context mContext;
        private final OnSwipePerformedListener mListener;

        /* Minimum percentage of the total supported fling velocity that counts as an adequate fling */
        private static final float MIN_SWIPE_VELOCITY_PERCENTAGE = 0.05f;
        /* Minimum percentage distance of the screen size that counts as an adequate fling */
        private static final float MIN_SWIPE_DISTANCE_PERCENTAGE = 0.3f;

        public SwipeGestureDetector(Context context, OnSwipePerformedListener listener) {
            this.mContext = context;
            this.mListener = listener;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(mContext != null) {
                float flingStart = e1.getX();
                float flingEnd = e2.getX();

                /* Get the velocity of the fling as a relative percentage to the maximum fling velocity for the phone */
                float relativeVelocityPercent = getRelativeVelocityPercentage(velocityX);

                /* Get percentage of the overall screen width swiped */
                float screenSwipedPercent = getScreenDistanceSwiped(flingStart, flingEnd);

                /* If velocity and screen swipe distances are greater than minimum, then this is a valid fling. */
                if(Math.abs(relativeVelocityPercent) > MIN_SWIPE_VELOCITY_PERCENTAGE
                && Math.abs(screenSwipedPercent) > MIN_SWIPE_DISTANCE_PERCENTAGE) {
                    /* If percentage of screen swiped was positive, user swiped right */
                    if(screenSwipedPercent>0) mListener.onSwipe(SwipeDirection.RIGHT);
                    /* Otherwise, user swiped left */
                    else mListener.onSwipe(SwipeDirection.LEFT);
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        /* Get the velocity of the fling as a relative percentage to the maximum fling velocity for the phone */

        private float getRelativeVelocityPercentage(float velocity) {
            /* Maximum velocity the user's phone uses to constitute a fling */
            float maxVelocity = ViewConfiguration.get(mContext).getScaledMaximumFlingVelocity();
            return velocity/maxVelocity;
        }

        /* Get percentage of the overall screen width swiped */
        private float getScreenDistanceSwiped(float xStart, float xEnd) {
            DisplayMetrics display = mContext.getResources().getDisplayMetrics();
            float width = display.widthPixels;

            return (Math.abs(xEnd) - Math.abs(xStart)) / width;
        }
    }

    public enum SwipeDirection {
        LEFT,
        RIGHT
    }

}
