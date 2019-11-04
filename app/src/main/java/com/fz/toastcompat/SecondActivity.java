package com.fz.toastcompat;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.fz.toast.ToastCompat;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
    }

    public void show1(View v) {
        for (int i = 0; i < 3; i++) {
            ToastCompat.makeText(MyApplication.getContext(), R.layout.toast_custom_view)
                    .setText("我是第" + (i + 1) + "个吐司")
                    .setDuration(ToastCompat.LENGTH_SHORT).show();
        }
    }

    public void show2(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {//我是子线程中弹出的吐司
                ToastCompat.makeText(MyApplication.getContext())
                        .setText("Tap the back key twice to exit the app.")
                        .setGravity(Gravity.BOTTOM, 0, 0).show();
            }
        }).start();
    }

    public void show3(View v) {
        ToastCompat.makeText(this, "单个吐司显示", ToastCompat.LENGTH_SHORT).show();
    }

    public void show4(View v) {
        ToastCompat.makeText(this, R.layout.toast_custom_view).setText("我是方法传参自定义Toast")
                .setGravity(Gravity.BOTTOM, 0, 72).show();
    }

    public void show5(View view) {
        ToastCompat toast = ToastCompat.makeText(this, "我是自定义Toast", ToastCompat.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(getLayoutInflater().inflate(R.layout.toast_custom_view, null));
        toast.show();
    }
}
