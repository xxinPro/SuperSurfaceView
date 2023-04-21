
GLSurfaceView预览摄像头
=====================

注意，获取SuperSurfaceView的当前帧时通过如下方式
```java
    superFaceView.getBitmap(new SuperSurfaceView.BitmapCallback() {
        @Override
        public void onResponse(Bitmap bitmap) {
            // 如果要显示bitmap图像。需在UI线程中
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 在UI线程中更新
                    ImageView imageView = new ImageView(MainActivity.this);
                    imageView.setImageBitmap(bitmap);
                }
            });
        }
    });
```

详细使用说明：[https://blog.xxin.xyz](https://blog.xxin.xyz)

