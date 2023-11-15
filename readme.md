# ToastCompat 
   一款针对Android平台下的自定义Toast，解决通知栏关闭无法显示toast，支持队列顺序显示，支持自定义toast布局。<br>

[![Jitpack](https://jitpack.io/v/peihua8858/ToastCompat.svg)](https://github.com/peihua8858)
[![PRs Welcome](https://img.shields.io/badge/PRs-Welcome-brightgreen.svg)](https://github.com/peihua8858)
[![Star](https://img.shields.io/github/stars/peihua8858/ToastCompat.svg)](https://github.com/peihua8858/ToastCompat)


## 目录
-[最新版本](https://github.com/peihua8858/ToastCompat/releases/tag/1.0.3)<br>
-[如何引用](#如何引用)<br>
-[进阶使用](#进阶使用)<br>
-[演示效果](#演示效果)<br>
-[混淆配置](#混淆配置)<br>
-[如何提Issues](https://github.com/peihua8858/ToastCompat/wiki/%E5%A6%82%E4%BD%95%E6%8F%90Issues%3F)<br>
-[License](#License)<br>



## 如何引用
* 把 `maven { url 'https://jitpack.io' }` 加入到 repositories 中
* 添加如下依赖，末尾的「latestVersion」指的是ToastCompat [![Download](https://jitpack.io/v/peihua8858/ToastCompat.svg)](https://jitpack.io/#peihua8858/ToastCompat) 里的版本名称，请自行替换。
使用Gradle
```sh
repositories {
  google()
  maven { url 'https://jitpack.io' }
}

dependencies {
  // ToastCompat
  implementation 'com.github.peihua8858:ToastCompat:${latestVersion}'
}
```

或者Maven:

```xml
<dependency>
  <groupId>com.github.peihua8858</groupId>
  <artifactId>ToastCompat</artifactId>
  <version>${latestVersion}</version>
</dependency>
```

## 进阶使用

### 1、ToastCompat 初始化:

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
### 2、按照原生系统方式使用

```java
 ToastCompat.makeText(this,"单个吐司显示", Toast.LENGTH_SHORT).show();
```

### 3、连续显示多个toast

```java
 //连续显示多个toast
 public void show1(View v) {
    for (int i = 0; i < 3; i++) {
       ToastCompat.show("我是第" + (i + 1) + "个吐司");
    }
}
```

### 4、在子线程中显示

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

### 5、传递自定义布局

```java
 public void show4(View v) {
     ToastCompat.show(R.layout.toast_custom_view,"我是方法传参自定义Toast",Gravity.BOTTOM, 0, 72);
 }
```

### 6、原生方式传递布局

```java
 public void show5(View view) {
        Toast toast = ToastCompat.makeText(this, "我是自定义Toast", ToastCompat.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(getLayoutInflater().inflate(R.layout.toast_custom_view, null));
        toast.show();
 }
```


## 演示效果

|          单选图片          |           预览           |           相册           |
|:----------------------:|:----------------------:|:----------------------:|
| ![](images/image1.jpg) | ![](images/image7.jpg) | ![](images/image3.jpg) |

|          多选图片          |           预览           |           相册           |
|:----------------------:|:----------------------:|:----------------------:|
| ![](images/image5.jpg) | ![](images/image8.jpg) | ![](images/image9.jpg) |

|           单图裁剪           |          多图裁剪           |
|:------------------------:|:-----------------------:|
|  ![](images/image4.jpg)  | ![](images/image10.jpg) |

## License
```sh
Copyright 2023 peihua

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
