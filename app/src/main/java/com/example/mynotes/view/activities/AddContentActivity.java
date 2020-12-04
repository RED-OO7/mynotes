package com.example.mynotes.view.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;

import com.example.mynotes.R;
import com.example.mynotes.model.Notes;
import com.example.mynotes.util.FileUtil;
import com.example.mynotes.util.PermisionUtils;
import com.example.mynotes.dao.NotesDB;
import com.example.mynotes.view.fragments.ContentFragment;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddContentActivity extends AppCompatActivity implements View.OnClickListener {

    /* 原始requestCode 1代表文字，2代表图片，3代表视频，4代表录音*/
    public static final int TEXT_REQUEST_CODE = 101;
    public static final int PICTURE_REQUEST_CODE = 102;
    public static final int VIDEO_REQUEST_CODE = 103;
    public static final int SOUND_REQUEST_CODE = 104;

    private AppBarLayout add_appBar;

    private String flag_str;
    private Button bt_save, bt_cancel, bt_display, bt_stop, bt_start, bt_finish;//声明按钮
    private EditText add_title;//输入内容标题框
    private EditText add_text;//输入内容的框
    private ImageView add_img;//图片
    private VideoView add_video;//视频
//    private TextView add_seize1;//占位用
    private LinearLayout add_linearSound;//装载播放声音按钮用
    private LinearLayout add_linearListen;//装载录制声音按钮用
    private MediaRecorder mediaRecorder = new MediaRecorder();
    private MediaPlayer mediaPlayer = new MediaPlayer();

    private NotesDB notesDB;//数据库接入对象
    private SQLiteDatabase notesWriter;//数据库写入对象

    private File imgFile;//用以存储图像
    private File videoFile;//用以存储视频
    private File soundFile;//用以存储录音
    private String owner;//用以存储信息拥有者

    private String alternativeTitle = "";//这是给拍照，录像和录音用的记事记录用的懒人替代标题

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // android 7.0系统解决拍照的问题
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_content);

        Toolbar toolbar = (Toolbar) findViewById(R.id.add_toolBar);
        toolbar.setTitle("");//将工具栏里的标题设置为空
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        add_appBar = findViewById(R.id.add_appBar);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        flag_str = getIntent().getStringExtra("flag");//获取传递过来的flag值
        owner = getIntent().getStringExtra("owner");//获取传递过来的owner值

        //以下为查找控件
        bt_save = (Button) findViewById(R.id.bt_save);
        bt_cancel = (Button) findViewById(R.id.bt_cancel);
        bt_display = (Button) findViewById(R.id.bt_display);
        bt_stop = (Button) findViewById(R.id.bt_stop);
        bt_start = (Button) findViewById(R.id.bt_start);
        bt_finish = (Button) findViewById(R.id.bt_finish);
        add_title = (EditText) findViewById(R.id.add_title);
        add_text = (EditText) findViewById(R.id.add_text);
        add_img = (ImageView) findViewById(R.id.add_img);
        add_video = (VideoView) findViewById(R.id.add_video);
//        add_seize1 = (TextView) findViewById(R.id.add_seize1);
        add_linearSound = (LinearLayout) findViewById(R.id.add_linearSound);
        add_linearListen = (LinearLayout) findViewById(R.id.add_linearListen);

        //以下为注册按钮的点击事件
        bt_save.setOnClickListener(this);
        bt_cancel.setOnClickListener(this);
        bt_display.setOnClickListener(this);
        bt_stop.setOnClickListener(this);
        bt_start.setOnClickListener(this);
        bt_finish.setOnClickListener(this);

        //以下为数据库操作
        notesDB = new NotesDB(this);
        notesWriter = notesDB.getWritableDatabase();

        initView();//判断flag值,保存图片路径
    }

    /**
     * 该方法用于创建详情活动的头部栏的菜单选项
     *
     * @param menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu, menu);
        return true;
    }

    /**
     * 该方法用于设置头部栏的按钮点击时的操作(包括最左边的按钮"home")
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home://设置左边的home键时进行的操作
//                clickedReturnBtn();
                clickedSaveBtn();
                break;
            case R.id.detail_delete:
//                Toast.makeText(this, "你点击了删除", Toast.LENGTH_SHORT).show();
//                clickedDeleteBtn();
                clickedCancelBtn();
                break;
            default:
        }
        return true;
    }

    /**
     * 该方法用于禁用滑动
     */
    public void disabledScrolling(){
        //使收缩栏不打开
        add_appBar.setExpanded(false);
        //做以下操作使得不能拖动
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) add_appBar.getLayoutParams();
        //在xml中设置layout_behavior使得DetailActivity获取behavior时不会返回null
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                return false;
            }
        });
        NestedScrollView add_nestedScrollView = findViewById(R.id.add_nestedScrollView);
        add_nestedScrollView.setNestedScrollingEnabled(false);
    }

    /**
     * 该方法用于启用滑动
     */
    public void enabledScrolling(){
        //使收缩栏打开
        add_appBar.setExpanded(true);
        //做以下操作使得能拖动
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) add_appBar.getLayoutParams();
        //在xml中设置layout_behavior使得DetailActivity获取behavior时不会返回null
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                return true;
            }
        });
        NestedScrollView add_nestedScrollView = findViewById(R.id.add_nestedScrollView);
        add_nestedScrollView.setNestedScrollingEnabled(true);
    }


    public static String getNowTimeStr() {//该方法用于获取当前时间的字符串
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        Date nowDate = new Date();
        String dateStr = format.format(nowDate);
        return dateStr;
    }


    public String getNowNameStr() {//该方法用于获取命名用的当前时间的字符串
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
        Date nowDate = new Date();
        String dateStr = format.format(nowDate);
        return dateStr;
    }


    public void addItem() {//该方法用于添加一条数据
        ContentValues contentValues = new ContentValues();
        //id不需要放入是因为设置了id自增
        String title_input = add_title.getText().toString().trim();
        String content_input = add_text.getText().toString();
        title_input = title_input.replaceAll("\r|\n", "");//将标题的换行都去掉
        if ("".equals(title_input)) {//如果标题为空
            if (!"".equals(content_input.trim())) {//如果内容不为空，则使用内容的前八个字作为标题
                title_input = content_input.trim().replaceAll("\r|\n", "");//将标题换行都去掉
                if (title_input.length() > 8){//如果内容长度大于8，则截断，否则自己也能当标题
                    title_input = title_input.substring(0, 8) + "……";
                }
            } else {//否则标题和内容都为空
                if (!"".equals(alternativeTitle))//如果代替标题不为空
                    title_input = alternativeTitle;//则可以使用替代标题
            }
        }
        contentValues.put(NotesDB.TITLE, title_input);//添加标题输入框里的内容进数据库
        contentValues.put(NotesDB.CONTENT, content_input);//添加文本输入框里的内容进数据库
        String nowTimeStr = getNowTimeStr();
        contentValues.put(NotesDB.TIME, nowTimeStr);//添加当前的时间
        contentValues.put(NotesDB.CHANGE_TIME, nowTimeStr);//添加修改的时间
        contentValues.put(NotesDB.PIC_PATH, imgFile + "");//添加图片路径  注意！ null + "" = "null" ！！！
        contentValues.put(NotesDB.VIDEO_PATH, videoFile + "");//添加视频路径
        contentValues.put(NotesDB.SOUND_PATH, soundFile + "");//添加录音路径
        contentValues.put(NotesDB.OWNER, owner);//添加当前拥有者名
        contentValues.put(NotesDB.NOTE_STATUS, Notes.NOTE_NEED_UPLOAD);//NOTE_NEED_UPLOAD表示需要服务器上传
        notesWriter.insert(NotesDB.TABLE_NAME, null, contentValues);
    }


    public void initView() {//该方法用于初始化view(如判断flag值,保存图片路径等)
        //flag 1代表文字，2代表图片，3代表视频，4代表录音
        if (flag_str.equals("1")) {
            disabledScrolling();//禁用滑动
            add_img.setVisibility(View.GONE);
            add_video.setVisibility(View.GONE);
//            add_seize1.setVisibility(View.GONE);
            add_linearSound.setVisibility(View.GONE);
        }
        if (flag_str.equals("2")) {
            enabledScrolling();//启用滑动
            alternativeTitle = "照相记录";

            add_img.setVisibility(View.VISIBLE);
            add_video.setVisibility(View.GONE);
            add_linearSound.setVisibility(View.GONE);
            Intent img_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            FileUtil.makeDir(Environment.getExternalStorageDirectory().getAbsoluteFile().getAbsolutePath() +
                    "/MyNotes");
            imgFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile().getAbsolutePath() +
                    "/MyNotes/img" + getNowNameStr() + ".jpg");//新建图片

            PermisionUtils.verifyStoragePermissions(this);//临时动态申请读取权限
            img_intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imgFile));//在img_intent里放入phoneFile
            startActivityForResult(img_intent, PICTURE_REQUEST_CODE);//传递意图
        }
        if (flag_str.equals("3")) {
            enabledScrolling();//启用滑动
            alternativeTitle = "视频记录";

            add_img.setVisibility(View.GONE);
            add_video.setVisibility(View.VISIBLE);
            add_linearSound.setVisibility(View.GONE);
            FileUtil.makeDir(Environment.getExternalStorageDirectory().getAbsoluteFile().getAbsolutePath() +
                    "/MyNotes");
            videoFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile().getAbsolutePath() +
                    "/MyNotes/video" + getNowNameStr() + ".mp4");//新建视频

            PermisionUtils.verifyStoragePermissions(this);//临时动态申请读取权限
            Intent video_intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            video_intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(videoFile));//在img_intent里放入phoneFile
            startActivityForResult(video_intent, VIDEO_REQUEST_CODE);//传递意图

        }
        if (flag_str.equals("4")) {
            disabledScrolling();//禁用滑动
            alternativeTitle = "录音记录";

            add_img.setVisibility(View.GONE);
            add_video.setVisibility(View.GONE);
//            add_seize1.setVisibility(View.GONE);//不需要占位了
            add_linearSound.setVisibility(View.GONE);
            add_linearListen.setVisibility(View.VISIBLE);

            FileUtil.makeDir(Environment.getExternalStorageDirectory().getAbsoluteFile().getAbsolutePath() +
                    "/MyNotes");

            bt_finish.setEnabled(false);
        }
    }

    public void startRecording() {//开始录音方法
        //点击开始按钮后，开始做得录音准备，实例化MediaRecorder、设置所需条件、准备完成、开始录音
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        File path = new File(Environment.getExternalStorageDirectory().getAbsoluteFile().getAbsolutePath() +
                "/MyNotes/");
        path.mkdir();
        try {
            soundFile = File.createTempFile("sound" + getNowNameStr(), ".3gp", path);
            mediaRecorder.setOutputFile(soundFile.getAbsolutePath());
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaRecorder.start();
//        status.setText("Recording...");
    }

    public void stopRecording() {
        //点击停止按钮后，停止录音，并释放录音组件
        mediaRecorder.stop();
        mediaRecorder.release();
        //实例化播放组件，并将录制的文件提交给MediaPlayer
        mediaPlayer = new MediaPlayer();
//        mediaPlayer.setOnCompletionListener(this);
        try {
            mediaPlayer.setDataSource(soundFile.getAbsolutePath());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        status.setText("ready to play");
    }

    public void display() {//播放录音按钮
        //点击开始按钮后，将开始、停止、播放按钮禁止
        mediaPlayer.start();
//        status.setText("playing");
    }

    public void stopDisplay() {//这是停止播放的方法
    }

    /**
     * 这是点击保存按钮后的处理方法
     */
    public void clickedSaveBtn() {
        //开始时判断是否能进行记录
        Intent get_intent = getIntent();

        String title_input = add_title.getText().toString().trim();//如果输入为空，则获取到""字符串
        title_input = title_input.replaceAll("\r|\n", "");//将标题换行都去掉
        String content_input = add_text.getText().toString();//如果输入为空，则获取到""字符串

        if ("4".equals(flag_str)) {//如果是录音记录模式，则需要判断录音是否正常，不正常则禁止记录
            MediaPlayer testMediaPlayer = null;
            try {//尝试调用录音查看是否正常
                testMediaPlayer = new MediaPlayer();
                testMediaPlayer.setDataSource(soundFile.getAbsolutePath());
            } catch (Exception e) {//没有录音则提醒并结束
                e.printStackTrace();
                Toast.makeText(this, "没有录音，记录失败！", Toast.LENGTH_SHORT).show();
            }finally {
                testMediaPlayer.release();//尝试释放资源
            }
            //若上面的测试全都通过，说明录音正常，则可以保存
            addItem();//则可以尝试记录并结束
            finish();
        } else {//否则其它三种记录模式另外单独处理
            if ("".equals(title_input)) {//如果输入的标题为空，如果标题为空，则判断是不是拍照或录像记录
                if (!"1".equals(flag_str)) {//如果flag_str不是1，说明不是记事记录
                    addItem();//则可以尝试记录并结束
                    finish();
                } else {//否则则为记事记录，记事记录标题不能为空！
                    if (!"".equals(content_input.trim())){//如果内容能成为标题
                        addItem();
                        finish();
                    }else //否则则警告
                        Toast.makeText(this, "记事标题不能为空！", Toast.LENGTH_SHORT).show();
                }
            } else {//否则输入标题不为空，可以记录
                addItem();//点击保存之后就会添加数据
                finish();//最后一定会结束
            }
        }
    }


    /**
     * 这是直接退出后的处理方法
     */
    public void ifNeedThenSave() {
        //开始时判断是否能进行记录
        Intent get_intent = getIntent();

        String title_input = add_title.getText().toString().trim();//如果输入为空，则获取到""字符串
        title_input = title_input.replaceAll("\r|\n", "");//将标题换行都去掉
        String content_input = add_text.getText().toString();//如果输入为空，则获取到""字符串

        if ("4".equals(flag_str)) {//如果是录音记录模式，则需要判断录音是否正常，不正常则禁止记录
            MediaPlayer testMediaPlayer = null;
            try {//尝试调用录音查看是否正常
                testMediaPlayer = new MediaPlayer();
                testMediaPlayer.setDataSource(soundFile.getAbsolutePath());
            } catch (Exception e) {//没有录音则提醒并结束
                e.printStackTrace();
                Toast.makeText(this, "未录音，保存失败！", Toast.LENGTH_SHORT).show();
                cleanTempFile();//清理临时文件
            }finally {
                testMediaPlayer.release();//尝试释放资源
            }
            //若上面的测试全都通过，说明录音正常，则可以保存
            addItem();//则可以尝试记录并结束
            finish();
        } else {//否则其它三种记录模式另外单独处理
            if ("".equals(title_input)) {//如果输入的标题为空，如果标题为空，则判断是不是拍照或录像记录
                if (!"1".equals(flag_str)) {//如果flag_str不是1，说明不是记事记录
                    addItem();//则可以尝试记录并结束
                } else {//否则则为记事记录，记事记录标题不能为空！
                    if (!"".equals(content_input.trim())){//如果内容能成为标题
                        addItem();
                    }else //否则则警告
                        cleanTempFile();//清理临时文件
                        Toast.makeText(this, "记事标题不能为空！保存失败！", Toast.LENGTH_LONG).show();
                }
            } else {//否则输入标题不为空，可以记录
                addItem();//点击保存之后就会添加数据
            }
        }
    }

    /**
     * 这是点击处理临时生成的文件的方法
     */
    public void cleanTempFile() {
        if (soundFile != null)
            if (soundFile.exists())
                soundFile.delete();//如果取消时文件存在，则删除
        if (videoFile != null)
            if (videoFile.exists())
                videoFile.delete();
        if (imgFile != null)
            if (imgFile.exists())
                imgFile.delete();
    }

    /**
     * 这是点击取消按钮后的处理方法
     */
    public void clickedCancelBtn() {
        cleanTempFile();//清理临时文件
        finish();//结束
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_save:
//                //开始时判断是否能进行记录
//                Intent get_intent = getIntent();
//
//                String title_input = add_title.getText().toString().trim();//如果输入为空，则获取到""字符串
//                title_input = title_input.replaceAll("\r|\n", "");//将标题换行都去掉
//                String content_input = add_text.getText().toString();//如果输入为空，则获取到""字符串
//
//                if ("4".equals(flag_str)) {//如果是录音记录模式，则需要判断录音是否正常，不正常则禁止记录
//                    MediaPlayer testMediaPlayer = null;
//                    try {//尝试调用录音查看是否正常
//                        testMediaPlayer = new MediaPlayer();
//                        testMediaPlayer.setDataSource(soundFile.getAbsolutePath());
//                    } catch (Exception e) {//没有录音则提醒并结束
//                        e.printStackTrace();
//                        Toast.makeText(this, "没有录音，记录失败！", Toast.LENGTH_SHORT).show();
//                    }finally {
//                        testMediaPlayer.release();//尝试释放资源
//                    }
//                    //若上面的测试全都通过，说明录音正常，则可以保存
//                    addItem();//则可以尝试记录并结束
//                    finish();
//                } else {//否则其它三种记录模式另外单独处理
//                    if ("".equals(title_input)) {//如果输入的标题为空，如果标题为空，则判断是不是拍照或录像记录
//                        if (!"1".equals(flag_str)) {//如果flag_str不是1，说明不是记事记录
//                            addItem();//则可以尝试记录并结束
//                            finish();
//                        } else {//否则则为记事记录，记事记录标题不能为空！
//                            if (!"".equals(content_input.trim())){//如果内容能成为标题
//                                addItem();
//                                finish();
//                            }else //否则则警告
//                                Toast.makeText(this, "记事标题不能为空！", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {//否则输入标题不为空，可以记录
//                        addItem();//点击保存之后就会添加数据
//                        finish();//最后一定会结束
//                    }
//                }
                clickedSaveBtn();
                break;

            case R.id.bt_cancel:
//                if (soundFile != null)
//                    if (soundFile.exists())
//                        soundFile.delete();//如果取消时文件存在，则删除
//                if (videoFile != null)
//                    if (videoFile.exists())
//                        videoFile.delete();
//                if (imgFile != null)
//                    if (imgFile.exists())
//                        imgFile.delete();
//                finish();//结束
                clickedCancelBtn();
                break;

            case R.id.bt_start://开始录音
                startRecording();//开始录音
                bt_display.setEnabled(false);
                bt_finish.setEnabled(true);//完成录音 能按了
                bt_start.setEnabled(false);
                Toast.makeText(this, "开始录音", Toast.LENGTH_SHORT).show();
                break;

            case R.id.bt_finish://完成录音
                add_linearListen.setVisibility(View.GONE);//完成录音后关闭录音栏
                add_linearSound.setVisibility(View.VISIBLE);//并打开播放栏
                stopRecording();//停止录音
                bt_display.setEnabled(true);
                bt_finish.setEnabled(false);
                bt_start.setEnabled(true);
                bt_stop.setEnabled(false);
                Toast.makeText(this, "停止录音", Toast.LENGTH_SHORT).show();
                break;

            case R.id.bt_display://播放录音
                display();//播放录音
                bt_display.setEnabled(false);
                bt_finish.setEnabled(false);
                bt_start.setEnabled(false);
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

    /**
     * 是活动跳转方法startActivityForResult()完成后的回调方法。
     *
     * @param requestCode 是startActivityForResult(intent, request_code)时传递的request_code
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case TEXT_REQUEST_CODE:
                break;

            case PICTURE_REQUEST_CODE:
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());//用bitmap工厂解码该图片
                try {
                    bitmap.getWidth();//用getWidth来判断该图片是否正常
                } catch (Exception e) {
                    Toast.makeText(this, "已取消拍照", Toast.LENGTH_SHORT).show();
                    finish();//不正常则直接结束活动
                }
                add_img.setImageBitmap(bitmap);//将获取到的bitmap设置到add_img中
                break;

            case VIDEO_REQUEST_CODE:
                try {
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(videoFile.getAbsolutePath());
                    String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);// 播放时长单位为毫秒
                    Toast.makeText(this, "该视频播放时间为：\n" + duration + "毫秒", Toast.LENGTH_SHORT);
                } catch (IllegalArgumentException e) {//有异常则提示并直接结束
                    Toast.makeText(this, "已取消录像", Toast.LENGTH_SHORT).show();
                    finish();
                }

                add_video.setVideoPath(videoFile.getAbsolutePath());//设置video的视频路径
                MediaController mediaController = new MediaController(this);//创建MediaController对象
                add_video.setMediaController(mediaController);//VideoView与MediaController建立关联
                add_video.requestFocus();//让VideoView获取焦点
                add_video.start();//开始播放视频
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ifNeedThenSave();//判断退出后要进行的操作
        notesWriter.close();
        notesDB.close();
        try{
            mediaPlayer.release();//尝试释放资源
//            Thread.sleep(200);
        }catch (Exception e){
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ContentFragment.getContentFragmentInstance().refreshNotesList();
            }
        });

    }
}
