package com.example.mynotes.util;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.LruCache;
import android.widget.ImageView;

public class MyVideoThumbLoader {
    private ImageView imgView;
    private String path;
    //创建cache
    private LruCache<String, Bitmap> lruCache;
    private static int cacheSize = 16 * 1024 * 1024; // 缓存大小16MiB

    @SuppressLint("NewApi")
    public MyVideoThumbLoader() {
        int maxMemory = (int) Runtime.getRuntime().maxMemory();//获取最大的运行内存
        int maxSize = maxMemory / 4;//拿到缓存的内存大小
        lruCache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //这个方法会在每次存入缓存的时候调用
                return value.getByteCount();
            }
        };
    }

    public void addVideoThumbToCache(String path, Bitmap bitmap) {
        if (getVideoThumbToCache(path) == null) {
            //当前地址没有缓存时，就添加
            lruCache.put(path, bitmap);
        }
    }

    public Bitmap getVideoThumbToCache(String path) {
        return lruCache.get(path);
    }

    public void showThumbByAsynctack(String path, ImageView imgview) {

        if (getVideoThumbToCache(path) == null) {
            //异步加载
            new MyBobAsynctack(imgview, path).execute(path);
        } else {
            imgview.setImageBitmap(getVideoThumbToCache(path));
        }

    }

    public void showThumbByAsynctack_forImg(String path, ImageView imgview) {

        if (getVideoThumbToCache(path) == null) {
            //异步加载
            new MyBobAsynctack_forImg(imgview, path).execute(path);
        } else {
            imgview.setImageBitmap(getVideoThumbToCache(path));
        }

    }


    class MyBobAsynctack extends AsyncTask<String, Void, Bitmap> {
        private ImageView imgView;
        private String path;

        public MyBobAsynctack(ImageView imageView, String path) {
            this.imgView = imageView;
            this.path = path;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            //这里的创建缩略图方法是调用VideoUtil类的方法，也是通过 android中提供的 ThumbnailUtils.createVideoThumbnail(vidioPath, kind);
//        Bitmap bitmap = VideoUtil.createVideoThumbnail(params[0], 200, 200, MediaStore.Video.Thumbnails.MICRO_KIND);

            Bitmap bitmap = null;
            bitmap = ThumbnailUtils.createVideoThumbnail(params[0], MediaStore.Images.Thumbnails.MICRO_KIND);
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, 160, 160, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

            //加入缓存中
            if (getVideoThumbToCache(params[0]) == null) {
                addVideoThumbToCache(path, bitmap);
            }
//            //加入缓存中
//            if (!"null".equals(getVideoThumbToCache(params[0]))) {//这里原本没有加 非
//                addVideoThumbToCache(path, bitmap);
//            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imgView.getTag().equals(path)) {//通过 Tag可以绑定 图片地址和 imageView，这是解决Listview加载图片错位的解决办法之一
                imgView.setImageBitmap(bitmap);
            }
        }
    }


    class MyBobAsynctack_forImg extends AsyncTask<String, Void, Bitmap> {
        private ImageView imgView;
        private String path;

        public MyBobAsynctack_forImg(ImageView imageView, String path) {
            this.imgView = imageView;
            this.path = path;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            //这里的创建缩略图方法是调用VideoUtil类的方法，也是通过 android中提供的 ThumbnailUtils.createVideoThumbnail(vidioPath, kind);
//        Bitmap bitmap = VideoUtil.createVideoThumbnail(params[0], 200, 200, MediaStore.Video.Thumbnails.MICRO_KIND);

            Bitmap bitmap = null;
            String img_path = params[0];
            BitmapFactory.Options options = new BitmapFactory.Options();//通过此对象获取缩略图
            options.inJustDecodeBounds = true;
            bitmap = BitmapFactory.decodeFile(img_path, options);
            options.inJustDecodeBounds = false;
            int beWidth = options.outWidth / 160;
            int beHeight = options.outHeight / 160;
            int be = 1;
            if (beWidth < beHeight) {//be需要取最小的那个
                be = beWidth;
            } else {
                be = beHeight;
            }
            if (be <= 0) {//be不能小于等于0
                be = 1;
            }

            options.inSampleSize = be;
            bitmap = BitmapFactory.decodeFile(img_path, options);
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, 160, 160, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            //加入缓存中
            if (getVideoThumbToCache(params[0]) == null) {
                addVideoThumbToCache(path, bitmap);
            }
//            //加入缓存中
//            if (!"null".equals(getVideoThumbToCache(params[0]))) {//这里原本没有加 非
//                addVideoThumbToCache(path, bitmap);
//            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imgView.getTag().equals(path)) {//通过 Tag可以绑定 图片地址和 imageView，这是解决Listview加载图片错位的解决办法之一
                imgView.setImageBitmap(bitmap);
            }
        }
    }

}