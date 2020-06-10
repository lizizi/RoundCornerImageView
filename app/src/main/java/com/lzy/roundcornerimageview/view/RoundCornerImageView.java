package com.lzy.roundcornerimageview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.legacy.widget.Space;

import com.lzy.roundcornerimageview.R;

public class RoundCornerImageView extends androidx.appcompat.widget.AppCompatImageView {
    /**
     * 经测试选用Xfermode实现圆角
     * 1,使用bitmapshader方式会出现有时候不出现圆角的问题 放弃
     * 2,使用clippath方式代码最少,最简单,但是会出现锯齿,因为这种方式没有paint 放弃
     */

    private static final String TAG = "RoundCornerImageView";
    private Paint paint;
    private Xfermode xfermode;
    private Bitmap scaleBitmap; //经缩放后的bitmap,保持和iamgeview 宽高一致
    private RectF roundCornerRectF;

    private boolean isCirCle; //是否为圆形 默认false 为true 忽略圆角设置
    private int allRadius; //所有圆角 优先级最高
    private int topRadius; //上半部左右圆角
    private int bottomRadius; //下半部左右圆角
    private boolean isNeedRecoverTop; //是否需要恢复上半部的左右圆角 恢复就是重新变为直角
    private boolean isNeedRecoverBottom;

    /**
     * 下面属性是扩展属性 暂时不实现
     */
//    private int leftRadius; //左半部上下圆角
//    private int rightRadius; //右半部上下圆角
//    private int leftTopRadius; //左上角圆角
//    private int rightTopRadius; //右上角圆角
//    private int rightBottomRadius; //右下角圆角
//    private int leftBottomRadius; //左下角圆角
    public RoundCornerImageView(Context context) {
        this(context, null);
    }

    public RoundCornerImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundCornerImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        obtainStyles(attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
        xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
//        radius = dp2px(radius);
        setOnPreDrawCallback();
    }

    private void obtainStyles(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.RoundCornerImageView);
        isCirCle = typedArray.getBoolean(R.styleable.RoundCornerImageView_isCircle, false);
        if (isCirCle) {
            typedArray.recycle();
            return;
        }
        allRadius = (int) typedArray.getDimension(R.styleable.RoundCornerImageView_allRadius, 0);
        if (allRadius != 0) {
            typedArray.recycle();
            return;
        }
        topRadius = (int) typedArray.getDimension(R.styleable.RoundCornerImageView_topRadius, 0);
        bottomRadius = (int) typedArray.getDimension(R.styleable.RoundCornerImageView_bottomRadius, 0);
        typedArray.recycle();
    }

    /**
     * 设置onpreDraw回调
     */
    private void setOnPreDrawCallback() {
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                Log.d(TAG, "onPreDraw: ");
                if (getViewTreeObserver().isAlive()) {
                    getViewTreeObserver().removeOnPreDrawListener(this);
                }
                int viewWidth = getMeasuredWidth();
                int viewHeight = getMeasuredHeight();
                setCorner(viewWidth, viewHeight);
                roundCornerRectF = new RectF(0, 0, viewWidth, viewHeight);

                BitmapDrawable bitmapDrawable = (BitmapDrawable) getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap == null) {
                    return true;
                }
                scaleBitmap = Bitmap.createScaledBitmap(bitmap, viewWidth, viewHeight, false);

                return true;
            }
        });
    }

    /**
     * 设置圆角的策略问题
     * @param viewWidth view的宽
     * @param viewHeight view的高
     */
    private void setCorner(int viewWidth, int viewHeight) {
        if (isCirCle) { //默认认为宽高相等
            allRadius = viewWidth / 2;
        } else {
            if (allRadius == 0) {
                if (topRadius != 0 && bottomRadius == 0) {
                    isNeedRecoverBottom = true;
                    allRadius = topRadius;
                }
                if (bottomRadius != 0 && topRadius == 0) {
                    isNeedRecoverTop = true;
                    allRadius = bottomRadius;
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int level = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
        //圆角矩形 dst
        canvas.drawRoundRect(roundCornerRectF, allRadius, allRadius, paint);

        if (isNeedRecoverTop) {
            canvas.drawRect(0, 0, allRadius, allRadius, paint);
            canvas.drawRect(roundCornerRectF.right - allRadius, 0, roundCornerRectF.right, allRadius, paint);
        }

        if (isNeedRecoverBottom) {
            canvas.drawRect(0, roundCornerRectF.bottom - allRadius, allRadius, roundCornerRectF.bottom, paint);
            canvas.drawRect(roundCornerRectF.right - allRadius, roundCornerRectF.bottom - allRadius,
                    roundCornerRectF.right, roundCornerRectF.bottom, paint);
        }

        //设置混合模式
        paint.setXfermode(xfermode);
        //画bitmap src
        if (scaleBitmap != null) {
            canvas.drawBitmap(scaleBitmap, 0, 0, paint);
        }
        // 还原混合模式
        paint.setXfermode(null);
        canvas.restoreToCount(level);
    }

    private int dp2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
