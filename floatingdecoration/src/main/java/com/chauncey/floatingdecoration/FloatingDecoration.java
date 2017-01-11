package com.chauncey.floatingdecoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.view.View;

import com.chauncey.decoration.R;

/**
 * Created by Chauncey on 2017/1/4 .
 */

public class FloatingDecoration extends RecyclerView.ItemDecoration {

    private DecorationCallback mDecorationCallback;
    private Paint.FontMetrics mMetrics;
    private Drawable mDividingLine;
    private TextPaint mTextPaint;
    private Paint mPaint;
    private int mLabelHeight;
    private Context mContext;

    public FloatingDecoration(Context context, DecorationCallback callback) {
        mContext = context;
        mDecorationCallback = callback;
        mTextPaint = new TextPaint();
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.sp_14));
        mTextPaint.setAntiAlias(true);
        mTextPaint.setFakeBoldText(true);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mLabelHeight = context.getResources().getDimensionPixelOffset(R.dimen.dp_25);

        mPaint = new Paint();
        mPaint.setColor(Color.LTGRAY);

        mMetrics = mTextPaint.getFontMetrics();
    }

    public void setDividingLine(@NonNull Drawable line) {
        mDividingLine = line;
    }

    public void enableFakeBoldText(boolean b) {
        mTextPaint.setFakeBoldText(b);
    }

    public void setLabelTextSize(int textSize) {
        mTextPaint.setTextSize(textSize);
    }

    public void setLabelTextAlign(Paint.Align align) {
        mTextPaint.setTextAlign(align);
    }

    public void setLabelTextColor(@ColorInt int color) {
        mTextPaint.setColor(color);
    }

    public void setLabelTextColorRes(@ColorRes int color) {
        mTextPaint.setColor(ContextCompat.getColor(mContext, color));
    }

    public void setLabelTextPaint(@NonNull TextPaint textPaint) {
        mTextPaint = textPaint;
        mMetrics = textPaint.getFontMetrics();
    }

    public void setFloatingBarHeight(int height) {
        mLabelHeight = height;
    }

    public void setFloatingBackgroundColor(@ColorInt int color) {
        mPaint.setColor(color);
    }

    public void setFloatingBarBackgroundColorRes(@ColorRes int color) {
        mPaint.setColor(ContextCompat.getColor(mContext, color));
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int pos = parent.getChildAdapterPosition(view);
        String groupLabel = mDecorationCallback.getGroupLabel(pos);
        if (groupLabel == null) return;
        if (isFirstInGroup(pos))
            outRect.top = mLabelHeight;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);

        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        int childCount = parent.getChildCount();
        if (mDividingLine != null) {
            drawDividingLine(c, parent, left, right, childCount);
        }
        drawLabelText(c, parent, left, right, childCount);
    }

    private void drawDividingLine(Canvas canvas, RecyclerView parent, int left, int right, int childCount) {
        for (int i = 0; i < childCount; i++) {
            View view = parent.getChildAt(i);
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
            int top = view.getBottom() + lp.bottomMargin;
            int bottom = top + mDividingLine.getIntrinsicHeight();
            mDividingLine.setBounds(left, top, right, bottom);
            mDividingLine.draw(canvas);
            int position = parent.getChildAdapterPosition(view);
            if (isFirstInGroup(position)) {
                top = view.getTop();
                bottom = top + mDividingLine.getIntrinsicHeight();
                mDividingLine.setBounds(left, top, right, bottom);
                mDividingLine.draw(canvas);
            }
        }
    }

    private void drawLabelText(Canvas canvas, RecyclerView parent, int left, int right, int childCount) {
        for (int i = 0; i < childCount; i++) {
            View view = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(view);
            if (isFirstInGroup(position)) {
                String groupLabel = mDecorationCallback.getGroupLabel(position);
                if (groupLabel == null) return;
                int bottom = view.getTop();
                int top = bottom - mLabelHeight;
                if(mDividingLine == null)
                {
                    canvas.drawRect(left, top , right, bottom, mPaint);
                }else {
                    canvas.drawRect(left, top + mDividingLine.getIntrinsicHeight() , right, bottom, mPaint);
                }
                canvas.drawText(groupLabel, left + 30, bottom - mLabelHeight / 2 + (float) getLabelHeight() / 4, mTextPaint);
            }
        }
    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(canvas, parent, state);
        int position = ((LinearLayoutManager) (parent.getLayoutManager())).findFirstVisibleItemPosition();
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        String label = mDecorationCallback.getGroupLabel(position);
        if (label == null) return;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (isLastInGroup(position)) {
                int bottom = child.getBottom();
                if (bottom <= mLabelHeight) {
                    canvas.drawRect(left, 0, right, bottom, mPaint);
                    if (mDividingLine != null) {
                        mDividingLine.setBounds(left, bottom , right, bottom + mDividingLine.getIntrinsicHeight());
                        mDividingLine.draw(canvas);
                    }
                    canvas.drawText(label, 30, mLabelHeight / 2 + (float) getLabelHeight() / 4 - (mLabelHeight - bottom), mTextPaint);
                    return;
                }
            }
        }
        canvas.drawRect(left, 0, right, mLabelHeight, mPaint);
        if (mDividingLine != null) {
            mDividingLine.setBounds(left, mLabelHeight, right, mLabelHeight + mDividingLine.getIntrinsicHeight());
            mDividingLine.draw(canvas);
        }
        canvas.drawText(label, 30, mLabelHeight / 2 + (float) getLabelHeight() / 4, mTextPaint);
    }

    private boolean isFirstInGroup(int pos) {
        if (pos == 0) {
            return true;
        } else {
            String prevLabel = mDecorationCallback.getGroupLabel(pos - 1);
            String label = mDecorationCallback.getGroupLabel(pos);

            if (prevLabel.equals(label)) {
                return false;
            } else {
                return true;
            }
        }
    }

    private boolean isLastInGroup(int pos) {

        String label = mDecorationCallback.getGroupLabel(pos);
        String nextLabel;
        try {
            nextLabel = mDecorationCallback.getGroupLabel(pos + 1);
        } catch (ArrayIndexOutOfBoundsException exception) {
            return true;
        }

        if (!label.equals(nextLabel)) return true;

        return false;
    }

    private double getLabelHeight() {
        return Math.ceil(mMetrics.bottom - mMetrics.top);
    }

    public interface DecorationCallback {
        String getGroupLabel(int position);
    }
}
