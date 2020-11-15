package com.example.mynotes.view.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MediaController;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.mynotes.R;
import com.example.mynotes.util.FileUtil;
import com.example.mynotes.dao.NotesDB;
import com.example.mynotes.model.Notes;

public class DetailActivity extends Activity implements View.OnClickListener {
    private Button bt_delete, bt_return, bt_display, bt_stop;
    private ImageView detail_img;
    private VideoView detail_video;
    private EditText detail_title;
    private EditText detail_text;
    private TextView detail_seize1;
    private TextView detail_time;

    private LinearLayout detail_linearSound;//该add_linearSound用于存放播放和暂停按钮

    private NotesDB notesDB;
    private SQLiteDatabase notesWriter;//该dbWriter用以删除数据库的记录
    private Notes nowNote;//该记录为目前的记事记录

    //    private MediaRecorder mediaRecorder = new MediaRecorder();
    private MediaPlayer mediaPlayer = new MediaPlayer();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);//设置详情页的显示布局

        Toast toast = Toast.makeText(this, "该信息ID为：" + getIntent().getIntExtra(NotesDB.ID, 0), Toast.LENGTH_SHORT);
//        toast.show();

        bt_delete = findViewById(R.id.bt_delete);
        bt_return = findViewById(R.id.bt_return);
        bt_display = findViewById(R.id.bt_display);
        bt_stop = findViewById(R.id.bt_stop);
        detail_img = findViewById(R.id.detail_img);
        detail_video = findViewById(R.id.detail_video);
        detail_title = findViewById(R.id.detail_title);
        detail_text = findViewById(R.id.detail_text);
        detail_seize1 = findViewById(R.id.detail_seize1);
        detail_time = findViewById(R.id.detail_time);
        detail_linearSound = findViewById(R.id.detail_linearSound);

//        notesDB = new NotesDB(this);
//        notesWriter=notesDB.getWritableDatabase();//创建数据库写入器
        bt_delete.setOnClickListener(this);//设置删除按钮的监听事件
        bt_return.setOnClickListener(this);//设置返回按钮的监听事件
        bt_display.setOnClickListener(this);
        bt_stop.setOnClickListener(this);

        Intent get_intent = getIntent();//获取到传递过来的意图
        nowNote = (Notes) get_intent.getSerializableExtra(Notes.CLASSNAME);//获取传递过来的Notes实例

        detail_seize1.setVisibility(View.GONE);
        detail_title.setText(nowNote.getTitle());
        detail_text.setText(nowNote.getContent());//给文本框设置内容
//        Toast.makeText(this,get_intent.getStringExtra(NotesDB.CONTENT),Toast.LENGTH_LONG).show();//显示意图传递的content

        detail_time.setText(nowNote.getTime());//设置时间
        detail_time.bringToFront();//设置到最上层
        //如果获取到图片路径的为空，则不显示
        String imgPath_get = nowNote.getPic_path();//这里获取的居然是string类型的null？？？
        if ("null".equals(imgPath_get)) {
            detail_img.setVisibility(View.GONE);
        } else {
            Toast.makeText(this, "该图片路径为：" + imgPath_get, Toast.LENGTH_SHORT).show();
            detail_img.setVisibility(View.VISIBLE);
            detail_seize1.setVisibility(View.VISIBLE);
            Bitmap bitmap = BitmapFactory.decodeFile(imgPath_get);//使用bitmap装载图片
            detail_img.setImageBitmap(bitmap);//使用bitmap来显示图片
        }
        //如果获取到视频路径的为空，则不显示
        String videoPath_get = nowNote.getVideo_path();//这里获取的居然是string类型的null？？？
        if ("null".equals(videoPath_get)) {
            detail_video.setVisibility(View.GONE);
        } else {
            detail_video.setVisibility(View.VISIBLE);
            detail_seize1.setVisibility(View.VISIBLE);
//            detail_video.setVideoURI(Uri.parse(videoPath_get));//直接使用uri加载视频
//            String temp_path="/storage/emulated/0/MyNotes/VID_20200619_170036.mp4";
//            String temp_path="/storage/emulated/0/MyNotes/img2020年06月19日153042.mp4";
            Toast.makeText(this, "该视频路径为：" + videoPath_get, Toast.LENGTH_SHORT).show();

            detail_video.setVideoPath(videoPath_get);
            MediaController mediaController = new MediaController(this);//创建MediaController对象
            detail_video.setMediaController(mediaController);//VideoView与MediaController建立关联
            detail_video.requestFocus();//让VideoView获取焦点
            detail_video.start();//启动播放视频
        }
        //如果获取到录音路径的为空，则不显示
        String soundPath_get = nowNote.getSound_path();//这里获取的居然是string类型的null？？？
        if ("null".equals(soundPath_get) || soundPath_get == null) {
//            detail_video.setVisibility(View.GONE);
//            Toast.makeText(this,"该录音路径为："+soundPath_get,Toast.LENGTH_SHORT).show();
        } else {
            detail_seize1.setVisibility(View.GONE);
            detail_linearSound.setVisibility(View.VISIBLE);
            bt_display.setEnabled(true);
            bt_stop.setEnabled(false);
            try{
                mediaPlayer.setDataSource(soundPath_get);
                mediaPlayer.prepare();
            }catch (Exception e){
                e.printStackTrace();
            }
            Toast.makeText(this, "该录音路径为：" + soundPath_get, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 该方法用于更新一条数据
     */
    public void updateItem() {
        String title_input = detail_title.getText().toString().trim();//如果输入为空，则获取到""字符串
        title_input = title_input.replaceAll("\r|\n", "");//将标题换行都去掉
        String content_input = detail_text.getText().toString();//如果输入为空，则获取到""字符串

        notesDB = new NotesDB(this);
        notesWriter = notesDB.getWritableDatabase();//创建数据库写入器

        int id = ((Notes)getIntent().getSerializableExtra(Notes.CLASSNAME)).getId();//获取该行数据的id
        ContentValues contentValues = new ContentValues();
        //id不需要放入是因为设置了id自增
        contentValues.put(NotesDB.TITLE, title_input);
        contentValues.put(NotesDB.CONTENT,content_input);
        contentValues.put(NotesDB.CHANGE_TIME, AddContentActivity.getNowTimeStr());//修改时间改了
        contentValues.put(NotesDB.IS_CHANGE, 1);//is_change标识为1表示需要服务器更新
        int resultInt = notesWriter.update(NotesDB.TABLE_NAME, contentValues,NotesDB.ID + " = " + id,null);

        if (resultInt == 1){//如果修改数量为1，说明修改成功
            Toast.makeText(this, "记录修改成功！", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "记录修改失败！(原因未知，不可知论)", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteItem() {//该方法用于删除一条数据
        notesDB = new NotesDB(this);
        notesWriter = notesDB.getWritableDatabase();//创建数据库写入器
        int id = ((Notes)getIntent().getSerializableExtra(Notes.CLASSNAME)).getId();//获取该行数据的id
        notesWriter.delete(NotesDB.TABLE_NAME, NotesDB.ID + " = " + id, null);//根据id删除该行数据
    }


    public void display() {//播放录音按钮
        try{
//            mediaPlayer.setDataSource(getIntent().getStringExtra(NotesDB.SOUND_PATH));
//            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void stopDisplay() {//这是停止播放的方法
//        mediaPlayer.stop();
    }


    @Override
    public void onClick(View v) {
//        Intent get_intent = getIntent();//获取到传递过来的意图
//        String title_str = get_intent.getStringExtra(NotesDB.TITLE);
//        String content_str = get_intent.getStringExtra(NotesDB.CONTENT);



        String picPath_str = nowNote.getPic_path();
        String videoPath_str = nowNote.getVideo_path();
        String soundPath_str = nowNote.getSound_path();
//        String soundPath_str=get_intent.getStringExtra(NotesDB.);

        switch (v.getId()) {
            case R.id.bt_delete:
                //如果获取到图片路径的为空，则不删除,否则得删除
                if ("null".equals(picPath_str) || picPath_str == null) {
//                    Toast.makeText(this,"该图片不存在！删除失败！",Toast.LENGTH_SHORT ).show();
                } else {
                    FileUtil.deleteBoth(this, picPath_str);//删除图片
                }
                //如果获取到视频路径的为空，则不删除,否则得删除
                if ("null".equals(videoPath_str) || videoPath_str == null) {
//                    Toast.makeText(this,"该文件不存在！删除失败！",Toast.LENGTH_SHORT ).show();
                } else {
                    FileUtil.deleteBoth(this, videoPath_str);//删除视频
                }
                //如果获取到录音路径的为空，则不删除,否则得删除
                if ("null".equals(soundPath_str) || soundPath_str == null) {
//                    Toast.makeText(this,"该文件不存在！删除失败！",Toast.LENGTH_SHORT ).show();
                } else {
                    FileUtil.deleteBoth(this, soundPath_str);//删除录音
                }

                deleteItem();//删除记录
                finish();
                break;

            case R.id.bt_return:
                //开始时判断是否有必要更新记录
                String title_str = nowNote.getTitle();
                String content_str = nowNote.getContent();//如果为null，则获取null本身，但不可能为null，只能为""
                String title_input = detail_title.getText().toString().trim();//如果输入为空，则获取到""字符串
                title_input = title_input.replaceAll("\r|\n", "");//将标题换行都去掉
                String content_input = detail_text.getText().toString();//如果输入为空，则获取到""字符串

                if ("".equals(title_input)){//如果输入的标题为空，则提示不能为空！
                    Toast.makeText(this, "记事标题不能为空！", Toast.LENGTH_SHORT).show();
                }else {//否则判断是否有必要更新记录
                    if (!title_input.equals(title_str)||!content_input.equals(content_str)){//否则一旦其中一项有不同，则需要更新记录
                        updateItem();//更新记录并执行更新
                    }
                    finish();//最后会直接结束
                }
                break;

            case R.id.bt_display://播放录音
                display();//播放录音
                bt_display.setEnabled(false);
                bt_stop.setEnabled(true);
                Toast.makeText(this, "录音播放", Toast.LENGTH_SHORT).show();
                break;

            case R.id.bt_stop://停止播放
                stopDisplay();
                bt_display.setEnabled(true);
                bt_stop.setEnabled(false);
                Toast.makeText(this, "停止播放", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    @Override
    protected void onDestroy() {//关闭资源
        super.onDestroy();
//        notesWriter.close();
//        notesDB.close();
    }
}
