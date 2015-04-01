package begnardi.luca.graphics;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;

import begnardi.luca.tests.R;

/**
 * Created by luca on 25/03/15.
 */

public class RoundButtonWithLoading extends RoundButton {

    private double percent;
    private int colorLoad;

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent * 360 / 100;
        invalidate();
    }

    public RoundButtonWithLoading(Context context) {
        super(context);
    }

    public RoundButtonWithLoading(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundButtonWithLoading(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(colorLoad);
        canvas.drawArc(new RectF(0, 0, maxRadius * 2, maxRadius * 2), -90, (float) percent, true, paint);
        super.onDraw(canvas);
    }

    protected void init(Context context, AttributeSet attributeSet) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.RoundButton, 0, 0);
        super.init(context, attributeSet);

        colorLoad = Color.parseColor(typedArray.getString(R.styleable.RoundButton_color_load));
        maxRadius = maxRadius + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics());
        percent = 0;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }
}