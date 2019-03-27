package com.example.mifans.download;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.sql.Struct;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public ProgressDialog progressDialog;
    private String fileName;
    private long downloadLength;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusbar();
        final EditText editText = findViewById(R.id.address_et);
        Button button = findViewById(R.id.download_bt);
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        //开启3个核心线程去下载
        final ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);

        //检查读写权限
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        //实例化下载监听器
        final DownloadListener downloadListener = new DownloadListener() {
            @Override
            public void onProgress(long progress) {
                progressDialog.setProgress((int) (progress*100/downloadLength));
                //下载成功
                if (progressDialog.getProgress() == 100) {
                    this.onSuccess();
                }

            }

            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "下载成功", Toast.LENGTH_SHORT).show();
                });


            }

            @Override
            public void onFailed() {
                progressDialog.dismiss();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
                });

            }
        };

        //下载点击事件
        button.setOnClickListener(v -> {
            if (TextUtils.isEmpty(editText.getText())){
                Toast.makeText(MainActivity.this,"下载地址不能为空哦",Toast.LENGTH_SHORT).show();
            }else {

                String downloadUrl = editText.getText().toString();
                fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                //进度条
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setTitle(downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1));
                progressDialog.setMessage("正在玩儿命下载中...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setMax(100);
                progressDialog.setCancelable(false);
                progressDialog.setProgress(0);

                if (TextUtils.isEmpty(editText.getText())) {
                    Toast.makeText(MainActivity.this, "请输入有效下载地址！！", Toast.LENGTH_SHORT).show();
                } else {
                    //获取下载文件长度，开启3个文件下载
                    new Thread(() -> {
                        OkHttpClient okHttpClient = new OkHttpClient();
                        Request request = new Request.Builder().url(editText.getText().toString())
                                .build();

                        Response response = null;
                        try {
                            response = okHttpClient.newCall(request).execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (response != null && response.isSuccessful()) {
                            downloadLength = response.body().contentLength();
                            //如果要下载的文件长度不能被3个线程平分，前两个线程下载平均值部分，最后一个线程下载剩下所有部分
                            long averageLength = downloadLength / 3;
                            //开启第一个线程下载
                            fixedThreadPool.execute(new DownloadTask(downloadListener
                                    , downloadUrl
                                    , fileName
                                    , 0
                                    , averageLength));
                            //开启第二个线程下载
                            fixedThreadPool.execute(new DownloadTask(downloadListener
                                    , downloadUrl
                                    , fileName
                                    , averageLength
                                    , 2 * averageLength - 1));
                            //开启第三个线程下载
                            fixedThreadPool.execute(new DownloadTask(downloadListener
                                    , downloadUrl
                                    , fileName
                                    , 2 * averageLength
                                    , downloadLength - 1));
                        }
                    }).start();

                    progressDialog.show();
                }
            }

        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "拒绝授权将无法使用本应用哦", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 判断系统版本，设置沉浸式状态栏
     */
    public void statusbar(){
        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
            getWindow().setStatusBarColor(Color.TRANSPARENT);

        }
    }

}
