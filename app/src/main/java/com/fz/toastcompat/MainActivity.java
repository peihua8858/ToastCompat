package com.fz.toastcompat;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fz.toast.ToastCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        ToastCompat.initialize(this);
    }

    public void show1(View v) {
        for (int i = 0; i < 3; i++) {
            ToastCompat.show("我是第" + (i + 1) + "个吐司");
        }
    }

    public void show2(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {//我是子线程中弹出的吐司
                ToastCompat.show("Tap the back key twice to exit the app.", Gravity.BOTTOM, 0, 0);
            }
        }).start();
    }

    public void show3(View v) {
        ToastCompat.makeText(this, "单个吐司显示", Toast.LENGTH_SHORT).show();
//        ToastCompat2.show("动态切换白色吐司样式成功");
    }

    public void show4(View v) {
        ToastCompat.show(R.layout.toast_custom_view, "我是方法传参自定义Toast", Gravity.BOTTOM, 0, 72);
    }

    public void show5(View view) {
        Toast toast = ToastCompat.makeText(this, "我是自定义Toast", ToastCompat.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(getLayoutInflater().inflate(R.layout.toast_custom_view, null));
        toast.show();
    }
}
