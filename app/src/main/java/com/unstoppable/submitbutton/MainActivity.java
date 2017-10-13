package com.unstoppable.submitbutton;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.unstoppable.submitbuttonview.SubmitButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SubmitButton sBtnLoading, sBtnProgress;
    private Button btnSucceed, btnFailed, btnReset;
    private Switch mSwitch;
    private MyTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sBtnLoading = (SubmitButton) findViewById(R.id.sbtn_loading);
        sBtnProgress = (SubmitButton) findViewById(R.id.sbtn_progress);
        mSwitch = (Switch) findViewById(R.id.switch1);

        btnFailed = (Button) findViewById(R.id.btn_failed);
        btnSucceed = (Button) findViewById(R.id.btn_succeed);
        btnReset = (Button) findViewById(R.id.btn_reset);

        sBtnLoading.setOnClickListener(this);
        sBtnProgress.setOnClickListener(this);
        btnSucceed.setOnClickListener(this);
        btnFailed.setOnClickListener(this);
        btnReset.setOnClickListener(this);


        sBtnLoading.setOnResultEndListener(new SubmitButton.OnResultEndListener() {
            @Override
            public void onResultEnd() {
                Toast.makeText(MainActivity.this, "ResultEnd", Toast.LENGTH_SHORT).show();
            }
        });

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sBtnLoading.setVisibility(View.GONE);
                    sBtnProgress.setVisibility(View.VISIBLE);
                    sBtnLoading.reset();
                } else {
                    sBtnLoading.setVisibility(View.VISIBLE);
                    sBtnProgress.setVisibility(View.GONE);
                    if (task != null && !task.isCancelled()) {
                        task.cancel(true);
                        sBtnProgress.reset();
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sbtn_loading:
                Toast.makeText(this, "SubmitButton be clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.sbtn_progress:
                if (task == null || task.isCancelled()) {
                    task = new MyTask();
                    task.execute();
                }
                break;
            case R.id.btn_succeed:
                if (mSwitch.isChecked()) {
                    sBtnProgress.doResult(true);
                } else {
                    sBtnLoading.doResult(true);
                }
                break;
            case R.id.btn_failed:
                if (mSwitch.isChecked()) {
                    sBtnProgress.doResult(false);
                } else {
                    sBtnLoading.doResult(false);
                }
                break;
            case R.id.btn_reset:
                if (mSwitch.isChecked()) {
                    if (task != null && !task.isCancelled()) {
                        task.cancel(true);
                        sBtnProgress.reset();
                    }
                } else {
                    sBtnLoading.reset();
                }
                break;
        }
    }

    private class MyTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            int i = 0;
            while (i <= 100) {
                if (isCancelled()) {
                    return null;
                }
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i++;
                publishProgress(i);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean == null) {
                sBtnProgress.reset();
            }
            sBtnProgress.doResult(aBoolean);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            sBtnProgress.setProgress(values[0]);
        }
    }
}
