package com.dmsherazi.cctpickerview;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.core.internal.view.SupportMenu;
import androidx.core.view.InputDeviceCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.dmsherazi.cctpickerview.interfaces.CctPickHSVCallback;


public class CctPickerView extends View {
    private static int PADDING = 0;
    private static final float PI = 3.1415925f;
    private int CENTER_X;
    private int CENTER_Y;
    int RADIUS;
    private CctPickHSVCallback callback = null;
    float cdx;
    float cdy;
    int[] cwhue = new int[361];
    float downx;
    float downy;
    private boolean inCircle = false;
    boolean isEffectshow = false;
    boolean isInit = false;
    boolean isOut;
    private Paint leftPaint;
    private RectF mBitmapRect;
    private int[] mColors;
    private float mDensity;
    private Paint mEffectBorderPaint;
    private int mEffectColor = -1;
    private Paint mEffectPaint;
    private Bitmap mGradualChangeBitmap;
    private float[] mHSV;
    private Paint mIconBorderPaint;
    private Paint mIconPaint;
    private Paint mPointerPaint;
    private Bitmap mRgbBitmap;
    private Bitmap mRgbTap;
    private int mRgbTapHeight;
    private int mRgbTapWidth;
    private Bitmap mTapBitmap;
    private Bitmap mTapBlack;
    private RectF mTapRect;
    private Bitmap mTapWhite;
    private int point_radius = 12;
    private float pointerx;
    private float pointery;
    private float[] sendHSV = new float[3];
    float tapLeft;
    float tapTop;

    public CctPickerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    private void init(Context context) {
        initCctColors();
        this.mColors = new int[]{SupportMenu.CATEGORY_MASK, -65281, -16776961, -16711681, -16711936,
                InputDeviceCompat.SOURCE_ANY, SupportMenu.CATEGORY_MASK};
        this.mPointerPaint = new Paint(1);
        this.mPointerPaint.setStyle(Style.STROKE);
        this.mPointerPaint.setStrokeWidth(6.0f);
        this.mPointerPaint.setColor(ViewCompat.MEASURED_STATE_MASK);
        this.mHSV = new float[3];
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        this.mDensity = displayMetrics.density;
        int i = displayMetrics.densityDpi;
        this.point_radius = Math.round(this.mDensity * 15.0f);
        this.mIconPaint = new Paint(1);
        this.mIconPaint.setStyle(Style.FILL);
        this.mIconBorderPaint = new Paint(1);
        this.mIconBorderPaint.setStrokeWidth(this.mDensity * 2.0f);
        this.mIconBorderPaint.setStyle(Style.STROKE);
        this.mIconBorderPaint.setColor(-1);
        this.mEffectPaint = new Paint(1);
        this.mEffectPaint.setStyle(Style.FILL);
        this.mEffectBorderPaint = new Paint(1);
        this.mEffectBorderPaint.setStyle(Style.STROKE);
        this.mEffectBorderPaint.setStrokeWidth(this.mDensity * 2.0f);
        this.mEffectBorderPaint.setColor(Color.argb(127, 255, 255, 255));
        this.mRgbBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cct_disk);
        this.mRgbTap = BitmapFactory.decodeResource(context.getResources(), R.drawable.tap_view_mask);
        this.mTapBlack = BitmapFactory.decodeResource(context.getResources(), R.drawable.tap_view_dark_border);
        this.mTapWhite = BitmapFactory.decodeResource(context.getResources(), R.drawable.tap_view_light_border);
        this.mRgbTapWidth = this.mRgbTap.getWidth();
        this.mRgbTapHeight = this.mRgbTap.getHeight();
        this.tapLeft = (float) (0 - (this.mRgbTapWidth / 2));
        this.tapTop = (float) (0 - this.mRgbTapHeight);
    }

    public void initView(int i) {
        Color.colorToHSV(i, this.mHSV);
        this.isInit = true;
        changePointerColor(this.mHSV);
        invalidate();
    }

    public void initView(float[] fArr) {
        this.mHSV = fArr;
        this.isInit = true;
        changePointerColor(this.mHSV);
        invalidate();
    }

    private void change(float f) {
        this.pointerx = (float) this.CENTER_X;
        float f2 = (float) this.CENTER_Y;
        int i = this.RADIUS;
        this.pointery = f2 - (((float) i) - (((float) (i * 2)) * f));
        this.mEffectColor = getCctColor(this.pointery);
        this.tapLeft = this.pointerx - ((float) (this.mRgbTapWidth / 2));
        this.tapTop = this.pointery - ((float) this.mRgbTapHeight);
    }

    private void changePointerColor(float[] fArr) {
        double d = (double) (((fArr[0] / 180.0f) * PI) + 1.5707963f);
        this.pointerx = (float) (((double) (((float) this.RADIUS) * fArr[1])) * Math.cos(d));
        this.pointery = (float) (((double) (((float) (this.RADIUS * -1)) * fArr[1])) * Math.sin(d));
        this.tapLeft = this.pointerx - ((float) (this.mRgbTapWidth / 2));
        float f = this.pointery;
        this.tapTop = f - ((float) this.mRgbTapHeight);
        this.mEffectColor = getCctColor(f);
    }

    private Bitmap getGradual() {
        if (this.mGradualChangeBitmap == null) {
            this.leftPaint = new Paint();
            this.leftPaint.setStrokeWidth(1.0f);
            int i = this.RADIUS;
            this.mGradualChangeBitmap = Bitmap.createBitmap(i * 2, i * 2, Config.ARGB_8888);
            Canvas canvas = new Canvas(this.mGradualChangeBitmap);
            LinearGradient linearGradient = new LinearGradient(0.0f, 0.0f, 0.0f,
                    (float) (this.RADIUS * 2),
                    new int[]{Color.rgb(90, ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION,
                            ItemTouchHelper.Callback.DEFAULT_SWIPE_ANIMATION_DURATION),
                            Color.rgb(255, 255, 255),
                            Color.rgb(255, 149, 0)},
                    null, TileMode.REPEAT);
            this.leftPaint.setShader(linearGradient);
            this.leftPaint.setAntiAlias(true);
            int i2 = this.RADIUS;
            canvas.drawOval(new RectF(new Rect(0, 0, i2 * 2, i2 * 2)), this.leftPaint);
        }
        return this.mGradualChangeBitmap;
    }

    /* access modifiers changed from: protected */
    @SuppressLint({"DrawAllocation"})
    public void onDraw(Canvas canvas) {
        PADDING = getPaddingBottom();
        this.RADIUS = Math.round(((((float) getWidth()) - (this.mDensity * 90.0f)) / 2.0f) - ((float) PADDING));
        this.CENTER_Y = (getBottom() - getTop()) / 2;
        this.CENTER_X = (getRight() - getLeft()) / 2;
        if (this.isInit) {
            changePointerColor(this.mHSV);
            this.isInit = false;
        }
        Bitmap bitmap = this.mRgbBitmap;
        int i = this.RADIUS;
        this.mRgbBitmap = BitmapUtils.zoomImg(bitmap, i * 2, i * 2);
        if (Math.abs(this.pointery) < ((float) (this.RADIUS / 3))) {
            this.mTapBitmap = this.mTapBlack;
        } else {
            this.mTapBitmap = this.mTapWhite;
        }
        canvas.translate((float) this.CENTER_X, (float) this.CENTER_Y);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setScale(1.0f, 1.0f, 1.0f, 1.0f);
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        int i2 = this.RADIUS;
        this.mBitmapRect = new RectF((float) (-i2), (float) (-i2), (float) i2, (float) i2);
        canvas.drawBitmap(this.mRgbBitmap, null, this.mBitmapRect, paint);
        paint.setColorFilter(new PorterDuffColorFilter(this.mEffectColor, Mode.SRC_IN));
        canvas.drawBitmap(this.mRgbTap, this.tapLeft, this.tapTop, paint);
        canvas.drawBitmap(this.mTapBitmap, this.tapLeft, this.tapTop, this.mIconPaint);
        float f = this.tapLeft;
        float f2 = this.tapTop;
        this.mTapRect = new RectF(f, f2, ((float) this.mRgbTapWidth) + f, ((float) this.mRgbTapHeight) + f2);
        canvas.save();
        canvas.restore();
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
    }

    private int ave(int i, int i2, float f) {
        return i + Math.round(f * ((float) (i2 - i)));
    }

    private int interpColor(int[] iArr, float f) {
        if (f <= 0.0f) {
            return iArr[0];
        }
        if (f >= 1.0f) {
            return iArr[iArr.length - 1];
        }
        float length = f * ((float) (iArr.length - 1));
        int i = (int) length;
        float f2 = length - ((float) i);
        int i2 = iArr[i];
        int i3 = iArr[i + 1];
        return Color.argb(ave(Color.alpha(i2), Color.alpha(i3), f2), ave(Color.red(i2), Color.red(i3), f2), ave(Color.green(i2), Color.green(i3), f2), ave(Color.blue(i2), Color.blue(i3), f2));
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        motionEvent.getX();
        int i = this.CENTER_X;
        motionEvent.getY();
        int i2 = this.CENTER_Y;
        switch (motionEvent.getAction()) {
            case 0:
                this.downx = motionEvent.getX() - ((float) this.CENTER_X);
                this.downy = motionEvent.getY() - ((float) this.CENTER_Y);
                if (!this.mTapRect.contains(this.downx, this.downy)) {
                    if (this.mBitmapRect.contains(this.downx, this.downy)) {
                        this.isOut = false;
                        break;
                    } else {
                        this.isOut = true;
                        break;
                    }
                } else {
                    this.inCircle = true;
                    this.cdx = this.downx - this.pointerx;
                    this.cdy = this.downy - this.pointery;
                    break;
                }
            case 1:
                this.inCircle = false;
                CctPickHSVCallback cctPickHSVCallback = this.callback;
                if (cctPickHSVCallback != null) {
                    cctPickHSVCallback.onStopChangeColor(Color.HSVToColor(this.mHSV), getCctValue(this.pointery));
                    if (this.isOut) {
                        this.isOut = false;
                        this.callback.onOutClick();
                        break;
                    }
                }
                break;
            case 2:
                if (!this.inCircle) {
                    float x = motionEvent.getX() - ((float) this.CENTER_X);
                    float f = x - this.downx;
                    float y = (motionEvent.getY() - ((float) this.CENTER_Y)) - this.downy;
                    if ((f * f) + (y * y) > 10.0f) {
                        this.isOut = false;
                        break;
                    }
                } else {
                    float x2 = motionEvent.getX() - ((float) this.CENTER_X);
                    float y2 = motionEvent.getY() - ((float) this.CENTER_Y);
                    float f2 = x2 - this.downx;
                    float f3 = y2 - this.downy;
                    float f4 = this.pointerx;
                    float f5 = f4 + f2;
                    float f6 = this.pointery;
                    float f7 = f6 + f3;
                    float f8 = (f5 * f5) + (f7 * f7);
                    int i3 = this.RADIUS;
                    if (f8 <= ((float) (i3 * i3))) {
                        this.pointerx = f4 + f2;
                        this.pointery = f6 + f3;
                        this.tapLeft += f2;
                        this.tapTop += f3;
                        this.downx = x2;
                        this.downy = y2;
                    } else {
                        System.out.println("tap move  out");
                        double atan2 = (double) ((float) Math.atan2((double) (y2 - this.cdy), (double) (x2 - this.cdx)));
                        this.pointerx = (float) (((double) this.RADIUS) * Math.cos(atan2));
                        this.pointery = (float) (((double) this.RADIUS) * Math.sin(atan2));
                        float f9 = this.pointerx;
                        this.tapLeft = f9 - ((float) (this.mRgbTapWidth / 2));
                        float f10 = this.pointery;
                        this.tapTop = f10 - ((float) this.mRgbTapHeight);
                        this.downx = f9 + this.cdx;
                        this.downy = f10 + this.cdy;
                    }
                    float atan22 = (((float) Math.atan2((double) this.pointery, (double) this.pointerx)) + 1.5707963f) / 6.283185f;
                    if (atan22 < 0.0f) {
                        atan22 += 1.0f;
                    }
                    float[] fArr = new float[3];
                    Color.colorToHSV(interpColor(this.mColors, atan22), fArr);
                    float f11 = this.pointerx;
                    float f12 = f11 * f11;
                    float f13 = this.pointery;
                    fArr[1] = (float) (Math.sqrt((double) (f12 + (f13 * f13))) / ((double) this.RADIUS));
                    float[] fArr2 = this.mHSV;
                    float f14 = this.pointerx;
                    float f15 = f14 * f14;
                    float f16 = this.pointery;
                    fArr2[1] = (float) (Math.sqrt((double) (f15 + (f16 * f16))) / ((double) this.RADIUS));
                    fArr[2] = 1.0f;
                    this.mEffectColor = getCctColor(this.pointery);
                    CctPickHSVCallback cctPickHSVCallback2 = this.callback;
                    if (cctPickHSVCallback2 != null) {
                        float[] fArr3 = this.sendHSV;
                        fArr3[0] = fArr[0];
                        fArr3[1] = fArr[1];
                        fArr3[2] = fArr[2];
                        cctPickHSVCallback2.onChangeColor(Color.HSVToColor(fArr), getCctValue(this.pointery));
                    }
                    float[] fArr4 = this.mHSV;
                    fArr4[0] = fArr[0];
                    fArr4[2] = fArr[2];
                    invalidate();
                    break;
                }
                break;
        }
        return true;
    }

    private int getCctValue(float f) {
        int i = this.RADIUS;
        return Math.round(((((float) i) + f) * 100.0f) / ((float) (i * 2)));
    }

    public void setCctCallback(CctPickHSVCallback cctPickHSVCallback) {
        this.callback = cctPickHSVCallback;
    }

    private int getCctColor(float f) {
        int i = this.RADIUS;
        int i2 = i * 2;
        float f2 = ((float) i) + f;
        float f3 = (float) i2;
        return this.cwhue[360 - Math.round(((((f3 - f2) * 100.0f) / f3) * 360.0f) / 100.0f)];
    }

    private void initCctColors() {
        float f;
        float f2;
        float f3;
        for (int i = 0; i < 361; i++) {
            if (i <= 180) {
                float f4 = (float) i;
                f2 = (0.30555555f * f4) + 200.0f;
                f = (0.027777778f * f4) + 250.0f;
                f3 = (0.9166667f * f4) + 90.0f;
            } else {
                float f5 = (float) (i - 180);
                f3 = (0.0f * f5) + 255.0f;
                f2 = (-0.5888889f * f5) + 255.0f;
                f = (-1.4166666f * f5) + 255.0f;
            }
            this.cwhue[i] = Color.rgb(Math.round(f3), Math.round(f2), Math.round(f));
        }
    }
}
