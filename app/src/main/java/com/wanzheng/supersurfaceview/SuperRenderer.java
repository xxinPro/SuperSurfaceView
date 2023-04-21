package com.wanzheng.supersurfaceview;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SuperRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private final String vertexShaderCode = "uniform mat4 textureTransform;\n" +
            "attribute vec4 position;\n" +
            "attribute vec2 inputTextureCoordinate;\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "void main() {\n" +
            "    gl_Position = position;\n" +
            "    textureCoordinate = inputTextureCoordinate;\n" +
            "}";
    private final String fragmentShaderCode = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "uniform samplerExternalOES videoTex;\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(videoTex, textureCoordinate);\n" +
            "}";

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        // 设置背景颜色
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        // 加载着色器，创建OpenGL ES程序程序
        createProgram();
        // 添加程序到ES环境中
        activeProgram();
    }

    private int textureTransformHandle;

    // 绘制范围矩阵
    private final float[] rangeMatrix = {-1, -1, -1, 1, 1, -1, 1, 1};
    // 前置摄像头绘制矩阵
    private final float[] frontCameraMatrix = {0, 1, 1, 1, 0, 0, 1, 0};
    // 后置摄像头绘制矩阵
    private final float[] rearCameraMatrix = {1, 1, 0, 1, 1, 0, 0, 0};

    /**
     * 添加程序到ES环境中
     */
    private void activeProgram() {
        // 将程序添加到OpenGL ES环境
        GLES20.glUseProgram(programId);

        // 获取顶点着色器的position句柄
        int positionHandle = GLES20.glGetAttribLocation(programId, "position");
        // 获取顶点着色器的inputTextureCoordinate句柄
        int textureHandle = GLES20.glGetAttribLocation(programId, "inputTextureCoordinate");

        textureTransformHandle = GLES20.glGetUniformLocation(programId, "textureTransform");

        // 绘制范围矩阵转换后的buffer
        FloatBuffer rangeMatrixBuffer = floatArr2FloatBuffer(rangeMatrix);
        // 摄像头映像矩阵转换后的buffer
        FloatBuffer cameraMatrixBuffer;
        if(cameraType == 0){
            cameraMatrixBuffer = floatArr2FloatBuffer(rearCameraMatrix);
        } else{
            cameraMatrixBuffer = floatArr2FloatBuffer(frontCameraMatrix);
        }

        // 准备positionHandle坐标数据
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, rangeMatrixBuffer);
        // 准备textureHandle坐标数据
        GLES20.glVertexAttribPointer(textureHandle, 2, GLES20.GL_FLOAT, false, 8, cameraMatrixBuffer);

        // 启用positionHandle的句柄
        GLES20.glEnableVertexAttribArray(positionHandle);
        // 启用textureHandle的句柄
        GLES20.glEnableVertexAttribArray(textureHandle);
    }

    /**
     * float数组转换为FloatBuffer
     */
    private FloatBuffer floatArr2FloatBuffer(float[] buffer) {
        FloatBuffer fb = ByteBuffer.allocateDirect(buffer.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        fb.put(buffer);
        fb.position(0);
        return fb;
    }

    private int programId;

    /**
     * 加载着色器，创建OpenGL ES程序程序
     */
    private void createProgram() {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        // 创建空的OpenGL ES程序
        programId = GLES20.glCreateProgram();
        // 添加顶点着色器到程序中
        GLES20.glAttachShader(programId, vertexShader);
        // 添加片段着色器到程序中
        GLES20.glAttachShader(programId, fragmentShader);
        // 创建OpenGL ES程序可执行文件
        GLES20.glLinkProgram(programId);
        // 释放shader资源
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);
    }

    public static int createTextureId() {
        int[] texture = new int[1];
        //生成一个纹理
        GLES20.glGenTextures(1, texture, 0);
        //将此纹理绑定到外部纹理上
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        //设置纹理过滤参数
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

        return texture[0];
    }

    /**
     * 加载着色器
     * @param shaderType    着色器类型
     * @param shaderSource  着色器资源代码
     */
    private int loadShader(int shaderType, String shaderSource) {
        int shaderHandle = GLES20.glCreateShader(shaderType);
        GLES20.glShaderSource(shaderHandle, shaderSource);
        GLES20.glCompileShader(shaderHandle);
        return shaderHandle;
    }

    private final float[] orthoMatrix = new float[16];  // 接收正交投影的变换矩阵
    private final float[] cameraMatrix = new float[16]; // 接收相机变换矩阵
    private final float[] scaleMatrix = new float[16];  // 接收缩放变换矩阵

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        // 设置视窗大小和位置
        GLES20.glViewport(0, 0, width, height);

        // x y z 代表缩放到比例
        Matrix.scaleM(scaleMatrix,0, 1f,1f,1f);

        float ratio = (float) width / height;
        Matrix.orthoM(orthoMatrix, 0,
                -1, 1,
                -ratio, ratio,
                1, 7);

        // 设置观察视角 eye相机坐标 center 目标坐标 up 相机正上方 向量vuv(相机头部指向)
        Matrix.setLookAtM(cameraMatrix, 0,
                0, 0, 1,
                0, 0, 0,
                0, 1, 0);

        Matrix.multiplyMM(scaleMatrix, 0, orthoMatrix, 0, cameraMatrix, 0);
    }

    private SurfaceTexture surfaceTexture;  // 作为中间件，建立摄像头和GLSurfaceView之间的连接
    public boolean isBound = false;

    @Override
    public void onDrawFrame(GL10 gl10) {
        if (isBound) {
            activeProgram();
            isBound = false;
        }

        if (surfaceTexture != null) {
            // 清除屏幕缓存和深度缓存
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            GLES20.glUniformMatrix4fv(textureTransformHandle, 1, false, scaleMatrix, 0);
            // 根据顶点数据绘制平面图形
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, rangeMatrix.length / 2);
            surfaceTexture.updateTexImage();
        }
    }

    private Camera camera;

    public void openCamera() throws Exception {
        if (surfaceTexture == null) {
            surfaceTexture = new SurfaceTexture(createTextureId());
            surfaceTexture.setOnFrameAvailableListener(this);
        }

        camera = Camera.open(cameraType);
        camera.setPreviewTexture(surfaceTexture);
        camera.startPreview();
    }

    public void closeCamera() throws Exception {
        if (camera != null) {
            camera.setPreviewTexture(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private int cameraType;           // 摄像头类型，0为后置摄像头，1为前置摄像头
    private final SuperSurfaceView superSurfaceView;

    public void setCameraType(int cameraType) {
        this.cameraType = cameraType;
    }

    public SuperRenderer(SuperSurfaceView superSurfaceView, int cameraType) {
        this.superSurfaceView = superSurfaceView;
        this.cameraType = cameraType;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        superSurfaceView.requestRender();
    }
}
