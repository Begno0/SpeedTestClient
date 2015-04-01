package begnardi.luca.events;

import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import begnardi.luca.graphics.GlobalResultsFragment;

/**
 * Created by luca on 16/02/15.
 */

public class TouchHandler implements View.OnTouchListener {

    private DisplayMetrics display;
    private int value;
    private int startValue;
    private static final int UP_VALUE = 85;
    private static final int MAX_VALUE = 200;
    private static final int MIN_VALUE = 30;
    private boolean goingUp;
    private GlobalResultsFragment parentFragment;
    private View layout;
    private View fragment;

    public int getValue() {
        return value;
    }

    public TouchHandler(GlobalResultsFragment parentFragment, View layout, View fragment) {
        display = parentFragment.getResources().getDisplayMetrics();
        this.parentFragment = parentFragment;
        this.layout = layout;
        this.fragment = fragment;
        goingUp = true;
    }

    public int pixelToDp(float pixel) {
        return (int)(pixel / (display.densityDpi / 160f));
    }

    public int dpToPixel(float dp) {
        return (int)(dp * display.densityDpi / 160f);
    }

    public boolean isUp() {
        if(goingUp) {
            return (value > UP_VALUE);
        }
        else {
            return (!(-value > UP_VALUE));
        }
    }

    public void startUpSequence() {
        int startHeight = layout.getLayoutParams().height;
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        params.height = dpToPixel(MAX_VALUE);
        layout.setLayoutParams(params);

        TranslateAnimation tr = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 0f, Animation.ABSOLUTE, layout.getLayoutParams().height - startHeight, Animation.RELATIVE_TO_PARENT, 0f);

        tr.setDuration(200);
        fragment.startAnimation(tr);
    }

    public void startDownSequence() {
        TranslateAnimation tr = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 1.0f);

        tr.setDuration(200);
        fragment.startAnimation(tr);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params = layout.getLayoutParams();
                params.height = dpToPixel(MIN_VALUE);
                layout.setLayoutParams(params);
            }
        }, 500);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE: {
                value = startValue - pixelToDp(event.getRawY());
                ViewGroup.LayoutParams params = layout.getLayoutParams();
                if(goingUp) {
                    if (value > 0 && value < MAX_VALUE) {
                        params.height = dpToPixel(value);
                        layout.setLayoutParams(params);
                    }
                }
                else {
                    if (value < 0) {
                        params.height = dpToPixel(Math.max(0, MAX_VALUE + value));
                        layout.setLayoutParams(params);
                    }
                }
            } break;
            case MotionEvent.ACTION_DOWN: {
                if(goingUp) { //have to go up
                    ViewGroup.LayoutParams params = layout.getLayoutParams();
                    params.height = dpToPixel(MIN_VALUE);
                    layout.setLayoutParams(params);
                    fragment.setVisibility(View.VISIBLE);
                    startValue = pixelToDp(event.getRawY());
                }
                else { //have to go down
                    startValue = pixelToDp(event.getRawY());
                }
            } break;
            case MotionEvent.ACTION_UP: {
                if(isUp()) {
                    goingUp = false;
                    startUpSequence();
                }
                else {
                    goingUp = true;
                    startDownSequence();
                }
            }
        }
        return true;
    }
}