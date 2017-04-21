package com.unstoppable.submitbutton;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.unstoppable.submitbuttonview.SubmitButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SubmitButton mSubmitButton;

    private Button btnSucceed, btnFailed, btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSubmitButton = (SubmitButton) findViewById(R.id.submit_btn);
        btnFailed = (Button) findViewById(R.id.btn_failed);
        btnSucceed = (Button) findViewById(R.id.btn_succeed);
        btnReset = (Button) findViewById(R.id.btn_reset);

        mSubmitButton.setOnClickListener(this);
        btnSucceed.setOnClickListener(this);
        btnFailed.setOnClickListener(this);
        btnReset.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit_btn:
                Toast.makeText(this, "SubmitButton be clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_succeed:
                mSubmitButton.doResult(true);
                break;
            case R.id.btn_failed:
                mSubmitButton.doResult(false);
                break;
            case R.id.btn_reset:
                mSubmitButton.reset();
                break;
        }
    }
}
