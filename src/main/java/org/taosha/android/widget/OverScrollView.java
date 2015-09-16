package org.taosha.android.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.taosha.android.apps.overscroll.R;

public class OverScrollView extends FrameLayout implements OverScrollTouchHelper.ScrollRange {

    public static final int ATTACH_TO_LEFT = 1;
    public static final int ATTACH_TO_TOP = 2;
    public static final int ATTACH_TO_RIGHT = 3;
    public static final int ATTACH_TO_BOTTOM = 4;

    private static final int DEFAULT_CHILD_GRAVITY = Gravity.TOP | Gravity.START;

    private OverScrollTouchHelper mTouchHelper = new OverScrollTouchHelper(this);

    public OverScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public OverScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public OverScrollView(Context context) {
        this(context, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mTouchHelper.onTouchEvent(ev);
    }

    @Override protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        final ViewGroup.LayoutParams lp = child.getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, getPaddingLeft() + getPaddingRight(), lp.width);
        final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(parentHeightMeasureSpec), MeasureSpec.UNSPECIFIED);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin + widthUsed, lp.width);
        final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(parentHeightMeasureSpec), MeasureSpec.UNSPECIFIED);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutChildren(left, top, right, bottom, false);
    }


    void layoutChildren(int left, int top, int right, int bottom, boolean forceLeftGravity) {
        final int count = getChildCount();

        // Region for children,
        final int parentLeft = getPaddingLeft();
        final int parentRight = right - left - getPaddingRight();
        final int parentTop = getPaddingTop();
        final int parentBottom = bottom - top - getPaddingBottom();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                int childLeft;
                int childTop;

                int gravity = lp.gravity;
                if (gravity == -1) {
                    gravity = DEFAULT_CHILD_GRAVITY;
                }

                final int layoutDirection = ViewCompat.getLayoutDirection(this);
                final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
                final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                if (lp.attachTo == ATTACH_TO_LEFT) {
                    childLeft = -width - lp.rightMargin;
                } else if (lp.attachTo == ATTACH_TO_RIGHT) {
                    childLeft = right - left + lp.leftMargin;
                } else {
                    switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                        case Gravity.CENTER_HORIZONTAL:
                            childLeft = parentLeft + (parentRight - parentLeft - width) / 2 + lp.leftMargin - lp.rightMargin;
                            break;
                        case Gravity.RIGHT:
                            if (!forceLeftGravity) {
                                childLeft = parentRight - width - lp.rightMargin;
                                break;
                            }
                        case Gravity.LEFT:
                        default:
                            childLeft = parentLeft + lp.leftMargin;
                    }
                }

                if (lp.attachTo == ATTACH_TO_TOP) {
                    childTop = -height - lp.bottomMargin;
                } else if (lp.attachTo == ATTACH_TO_BOTTOM) {
                    childTop = bottom - left + lp.topMargin;
                } else {
                    switch (verticalGravity) {
                        default:
                        case Gravity.TOP:
                            childTop = parentTop + lp.topMargin;
                            break;
                        case Gravity.CENTER_VERTICAL:
                            childTop = parentTop + (parentBottom - parentTop - height) / 2 + lp.topMargin - lp.bottomMargin;
                            break;
                        case Gravity.BOTTOM:
                            childTop = parentBottom - height - lp.bottomMargin;
                            break;
                    }
                }
                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }
        }
    }

    @Override public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override public void getScrollBounds(final Rect outBounds) {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            outBounds.left = Math.min(outBounds.left, child.getLeft() + lp.leftMargin);
            outBounds.top = Math.min(outBounds.top, child.getTop() + lp.topMargin);
            outBounds.right = Math.max(outBounds.right, child.getRight() + lp.rightMargin);
            outBounds.bottom = Math.max(outBounds.bottom, child.getBottom() + lp.bottomMargin);
        }
        outBounds.right -= getWidth();
        outBounds.bottom -= getHeight();
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {

        public int attachTo;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            final TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.OverScrollView_Layout, 0, 0);
            attachTo = a.getInt(R.styleable.OverScrollView_Layout_layout_attachTo, 0);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height, gravity);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        @TargetApi(Build.VERSION_CODES.KITKAT) public LayoutParams(FrameLayout.LayoutParams source) {
            super(source);
        }
    }

}
