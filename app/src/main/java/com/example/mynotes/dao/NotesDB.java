package com.example.mynotes.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.mynotes.model.Notes;

public class NotesDB extends SQLiteOpenHelper {

    // 1 是最初始的版本，此版本只有 id，记录，创建时间，图片路径，视频路径，录音路径(6列)
    // 1002 是旧版本，此版本只有 id，记录，创建时间，修改时间，图片路径，视频路径，录音路径，拥有者，是否修改标识(9列)
    // 1003 是旧版本，此版本有 id，标题，记录，创建时间，修改时间，图片路径，视频路径，录音路径，拥有者，是否修改标识(10列)
    // 1004 是目前最新版本，此版本有 id，标题，记录，创建时间，修改时间，图片路径，视频路径，录音路径，拥有者，记录的修改状态(10列)
    private static final int version = 1004;

    public static final String LOCAL_OWNER_STRING = "Local Owner";//本地记事用户(未登录用户)的名字

    public static final String TABLE_NAME = "notes";
    public static final String TABLE_TEMP_OLD = "table_temp_old";

    public static final String TITLE = "title";//标题
    public static final String CONTENT = "content";//内容
    public static final String PIC_PATH = "pic_path";//照片路径
    public static final String VIDEO_PATH = "video_path";//录像路径
    public static final String SOUND_PATH = "sound_path";//录音路径
    public static final String ID = "_id";
    public static final String TIME = "time";
    public static final String CHANGE_TIME = "change_time";//更新的笔记的时间
    public static final String IS_CHANGE = "is_change";//是否更新过的标识
    public static final String NOTE_STATUS = "note_status";//记录的修改状态
    public static final String OWNER = "owner";

    public static String Create_table_str_v1 = "CREATE TABLE " + TABLE_NAME + "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CONTENT + " TEXT , " +
            PIC_PATH + " TEXT , " +
            VIDEO_PATH + " TEXT , " +
            SOUND_PATH + " TEXT , " +
            TIME + " TEXT NOT NULL )";

    public static String Create_table_str_v1002 = "CREATE TABLE " + TABLE_NAME + "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CONTENT + " TEXT , " +
            PIC_PATH + " TEXT , " +
            VIDEO_PATH + " TEXT , " +
            SOUND_PATH + " TEXT , " +
            OWNER + " TEXT , " +
            TIME + " TEXT NOT NULL," +
            CHANGE_TIME + " TEXT NOT NULL," +
            IS_CHANGE + " BOOLEAN NOT NULL DEFAULT 1 )";

    public static String Create_table_str_v1003 = "CREATE TABLE " + TABLE_NAME + "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TITLE + " TEXT NOT NULL, " +
            CONTENT + " TEXT , " +
            PIC_PATH + " TEXT , " +
            VIDEO_PATH + " TEXT , " +
            SOUND_PATH + " TEXT , " +
            OWNER + " TEXT , " +
            TIME + " TEXT NOT NULL," +
            CHANGE_TIME + " TEXT NOT NULL," +
            IS_CHANGE + " BOOLEAN NOT NULL DEFAULT 1 )";

    public static String Create_table_str_v1004 = "CREATE TABLE " + TABLE_NAME + "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TITLE + " TEXT NOT NULL, " +
            CONTENT + " TEXT , " +
            PIC_PATH + " TEXT , " +
            VIDEO_PATH + " TEXT , " +
            SOUND_PATH + " TEXT , " +
            OWNER + " TEXT , " +
            TIME + " TEXT NOT NULL," +
            CHANGE_TIME + " TEXT NOT NULL," +
            NOTE_STATUS + " INTEGER NOT NULL DEFAULT "+ Notes.NOTE_NEED_DELETE+" )";

    public static String Create_table_str = Create_table_str_v1004;//创建表的记录必须是最新的版本的

    public Context context;

    public NotesDB(Context context) {
        super(context, TABLE_NAME, null, version);
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {//该方法用于创建我们记事本要用到的数据库
        db.execSQL(Create_table_str);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Cursor cursor = null;//声明游标命名空间

        switch (oldVersion) {
            case 1://是最初始的版本，此版本只有 id，记录，创建时间，图片路径，视频路径，录音路径(6列)

                //修改表名为旧表
                db.execSQL("ALTER TABLE " + TABLE_NAME + " RENAME TO " + TABLE_TEMP_OLD);

                //创建新的v1002的数据表，此版本只有 id，记录，创建时间，修改时间，图片路径，视频路径，录音路径，拥有者，是否修改标识(8列)
                db.execSQL(Create_table_str_v1002);

                //从旧数据表中把数据存到新表中
                cursor = db.query(TABLE_TEMP_OLD, null, null, null, null, null, null);
                while (cursor.moveToNext()) {//遍历所有数据，并初始化数据库内容
                    String contentStr = cursor.getString(cursor.getColumnIndex(NotesDB.CONTENT));
                    String picPathStr = cursor.getString(cursor.getColumnIndex(NotesDB.PIC_PATH));
                    String videoPathStr = cursor.getString(cursor.getColumnIndex(NotesDB.VIDEO_PATH));
                    String soundPathStr = cursor.getString(cursor.getColumnIndex(NotesDB.SOUND_PATH));
                    String timeStr = cursor.getString(cursor.getColumnIndex(NotesDB.TIME));

                    ContentValues contentValues = new ContentValues();
                    //id不需要放入是因为设置了id自增
                    contentValues.put(NotesDB.CONTENT, contentStr);//添加文本输入框里的内容进数据库
                    contentValues.put(NotesDB.TIME, timeStr);//添加当前的时间
                    contentValues.put(NotesDB.CHANGE_TIME, timeStr);//添加修改的时间
                    contentValues.put(NotesDB.PIC_PATH, picPathStr + "");//添加图片路径  我怀疑这个路径有问题
                    contentValues.put(NotesDB.VIDEO_PATH, videoPathStr + "");//添加视频路径
                    contentValues.put(NotesDB.SOUND_PATH, soundPathStr + "");//添加录音路径
                    contentValues.put(NotesDB.OWNER, NotesDB.LOCAL_OWNER_STRING);//添加当前拥有者名（当前旧数据库为0001版本）
                    contentValues.put(NotesDB.IS_CHANGE, 1);//添加是否修改的标识

                    db.insert(NotesDB.TABLE_NAME, null, contentValues);//插入单条数据
                }

                //删除旧临时表
                db.execSQL("DROP TABLE " + TABLE_TEMP_OLD);
                //升级完后是1002版本
                Log.i("onUpgrade", "摸鱼记事本在没有卸载的情况下，本地数据库更新到了版本1002,同时列表增加了owner，change_time和is_change");
//                break;//break是完全不能要的

            case 1002://次旧版本，此版本只有 id，记录，创建时间，修改时间，图片路径，视频路径，录音路径，拥有者，是否修改标识(9列)

                //修改表名为旧表
                db.execSQL("ALTER TABLE " + TABLE_NAME + " RENAME TO " + TABLE_TEMP_OLD);

                //创建新的数据表
                db.execSQL(Create_table_str_v1003);

                //从旧数据表中把数据存到新表中
                cursor = db.query(TABLE_TEMP_OLD, null, null, null, null, null, null);
                while (cursor.moveToNext()) {//遍历所有数据，并初始化数据库内容
                    String contentStr = cursor.getString(cursor.getColumnIndex(NotesDB.CONTENT));
                    String picPathStr = cursor.getString(cursor.getColumnIndex(NotesDB.PIC_PATH));
                    String videoPathStr = cursor.getString(cursor.getColumnIndex(NotesDB.VIDEO_PATH));
                    String soundPathStr = cursor.getString(cursor.getColumnIndex(NotesDB.SOUND_PATH));
                    String ownerStr = cursor.getString(cursor.getColumnIndex(NotesDB.OWNER));
                    String timeStr = cursor.getString(cursor.getColumnIndex(NotesDB.TIME));
                    int isChangeInt = cursor.getInt(cursor.getColumnIndex(NotesDB.IS_CHANGE));

                    ContentValues contentValues = new ContentValues();
                    //id不需要放入是因为设置了id自增
                    String titleStr = contentStr.replaceAll("\r|\n", "");//将换行都去掉
                    if (titleStr.length() < 8)//内容长度如果小于8的话
                        titleStr = titleStr;//则内容可以直接当标题
                    else
                        titleStr = titleStr.substring(0, 8) + "……";//否则内容需截断后才能当标题

                    contentValues.put(NotesDB.TITLE, titleStr);//substring(start,end)中start为闭合，end为开合
                    contentValues.put(NotesDB.CONTENT, contentStr);//添加文本输入框里的内容进数据库
                    contentValues.put(NotesDB.TIME, timeStr);//添加当前的时间
                    contentValues.put(NotesDB.CHANGE_TIME, timeStr);//添加修改的时间
                    contentValues.put(NotesDB.PIC_PATH, picPathStr + "");//添加图片路径  我怀疑这个路径有问题
                    contentValues.put(NotesDB.VIDEO_PATH, videoPathStr + "");//添加视频路径
                    contentValues.put(NotesDB.SOUND_PATH, soundPathStr + "");//添加录音路径
                    contentValues.put(NotesDB.OWNER, ownerStr);//添加当前拥有者名（本数据库为1002版本）
                    contentValues.put(NotesDB.IS_CHANGE, isChangeInt);//添加是否修改的标识

                    db.insert(NotesDB.TABLE_NAME, null, contentValues);//插入单条数据
                }

                //删除旧临时表
                db.execSQL("DROP TABLE " + TABLE_TEMP_OLD);
                //升级完后是1003版本
                Log.i("onUpgrade", "摸鱼记事本在没有卸载的情况下，本地数据库更新到了版本1003,同时列表增加了title");
//                break;//break是完全不能要的
            case 1003://次旧版本，此版本有 id，标题，记录，创建时间，修改时间，图片路径，视频路径，录音路径，拥有者，是否修改标识(10列)

                //修改表名为旧表
                db.execSQL("ALTER TABLE " + TABLE_NAME + " RENAME TO " + TABLE_TEMP_OLD);

                //创建新的数据表
                db.execSQL(Create_table_str_v1004);

                //从旧数据表中把数据存到新表中
                cursor = db.query(TABLE_TEMP_OLD, null, null, null, null, null, null);
                while (cursor.moveToNext()) {//遍历所有数据，并初始化数据库内容
                    String titleStr = cursor.getString(cursor.getColumnIndex(NotesDB.TITLE));
                    String contentStr = cursor.getString(cursor.getColumnIndex(NotesDB.CONTENT));
                    String picPathStr = cursor.getString(cursor.getColumnIndex(NotesDB.PIC_PATH));
                    String videoPathStr = cursor.getString(cursor.getColumnIndex(NotesDB.VIDEO_PATH));
                    String soundPathStr = cursor.getString(cursor.getColumnIndex(NotesDB.SOUND_PATH));
                    String ownerStr = cursor.getString(cursor.getColumnIndex(NotesDB.OWNER));
                    String timeStr = cursor.getString(cursor.getColumnIndex(NotesDB.TIME));
                    String changeTimeStr = cursor.getString(cursor.getColumnIndex(NotesDB.CHANGE_TIME));
                    int isChangeInt = cursor.getInt(cursor.getColumnIndex(NotesDB.IS_CHANGE));//0是false,1是true

                    ContentValues contentValues = new ContentValues();
                    //id不需要放入是因为设置了id自增
                    contentValues.put(NotesDB.TITLE, titleStr);//substring(start,end)中start为闭合，end为开合
                    contentValues.put(NotesDB.CONTENT, contentStr);//添加文本输入框里的内容进数据库
                    contentValues.put(NotesDB.TIME, timeStr);//添加当前的时间
                    contentValues.put(NotesDB.CHANGE_TIME, changeTimeStr);//添加修改的时间
                    contentValues.put(NotesDB.PIC_PATH, picPathStr);//添加图片路径  我怀疑这个路径有问题
                    contentValues.put(NotesDB.VIDEO_PATH, videoPathStr);//添加视频路径
                    contentValues.put(NotesDB.SOUND_PATH, soundPathStr);//添加录音路径
                    contentValues.put(NotesDB.OWNER, ownerStr);//添加当前拥有者名（本数据库为1002版本）

                    int note_status;
                    if(isChangeInt == 1){//如果是已修改的话
                        note_status = Notes.NOTE_NEED_UPLOAD;
                    }else {
                        note_status = Notes.NOTE_UPDATED;
                    }

                    contentValues.put(NotesDB.NOTE_STATUS, note_status);//添加是否修改的标识

                    db.insert(NotesDB.TABLE_NAME, null, contentValues);//插入单条数据
                }

                //删除旧临时表
                db.execSQL("DROP TABLE " + TABLE_TEMP_OLD);
                //升级完后是1004版本
                Log.i("onUpgrade", "摸鱼记事本在没有卸载的情况下，本地数据库更新到了版本1004,同时表的is_change标识换成了note_status状态");
//                break;//break是完全不能要的

            default:
                break;
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        super.onDowngrade(db, oldVersion, newVersion);//旧方法会直接抛出异常
        //我什么都不做，因为当前我不需要关心数据库降级后会发生什么
    }

}
