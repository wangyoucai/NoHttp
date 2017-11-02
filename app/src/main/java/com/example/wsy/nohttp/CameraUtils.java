package com.example.wsy.nohttp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 作者：wsy on 2017/10/31 09:41
 * <p>
 * 邮箱：445979770@qq.com
 */

public class CameraUtils {

    private static Uri outputFileUri;//图片地址
    public static final int SMALL_PICTURE = 0;//原图
    public static final int BIG_PICTURE = 1;//原图
    private static String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());//时间戳
    private static final String imagePath = getRootPath();//文件路径/sdcard/pictures/
    private static final String imageFileName = timeStamp + ".jpg";//文件名

    /**
     * 打开相机返回原图
     * @param activity
     * @param cameraResult
     */
    public static void openCameraReturnOriginal (Activity activity , CameraResultInterface cameraResult) {
        File file = createImageFile();
        outputFileUri = Uri.fromFile(file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            cameraResult.returnIntent(intent , BIG_PICTURE , getRealPathFromURI(activity , outputFileUri));
        }else {
            Toast.makeText(activity , "请安装相机" , Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 打开相机返回缩略图
     * @param activity
     * @param cameraResult
     */
    public static void openCameraReturnAbbreviations (Activity activity , CameraResultInterface cameraResult) {
        File file = createImageFile();
        outputFileUri = Uri.fromFile(file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            cameraResult.returnIntent(intent , SMALL_PICTURE  , getRealPathFromURI(activity,outputFileUri) );
        }else {
            Toast.makeText(activity , "请安装相机" , Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 图片储存的位置
     * @return
     */
    public static File createImageFile() {
        File imageFile = new File(imagePath + imageFileName);
        return imageFile;
//        try {
//            File imageFile = File.createTempFile(imageFileName,  /* prefix */
//                    ".jpg",         /* suffix */
//                    Environment.getExternalStorageDirectory()      /* directory */);
//            return imageFile;
//        } catch (IOException e) {
//            //do noting
//            return null;
//        }
    }

    /**
     * 获取日志根目录
     * @author leij
     * @return 日志根目录
     */
    static String getRootPath() {
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator;
        return rootPath;
    }
    /**
     * 计算图片的缩放值
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
             */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    /**
     * 根据路径获得突破并压缩返回bitmap用于显示
     *
     * @param filePath
     * @return
     */
    public static Bitmap getSmallBitmap(String filePath, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;  //只返回图片的大小信息
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 根据路径删除图片
     *
     * @param path
     */
    public static void deleteTempFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 添加到图库
     */
    public static void galleryAddPic(Context context, String path) {
        Intent mediaScanIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }


    /**
     *
     * android4.4以后返回的URI只有图片编号
     * 获取图片真实路径
     *
     * @param contentURI
     * @return
     */
    private static String getRealPathFromURI(Activity activity , Uri contentURI) {
        String result;
        Cursor cursor = activity.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
    /**
     * 根据图片路径转换为bitmap
     * @param path
     * @return
     */
    public static Bitmap uriToBitmap (String path) {
        File file = new File(path);
        if (file.exists()) {
            Bitmap bm = BitmapFactory.decodeFile(path);
            return bm;
        }else {
            return null;
        }
    }

    /**
     * 决解android7.0相机问题，在application的oncreat里加上
     */
    @SuppressLint("NewApi")
    public static void solveTheAndroidSevenEditionCameraProblem () {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
    }
    public interface CameraResultInterface {
        void returnIntent(Intent intent, int requestCode, String path);
    }

}
