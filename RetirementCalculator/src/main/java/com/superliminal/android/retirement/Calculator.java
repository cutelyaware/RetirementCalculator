package com.superliminal.android.retirement;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Retirement Calculator
 *
 * @author Melinda Green
 */
public class Calculator extends Activity {
    private final static String WEALTH = "Wealth  ";
    private final static String INTEREST = "Interest";
    private final static String EXPENSES = "Expenses";
    private final static String DEATH_IN = "Death in";
    private List<ViewGroup> rows = new ArrayList<ViewGroup>();
    private ViewGroup selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ViewGroup wealth = (ViewGroup) findViewById(R.id.wealth);
        final ViewGroup interest = (ViewGroup) findViewById(R.id.interest);
        final ViewGroup expenses = (ViewGroup) findViewById(R.id.expenses);
        final ViewGroup death = (ViewGroup) findViewById(R.id.death);
        initRow(wealth, WEALTH, 10000, 5000000, 100000);
        initRow(interest, INTEREST, .1, 15, 5);
        initRow(expenses, EXPENSES, 100 * 12, 20000 * 12, 500 * 12); // Slider thinks in years but displays in months.
        initRow(death, DEATH_IN, 1, 80, 25);
        selected = expenses;
        selected.findViewById(R.id.button).callOnClick(); // Selects an initial "solve for" variable.
        for (final ViewGroup r : rows) {
            final RealSlider s = (RealSlider) r.findViewById(R.id.slider);
            // Add a listener that updates the dependent slider while user drags another.
            s.addListener(new RealSlider.ChangeListener() {
                @Override
                public void onChange(double newValue) {
                    double
                            W = ((RealSlider) rows.get(0).findViewById(R.id.slider)).getRealValue(),
                            I = ((RealSlider) rows.get(1).findViewById(R.id.slider)).getRealValue(),
                            E = ((RealSlider) rows.get(2).findViewById(R.id.slider)).getRealValue(),
                            D = ((RealSlider) rows.get(3).findViewById(R.id.slider)).getRealValue();
                    // Adjust the selected row due to changes to this slider.
                    RealSlider selected_slider = ((RealSlider) selected.findViewById(R.id.slider));
                    if (selected.equals(wealth))
                        selected_slider.setRealValue(W = solveForWealth(I, D, E));
                    else if (selected.equals(interest))
                        selected_slider.setRealValue(I = solveForInterest(W, D, E));
                    else if (selected.equals(expenses))
                        selected_slider.setRealValue(E = solveForExpenses(W, I, D));
                    else if (selected.equals(death))
                        selected_slider.setRealValue(D = solveForDeath(W, I, E));
                }
            });
        }
        gooseSliders(); // To make sure they start with possible values.
    } // end onCreate()

    private void gooseSliders() {
        // Goose all sliders to get them to push their initial values into the text fields.
        for(ViewGroup v : rows) {
            RealSlider s = ((RealSlider) v.findViewById(R.id.slider));
            double cur_val = s.getRealValue();
            s.setRealValue(cur_val * 0.000001);
            s.setRealValue(cur_val);
        }
    }

    private void initRow(final ViewGroup row, final String name, double min, double max, double cur) {
        ((RadioButton) row.findViewById(R.id.button)).setText(name);
        rows.add(row);
        final RealSlider rs = (RealSlider) row.findViewById(R.id.slider);
        rs.setAll(min, max, cur, true);
        // Add a listener that keeps the value boxes updated.
        rs.addListener(new RealSlider.ChangeListener() {
            @Override
            public void onChange(double newValue) {
                try {
                    EditText text = ((EditText) row.findViewById(R.id.value));
                    if (WEALTH.equals(name))
                        text.setText("$" + Math.round(newValue));
                    else if (INTEREST.equals(name))
                        text.setText("" + Double.parseDouble(new DecimalFormat("#.#").format(newValue)) + "%");
                    else if (EXPENSES.equals(name))
                        text.setText("$" + Math.round(newValue / 12));
                    else if (DEATH_IN.equals(name))
                        text.setText("" + Double.parseDouble(new DecimalFormat("#.#").format(newValue)) + " years");
                    // Erase any bogus values in the selected row.
                    if(row.equals(selected) && (rs.getRealValue() == rs.getRealMinimum() || rs.getRealValue() == rs.getRealMaximum()))
                        text.setText("");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        final RadioButton my_butt = (RadioButton) row.findViewById(R.id.button);
        my_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    for (ViewGroup r : rows) {
                        RadioButton b = (RadioButton) r.findViewById(R.id.button);
                        RealSlider s = (RealSlider) r.findViewById(R.id.slider);
                        boolean this_row = b.equals(my_butt);
                        if (this_row)
                            selected = r;
                        b.setChecked(this_row); // Checks this row's button and unchecks all others.
                        s.setEnabled(!this_row); // Disallow adjustments to selected row since this is the row we are solving for.
                        r.setBackgroundColor(this_row ? Color.GRAY : Color.TRANSPARENT);
                    }
                } catch(Throwable t) {
                    t.printStackTrace();
                }
                gooseSliders();
            }
        });
    } // end initRow()

    private static double solveForWealth(double I, double D, double E) {
        if (I == 0.) {
            // Avoid 0 divided by 0: when x is small,
            // 1 - exp(x) is approximately -x,
            // so the answer will be E * (D*I/100) / (I/100) = E*D
            return E * D;
        }
        return E * (1 - Math.exp(-D * I / 100)) / (I / 100);
    }

    private static double solveForExpenses(double W, double I, double D) {
        if (I == 0.) {
            // Avoid 0 divided by 0: when x is small,
            // 1 - exp(x) is approximately -x,
            // so the answer will be (W*I/100) / (D*I/100) = W/D
            return W / D;
        }
        return (W * I / 100) / (1 - Math.exp(-D * I / 100));
    }

    private static double solveForDeath(double W, double I, double E) {
        if (I == 0.) {
            // Avoid 0 divided by 0: when x is small,
            // log(1 - x) is approximately -x,
            // so the answer will be W*(I/100)/E / (I/100) = W/E
            return W / E;
        }
        return -Log(1 - W * (I / 100) / E) / (I / 100);
    }

    private static double solveForInterest(double W, double D, double E) {
        // searches for the 'I' that makes the wealth calculation match the given 'W'
        final double EP = 0.00001;
        double I = 5.; // initial guess pulled out of thin air
        double delta = 1.; // initial step size, likewise arbitrary
        double bestW = solveForWealth(I, D, E); // resulting D using initial guess
        while (Math.abs(delta) > EP) {
            double testW = solveForWealth(I + delta, D, E);
            if (Math.abs(W - testW) < Math.abs(W - bestW)) {
                I += delta; // I+delta was an improvement
                bestW = testW;
            } else
                delta /= -2; // search in the other direction by smaller increments
        }

        return I;
    }

    // Math.log returns NaN if value is too small, which is not what we want
    private static double Log(double x) {
        if (x <= 0.)
            return Double.NEGATIVE_INFINITY;
        return Math.log(x);
    }

    private static double clamp(double x, double a, double b) {
        return x <= a ? a :
                x >= b ? b : x;
    }

}
