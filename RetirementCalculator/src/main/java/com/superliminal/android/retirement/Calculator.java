package com.superliminal.android.retirement;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

/**
 * Retirement Calculator
 * @author Melinda Green
 */
public class Calculator extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.calculator, menu);
        return true;
    }
    
}
