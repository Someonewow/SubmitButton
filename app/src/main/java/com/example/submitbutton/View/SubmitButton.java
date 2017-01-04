package com.example.submitbutton.View;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Sampler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Toast;

import com.example.submitbutton.R;

/**
 * Created by Unstoppable on 2016/12/31.
 */

public class SubmitButton extends View {

    private static final String TAG = "SubmitButton";

    //View宽高
    private int mWidth;
    private int mHeight;
    private int buttonWidth;

    private Paint bgPaint;
    private Paint loadingPaint;
    private Paint resultPaint;

    private Path buttonPath;
    private Path loadPath;
    private Path dst;
    private PathMeasure pathMeasure;

    private Path resultPath1;
    private Path resultPath2;

    private RectF oval1;
    private RectF oval2;

    private Handler mHandler;

    private boolean isSucceed;

    /**
     * View动画状态 0-初始状态（无动画），1-点击，2-等待，3-结果
     */
    private int ViewState = 0;

    private ValueAnimator clickAnimator;
    private ValueAnimator loadingAnimator;
    private ValueAnimator resultAnimator;

    private float loadValue;


    public SubmitButton(Context context) {
        this(context, null);
    }

    public SubmitButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SubmitButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 初始化Paint
     */
    private void initPaint() {
        bgPaint = new Paint();
        bgPaint.setColor(getResources().getColor(R.color.green1));
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeWidth(mHeight / 20);
        bgPaint.setAntiAlias(true);

        loadingPaint = new Paint();
        loadingPaint.setColor(getResources().getColor(R.color.green1));
        loadingPaint.setStyle(Paint.Style.STROKE);
        loadingPaint.setStrokeWidth(mHeight / 20);
        loadingPaint.setAntiAlias(true);

        resultPaint = new Paint();
        resultPaint.setColor(getResources().getColor(R.color.green1));
        resultPaint.setStyle(Paint.Style.STROKE);
        resultPaint.setStrokeWidth(mHeight / 20);
        resultPaint.setAntiAlias(true);
    }

    private void initRecF() {
        oval1 = new RectF();
        oval2 = new RectF();
    }

    private void initPath() {
        buttonPath = new Path();
        loadPath = new Path();
        dst = new Path();
        RectF oval = new RectF(-mHeight / 2, -mHeight / 2, mHeight / 2, mHeight / 2);
        loadPath.addArc(oval, 270, 359.999f);
        pathMeasure = new PathMeasure();
        pathMeasure.setPath(loadPath, true);
    }

    private void initValueAnimator() {
        Animator.AnimatorListener listener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                switch (ViewState) {
                    case 1:
                        ViewState = 2;
                        mHandler.sendEmptyMessage(0);
                        break;
                    case 2:
                        mHandler.sendEmptyMessage(0);
                        break;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };

        clickAnimator = new ValueAnimator().ofInt(mWidth, mHeight);
        clickAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                buttonWidth = (int) animation.getAnimatedValue();
                if (buttonWidth == mHeight) {
                    bgPaint.setColor(getResources().getColor(R.color.gray));
                }
                invalidate();
            }
        });
        clickAnimator.setDuration(500);
        clickAnimator.addListener(listener);

        loadingAnimator = new ValueAnimator().ofFloat(0.0f, 1.0f);
        loadingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                loadValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        loadingAnimator.setDuration(2000);
        loadingAnimator.addListener(listener);

        resultAnimator = new ValueAnimator().ofInt(mHeight, mWidth);
        resultAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                buttonWidth = (int) animation.getAnimatedValue();
                resultPaint.setAlpha(((buttonWidth - mHeight) * 255) / (mWidth - mHeight));
                if (buttonWidth == mHeight) {
                    bgPaint.setColor(getResources().getColor(R.color.green1));
                }
                invalidate();
            }
        });
        resultAnimator.setDuration(500);
        resultAnimator.addListener(listener);
    }

    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (ViewState) {
                    case 1:
                        clickAnimator.start();
                        break;
                    case 2:
                        loadingAnimator.start();
                        break;
                    case 3:
                        resultAnimator.start();
                        break;
                }
            }
        };
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w - h / 20;
        mHeight = h - h / 20;
        buttonWidth = mWidth;

        initPaint();
        initRecF();
        initPath();
        initValueAnimator();
        initHandler();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate((mWidth + mHeight / 20) / 2, (mHeight + mHeight / 20) / 2);

        oval1.set(-buttonWidth / 2, -mHeight / 2, -buttonWidth / 2 + mHeight, mHeight / 2);
        oval2.set(buttonWidth / 2 - mHeight, -mHeight / 2, buttonWidth / 2, mHeight / 2);

        buttonPath.reset();
        buttonPath.arcTo(oval1, 90, 180);
        buttonPath.lineTo(buttonWidth / 2 - mHeight / 2, -mHeight / 2);
        buttonPath.arcTo(oval2, 270, 180);
        buttonPath.lineTo(-buttonWidth / 2 + mHeight / 2, mHeight / 2);

        canvas.drawPath(buttonPath, bgPaint);

        if (ViewState == 2) {
            dst.reset();
            float startD = pathMeasure.getLength() * loadValue;
            float stopD = startD + pathMeasure.getLength() / 2 * loadValue;
            pathMeasure.getSegment(startD, stopD, dst, true);
            canvas.drawPath(dst, loadingPaint);
        }
        if (ViewState == 3) {
            if (isSucceed) {
                canvas.drawPath(resultPath1, resultPaint);
            } else {
                canvas.drawPath(resultPath1, resultPaint);
                canvas.drawPath(resultPath2, resultPaint);
            }

        }
    }

    public void doClickAnimation() {
        if (ViewState != 0) {
            return;
        }
        ViewState = 1;
        mHandler.sendEmptyMessage(0);
    }


    public void doResultAnimation(boolean isSucceed) {
        if (ViewState != 2) {
            return;
        }
        this.isSucceed = isSucceed;
        if (isSucceed) {
            resultPath1 = new Path();
            resultPath1.moveTo(-mHeight / 6, 0);
            resultPath1.lineTo(0, (float) (-mHeight / 6 + (1 + Math.sqrt(5)) * mHeight / 12));
            resultPath1.lineTo(mHeight / 6, -mHeight / 6);
        } else {
            resultPath1 = new Path();
            resultPath2 = new Path();
            resultPath1.moveTo(-mHeight / 6, mHeight / 6);
            resultPath1.lineTo(mHeight / 6, -mHeight / 6);
            resultPath2.moveTo(-mHeight / 6, -mHeight / 6);
            resultPath2.lineTo(mHeight / 6, mHeight / 6);
        }
        ViewState = 3;
        mHandler.sendEmptyMessage(0);
    }
}
