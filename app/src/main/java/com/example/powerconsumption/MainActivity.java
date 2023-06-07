package com.example.powerconsumption;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_SCREEN_CAPTURE = 1;
    private static final int REQUEST_GPS_PERMISSION = 2;
    private static final int REQUEST_CODE_TAKE_VIDEO = 101;
    private CameraManager mCameraManager;
    private String mCameraId;
    private BluetoothAdapter mBluetoothAdapter;
    private Vibrator vibrator; // 振动器对象
    private Handler handler = new Handler(); // 处理程序对象
    private static final int VIBRATE_INTERVAL = 1000;//震动时间
    private static final int VIDEO_CAPTURE = 1;
    private static final int CAPTURE_REQUEST_CODE = 200;
    //    private AllApp allApp;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startButton = (Button) findViewById(R.id.btn_start);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        TelephonyManager teleManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        // 初始化CameraManager
        initCameraManager();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        AllApp allApp = new AllApp(this);
        // 请求屏幕捕捉权限
//        MediaProjectionManager manager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
//        startActivityForResult(manager.createScreenCaptureIntent(), REQUEST_CODE_SCREEN_CAPTURE);

        //点击事件
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOnWifi();
                onGPS();
                openBlueTooth();
                //开始振动
                vibrator.vibrate(new long[]{0, 1000}, 0);
                turnOnFlashLight();
//                video.openVide();
//                offPowerSaving();
                allApp.openAllApps();
                requestGPSPermission();
                setDataEnabled(true);
                openHighBrightness();

                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
                    // Permission not granted, request for it
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_SETTINGS}, 1);
                } else {
                    // Permission already granted, perform operation
                    openHighBrightness();
                }
            }
        });
    }


    //初始化相机
    private void initCameraManager() {
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (mCameraManager != null) {
                //获取相机id列表
                mCameraId = mCameraManager.getCameraIdList()[0];
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //关闭省电(失败了)
    private void offPowerSaving() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(MainActivity.this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:" + MainActivity.this.getPackageName()));
            startActivityForResult(intent, 100);
        } else {
            // 已有WRITE_SETTINGS权限，执行关闭省电功能的代码
            Settings.Global.putInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 1);
            Toast.makeText(MainActivity.this, "已关闭省电功能", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (Settings.System.canWrite(MainActivity.this)) {
                Settings.Global.putInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 1);
                Toast.makeText(MainActivity.this, "已关闭省电功能", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "授权失败，无法关闭省电功能", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // 启用手机灯光要添加相机权限
    private void turnOnFlashLight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.setTorchMode(mCameraId, true);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //开启最高屏幕亮度
    private void openHighBrightness() {
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 2000);
    }

    //开启蓝牙
    private void openBlueTooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        }
        bluetoothAdapter.enable();
    }





    //开启无线连接

    private void turnOnWifi() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            // 如果没开启，则开启WiFi
            wifiManager.setWifiEnabled(true);
        }
    }
    //开启GPS权限
    private void checkGPSPermission() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    // 请求开启 GPS 权限
    private void requestGPSPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_GPS_PERMISSION);
    }

    // 处理授权结果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_GPS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授权成功，可以进行相关操作
                Toast.makeText(this, "GPS权限已开启", Toast.LENGTH_SHORT).show();
            } else {
                // 用户拒绝授权，无法进行相关操作
                Toast.makeText(this, "未授权，无法使用定位功能", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void onGPS(){
        // 获取 LocationManager 实例
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

// 检查是否有定位权限
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

// 设置定位监听器
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // 获取定位信息
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                // ...
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }
    // 打开或关闭移动数据网络
    public void setDataEnabled(boolean enabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android10.0及以上版本
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.requestNetwork(
                    new NetworkRequest.Builder()
                            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                            .build(),
                    new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(Network network) {
                            super.onAvailable(network);
                            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                            Method setMobileDataEnabledMethod;
                            try {
                                setMobileDataEnabledMethod = tm.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
                                setMobileDataEnabledMethod.invoke(tm, enabled);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } else {
            // Android10.0以下版本
            String command = enabled ? "svc data enable" : "svc data disable";
            try {
                Process process = Runtime.getRuntime().exec("su");
                process.getOutputStream().write((command + "\n").getBytes());
                process.getOutputStream().write("exit ".getBytes());
                        process.getOutputStream().flush();
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}




