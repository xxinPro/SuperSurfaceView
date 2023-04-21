package com.wanzheng.supersurfaceview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import java.nio.IntBuffer;

public class SuperSurfaceView extends GLSurfaceView {

    public SuperSurfaceView(Context context) {
        super(context);
    }

    public SuperSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private int cameraType = 0; // 摄像头类型，一般情况下，0为后置摄像头，1为前置摄像头

    public SuperSurfaceView setCameraType(int _cameraType) {
        this.cameraType = _cameraType;
        return this;
    }

    private SuperRenderer superRenderer;

    public void launch() throws Exception {
        if (superRenderer == null) {
            // 创建渲染器对象
            superRenderer = new SuperRenderer(this, cameraType);
            // 设置OpenGL ES版本
            this.setEGLContextClientVersion(2);
            // 设置渲染器
            this.setRenderer(superRenderer);
            //将模式设置为RENDERMODE_WHEN_DIRTY,这样可以减少渲染次数,也就可以减少电量的使用以及更少的使用系统的GPU和CPU资源.
            this.setRenderMode(RENDERMODE_WHEN_DIRTY);
        } else {
            // 关闭摄像头
            superRenderer.closeCamera();
            // 设置摄像头类型
            superRenderer.setCameraType(cameraType);

            superRenderer.isBound = true;
        }

        // 打开摄像头
        superRenderer.openCamera();
    }

    public void close() throws Exception {
        if (superRenderer != null) {
            superRenderer.closeCamera();
        }
    }

    /**
     * 获取bitmap的接口
     */
    public interface BitmapCallback{
        void onResponse(Bitmap bitmap);
    }

    /**
     * 获取bitmap
     */
    public void getBitmap(BitmapCallback bitmapCallback) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                // 获取GLSurfaceView的宽度和高度
                int width = SuperSurfaceView.this.getWidth();
                int height = SuperSurfaceView.this.getHeight();

                // 创建一个Bitmap对象
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                // 创建一个IntBuffer对象
                IntBuffer pixelBuffer = IntBuffer.allocate(width * height);

                // 读取GLSurfaceView的像素数据
                GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);

                // 将IntBuffer中的像素数据复制到Bitmap对象中
                pixelBuffer.position(0);
                bitmap.copyPixelsFromBuffer(pixelBuffer);

                Matrix matrix = new Matrix();
                matrix.postScale(1, -1); // 翻转垂直方向
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);

                if (bitmapCallback != null) {
                    bitmapCallback.onResponse(bitmap);
                }
            }
        });
    }
}
