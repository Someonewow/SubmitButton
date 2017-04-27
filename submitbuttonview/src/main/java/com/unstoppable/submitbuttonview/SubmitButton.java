package com.unstoppable.submitbuttonview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

/**
 * Created by Unstoppable on 2016/12/31.
 */

public class SubmitButton extends View {

    private static final int STATE_NONE = 0;
    private static final int STATE_SUBMIT = 1;
    private static final int STATE_LOADING = 2;
    private static final int STATE_RESULT = 3;

    //view状态
    private int viewState = STATE_NONE;

    //View宽高
    private int mWidth;
    private int mHeight;

    private int MAX_WIDTH;
    private int MAX_HEIGHT;

    //画布坐标原点
    private int x, y;

    private String buttonText = "";
    private int buttonColor;
    private int succeedColor;
    private int failedColor;
    private int textSize;

    //文本宽高
    private int textWidth;
    private int textHeight;

    private Paint bgPaint, loadingPaint, resultPaint, textPaint;

    private Path buttonPath;
    private Path loadPath;
    private Path dst;
    private PathMeasure pathMeasure;
    private Path resultPath;

    private RectF circleLeft, circleMid, circleRight;

    private float loadValue;

    private ValueAnimator submitAnim, loadingAnim, resultAnim;

    private boolean isDoResult;
    private boolean isSucceed;

    private static final int STYLE_LOADING = 0;
    private static final int STYLE_PROGRESS = 1;

    //view加载等待模式
    private int progressStyle = STYLE_LOADING;
    private float currentProgress;

    public SubmitButton(Context context) {
        this(context, null);
    }

    public SubmitButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SubmitButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SubmitButton, defStyleAttr, 0);
        if (typedArray.getString(R.styleable.SubmitButton_buttonText) != null) {
            buttonText = typedArray.getString(R.styleable.SubmitButton_buttonText);
        }
        buttonColor = typedArray.getColor(R.styleable.SubmitButton_buttonColor, Color.parseColor("#19CC95"));
        succeedColor = typedArray.getColor(R.styleable.SubmitButton_succeedColor, Color.parseColor("#19CC95"));
        failedColor = typedArray.getColor(R.styleable.SubmitButton_failedColor, Color.parseColor("#FC8E34"));
        textSize = (int) typedArray.getDimension(R.styleable.SubmitButton_buttonTextSize, sp2px(15));
        progressStyle = typedArray.getInt(R.styleable.SubmitButton_progressStyle, STYLE_LOADING);
        typedArray.recycle();
        init();
    }

    /**
     * 初始化Paint、Path
     */
    private void init() {
        bgPaint = new Paint();
        bgPaint.setColor(buttonColor);
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeWidth(5);
        bgPaint.setAntiAlias(true);

        loadingPaint = new Paint();
        loadingPaint.setColor(buttonColor);
        loadingPaint.setStyle(Paint.Style.STROKE);
        loadingPaint.setStrokeWidth(9);
        loadingPaint.setAntiAlias(true);

        resultPaint = new Paint();
        resultPaint.setColor(Color.WHITE);
        resultPaint.setStyle(Paint.Style.STROKE);
        resultPaint.setStrokeWidth(9);
        resultPaint.setStrokeCap(Paint.Cap.ROUND);
        resultPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(buttonColor);
        textPaint.setStrokeWidth(textSize / 6);
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);

        textWidth = getTextWidth(textPaint, buttonText);
        textHeight = getTextHeight(textPaint, buttonText);

        buttonPath = new Path();
        loadPath = new Path();
        resultPath = new Path();
        dst = new Path();
        circleMid = new RectF();
        circleLeft = new RectF();
        circleRight = new RectF();
        pathMeasure = new PathMeasure();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = textWidth + 100;
        }

        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = (int) (textHeight * 2.5);
        }

        if (heightSize > widthSize) {
            heightSize = (int) (widthSize * 0.25);
        }

        mWidth = widthSize - 10;
        mHeight = heightSize - 10;
        x = (int) (widthSize * 0.5);
        y = (int) (heightSize * 0.5);
        MAX_WIDTH = mWidth;
        MAX_HEIGHT = mHeight;

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(x, y);
        drawButton(canvas);
        if (viewState == STATE_NONE || viewState == STATE_SUBMIT && mWidth > textWidth) {
            drawButtonText(canvas);
        }
        if (viewState == STATE_LOADING) {
            drawLoading(canvas);
        }
        if (viewState == STATE_RESULT) {
            drawResult(canvas, isSucceed);
        }
    }

    /**
     * 绘制初始状态Button
     *
     * @param canvas 画布
     */
    private void drawButton(Canvas canvas) {
        buttonPath.reset();
        circleLeft.set(-mWidth / 2, -mHeight / 2, -mWidth / 2 + mHeight, mHeight / 2);
        buttonPath.arcTo(circleLeft, 90, 180);
        buttonPath.lineTo(mWidth / 2 - mHeight / 2, -mHeight / 2);
        circleRight.set(mWidth / 2 - mHeight, -mHeight / 2, mWidth / 2, mHeight / 2);
        buttonPath.arcTo(circleRight, 270, 180);
        buttonPath.lineTo(-mWidth / 2 + mHeight / 2, mHeight / 2);
        canvas.drawPath(buttonPath, bgPaint);
    }

    /**
     * 绘制加载状态Button
     *
     * @param canvas 画布
     */
    private void drawLoading(Canvas canvas) {
        dst.reset();
        circleMid.set(-MAX_HEIGHT / 2, -MAX_HEIGHT / 2, MAX_HEIGHT / 2, MAX_HEIGHT / 2);
        loadPath.addArc(circleMid, 270, 359.999f);
        pathMeasure.setPath(loadPath, true);
        float startD = 0f, stopD;
        if (progressStyle == STYLE_LOADING) {
            startD = pathMeasure.getLength() * loadValue;
            stopD = startD + pathMeasure.getLength() / 2 * loadValue;
        } else {
            stopD = pathMeasure.getLength() * currentProgress;
        }
        pathMeasure.getSegment(startD, stopD, dst, true);
        canvas.drawPath(dst, loadingPaint);
    }

    /**
     * 绘制结果状态Button
     *
     * @param canvas 画布
     */
    private void drawResult(Canvas canvas, boolean isSucceed) {
        if (isSucceed) {
            resultPath.moveTo(-mHeight / 6, 0);
            resultPath.lineTo(0, (float) (-mHeight / 6 + (1 + Math.sqrt(5)) * mHeight / 12));
            resultPath.lineTo(mHeight / 6, -mHeight / 6);
        } else {
            resultPath.moveTo(-mHeight / 6, mHeight / 6);
            resultPath.lineTo(mHeight / 6, -mHeight / 6);
            resultPath.moveTo(-mHeight / 6, -mHeight / 6);
            resultPath.lineTo(mHeight / 6, mHeight / 6);
        }
        canvas.drawPath(resultPath, resultPaint);
    }

    /**
     * 绘制Button文本
     *
     * @param canvas 画布
     */
    private void drawButtonText(Canvas canvas) {
        textPaint.setAlpha(((mWidth - textWidth) * 255) / (MAX_WIDTH - textWidth));
        canvas.drawText(buttonText, -textWidth / 2, getTextBaseLineOffset(), textPaint);
    }

    /**
     * 开始提交动画
     */
    private void startSubmitAnim() {
        viewState = STATE_SUBMIT;
        submitAnim = new ValueAnimator().ofInt(MAX_WIDTH, MAX_HEIGHT);
        submitAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mWidth = (int) animation.getAnimatedValue();
                if (mWidth == mHeight) {
                    bgPaint.setColor(Color.parseColor("#DDDDDD"));
                }
                invalidate();
            }
        });
        submitAnim.setDuration(300);
        submitAnim.setInterpolator(new AccelerateInterpolator());
        submitAnim.start();
        submitAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isDoResult) {
                    startResultAnim();
                } else {
                    startLoadingAnim();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    /**
     * 开始加载动画
     */
    private void startLoadingAnim() {
        viewState = STATE_LOADING;
        if (progressStyle == STYLE_PROGRESS) {
            return;
        }
        loadingAnim = new ValueAnimator().ofFloat(0.0f, 1.0f);
        loadingAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                loadValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        loadingAnim.setDuration(2000);
        loadingAnim.setRepeatCount(ValueAnimator.INFINITE);
        loadingAnim.start();
    }

    /**
     * 开始结果动画
     */
    private void startResultAnim() {
        viewState = STATE_RESULT;
        if (loadingAnim != null) {
            loadingAnim.cancel();
        }
        resultAnim = new ValueAnimator().ofInt(MAX_HEIGHT, MAX_WIDTH);
        resultAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mWidth = (int) animation.getAnimatedValue();
                resultPaint.setAlpha(((mWidth - mHeight) * 255) / (MAX_WIDTH - MAX_HEIGHT));
                if (mWidth == mHeight) {
                    if (isSucceed) {
                        bgPaint.setColor(succeedColor);
                    } else {
                        bgPaint.setColor(failedColor);
                    }
                    bgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                }
                invalidate();
            }
        });
        resultAnim.setDuration(300);
        resultAnim.setInterpolator(new AccelerateInterpolator());
        resultAnim.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (viewState == STATE_NONE) {
                    startSubmitAnim();
                }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 设置submit结果
     *
     * @param isSucceed 是否成功
     */
    public void doResult(boolean isSucceed) {
        if (viewState == STATE_NONE || viewState == STATE_RESULT || isDoResult) {
            return;
        }
        isDoResult = true;
        this.isSucceed = isSucceed;
        if (viewState == STATE_LOADING) {
            startResultAnim();
        }
    }

    /**
     * 恢复初始化Button状态
     */
    public void reset() {
        if (submitAnim != null) {
            submitAnim.cancel();
        }
        if (loadingAnim != null) {
            loadingAnim.cancel();
        }
        if (resultAnim != null) {
            resultAnim.cancel();
        }
        viewState = STATE_NONE;
        mWidth = MAX_WIDTH;
        mHeight = MAX_HEIGHT;
        isSucceed = false;
        isDoResult = false;
        currentProgress = 0;
        init();
        invalidate();
    }

    /**
     * 设置进度
     *
     * @param progress 进度值 (0-100)
     */
    public void setProgress(int progress) {
        if (progress < 0 || progress > 100) {
            return;
        }
        currentProgress = (float) (progress * 0.01);
        if (progressStyle == STYLE_PROGRESS && viewState == STATE_LOADING) {
            invalidate();
        }
    }

    /**
     * sp to dp
     *
     * @param sp
     * @return dp
     */
    private int sp2px(float sp) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * fontScale + 0.5f);
    }

    /**
     * 计算水平居中的baseline
     *
     * @return
     */
    private float getTextBaseLineOffset() {
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        return -(fm.bottom + fm.top) / 2;
    }

    /**
     * 获取Text高度
     *
     * @param paint
     * @param str   文本内容
     * @return
     */
    private int getTextHeight(Paint paint, String str) {
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, str.length(), rect);
        return rect.height();
    }

    /**
     * 获取Text宽度
     *
     * @param paint
     * @param str   文本内容
     * @return
     */
    private int getTextWidth(Paint paint, String str) {
        int mRet = 0;
        if (str != null && str.length() > 0) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                mRet += (int) Math.ceil(widths[j]);
            }
        }
        return mRet;
    }
}