package com.example.mifans.download;

/**
 * 下载监听器
 */
public interface DownloadListener {
    void onProgress(long progress);
    void onSuccess();
    void onFailed();
}
