# Android 自定义Toast

解决通知栏关闭无法显示toast，支持队列顺序显示，支持自定义toast布局

## ToastCompat 初始化:

在Application 的onCreate中初始化：

```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ToastCompat.initialize(this);
    }
}
```

## ToastCompat 使用方法

### 1、按照原生系统方式使用

```java
 ToastCompat.makeText(this,"单个吐司显示", Toast.LENGTH_SHORT).show();
```

### 2、连续显示多个toast

```java
 //连续显示多个toast
 public void show1(View v) {
    for (int i = 0; i < 3; i++) {
       ToastCompat.show("我是第" + (i + 1) + "个吐司");
    }
}
```

### 3、在子线程中显示

```java
 public void show2(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ToastCompat.show("我是子线程中弹出的吐司");
            }
        }).start();
}
```

### 4、传递自定义布局

```java
 public void show4(View v) {
     ToastCompat.show(R.layout.toast_custom_view,"我是方法传参自定义Toast",Gravity.BOTTOM, 0, 72);
 }
```

### 5、原生方式传递布局

```java
 public void show5(View view) {
        Toast toast = ToastCompat.makeText(this, "我是自定义Toast", ToastCompat.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(getLayoutInflater().inflate(R.layout.toast_custom_view, null));
        toast.show();
 }
```

## 添加存储库

```py
 repositories {
        maven { url 'http://10.32.2.200:8081/repository/maven-public/' }
    }
```

## 添加依赖

```py
dependencies {
    implementation 'com.fz.toast:ToastCompat:1.0.4'
}
```



