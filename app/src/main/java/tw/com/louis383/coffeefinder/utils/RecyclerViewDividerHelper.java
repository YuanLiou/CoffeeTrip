package tw.com.louis383.coffeefinder.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import tw.com.louis383.coffeefinder.R;

/**
 * Created by louis383 on 2017/2/26.
 */

public class RecyclerViewDividerHelper extends RecyclerView.ItemDecoration {

    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;
    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

    private Drawable divider;
    private int orientation;
    private boolean showFirstDivider, showLastDivider;

    public RecyclerViewDividerHelper(Context context, int orientation) {
        divider = ContextCompat.getDrawable(context, R.drawable.divider);
        setOrientation(orientation);
    }

    public RecyclerViewDividerHelper(Context context, int orientation, boolean showFirstDivider, boolean showLastDivider) {
        this(context, orientation);
        this.showFirstDivider = showFirstDivider;
        this.showLastDivider = showLastDivider;
    }

    public RecyclerViewDividerHelper(Context context, int orientation, int resId) {
        divider = ContextCompat.getDrawable(context, resId);
        setOrientation(orientation);
    }

    public RecyclerViewDividerHelper(Context context, int orientation, int resId, boolean showFirstDivider, boolean showLastDivider) {
        this(context, orientation, resId);
        this.showFirstDivider = showFirstDivider;
        this.showLastDivider = showLastDivider;
    }

    private void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public void setColor(int colorId) {
        divider.setColorFilter(colorId, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (divider != null) {
            if (orientation == VERTICAL_LIST) {
                drawVertical(c, parent);
            } else {
                drawHorizontal(c, parent);
            }
        } else {
            super.onDraw(c, parent, state);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (divider == null) {
            return;
        }

        if (parent.getChildAdapterPosition(view) < 0) {
            return;
        }

        if (orientation == VERTICAL_LIST) {
            outRect.top = divider.getIntrinsicHeight();
        } else {
            outRect.left = divider.getIntrinsicWidth();
        }
    }

    private void drawVertical(Canvas canvas, RecyclerView parent) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        int childCount = parent.getChildCount();

        for (int index = showFirstDivider ? 0 : 1; index < childCount; index++) {
            View child = parent.getChildAt(index);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int top = child.getTop() - params.topMargin;
            int bottom = top + divider.getIntrinsicHeight();
            divider.setBounds(left, top, right, bottom);
            divider.draw(canvas);
        }

        if (showLastDivider && childCount > 0) {
            drawLastVertical(canvas, parent, left, right);
        }
    }

    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        int top = parent.getPaddingTop();
        int bottom = parent.getHeight() - parent.getPaddingBottom();
        int childCount = parent.getChildCount();
        for (int index = showFirstDivider ? 0 : 1; index < childCount; index++) {
            View child = parent.getChildAt(index);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int left = child.getLeft() - params.leftMargin;
            int right = left + divider.getIntrinsicWidth();
            divider.setBounds(left, top, right, bottom);
            divider.draw(canvas);
        }

        if (showLastDivider && childCount > 0) {
            drawLastHorizontal(canvas, parent, top, bottom);
        }
    }

    private void drawLastVertical(Canvas canvas, RecyclerView parent, int left, int right) {
        int childCount = parent.getChildCount();
        View child = parent.getChildAt(childCount - 1);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
        int top = child.getBottom() + params.bottomMargin;
        int bottom = top + divider.getIntrinsicHeight();
        divider.setBounds(left, top, right, bottom);
        divider.draw(canvas);
    }

    private void drawLastHorizontal(Canvas canvas, RecyclerView parent, int top, int bottom) {
        int childCount = parent.getChildCount();
        View child = parent.getChildAt(childCount - 1);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
        int left = child.getRight() + params.rightMargin;
        int right = left + divider.getIntrinsicWidth();
        divider.setBounds(left, top, right, bottom);
        divider.draw(canvas);
    }
}
