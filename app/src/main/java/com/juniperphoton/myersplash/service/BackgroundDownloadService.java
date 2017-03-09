package com.juniperphoton.myersplash.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.juniperphoton.myersplash.App;
import com.juniperphoton.myersplash.R;
import com.juniperphoton.myersplash.cloudservice.CloudService;
import com.juniperphoton.myersplash.model.DownloadItem;
import com.juniperphoton.myersplash.utils.DownloadUtil;
import com.juniperphoton.myersplash.utils.NotificationUtil;
import com.juniperphoton.myersplash.utils.Params;
import com.juniperphoton.myersplash.utils.SingleMediaScanner;
import com.juniperphoton.myersplash.utils.ToastService;

import java.io.File;
import java.util.HashMap;

import io.realm.Realm;
import okhttp3.ResponseBody;
import rx.Subscriber;

@SuppressWarnings("UnusedDeclaration")
public class BackgroundDownloadService extends IntentService {
    private static String TAG = BackgroundDownloadService.class.getName();
    private static HashMap<String, Subscriber> subscriptionMap = new HashMap<>();

    public BackgroundDownloadService(String name) {
        super(name);
    }

    public BackgroundDownloadService() {
        super("BackgroundDownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String url = intent.getStringExtra(Params.URL_KEY);
        String fileName = intent.getStringExtra(Params.NAME_KEY);
        boolean canceled = intent.getBooleanExtra(Params.CANCELED_KEY, false);

        int nId = intent.getIntExtra(Params.CANCEL_NID_KEY, NotificationUtil.Companion.getNOT_ALLOCATED_ID());
        if (nId != NotificationUtil.Companion.getNOT_ALLOCATED_ID()) {
            NotificationUtil.Companion.cancelNotificationById(nId);
        }

        if (canceled) {
            Log.d(TAG, "on handle intent cancelled");
            Subscriber subscriber = subscriptionMap.get(url);
            if (subscriber != null) {
                subscriber.unsubscribe();
                NotificationUtil.Companion.cancelNotification(Uri.parse(url));
                ToastService.INSTANCE.sendShortToast(getString(R.string.cancelled_download));
            }
        } else {
            Log.d(TAG, "on handle intent progress");
            String filePath = downloadImage(url, fileName);
            NotificationUtil.Companion.showProgressNotification(getString(R.string.app_name),
                    getString(R.string.downloading),
                    0, filePath, Uri.parse(url));
        }
    }

    protected String downloadImage(final String url, final String fileName) {
        final File file = DownloadUtil.INSTANCE.getFileToSave(fileName);
        Subscriber<ResponseBody> subscriber = new Subscriber<ResponseBody>() {
            File outputFile = null;

            @Override
            public void onCompleted() {
                if (outputFile == null) {
                    NotificationUtil.Companion.showErrorNotification(Uri.parse(url), fileName, url);

                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();

                    DownloadItem downloadItem = realm.where(DownloadItem.class)
                            .equalTo(DownloadItem.DOWNLOAD_URL, url).findFirst();
                    if (downloadItem != null) {
                        downloadItem.setStatus(DownloadItem.DOWNLOAD_STATUS_FAILED);
                    }

                    realm.commitTransaction();
                } else {
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();

                    DownloadItem downloadItem = realm.where(DownloadItem.class)
                            .equalTo(DownloadItem.DOWNLOAD_URL, url).findFirst();
                    if (downloadItem != null) {
                        downloadItem.setStatus(DownloadItem.DOWNLOAD_STATUS_OK);
                        Log.d(TAG, "output file:" + outputFile.getAbsolutePath());
                        File newFile = new File(outputFile.getPath() + ".jpg");
                        outputFile.renameTo(newFile);
                        Log.d(TAG, "renamed file:" + newFile.getAbsolutePath());
                        downloadItem.setFilePath(newFile.getPath());

                        SingleMediaScanner.INSTANCE.sendScanFileBroadcast(App.Companion.getInstance(), newFile);
                    }

                    realm.commitTransaction();

                    NotificationUtil.Companion.showCompleteNotification(Uri.parse(url), Uri.fromFile(outputFile));
                }
                Log.d(TAG, getString(R.string.completed));
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Log.d(TAG, "on handle intent error " + e.getMessage() + ",url:" + url);
                NotificationUtil.Companion.showErrorNotification(Uri.parse(url), fileName, url);

                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();

                DownloadItem downloadItem = realm.where(DownloadItem.class)
                        .equalTo(DownloadItem.DOWNLOAD_URL, url).findFirst();
                if (downloadItem != null) {
                    downloadItem.setStatus(DownloadItem.DOWNLOAD_STATUS_FAILED);
                }

                realm.commitTransaction();
            }

            @Override
            public void onNext(ResponseBody responseBody) {
                Log.d(TAG, "outputFile download onNext,size" + responseBody.contentLength());
                this.outputFile = DownloadUtil.INSTANCE.writeResponseBodyToDisk(responseBody, file.getPath(), url);
            }
        };
        CloudService.getInstance().downloadPhoto(subscriber, url);
        subscriptionMap.put(url, subscriber);

        return file.getPath();
    }
}
