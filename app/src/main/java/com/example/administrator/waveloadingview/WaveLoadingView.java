package com.example.administrator.waveloadingview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by lw on 2016/10/10.
 * 功能描述:
 * 水波纹进度框
 */

public class WaveLoadingView extends View {
    /**
     * Y方向上的每次增长值
     */
    private int increateHeight;
    /**
     * X方向上的每次增长值
     */
    private final int INCREATE_WIDTH = 0x00000005;
    /**
     * 画笔
     */
    private Paint mPaint;
    /**
     * 临时画布
     */
    private Canvas mTempCanvas;
    /**
     * 贝塞尔曲线路径
     */
    private Path mBezierPath;

    /**
     * 当前波纹的y值
     */
    private float mWaveY;
    /**
     * 贝塞尔曲线控制点距离原点x的增量
     */
    private float mBezierDiffX;
    /**
     * 水波纹的X左边是否在增长
     */
    private boolean mIsXDiffIncrease = true;
    /**
     * 水波纹最低控制点y
     */
    private float mWaveLowestY;
    /**
     * 来源图片
     */
    private Bitmap mOriginalBitmap;
    /**
     * 来源图片的宽度
     */
    private int mOriginalBitmapWidth;
    /**
     * 来源图片的高度
     */
    private int mOriginalBitmapHeight;
    /**
     * 临时图片
     */
    private Bitmap mTempBitmap;
    /**
     * 组合图形
     */
    private Bitmap mCombinedBitmap;

    /**
     * 是否测量过
     */
    private boolean mIsMeasured = false;
    /**
     * 停止重绘
     */
    private boolean mStopInvalidate = false;

    public WaveLoadingView(Context context) {
        super(context);
    }

    public WaveLoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (null == mTempBitmap) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        float widthRatio = 1f, heightRatio = 1f;
        if (MeasureSpec.AT_MOST == widthMode) {
            widthSize = mTempBitmap.getWidth() + getPaddingLeft() + getPaddingRight();
        }
        if (MeasureSpec.AT_MOST == heightMode) {
            heightSize = mTempBitmap.getHeight() + getPaddingLeft() + getPaddingRight();
        }
        //只在首次绘制的时候进行onDraw()操作前的初始化
        if (!mIsMeasured) {
            if (MeasureSpec.EXACTLY == widthMode) {
                widthRatio = (float) (widthSize - getPaddingLeft() - getPaddingRight()) / mTempBitmap.getWidth();
            }
            if (MeasureSpec.EXACTLY == widthMode) {
                heightRatio = (float) (heightSize - getPaddingTop() - getPaddingBottom()) / mTempBitmap.getHeight();
            }
            initDraw(mTempBitmap, widthRatio, heightRatio);
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mCombinedBitmap == null) {
            return;
        }
        combinedBitMap();
        //从左上角开始绘图（需要计算padding值）
        canvas.drawBitmap(mCombinedBitmap, getPaddingLeft(), getPaddingTop(), null);
        if (!mStopInvalidate)
            //重绘
            invalidate();
    }


    /**
     * 初始化Draw所需数据
     *
     * @param tempBitmap
     * @param widthRatio
     * @param heightRatio
     */
    private void initDraw(Bitmap tempBitmap, float widthRatio, float heightRatio) {
        mOriginalBitmap = scaleBitmap(tempBitmap, widthRatio, heightRatio);
        initData();
        if (null == mPaint)
            initPaint();
        initCanvas();
        mIsMeasured = true;
    }

    /**
     * 初始化绘画曲线和左边所需的一些变量值
     */
    private void initData() {
        mOriginalBitmapWidth = mOriginalBitmap.getWidth();
        mOriginalBitmapHeight = mOriginalBitmap.getHeight();
        mWaveY = mOriginalBitmapHeight;
        mBezierDiffX = INCREATE_WIDTH;
        mWaveLowestY = 1.4f * mOriginalBitmapHeight;
        increateHeight = mOriginalBitmapHeight / 100;
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mPaint = new Paint();
        mBezierPath = new Path();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
    }
    /**
     * 初始化画布讲2个图层绘画至mCombinedBitmap
     */
    private void initCanvas() {
        mTempCanvas = new Canvas();
        mCombinedBitmap = Bitmap.createBitmap(mOriginalBitmapWidth + getPaddingLeft() + getPaddingRight(),
                mOriginalBitmapHeight + getPaddingTop() + getPaddingBottom(), Bitmap.Config.ARGB_8888);
        mTempCanvas.setBitmap(mCombinedBitmap);
    }


    /**
     * 合成bitmap
     */
    private void combinedBitMap() {
        mCombinedBitmap.eraseColor(Color.parseColor("#00ffffff"));
        mTempCanvas.drawBitmap(mOriginalBitmap, 0, 0, mPaint);
        //取两层交集显示在上层
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        drawWaveBitmap();
    }

    private void checkIncrease(float mBezierDiffX) {
        if (mIsXDiffIncrease) {
            mIsXDiffIncrease = mBezierDiffX > 0.5 * mOriginalBitmapWidth ? !mIsXDiffIncrease : mIsXDiffIncrease;
        } else {
            mIsXDiffIncrease = mBezierDiffX < 10 ? !mIsXDiffIncrease : mIsXDiffIncrease;
        }
    }

    /**
     * 计算path，绘画水波纹图层
     */
    private void drawWaveBitmap() {
        mBezierPath.reset();
        if (mIsXDiffIncrease) {
            mBezierDiffX += INCREATE_WIDTH;
        } else {
            mBezierDiffX -= INCREATE_WIDTH;
        }
        checkIncrease(mBezierDiffX);
        if (mWaveY >= 0) {
            mWaveY -= increateHeight;
            mWaveLowestY -= increateHeight;
        } else {
            //还原坐标
            mWaveY = mOriginalBitmapHeight;
            mWaveLowestY = 1.2f * mOriginalBitmapHeight;
        }
        //曲线路径
        mBezierPath.moveTo(0, mWaveY);
        mBezierPath.cubicTo(
                mBezierDiffX, mWaveY - (mWaveLowestY - mWaveY),
                mBezierDiffX + mOriginalBitmapWidth / 2, mWaveLowestY,
                mOriginalBitmapWidth, mWaveY);
        //竖直线
        mBezierPath.lineTo(mOriginalBitmapWidth, mOriginalBitmapHeight);
        //横直线
        mBezierPath.lineTo(0, mOriginalBitmapHeight);
        mBezierPath.close();
        mTempCanvas.drawPath(mBezierPath, mPaint);
        mPaint.setXfermode(null);
    }


    /**
     * 按比例缩放图片
     *
     * @param origin      原图
     * @param widthRatio  width缩放比例
     * @param heightRatio heigt缩放比例
     * @return 新的bitmap
     */
    private Bitmap scaleBitmap(Bitmap origin, float widthRatio, float heightRatio) {
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(widthRatio, heightRatio);
        Bitmap newBitmap = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBitmap.equals(origin)) {
            return newBitmap;
        }
        origin.recycle();
        origin = null;
        return newBitmap;
    }


    /**
     * 设置原始图片资源
     *
     * @param resId
     */
    public void setOriginalImage(@DrawableRes int resId) {
        mTempBitmap = BitmapFactory.decodeResource(getResources(), resId);
        mIsMeasured = false;
        requestLayout();
    }

    /**
     * 设置最终生成图片的填充颜色资源
     *
     * @param color
     */
    public void setWaveColor(@ColorInt int color) {
        if (null == mPaint)
            initPaint();
        mPaint.setColor(color);
    }

    /**
     * 停止/开启 重绘
     *
     * @param mStopInvalidate
     */
    public void setmStopInvalidate(boolean mStopInvalidate) {
        this.mStopInvalidate = mStopInvalidate;
        if (!mStopInvalidate)
            invalidate();
    }

}
