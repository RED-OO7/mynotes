package com.example.mynotes.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mynotes.MainActivity;
import com.example.mynotes.R;
import com.example.mynotes.control.MyVideoThumbLoader;
import com.example.mynotes.database.NotesDB;
import com.example.mynotes.model.Notes;

import java.util.ArrayList;
import java.util.List;

public class ShowListContentApdater extends ArrayAdapter<Notes> {
    private Cursor cursor;
    //    private LinearLayout cellLayout;//该布局就是每一条记录的显示布局
    private ArrayList<String> videoStr_list;
    private static MyVideoThumbLoader mVideoThumbLoader;//用于加载缩略图的自定义加载器
    private static int textViewResourceId = R.id.cellListView;//这是控件的id

    public ShowListContentApdater(Context context, int textViewResourceId, List<Notes> objects) {
        super(context, textViewResourceId, objects);
        this.textViewResourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater;//获取layoutInflater加载布局的系统服务
        View cellLayout;//利用layoutInflater获取每条记录的布局

        MyViewHolder myViewHolder;

        if (convertView == null) {//如果为空，则说明是刚创建
            /*------------------新操作----------------------*/
            layoutInflater = LayoutInflater.from(getContext());//获取layoutInflater加载布局的系统服务
            cellLayout = layoutInflater.inflate(R.layout.cell, parent, false);//利用layoutInflater获取每条记录的布局

            //新建myViewHolder并初始化
            myViewHolder = new MyViewHolder();
            myViewHolder.myCell_linear_content = cellLayout.findViewById(R.id.cell_linear_content);
            myViewHolder.myCell_linear_time = cellLayout.findViewById(R.id.cell_linear_time);
            myViewHolder.myCell_linear_owner = cellLayout.findViewById(R.id.cell_linear_owner);
            myViewHolder.myCell_text = cellLayout.findViewById(R.id.cell_text);
            myViewHolder.myCell_sound = cellLayout.findViewById(R.id.cell_sound);
            myViewHolder.myCell_img = cellLayout.findViewById(R.id.cell_img);
            myViewHolder.myCell_video = cellLayout.findViewById(R.id.cell_video);

            cellLayout.setTag(myViewHolder);//给该每一个单个个显示框设置viewHolder

            mVideoThumbLoader = MainActivity.mainVideoThumbLoader;// 初始化缩略图载入方法
            /*------------------新操作----------------------*/

        } else {//否则view不为空，则可以取出viewholder
            cellLayout = (LinearLayout) convertView;//取出convertView并强转为LinearLayout
            myViewHolder = (MyViewHolder) cellLayout.getTag();//取出ViewHolder并强制转换为MyViewHolder
        }

        Notes nowNote = getItem(position);

        String content_str = nowNote.getContent();
        String title_str = nowNote.getTitle();
        String time_str = nowNote.getTime();
        String img_path = nowNote.getPic_path();
        String video_path = nowNote.getVideo_path();
        String sound_path = nowNote.getSound_path();
        String owner_str = nowNote.getOwner();

        myViewHolder.myCell_linear_content.setText(title_str);//给获取到的cell文本内容框设置数据库获取到的文本内容
        myViewHolder.myCell_linear_time.setText(time_str);
        myViewHolder.myCell_linear_owner.setText("记录者:" + owner_str);

        //原本那一大段显示与隐藏缩略图框的操作代码被移到了初始化view的if判断语句里面
        //初始化时，列表左边显示的记事模式提示全部隐藏
        myViewHolder.myCell_text.setVisibility(View.GONE);
        myViewHolder.myCell_img.setVisibility(View.GONE);
        myViewHolder.myCell_video.setVisibility(View.GONE);
        myViewHolder.myCell_sound.setVisibility(View.GONE);

        if ("null".equals(img_path) && "null".equals(video_path) && "null".equals(sound_path)) {//三种模式路径都为空则说明为文字记录
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


    /**
     * 该类用于存储各种控件，如缩略图控件
     */
    private class MyViewHolder {
        TextView myCell_linear_content;
        TextView myCell_linear_time;
        TextView myCell_linear_owner;
        TextView myCell_text;
        TextView myCell_sound;
        ImageView myCell_img;
        ImageView myCell_video;
    }
}
