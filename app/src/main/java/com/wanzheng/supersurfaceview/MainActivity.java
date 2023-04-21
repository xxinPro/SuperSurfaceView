package com.wanzheng.supersurfaceview;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private SuperSurfaceView superFaceView;
    private Button switch_camera;

    private int cameraType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        superFaceView = findViewById(R.id.superFaceView);
        switch_camera = findViewById(R.id.switch_camera);

        // 切换前后摄像头
        switch_camera.setOnClickListener(view -> {
            cameraType = (cameraType == 0) ? 1 : 0;
            initCamera();
        });

        request();
    }

    /**
     * 申请权限
     */
    private void request() {
        if (!isCamera()) {
            Toast.makeText(this, "没有摄像头权限", Toast.LENGTH_SHORT).show();
            // 申请权限
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.CAMERA
            }, 10001);
        } else {
            initCamera();
        }
    }

    private void initCamera() {
        try {
            superFaceView.setCameraType(cameraType).launch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10001) {
            request();
        }
    }

    private boolean isCamera() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            superFaceView.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


