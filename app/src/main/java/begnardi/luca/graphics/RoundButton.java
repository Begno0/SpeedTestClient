package begnardi.luca.graphics;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import begnardi.luca.tests.R;

/**
 * Created by luca on 01/03/15.
 */
public class RoundButton extends View implements View.OnTouchListener,View.OnClickListener{

    private int colorBase;
    private int colorHover;
    private boolean isShadow;
    private boolean isHover;
    private  int radius;
    private int radiusImg;
    private int radiusShadow;
    protected Paint paint;
    private Bitmap image;
    private String text;
    private int startX;
    private int startY;
    private   int radiusShadowDp;
    protected int maxRadius;

    //public TouchGroup touchGroup;

    public RoundButton(Context context) {
        super(context);
    }

    public RoundButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RoundButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    public void setState(boolean isHover) {
        this.isHover = isHover;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(isShadow) {
            paint.setColor(Color.BLACK);
            for (int i = 0; radiusShadow - i > radius; i++) {
                paint.setAlpha(4);
                canvas.drawArc(new RectF(i, i, radiusShadow * 2 - i, radiusShadow * 2 - i), 20 + radiusShadow - radius - i * 4, 140 - 2 * (radiusShadow - radius - i * 4), true, paint);
            }
        }

        if(!isHover)
            paint.setColor(colorBase);
        else
            paint.setColor(colorHover);

        canvas.drawCircle(maxRadius, maxRadius, radius, paint);
        canvas.drawBitmap(image, maxRadius - radiusImg, maxRadius - radiusImg, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(maxRadius * 2, maxRadius * 2);
    }

    protected void init(Context context,AttributeSet attributeSet) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.RoundButton, 0, 0);

        try {
            int radiusDp = typedArray.getInteger(R.styleable.RoundButton_radius, 28);
            int radiusShadowDp = radiusDp + 6;
            int radiusImgDp = radiusDp / 2;

            radius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radiusDp, getResources().getDisplayMetrics());
            radiusShadow = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radiusShadowDp, getResources().getDisplayMetrics());
            radiusImg = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radiusImgDp, getResources().getDisplayMetrics());

            colorBase = Color.parseColor(typedArray.getString(R.styleable.RoundButton_color_base));
            colorHover = Color.parseColor(typedArray.getString(R.styleable.RoundButton_color_hover));
            isShadow = typedArray.getBoolean(R.styleable.RoundButton_shadow, true);
            image = BitmapFactory.decodeResource(getResources(), typedArray.getResourceId(R.styleable.RoundButton_image, 0));
            image = Bitmap.createScaledBitmap(image, radiusImg * 2, radiusImg * 2, true);
            text = typedArray.getString(R.styleable.RoundButton_text);
        }
        finally {
            typedArray.recycle();
        }
        if(isShadow)
            maxRadius = radiusShadow;
        else
            maxRadius = radius;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(colorBase);

        isHover = false;
        //touchGroup=new TouchGroup();

        setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case(MotionEvent.ACTION_DOWN): {
                startX = (int) event.getRawX();
                startY = (int) event.getRawY();
                isHover = true;
                invalidate();
                break;
            }
            case(MotionEvent.ACTION_UP): {
                if(distance(startX, startY, (int) event.getRawX(), (int) event.getRawY()) <= radius)
                    callOnClick();
                invalidate();
                break;
            }
            case(MotionEvent.ACTION_MOVE): {
                isHover = (distance(startX, startY, (int) event.getRawX(), (int) event.getRawY()) <= radius);
                invalidate();
                break;
            }
        }
        return true;
    }

    private int distance(int startX, int startY, int finalX, int finalY) {
        int mem1 = (int) Math.pow(startX - finalX, 2);
        int mem2 = (int) Math.pow(startY - finalY, 2);
        return (int) Math.sqrt(mem1 + mem2);
    }

    @Override
    public void onClick(View v) {
        System.out.println("button");
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        isHover = !enabled;
    }
}
