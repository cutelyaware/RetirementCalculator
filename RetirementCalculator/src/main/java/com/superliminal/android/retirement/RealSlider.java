package com.superliminal.android.retirement;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

import java.util.HashSet;
import java.util.Set;

/**
 * Floating point version of Android SeekBar widget with support for minimum values, multiple listeners, and logarithmic scales.
 * It is an error to call the parent implementations or other methods related to current and maximum values.
 * Note that unlike in the base class, values set via setRealValue(val) are allowed
 * to be outside the min/max range values. in those cases the slider thumb will clamp
 * to the appropriate slider end (just like in the base class) but the out-of-range
 * value will still be retrievable with getRealValue().
 *
 * @author Melinda Green
 * @author Don Hatch
 */
public class RealSlider extends SeekBar {
    private final static int SLIDER_RANGE = 1000; // number of discrete steps in native widget.
    private double curReal, minReal, maxReal;
    private boolean isLogScale;
    private Set<ChangeListener> listeners = new HashSet<ChangeListener>();
    public interface ChangeListener {
        public void onChange(double newValue);
    }

    //
    // CONSTRUCTORS
    //

    public RealSlider(Context context, double cur, double min, double max) {
        super(context);
        init(cur, min, max, false);
    }
    public RealSlider(Context context) {
        this(context, 0, 0, SLIDER_RANGE);
    }

    public RealSlider(Context context, AttributeSet attrs, double cur, double min, double max) {
        super(context, attrs);
        init(cur, min, max, false);
    }
    public RealSlider(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0, SLIDER_RANGE);
    }

    public RealSlider(Context context, AttributeSet attrs, int defStyle, double cur, double min, double max) {
        super(context, attrs, defStyle);
        init(cur, min, max, false);
    }
    public RealSlider(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0, 0, SLIDER_RANGE);
    }

    /**
     * @param cur - real valued initial value.
     * @param min - real valued range minimum.
     * @param max - real valued range maximum.
     * @param log - log scale if true, linear otherwise.
     */
    private void init(double cur, double min, double max, boolean log) {
        isLogScale = log;
        setAll(min, max, cur, log);
        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int ival, boolean fromUser) {
                double dval = transformRange(
                        false, 0, SLIDER_RANGE, ival,
                        isLogScale, minReal, maxReal
                );
                curReal = dval;
                // Notify all listeners.
                fireChangeEvent();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };
        super.setOnSeekBarChangeListener(listener);
    } // end init()

    //
    // LISTENER SUPPORT
    //

    public void addListener(ChangeListener l) {
        listeners.add(l);
    }

    public boolean removeListener(ChangeListener l) {
        return listeners.remove(l);
    }

    protected void fireChangeEvent() {
        for (ChangeListener l : listeners)
            l.onChange(RealSlider.this.getRealValue());
    }

    //
    // GETTERS & SETTERS
    //

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        // We can't allow users to set the base class' listener both because this class needs to do that
        // and because the values it returns make no sense outside of this class.
        throw new UnsupportedOperationException ("Use addListener() instead");
    }

    public double getRealMinimum() {
        return minReal;
    }

    public void setRealMinimum(double newmin) {
        setAll(newmin, maxReal, getRealValue(), isLogScale);
    }

    public double getRealMaximum() {
        return maxReal;
    }

    public void setRealMaximum(double newmax) {
        setAll(minReal, newmax, getRealValue(), isLogScale);
    }

    public double getRealValue() {
        return curReal;
    }

    public void setRealValue(double newcur) {
        // update the model
        curReal = newcur;
        // update the view
        int icur = rangeValue(newcur);
        super.setProgress(icur);
    }

    public void setAll(double newmin, double newmax, double newcur, boolean log) {
        minReal = newmin;
        maxReal = newmax;
        isLogScale = log;
        int imax = rangeValue(newmax);
        this.setMax(imax);
        setRealValue(newcur);
    }

    //
    // MODEL IMPLEMENTATION
    //

    private static double clamp(double x, double a, double b) {
        return x <= a ? a :
                x >= b ? b : x;
    }

    // linear interpolation
    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    // geometric interpolation
    private static double gerp(double a, double b, double t) {
        return a * Math.pow(b / a, t);
    }

    // interpolate between A and B (linearly or geometrically)
    // by the fraction that x is between a and b (linearly or geometrically)
    private static double transformRange(
            boolean isLog, double a, double b, double x,
            boolean IsLog, double A, double B) {
        if (isLog) {
            a = Math.log(a);
            b = Math.log(b);
            x = Math.log(x);
        }
        double t = (x - a) / (b - a);
        double X = IsLog ?
                gerp(A, B, t) :
                lerp(A, B, t);
        return X;
    }

    /**
     * @return the closest integer in the range of the actual int extents of the base class.
     */
    protected int rangeValue(double dval) {
        dval = clamp(dval, minReal, maxReal);
        int ival = (int) Math.round(
                transformRange(isLogScale, minReal, maxReal, dval, false, 0, SLIDER_RANGE));
        return ival;
    }

}

