package com.example.submitbutton.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.submitbutton.R;
import com.example.submitbutton.View.SubmitButton;

public class MainActivity extends AppCompatActivity {

    private SubmitButton submitButton;

    private Button btnSucceed, btnFailure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        submitButton = (SubmitButton) findViewById(R.id.submit_btn);
        btnSucceed = (Button) findViewById(R.id.btn_succeed);
        btnFailure = (Button) findViewById(R.id.btn_failure);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitButton.doClickAnimation();
            }
        });

        btnSucceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitButton.doResultAnimation(true);
            }
        });

        btnFailure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitButton.doResultAnimation(false);
            }
        });
    }
}
