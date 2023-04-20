package com.wanzheng.supersurfaceview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private SuperSurfaceView superFaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        superFaceView = findViewById(R.id.superFaceView);
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
        superFaceView.setCameraType(1).launch();
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
}