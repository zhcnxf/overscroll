package org.taosha.android.widget;

import android.graphics.Rect;
import android.support.v4.view.ViewConfigurationCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Created by San on 9/16/15.
 */
class OverScrollTouchHelper {
    private final View view;
    private final int mTouchSlop;

    private float mLastMotionX, mLastMotionY;
    private int mActivePointerId;
    private boolean mIsBeingDragged;
    private final Rect mScrollBounds;

    public OverScrollTouchHelper(View view) {
        this.view = view;
        final ViewConfiguration configuration = ViewConfiguration.get(view.getContext());
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
        mScrollBounds = new Rect();
    }

    public boolean onTouchEvent(MotionEvent ev) {
        final View view = this.view;
        final int action = ev.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                // Remember where the motion event started
                mLastMotionX = ev.getX();
                mLastMotionY = ev.getY();
                mActivePointerId = ev.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_MOVE:
                final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(activePointerIndex);
                final float y = ev.getY(activePointerIndex);
                final int deltaX = (int) (mLastMotionX - x);
                final int deltaY = (int) (mLastMotionY - y);

                if (!mIsBeingDragged) {
                    if (Math.abs(deltaX) > mTouchSlop || Math.abs(deltaY) > mTouchSlop) {
                        mIsBeingDragged = true;
                    }
                }

                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    mLastMotionX = x;
                    mLastMotionY = y;

                    float oldScrollX = view.getScrollX();
                    float oldScrollY = view.getScrollY();
                    float scrollX = oldScrollX + deltaX;
                    float scrollY = oldScrollY + deltaY;

                    // Clamp values if at the limits and record
                    if (view instanceof ScrollRange) {
                        final Rect scrollBounds = mScrollBounds;
                        ((ScrollRange) view).getScrollBounds(scrollBounds);

                        if (scrollX < scrollBounds.left) {
                            scrollX = scrollBounds.left;
                        } else if (scrollX > scrollBounds.right) {
                            scrollX = scrollBounds.right;
                        }

                        if (scrollY < scrollBounds.top) {
                            scrollY = scrollBounds.top;
                        } else if (scrollY > scrollBounds.bottom) {
                            scrollY = scrollBounds.bottom;
                        }
                    }
                    view.scrollTo((int) (scrollX), (int) scrollY);
                }
                break;
        }
        return true;
    }

    public interface ScrollRange {
        void getScrollBounds(Rect outBounds);
    }
}
