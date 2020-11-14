package com.example.mynotes.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mynotes.MainActivity;
import com.example.mynotes.R;
import com.example.mynotes.control.MyVideoThumbLoader;
import com.example.mynotes.database.NotesDB;

import java.util.ArrayList;

public class ShowListContentApdater extends BaseAdapter {
    private Context context;
    private Cursor cursor;
//    private LinearLayout cellLayout;//该布局就是每一条记录的显示布局
    private ArrayList<String> videoStr_list;
    private static MyVideoThumbLoader mVideoThumbLoader;//用于加载缩略图的自定义加载器

    TextView cell_linear_content;
    TextView cell_linear_time;
    TextView cell_linear_owner;
    TextView cell_text;
    TextView cell_sound;
    ImageView cell_img;
    ImageView cell_video;

    public ShowListContentApdater(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public Object getItem(int position) {//该方法用于返回物品
        return cursor.getPosition();
    }

    @Override
    public long getItemId(int position) {//该方法用于返回位置
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

//        LayoutInflater layoutInflater = LayoutInflater.from(context);//获取layoutInflater加载布局的系统服务
//        LinearLayout cellLayout = (LinearLayout) layoutInflater.inflate(R.layout.cell, null);//利用layoutInflater获取每条记录的布局
        LayoutInflater layoutInflater ;//获取layoutInflater加载布局的系统服务
        View cellLayout;//利用layoutInflater获取每条记录的布局

        MyViewHolder myViewHolder;

        if (convertView == null) {//如果为空，则说明是刚创建

            /*------------------原操作----------------------*/
//            layoutInflater = LayoutInflater.from(context);//获取layoutInflater加载布局的系统服务
//            cellLayout = (LinearLayout) layoutInflater.inflate(R.layout.cell, null);//利用layoutInflater获取每条记录的布局
//            cell_linear_content = cellLayout.findViewById(R.id.cell_linear_content);
//            cell_linear_time = cellLayout.findViewById(R.id.cell_linear_time);
//            cell_linear_owner = cellLayout.findViewById(R.id.cell_linear_owner);
//            cell_text = cellLayout.findViewById(R.id.cell_text);
//            cell_sound = cellLayout.findViewById(R.id.cell_sound);
//            cell_img = cellLayout.findViewById(R.id.cell_img);
//            cell_video = cellLayout.findViewById(R.id.cell_video);
//
//            //新建myViewHolder并初始化
//            myViewHolder = new MyViewHolder();
//            myViewHolder.myCell_linear_content = cell_linear_content;
//            myViewHolder.myCell_linear_time = cell_linear_time;
//            myViewHolder.myCell_linear_owner = cell_linear_owner;
//            myViewHolder.myCell_text = cell_text;
//            myViewHolder.myCell_sound = cell_sound;
//            myViewHolder.myCell_img = cell_img;
//            myViewHolder.myCell_video = cell_video;
//
//            cellLayout.setTag(myViewHolder);//给该m每一个单个个显示框设置viewHolder
            /*------------------原操作----------------------*/

            /*------------------新操作----------------------*/
            layoutInflater = LayoutInflater.from(context);//获取layoutInflater加载布局的系统服务
            cellLayout = layoutInflater.inflate(R.layout.cell, parent,false);//利用layoutInflater获取每条记录的布局

            //新建myViewHolder并初始化
            myViewHolder = new MyViewHolder();
            myViewHolder.myCell_linear_content =  cellLayout.findViewById(R.id.cell_linear_content);
            myViewHolder.myCell_linear_time = cellLayout.findViewById(R.id.cell_linear_time);
            myViewHolder.myCell_linear_owner = cellLayout.findViewById(R.id.cell_linear_owner);
            myViewHolder.myCell_text = cellLayout.findViewById(R.id.cell_text);
            myViewHolder.myCell_sound = cellLayout.findViewById(R.id.cell_sound);
            myViewHolder.myCell_img =  cellLayout.findViewById(R.id.cell_img);
            myViewHolder.myCell_video = cellLayout.findViewById(R.id.cell_video);

            cellLayout.setTag(myViewHolder);//给该每一个单个个显示框设置viewHolder

            mVideoThumbLoader = MainActivity.mainVideoThumbLoader;// 初始化缩略图载入方法
            /*------------------新操作----------------------*/

        } else {//否则view不为空，则可以取出viewholder
            cellLayout = (LinearLayout) convertView;//取出convertView并强转为LinearLayout
            myViewHolder = (MyViewHolder) cellLayout.getTag();//取出ViewHolder并强制转换为MyViewHolder
        }

//        cell_text = cellLayout.findViewById(R.id.cell_text);
//        cell_sound = cellLayout.findViewById(R.id.cell_sound);
//        cell_img = cellLayout.findViewById(R.id.cell_img);
//        cell_video = cellLayout.findViewById(R.id.cell_video);


        cursor.moveToPosition(position);//需要手动移动游标进行查询
        String content_str = cursor.getString(cursor.getColumnIndex(NotesDB.CONTENT));//获取数据库的内容
        String title_str = cursor.getString(cursor.getColumnIndex(NotesDB.TITLE));
        String time_str = cursor.getString(cursor.getColumnIndex(NotesDB.CHANGE_TIME));
        String img_path = cursor.getString(cursor.getColumnIndex(NotesDB.PIC_PATH));
        String video_path = cursor.getString(cursor.getColumnIndex(NotesDB.VIDEO_PATH));
        String sound_path = cursor.getString(cursor.getColumnIndex(NotesDB.SOUND_PATH));
        String owner_str = cursor.getString(cursor.getColumnIndex(NotesDB.OWNER));

        myViewHolder.myCell_linear_content.setText(title_str);//给获取到的cell文本内容框设置数据库获取到的文本内容
        myViewHolder.myCell_linear_time.setText(time_str);
        myViewHolder.myCell_linear_owner.setText("记录者:" + owner_str);

        //原本那一大段显示与隐藏缩略图框的操作代码被移到了初始化view的if判断语句里面
        //初始化时，列表左边显示的记事模式提示全部隐藏
        myViewHolder.myCell_text.setVisibility(View.GONE);
        myViewHolder.myCell_img.setVisibility(View.GONE);
        myViewHolder.myCell_video.setVisibility(View.GONE);
        myViewHolder.myCell_sound.setVisibility(View.GONE);

        if ("null".equals(img_path) && "null".equals(video_path) && "null".equals(sound_path)){//三种模式路径都为空则说明为文字记录
            myViewHolder.myCell_text.setVisibility(View.VISIBLE);
        }
        if (!"null".equals(img_path) && !(img_path == null)) {//如果图片路径不为空，则显示
            myViewHolder.myCell_text.setVisibility(View.GONE);
            myViewHolder.myCell_img.setVisibility(View.VISIBLE);
//            cell_img.setImageBitmap(getImageThumbnail(img_path,200,200));//原设置缩略图的方法
            String path = img_path;
            myViewHolder.myCell_img.setTag(path);//绑定imageview
            mVideoThumbLoader.showThumbByAsynctack_forImg(path, myViewHolder.myCell_img);
        }
        if (!"null".equals(video_path) && !(video_path == null)) {//如果视频路径不为空，则显示
            myViewHolder.myCell_text.setVisibility(View.GONE);
            myViewHolder.myCell_video.setVisibility(View.VISIBLE);
//            cell_video.setImageBitmap(getVideoThumbnail(video_path,200,200, MediaStore.Images.Thumbnails.MICRO_KIND));
            String path = video_path;
            myViewHolder.myCell_video.setTag(path);//绑定imageview
            mVideoThumbLoader.showThumbByAsynctack(path, myViewHolder.myCell_video);
        }
        if (!"null".equals(sound_path) && !(sound_path == null)) {//如果录音路径不为空，则显示
            myViewHolder.myCell_text.setVisibility(View.GONE);
            myViewHolder.myCell_sound.setVisibility(View.VISIBLE);
        }

        return cellLayout;//原返回对象
//        return convertView;
    }


    public Bitmap getImageThumbnail(String img_path, int width, int height) {//该方法用于获取图片的缩略图
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();//通过此对象获取缩略图
        options.inJustDecodeBounds = true;
        bitmap = BitmapFactory.decodeFile(img_path, options);
        options.inJustDecodeBounds = false;
        int beWidth = options.outWidth / width;
        int beHeight = options.outHeight / height;
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
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

        return bitmap;
    }


    public Bitmap getVideoThumbnail(String video_path, int width, int height, int kind) {//该方法用于获取视频的缩略图
        Bitmap bitmap = null;
        bitmap = ThumbnailUtils.createVideoThumbnail(video_path, kind);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

//        MediaMetadataRetriever media = new MediaMetadataRetriever();
//        media.setDataSource(video_path);
//        Bitmap bitmap = media.getFrameAtTime();

        return bitmap;
    }

    /**
     * 该类用于存储缩略图
     */
    private class MyViewHolder {
        int id;//该id为记录的id，用以判别当前id是否一样，不一样则还是要找寻控件
        TextView myCell_linear_content;
        TextView myCell_linear_time;
        TextView myCell_linear_owner;
        TextView myCell_text;
        TextView myCell_sound;
        ImageView myCell_img;
        ImageView myCell_video;
    }
}
