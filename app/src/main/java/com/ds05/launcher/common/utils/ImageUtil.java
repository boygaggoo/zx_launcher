package com.ds05.launcher.common.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;

/**
 * Created by kabru on 2017/6/18.
 */

public class ImageUtil {

    private static final String SDCARD_CACHE_IMG_PATH = Environment.getExternalStorageDirectory().getPath() + "/DS05/images/";

    /**
     * 保存图片到SD卡
     * @param imagePath
     * @param buffer
     * @throws IOException
     */
    public static void saveImage(String imagePath, byte[] buffer)  throws IOException {
        File f = new File(imagePath);
        if (f.exists()) {
            return;
        } else {
            File parentFile = f.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(imagePath);
            fos.write(buffer);
            fos.flush();
            fos.close();
        }
    }

    /**
     * 从SD卡加载图片
     * @param imagePath
     * @return
     */
    public static Bitmap getImageFromLocal(String imagePath){
        File file = new File(imagePath);
        if(file.exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            file.setLastModified(System.currentTimeMillis());
            return bitmap;
        }
        return null;
    }

    /**
     * Bitmap转换到Byte[]
     * @param bm
     * @return
     */
    public static byte[] bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bas);
        return bas.toByteArray();
    }

    /**
     * 从本地或者服务端加载图片
     * @return
     * @throws IOException
     */
    public static Bitmap loadImage(final String imagePath,final String imgUrl,final ImageCallback callback) {
        Bitmap bitmap = getImageFromLocal(imagePath);
        if(bitmap != null){
            return bitmap;
        }else{//从网上加载
            final Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    if(msg.obj!=null){
                        Bitmap bitmap = (Bitmap) msg.obj;
                        callback.loadImage(bitmap, imagePath);
                    }
                }
            };

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(imgUrl);
                        Log.e("图片加载", imgUrl);
                        URLConnection conn = url.openConnection();
                        conn.connect();
                        BufferedInputStream bis = new BufferedInputStream(conn.getInputStream(),8192) ;
                        Bitmap bitmap = BitmapFactory.decodeStream(bis);
                        //保存文件到sd卡
                        saveImage(imagePath,bitmap2Bytes(bitmap));
                        Message msg = handler.obtainMessage();
                        msg.obj = bitmap;
                        handler.sendMessage(msg);
                    } catch (IOException e) {
                        Log.e(ImageUtil.class.getName(), "保存图片到本地存储卡出错！");
                    }
                }
            };
           // ThreadPoolManager.getInstance().addAsyncTask(runnable);
        }
        return null;
    }

    // 返回图片存到sd卡的路径
    public static String getCacheImgPath() {
        return SDCARD_CACHE_IMG_PATH;
    }

    public static String md5(String paramString) {
        String returnStr;
        try {
            MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
            localMessageDigest.update(paramString.getBytes());
            returnStr = byteToHexString(localMessageDigest.digest());
            return returnStr;
        } catch (Exception e) {
            return paramString;
        }
    }

    /**
     * 将指定byte数组转换成16进制字符串
     *
     * @param b
     * @return
     */
    public static String byteToHexString(byte[] b) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            hexString.append(hex.toUpperCase());
        }
        return hexString.toString();
    }

    /**
     *
     * @author Mathew
     *
     */
    public interface ImageCallback{
        public void loadImage(Bitmap bitmap, String imagePath);
    }

    /**
     * 按尺寸压缩图片
     *
     * @param srcPath  图片路径
     * @param desWidth 压缩的图片宽度
     * @return Bitmap 对象
     */

    public static Bitmap compressImageFromFile(String srcPath, float desWidth) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;//只读边,不读内容
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float desHeight = desWidth * h / w;
        int be = 1;
        if (w > h && w > desWidth) {
            be = (int) (newOpts.outWidth / desWidth);
        } else if (w < h && h > desHeight) {
            be = (int) (newOpts.outHeight / desHeight);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置采样率

//        newOpts.inPreferredConfig = Config.ARGB_8888;//该模式是默认的,可不设
        newOpts.inPurgeable = true;// 同时设置才会有效
        newOpts.inInputShareable = true;//。当系统内存不够时候图片自动被回收

        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return bitmap;
    }

    /**
     * 压缩图片（质量压缩）
     *
     * @param image
     */

    public static File compressImage(Bitmap image,String saveFileName) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中

        int options = 100;

        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            options -= 10;//每次都减少10
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            long length = baos.toByteArray().length;
        }
//        long length = baos.toByteArray().length;
//        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
//        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片


        File file = new File(Environment.getExternalStorageDirectory() + File.separator+saveFileName+".png");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            try {
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return file;
    }


    /**
     * Java文件操作 获取不带扩展名的文件名
     * @param filename
     * @return
     */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }
}
