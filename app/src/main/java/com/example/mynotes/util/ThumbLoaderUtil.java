package com.example.mynotes.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.example.mynotes.MainActivity;

public class ThumbLoaderUtil {
    private ImageView imgView;
    private String path;
    //创建cache
    private LruCache<String, Bitmap> lruCache;
    private static int cacheSize = 16 * 1024 * 1024; // 缓存大小16MiB

    @SuppressLint("NewApi")
    public ThumbLoaderUtil() {
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

    /**
     * 该方法用于返回一个提示图片已丢失的bitmap
     */
    public Bitmap getTempBitmap(){
        int width = 160, height = 160;
        int[] colors = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
//                int r = x * 255 / (width - 1);
//                int g = y * 255 / (width - 1);
//                int b = 255 - Math.min(r, g);
                int r = 119;
                int g = 136;
                int b = 153;
                int a = Math.max(r, g);
//                colors[y * width + x] = Color.argb(a, r, g, b);
                colors[y * width + x] = Color.rgb( r, g, b);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888);
        Bitmap newBitmap = bitmap.copy(bitmap.getConfig(),true);

        Resources resources = ((Context)((MainActivity)MainActivity.getMainActivityInstance())).getResources();
        float scale = resources.getDisplayMetrics().density;

        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG); // new antialised Paint
        paint.setColor(Color.rgb(68, 68, 68));       // text color - #3D3D3D
        paint.setTextSize((int)(13 * scale));           // text size in pixels
        paint.setShadowLayer(1f, 0f, 1f, Color.DKGRAY); // text shadow

        // draw text to the Canvas center
        Rect bounds1 = new Rect();
//        bounds1.set(0,0,160,80);
        String text1 = "资源损坏";
        String text2 = "损坏";
        paint.getTextBounds(text1, 0, text1.length(), bounds1);
        int x = (bitmap.getWidth() - bounds1.width()) / 6;
        int y = (bitmap.getHeight() + bounds1.height()) / 6;

        canvas.drawText(text1, x * scale , y * scale, paint);

//        Rect bounds2 = new Rect();
//        bounds2.set(0,80,160,160);
//        paint.getTextBounds(text1, 0, text1.length(), bounds2);
//        canvas.drawText(text2, x * scale , y * scale, paint);

        return newBitmap;
    }


    public void addThumbToCache(String path, Bitmap bitmap) {
        if (getThumbFromCache(path) == null) {
            if (bitmap == null) {
                bitmap = getTempBitmap();
            }
            //当前地址没有缓存时，就添加
            Log.e("mymsg", "           path:" + path + ",  bitmap:" + bitmap);
            lruCache.put(path, bitmap);
        }
    }

    public Bitmap getThumbFromCache(String path) {
        return lruCache.get(path);
    }

    public void showThumbByAsyncTask_forVideo(String path, ImageView imgview) {
        if (getThumbFromCache(path) == null) {
            //异步加载
            new MyBobAsyncTask_forVideo(imgview, path).execute(path);
        } else {
            imgview.setImageBitmap(getThumbFromCache(path));
        }
    }

    public void showThumbByAsyncTask_forImg(String path, ImageView imgview) {
        if (getThumbFromCache(path) == null) {
            //异步加载
            new MyBobAsyncTask_forImg(imgview, path).execute(path);
        } else {
            imgview.setImageBitmap(getThumbFromCache(path));
        }
    }


    class MyBobAsyncTask_forVideo extends AsyncTask<String, Void, Bitmap> {
        private ImageView imgView;
        private String path;

        public MyBobAsyncTask_forVideo(ImageView imageView, String path) {
            this.imgView = imageView;
            this.path = path;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            //这里的创建缩略图方法是调用VideoUtil类的方法，也是通过 android中提供的 ThumbnailUtils.createVideoThumbnail(vidioPath, kind);
//        Bitmap bitmap = VideoUtil.createVideoThumbnail(params[0], 200, 200, MediaStore.Video.Thumbnails.MICRO_KIND);

            Bitmap bitmap = null;

            try {
                bitmap = ThumbnailUtils.createVideoThumbnail(params[0], MediaStore.Images.Thumbnails.MICRO_KIND);
                bitmap = ThumbnailUtils.extractThumbnail(bitmap, 160, 160, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            }catch (Exception e){
                e.printStackTrace();
                bitmap = getTempBitmap();
            }

            //加入缓存中
            if (getThumbFromCache(params[0]) == null) {
                addThumbToCache(path, bitmap);
            }
//            //加入缓存中
//            if (!"null".equals(getThumbFromCache(params[0]))) {//这里原本没有加 非
//                addThumbToCache(path, bitmap);
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


    class MyBobAsyncTask_forImg extends AsyncTask<String, Void, Bitmap> {
        private ImageView imgView;
        private String path;

        public MyBobAsyncTask_forImg(ImageView imageView, String path) {
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
//            bitmap = BitmapFactory.decodeFile(img_path, options);

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
//            bitmap = BitmapFactory.decodeFile(img_path, options);

            bitmap = BitmapFactory.decodeFile(img_path, options);

            bitmap = ThumbnailUtils.extractThumbnail(bitmap, 160, 160, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            //加入缓存中
            if (getThumbFromCache(params[0]) == null) {
                addThumbToCache(path, bitmap);
            }
//            //加入缓存中
//            if (!"null".equals(getThumbFromCache(params[0]))) {//这里原本没有加 非
//                addThumbToCache(path, bitmap);
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