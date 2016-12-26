package com.alms.expand;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout.LayoutParams;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Alamusi on 2016/11/2.
 */
public class AutoExpandImageView extends View implements View.OnClickListener, View.OnTouchListener {

    /**
     * 是否展开，true为折叠，false为展开
     */
    private boolean mFold;
    /**
     * 是否可操作，当动画未结束时不可操作
     */
    private boolean mCanOperate;
    /**
     * 控件高度
     */
    private int mHeight;
    /**
     * 控件未展开时的宽度
     */
    private int mMinWidth;
    /**
     * 控件展开时的宽度
     */
    private int mMaxWidth;
    /**
     * 画笔
     */
    private Paint mPaint;
    /**
     * 绘画的区域
     */
    private Rect mSrcRect;
    /**
     * 可以绘画的区域
     */
    private Rect mDestRect;
    /**
     * 图片数据Bitmap
     */
    private Bitmap mBitmap;
    /**
     * 计时器
     */
    private Timer mTimer;
    /**
     * 倒计时任务
     */
    private TimerTask mHideTask;
    /**
     * 下载事件监听器
     */
    private OnClickListener mListener;
    /**
     * 折叠动画
     */
    private ObjectAnimator mFoldAnim;
    /**
     * 展开动画
     */
    private ObjectAnimator mUnFoldAnim;

    public AutoExpandImageView(Context context) {
        this(context, null);
    }

    public AutoExpandImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoExpandImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(Color.TRANSPARENT);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        mPaint.setDither(true);

        mTimer = new Timer();
        mFold = true;
        mCanOperate = true;

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        setOnClickListener(this);
        setOnTouchListener(this);
    }

    /**
     * set minWidth & maxWidth with px
     *
     * @param minWidth min width of view
     * @param maxWidth max width of view
     */
    public void setWidth(int minWidth, int maxWidth, int height) {
        mMinWidth = minWidth;
        mMaxWidth = maxWidth;
        mHeight = height;
        mSrcRect = new Rect(0, 0, mMinWidth, mHeight);
        mDestRect = new Rect(0, 0, mMinWidth, mHeight);

    }

    /**
     * set bitmap source to show
     *
     * @param bitmap source of bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        int bmpWidth = bitmap.getWidth();
        int bmpHeight = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.postScale((mMaxWidth / (float) bmpWidth), mHeight / (float) bmpHeight);
        mBitmap = Bitmap.createBitmap(bitmap, 0, 0, bmpWidth, bmpHeight, matrix, true);
    }

    /**
     * set listener of click download app
     *
     * @param listener download app listener
     */
    public void setDownloadListener(OnClickListener listener) {
        mListener = listener;
    }

    /**
     * 旋转屏幕的时候如果在展开状态折叠
     */
    private void reset() {
        if (!mFold) {
            removeHideTask();
            LayoutParams lp = (LayoutParams) getLayoutParams();
            lp.width = mMinWidth;
            setLayoutParams(lp);
            mFold = true;
        }
    }

    /**
     * 折叠状态下执行展开动画，展开状态下执行折叠动画
     */
    private void performAnimation() {
        AnimatorListenerAdapter animListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCanOperate = true;
                mFold = !mFold;
                addHideTask();
            }
        };
        if (mFold) {
            final int start = getLeft();
            final int to = start - (mMaxWidth - mMinWidth);
            mUnFoldAnim = ObjectAnimator.ofInt(this, "left", start, to);
            mUnFoldAnim.setDuration(200);
            mUnFoldAnim.setInterpolator(new LinearInterpolator());
            mUnFoldAnim.addListener(animListener);
            mUnFoldAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int curPosition = (int) valueAnimator.getAnimatedValue();
                    int newWidth = mMinWidth + (start - curPosition);
                    LayoutParams lp = (LayoutParams) getLayoutParams();
                    lp.width = newWidth;
                    setLayoutParams(lp);
                }
            });
            mUnFoldAnim.start();
        } else {
            final int start = getLeft();
            final int to = start + (mMaxWidth - mMinWidth);
            mFoldAnim = ObjectAnimator.ofInt(this, "left", start, to);
            mFoldAnim.setDuration(200);
            mFoldAnim.setInterpolator(new LinearInterpolator());
            mFoldAnim.addListener(animListener);
            mFoldAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int curPosition = (int) valueAnimator.getAnimatedValue();
                    int newWidth = (mMinWidth + (to - curPosition));
                    LayoutParams lp = (LayoutParams) getLayoutParams();
                    lp.width = newWidth;
                    setLayoutParams(lp);
                }
            });
            mFoldAnim.start();
        }
    }

    @Override
    public void onClick(View view) {
        removeHideTask();
        if (!mCanOperate) {
            return;
        }
        mCanOperate = false;
        if (!mFold && mListener != null) {
            mListener.onClickView();
        }
        performAnimation();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        removeHideTask();
        if (!mCanOperate) {
            return true;
        }
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP:
                addHideTask();
                break;
        }
        return false;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        reset();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDestRect.right = w;
        mSrcRect.right = w;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, mSrcRect, mDestRect, mPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeHideTask();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mFoldAnim != null) {
            mFoldAnim.cancel();
            mFoldAnim = null;
        }
        if (mUnFoldAnim != null) {
            mUnFoldAnim.cancel();
            mUnFoldAnim = null;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = mMinWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = mHeight;
        }
        setMeasuredDimension(width, height);
    }

    /**
     * remove auto hide timerTask
     */
    private void removeHideTask() {
        if (mHideTask != null) {
            mHideTask.cancel();
            mHideTask = null;
        }
    }

    /**
     * add auto hide timerTask when non touch on view
     */
    private void addHideTask() {
        if (mFold) {
            return;
        }
        if (mHideTask != null) {
            mHideTask.cancel();
            mHideTask = null;
        }
        mHideTask = new TimerTask() {
            @Override
            public void run() {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        performAnimation();
                    }
                });
            }
        };
        if (mTimer != null) {
            mTimer.schedule(mHideTask, 5 * 1000);
        }
    }

    /**
     * click download app interface
     */
    public interface OnClickListener {
        void onClickView();
    }
}
