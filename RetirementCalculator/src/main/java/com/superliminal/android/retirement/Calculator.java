package com.superliminal.android.retirement;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;

/**
 * Retirement Calculator
 *
 * @author Melinda Green
 */
public class Calculator extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initRow((ViewGroup) findViewById(R.id.wealth), "Wealth  ", 10000, 5000000, 100000);
        initRow((ViewGroup) findViewById(R.id.interest), "Interest", .1, 20, 4);
        initRow((ViewGroup) findViewById(R.id.expenses), "Expenses", 10, 20000, 500);
        initRow((ViewGroup) findViewById(R.id.death), "Death in", 1, 100, 25);
    }

    private void initRow(final ViewGroup row, String name, double min, double max, double cur) {
        ((RadioButton) row.findViewById(R.id.button)).setText(name);
        RealSlider fs = (RealSlider) row.findViewById(R.id.slider);
        fs.setAll(min, max, cur, true);
        fs.addListener(new RealSlider.ChangeListener() {
            @Override
            public void onChange(double newValue) {
                Log.d("retirement", "test slider val = " + newValue);
                ((EditText)row.findViewById(R.id.value)).setText(""+Math.round(newValue));
            }
        });
    }

}
