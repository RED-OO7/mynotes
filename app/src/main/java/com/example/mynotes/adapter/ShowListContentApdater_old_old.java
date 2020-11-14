package com.example.mynotes.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mynotes.R;
import com.example.mynotes.database.NotesDB;

public class ShowListContentApdater_old_old extends BaseAdapter {
    private Context context;
    private Cursor cursor;
    private LinearLayout cellLayout;//该布局就是每一条记录的显示布局

    public ShowListContentApdater_old_old(Context context, Cursor cursor){
        this.context=context;
        this.cursor=cursor;
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

        LayoutInflater layoutInflater=LayoutInflater.from(context);//获取layoutInflater加载布局的系统服务
        cellLayout = (LinearLayout) layoutInflater.inflate(R.layout.cell,null);//利用layoutInflater获取每条记录的布局

        TextView cell_linear_content=cellLayout.findViewById(R.id.cell_linear_content);
        TextView cell_linear_time=cellLayout.findViewById(R.id.cell_linear_time);
        ImageView cell_img=cellLayout.findViewById(R.id.cell_img);
        ImageView cell_video=cellLayout.findViewById(R.id.cell_video);

        cursor.moveToPosition(position);//需要手动移动游标进行查询
        String content_str=cursor.getString(cursor.getColumnIndex(NotesDB.CONTENT));//获取数据库的内容
        String time_str=cursor.getString(cursor.getColumnIndex(NotesDB.TIME));
        String img_path=cursor.getString(cursor.getColumnIndex(NotesDB.PIC_PATH));
        String video_path=cursor.getString(cursor.getColumnIndex(NotesDB.VIDEO_PATH));

        cell_linear_content.setText(content_str);//给获取到的cell文本内容框设置数据库获取到的文本内容
        cell_linear_time.setText(time_str);
        cell_img.setImageBitmap(getImageThumbnail(img_path,200,200));
        cell_video.setImageBitmap(getVideoThumbnail(video_path,200,200, MediaStore.Images.Thumbnails.MICRO_KIND));

        if (!"null".equals(img_path)&&!(img_path==null)){//如果图片路径不为空，则显示
            cell_img.setVisibility(View.VISIBLE);
        }
        if (!"null".equals(video_path)&&!(video_path==null)){//如果视频路径不为空，则显示
            cell_video.setVisibility(View.VISIBLE);
        }

        return cellLayout;
    }


    public Bitmap getImageThumbnail(String img_path,int width,int height){//该方法用于获取图片的缩略图
        Bitmap bitmap=null;
        BitmapFactory.Options options = new BitmapFactory.Options();//通过此对象获取缩略图
        options.inJustDecodeBounds = true;
        bitmap = BitmapFactory.decodeFile(img_path,options);
        options.inJustDecodeBounds = false;
        int beWidth = options.outWidth / width;
        int beHeight = options.outHeight / height;
        int be=1;
        if (beWidth<beHeight){//be需要取最小的那个
            be=beWidth;
        }else {
            be=beHeight;
        }
        if (be<=0){//be不能小于等于0
            be=1;
        }

        options.inSampleSize=be;
        bitmap=BitmapFactory.decodeFile(img_path,options);
        bitmap= ThumbnailUtils.extractThumbnail(bitmap,width,height,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

        return bitmap;
    }


    public Bitmap getVideoThumbnail(String video_path, int width, int height,int kind){//该方法用于获取视频的缩略图
        Bitmap bitmap=null;
        bitmap = ThumbnailUtils.createVideoThumbnail(video_path,kind);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width,height,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

//        MediaMetadataRetriever media = new MediaMetadataRetriever();
//        media.setDataSource(video_path);
//        Bitmap bitmap = media.getFrameAtTime();

        return bitmap;
    }
}
