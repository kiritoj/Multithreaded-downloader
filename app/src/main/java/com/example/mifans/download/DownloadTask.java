package com.example.mifans.download;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadTask implements Runnable {
    private DownloadListener downloadListener;//监听下载进度
    private String downloadUrl;//下载链接
    private long startPosition;//开始下载的位置
    private long endPosition;//下载的结束位置
    private String fileName;
    public DownloadTask(DownloadListener downloadListener,
                        String downloadUrl, String fileName,long startPosition,long endPosition) {
        this.downloadListener = downloadListener;
        this.downloadUrl = downloadUrl;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.fileName = fileName;
    }

    @Override
    public void run() {
        File file = null;
        InputStream inputStream = null;
        RandomAccessFile randomAccessFile = null;
        OkHttpClient okHttpClient = new OkHttpClient();
        //下载文件存放在系统Download目录下
        String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        file = new File(downloadPath + fileName);
        //不同线程分别下载各自的部分
        Request request = new Request.Builder()
                .addHeader("RANGE", "bytes=" + startPosition + "-" + endPosition)
                .url(downloadUrl).build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response != null) {
                inputStream = response.body().byteStream();
                randomAccessFile = new RandomAccessFile(file, "rw");
                randomAccessFile.seek(startPosition);
                byte[] b = new byte[1024];
                int length ;
                while ((length = inputStream.read(b)) != -1) {
                    randomAccessFile.write(b, 0, length);
                    downloadListener.onProgress(file.length());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }


}
