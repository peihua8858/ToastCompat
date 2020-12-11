package com.fz.toastcompat;

import android.content.Intent;
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
        getApplicationContext().getTheme().applyStyle(R.style.AppTheme, false);
        setContentView(R.layout.activity_main);
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
//            if (!Settings.canDrawOverlays(this)) {
//                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                        Uri.parse("package:" + getPackageName()));
//                startActivityForResult(intent, 1234);
//            }
//        }
    }

    public void show1(View v) {
        showToast
        for (int i = 0; i < 3; i++) {
            showToast(this,"我是第" + (i + 1) + "个吐司");
//            ToastCompat.makeText(this, R.layout.toast_custom_view)
//                    .setText("我是第" + (i + 1) + "个吐司")
//                    .setDuration(ToastCompat.LENGTH_SHORT).show();
        }
    }

    public void show2(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {//我是子线程中弹出的吐司
                showToast(MainActivity.this, "Tap the back key twice to exit the app.");
//                ToastCompat.makeText(MyApplication.getContext())
//                        .setText("Tap the back key twice to exit the app.")
//                        .setGravity(Gravity.BOTTOM, 0, 96).show();
            }
        }).start();
    }

    public void show3(View v) {
        showToast(this,"单个吐司显示");
//        ToastCompat.makeText(this, "单个吐司显示", ToastCompat.LENGTH_SHORT).show();
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

    public void show6(View view) {
        Toast.makeText(this, "系统Toast", Toast.LENGTH_SHORT).show();
    }

    public void show7(View view) {
        startActivity(new Intent(this, SecondActivity.class));
    }
}
