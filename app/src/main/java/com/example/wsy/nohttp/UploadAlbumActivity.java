package com.example.wsy.nohttp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import com.yanzhenjie.nohttp.FileBinary;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.OnUploadListener;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.Response;


import java.io.File;

/**
 * 作者：wsy on 2017/10/31 17:28
 * <p>
 * 邮箱：445979770@qq.com
 */
public class UploadAlbumActivity extends BaseActivity implements View.OnClickListener {

    /**
     * 展示照片。
     */
    ImageView mIvIcon;
    /**
     * 显示状态。
     */
    TextView mTvResult;
    /**
     * 显示进度。
     */
    ProgressBar mProgressBar;

    /**
     * 照片路径。
     */
    private String filePath;

    @Override
    protected void onActivityCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_upload_album);
        mIvIcon = (ImageView) findViewById(R.id.iv_icon);
        mTvResult = (TextView) findViewById(R.id.tv_result);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_progress);

        findViewById(R.id.btn_album).setOnClickListener(this);
        findViewById(R.id.btn_start).setOnClickListener(this);
    }

    /**
     * 按钮点击。
     */
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_album) {
            CameraUtils.openCameraReturnOriginal(UploadAlbumActivity.this, new CameraUtils.CameraResultInterface() {
                @Override
                public void returnIntent(Intent intent, int requestCode, String path) {
                    filePath = path;
                    startActivityForResult(intent, requestCode);
                }
            });
        }else if (v.getId() == R.id.btn_start) {
            if (!TextUtils.isEmpty(filePath)) {
                executeUpload();
            }else {
                Toast.show(UploadAlbumActivity.this , "请选择照片");
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CameraUtils.SMALL_PICTURE && resultCode == RESULT_OK) {
//            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
//            smallImageView.setImageBitmap(imageBitmap);
        }else if (requestCode == CameraUtils.BIG_PICTURE && resultCode == RESULT_OK) {
            mIvIcon.setImageBitmap(CameraUtils.uriToBitmap(filePath));
            android.widget.Toast.makeText(UploadAlbumActivity.this , filePath , android.widget.Toast.LENGTH_SHORT).show();
//            executeUpload();
        }
    }

    /**
     * 执行上传任务。
     */
    private void executeUpload() {
        Request<String> request = NoHttp.createStringRequest("http://192.168.0.33:8280/file_server/upload", RequestMethod.POST);

        // 添加普通参数。
        request.add("user", "yolanda");

        // 上传文件需要实现NoHttp的Binary接口，NoHttp默认实现了FileBinary、InputStreamBinary、ByteArrayBitnary、BitmapBinary。
        FileBinary fileBinary0 = new FileBinary(new File(filePath));
        Toast.show(UploadAlbumActivity.this , filePath);
        /**
         * 监听上传过程，如果不需要监听就不用设置。
         * 第一个参数：what，what和handler的what一样，会在回调被调用的回调你开发者，作用是一个Listener可以监听多个文件的上传状态。
         * 第二个参数： 监听器。
         */
        fileBinary0.setUploadListener(0, mOnUploadListener);

        request.add("userHead", fileBinary0);// 添加1个文件

        request(0, request, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) {
                showMessageDialog(R.string.request_succeed, response.get());
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                showMessageDialog(R.string.request_failed, response.getException().getMessage());
            }
        }, false, true);
    }

    /**
     * 文件上传监听。
     */
    private OnUploadListener mOnUploadListener = new OnUploadListener() {

        @Override
        public void onStart(int what) {// 这个文件开始上传。
            mTvResult.setText(R.string.upload_start);
        }

        @Override
        public void onCancel(int what) {// 这个文件的上传被取消时。
            mTvResult.setText(R.string.upload_cancel);
        }

        @Override
        public void onProgress(int what, int progress) {// 这个文件的上传进度发生边耍
            mProgressBar.setProgress(progress);
        }

        @Override
        public void onFinish(int what) {// 文件上传完成
            mTvResult.setText(R.string.upload_succeed);
        }

        @Override
        public void onError(int what, Exception exception) {// 文件上传发生错误。
            mTvResult.setText(R.string.upload_error);
        }
    };
}

