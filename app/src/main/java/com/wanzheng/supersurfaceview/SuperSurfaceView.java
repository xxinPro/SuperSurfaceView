package com.wanzheng.supersurfaceview;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

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

    public void launch() {
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
            superRenderer.setCameraType(cameraType);
        }

        try {
            superRenderer.openCamera();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
