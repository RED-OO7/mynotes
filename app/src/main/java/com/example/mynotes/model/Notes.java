package com.example.mynotes.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.example.mynotes.dao.NotesDB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Notes implements Serializable {//该Notes是面向本地sqlite数据库的

    public static final String CLASSNAME = "Notes";

//    public static final int UPDATE_SUCCESS = 101;//给update_status用，用于表示该记录更新成功
//    public static final int UPDATE_FAILED = 102;//给update_status用，用于表示该记录更新失败
//    public static final int NEED_DOWNLOAD = 103;//给update_status用，用于表示该记录需要被客户端下载

    public static final int NOTE_UPDATE_SUCCESS = 101;//给note_status用，用于表示该记录更新成功
    public static final int NOTE_UPDATE_FAILED = 102;//给note_status用，用于表示该记录更新失败
    public static final int NOTE_NEED_DOWNLOAD= 103;//给note_status用，用于表示该记录需要被客户端下载

    public static final int NOTE_NEED_UPLOAD = 104;//给note_status用，需要上传到服务器的标识
    public static final int NOTE_UPDATED = 105;//给note_status用，已经被上传的标识(不再需要更新了)
    public static final int NOTE_NEED_DELETE = 106;//给note_status用，需要服务器删除这条记录的标识
    public static final int NOTE_DELETE_SUCCESS = 107;//给note_status用，表示服务器已成功删除这条记录

    private int id;//记录者id
    private String title;//标题
    private String content;//内容
    private String pic_path;//照片路径
    private String video_path;//录像路径
    private String sound_path;//录音路径
    private String time;//记录创建时间
    private String change_time;//记录更新的时间
    //    private boolean is_change;//是否更新过的标识
    private int note_status;//记录的状态
    private String owner;//拥有者

    public Notes() {
    }

    public Notes(String title, String content, String pic_path, String video_path, String sound_path, String time, String change_time, int note_status, String owner) {
        this.title = title;
        this.content = content;
        this.pic_path = pic_path;
        this.video_path = video_path;
        this.sound_path = sound_path;
        this.time = time;
        this.change_time = change_time;
        this.note_status = note_status;
        this.owner = owner;
    }

    public Notes(int id, String title, String content, String pic_path, String video_path, String sound_path, String time, String change_time, int note_status, String owner) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.pic_path = pic_path;
        this.video_path = video_path;
        this.sound_path = sound_path;
        this.time = time;
        this.change_time = change_time;
        this.note_status = note_status;
        this.owner = owner;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPic_path() {
        return pic_path;
    }

    public void setPic_path(String pic_path) {
        this.pic_path = pic_path;
    }

    public String getVideo_path() {
        return video_path;
    }

    public void setVideo_path(String video_path) {
        this.video_path = video_path;
    }

    public String getSound_path() {
        return sound_path;
    }

    public void setSound_path(String sound_path) {
        this.sound_path = sound_path;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getChange_time() {
        return change_time;
    }

    public void setChange_time(String change_time) {
        this.change_time = change_time;
    }

    public int getNote_status() {
        return note_status;
    }

    public void setNote_status(int note_status) {
        this.note_status = note_status;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * 该重写的toString方法已可将notes转换为符合该项目规定的Json格式的字符串
     */
    @Override
    public String toString() {
        JSONObject dataJsonPackObject = new JSONObject();

        try {
            dataJsonPackObject.put("CLASSNAME", CLASSNAME);
            dataJsonPackObject.put("note_status", note_status);// 记事记录的状态
            dataJsonPackObject.put("owner", owner);// 拥有者
            dataJsonPackObject.put("time", time);// 记录创建时间
            dataJsonPackObject.put("change_time", change_time);// 记录更新的时间

//            if (is_change) {
            dataJsonPackObject.put("title", title);//
            dataJsonPackObject.put("content", content);//
            dataJsonPackObject.put("pic_path", pic_path);// 照片路径
            dataJsonPackObject.put("video_path", video_path);// 录像路径
            dataJsonPackObject.put("sound_path", sound_path);// 录音路径
//            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dataJsonPackObject.toString();
    }

    /**
     * 该方法返回的notes列表里的note 在isChange标识为0的情况下，<br/>
     * 将不会加入除创建时间和更新时间之外的额外内容
     */
    @NonNull
    public static List<Notes> getUserTextNotesList(Context context, String nowUsername) {
        List<Notes> notesList = new ArrayList<Notes>();//将要返回的笔记本列表

        NotesDB notesDB = new NotesDB(context);
        String selectionArgs[] = {NotesDB.LOCAL_OWNER_STRING, nowUsername, "null", "null", "null"};
        SQLiteDatabase dbReader = notesDB.getReadableDatabase();//获取可读取数据库
        //该cursor游标设置为使用NotesDB.OWNER限定搜索结果，再使用NotesDB.CHANGE_TIME排序
        Cursor cursor = dbReader.query(NotesDB.TABLE_NAME, null, " ( " + NotesDB.OWNER + " = ? or " + NotesDB.OWNER + " = ? ) and " +
                NotesDB.PIC_PATH + " = ? and " +
                NotesDB.VIDEO_PATH + " = ? and " +
                NotesDB.SOUND_PATH + " = ? ", selectionArgs, null, null, NotesDB.CHANGE_TIME + " Desc");

        while (cursor.moveToNext()) {//遍历游标里的所有数据
            String titleStr = cursor.getString(cursor.getColumnIndex(NotesDB.TITLE));
            String contentStr = cursor.getString(cursor.getColumnIndex(NotesDB.CONTENT));
            String pic_pathStr = cursor.getString(cursor.getColumnIndex(NotesDB.PIC_PATH));
            String video_pathStr = cursor.getString(cursor.getColumnIndex(NotesDB.VIDEO_PATH));
            String sound_pathStr = cursor.getString(cursor.getColumnIndex(NotesDB.SOUND_PATH));
            String timeStr = cursor.getString(cursor.getColumnIndex(NotesDB.TIME));
            String changeTimeStr = cursor.getString(cursor.getColumnIndex(NotesDB.CHANGE_TIME));
//            int isChange_int = cursor.getInt(cursor.getColumnIndex(NotesDB.IS_CHANGE));// 0 为 false ， 1为true
//            boolean isChange = (isChange_int == 1);
            int note_status = cursor.getInt(cursor.getColumnIndex(NotesDB.NOTE_STATUS));
            String ownerStr = cursor.getString(cursor.getColumnIndex(NotesDB.OWNER));

            Notes note = new Notes();
            note.setTime(timeStr);
            note.setChange_time(changeTimeStr);
            note.setNote_status(note_status);
            note.setOwner(ownerStr);

            if (note_status == Notes.NOTE_NEED_UPLOAD) {//如果该记录需要被上传的话
                note.setTitle(titleStr);
                note.setContent(contentStr);
                note.setPic_path(pic_pathStr);
                note.setVideo_path(video_pathStr);
                note.setSound_path(sound_pathStr);
            }

            notesList.add(note);
        }

        //最后关闭资源
        cursor.close();
        notesDB.close();

        return notesList;
    }


    /**
     * 该方法返回的notes列表里的note适合显示在列表的或显示在详情里
     */
    @NonNull
    public static List<Notes> getNotesListContent(Context context, String nowUsername) {
        List<Notes> notesList = new ArrayList<Notes>();//将要返回的笔记本列表

        NotesDB notesDB = new NotesDB(context);
        String selectionArgs[] = {NotesDB.LOCAL_OWNER_STRING, nowUsername};
        SQLiteDatabase dbReader = notesDB.getReadableDatabase();//获取可读取数据库
        //该cursor游标设置为使用NotesDB.OWNER限定搜索结果，再使用NotesDB.CHANGE_TIME排序
        Cursor cursor = dbReader.query(NotesDB.TABLE_NAME, null,
                "( " +NotesDB.OWNER + " = ? or " + NotesDB.OWNER + " = ? )" +
                        " and " + NotesDB.NOTE_STATUS + " != " + Notes.NOTE_NEED_DELETE ,
                selectionArgs, null, null, NotesDB.CHANGE_TIME + " Desc");

        while (cursor.moveToNext()) {//遍历游标里的所有数据
            int id = cursor.getInt(cursor.getColumnIndex(NotesDB.ID));
            String titleStr = cursor.getString(cursor.getColumnIndex(NotesDB.TITLE));
            String contentStr = cursor.getString(cursor.getColumnIndex(NotesDB.CONTENT));
            String pic_pathStr = cursor.getString(cursor.getColumnIndex(NotesDB.PIC_PATH));
            String video_pathStr = cursor.getString(cursor.getColumnIndex(NotesDB.VIDEO_PATH));
            String sound_pathStr = cursor.getString(cursor.getColumnIndex(NotesDB.SOUND_PATH));
            String timeStr = cursor.getString(cursor.getColumnIndex(NotesDB.TIME));
            String changeTimeStr = cursor.getString(cursor.getColumnIndex(NotesDB.CHANGE_TIME));
//            int isChange_int = cursor.getInt(cursor.getColumnIndex(NotesDB.IS_CHANGE));// 0 为 false ， 1为true
//            boolean isChange = (isChange_int == 1);
            int noteStatus_int =cursor.getInt(cursor.getColumnIndex(NotesDB.NOTE_STATUS));
            String ownerStr = cursor.getString(cursor.getColumnIndex(NotesDB.OWNER));

            Notes note = new Notes();

            note.setId(id);
            note.setTime(timeStr);
            note.setChange_time(changeTimeStr);
            note.setNote_status(noteStatus_int);

            note.setTitle(titleStr);
            note.setContent(contentStr);
            note.setPic_path(pic_pathStr);
            note.setVideo_path(video_pathStr);
            note.setSound_path(sound_pathStr);
            note.setOwner(ownerStr);

            notesList.add(note);
        }
        //最后关闭资源
        cursor.close();
        notesDB.close();

        return notesList;
    }


    /**
     * 该方法可以更新本客户端的文本记录<br/>
     *
     * @param jsonArray 是携带notes信息的JSONArray对象
     */
    @NonNull
    public static int updateUserTextNotes(Context context, JSONArray jsonArray, String nowUsername) {
        int iUpdateSuccess = 0;

        NotesDB notesDB = new NotesDB(context);
        Cursor cursor = null;//该游标用于查询该创建时间和拥有者的记录是否存在

        SQLiteDatabase dbWriter = notesDB.getWritableDatabase();//获取可写入数据库
        SQLiteDatabase dbReader = notesDB.getWritableDatabase();//获取可读取数据库

        for (int i = 0; i < jsonArray.length(); i++) {//遍历jsonArray里的对象
            try {
                JSONObject noteObject = jsonArray.getJSONObject(i);//获取noteObject
                int status_code = noteObject.getInt("note_status");//获取该记事记录的状态码

                String time = null;
                String owner = null;
                String whereArgs[] = null;//用于限制更新成功后修改is_change的参数
                String selectionArgs[] = null;//用于限制查询下载记录是否存在的参数

                switch (status_code) {
                    case Notes.NOTE_UPDATE_SUCCESS://这是更新成功的情况
                        ContentValues updateSuccessCV = new ContentValues();

                        updateSuccessCV.put(NotesDB.NOTE_STATUS, Notes.NOTE_UPDATED);//NOTE_UPDATED表示该记录已经上传到服务器了

                        time = noteObject.getString(NotesDB.TIME);//获取创建时间，用作识别标识
                        owner = noteObject.getString(NotesDB.OWNER);//获取该记录用户，用作识别标识
                        whereArgs = new String[]{time, owner};

                        dbWriter.update(NotesDB.TABLE_NAME, updateSuccessCV, NotesDB.TIME + " =? and " + NotesDB.OWNER + " =? ", whereArgs);
                        updateSuccessCV.clear();
                        iUpdateSuccess++;
                        break;
                    case Notes.NOTE_UPDATE_FAILED://这是更新失败的情况
                        break;
                    case Notes.NOTE_NEED_DOWNLOAD://这是服务器回传的需更新的记录的情况
                        ContentValues downloadCV = new ContentValues();

                        downloadCV.put(NotesDB.TITLE, noteObject.getString(NotesDB.TITLE));//TITLE
                        downloadCV.put(NotesDB.CONTENT, noteObject.getString(NotesDB.CONTENT));//CONTENT
                        downloadCV.put(NotesDB.PIC_PATH, noteObject.getString(NotesDB.PIC_PATH));//PIC_PATH
                        downloadCV.put(NotesDB.VIDEO_PATH, noteObject.getString(NotesDB.VIDEO_PATH));//VIDEO_PATH
                        downloadCV.put(NotesDB.SOUND_PATH, noteObject.getString(NotesDB.SOUND_PATH));//SOUND_PATH
                        downloadCV.put(NotesDB.OWNER, noteObject.getString(NotesDB.OWNER));//OWNER
                        downloadCV.put(NotesDB.TIME, noteObject.getString(NotesDB.TIME));//TIME
                        downloadCV.put(NotesDB.CHANGE_TIME, noteObject.getString(NotesDB.CHANGE_TIME));//CHANGE_TIME
                        downloadCV.put(NotesDB.NOTE_STATUS, NOTE_UPDATED);//NOTE_UPDATED表示更新成功

                        time = noteObject.getString(NotesDB.TIME);//获取创建时间，用作识别标识
                        owner = noteObject.getString(NotesDB.OWNER);//获取该记录用户，用作识别标识

                        selectionArgs = new String[]{time, owner};
                        cursor = dbReader.query(NotesDB.TABLE_NAME,
                                null,
                                NotesDB.TIME + " = ? AND " + NotesDB.OWNER + " = ?",
                                selectionArgs,
                                null,
                                null,
                                null);

                        if (cursor.moveToNext()) {//如果移动成功，则说明该记录存在，则需要更新该记录
                            int updateId = cursor.getInt(cursor.getColumnIndex(NotesDB.ID));//获取到id
                            dbWriter.update(NotesDB.TABLE_NAME, downloadCV, NotesDB.ID + " = " + updateId, null);
                        } else {//否则该记录不存在本客户端，则需要插入
//                            Toast.makeText(context,"似乎有执行插入:"+noteObject.getString(NotesDB.TIME),Toast.LENGTH_LONG).show();
                            dbWriter.insert(NotesDB.TABLE_NAME, null, downloadCV);
                        }
                        downloadCV.clear();//在下次更新前必须先清理旧的值
                        iUpdateSuccess++;
                        break;
                    case Notes.NOTE_DELETE_SUCCESS:
                        time = noteObject.getString(NotesDB.TIME);//获取创建时间，用作识别标识
                        owner = noteObject.getString(NotesDB.OWNER);//获取该记录用户，用作识别标识

                        selectionArgs = new String[]{time, owner};

                        dbWriter.delete(NotesDB.TABLE_NAME,
                                NotesDB.TIME + " = ? AND " + NotesDB.OWNER + " = ?",
                                selectionArgs);//根据条件删除该行数据
                        iUpdateSuccess++;
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        dbReader.close();
        dbWriter.close();//关闭数据库节约资源

        return iUpdateSuccess;
    }


//    public static List<Notes> getUserTextNotesList() {
//        List<Notes> notesList = new ArrayList<Notes>();//将要返回的笔记本列表
//
//        int i = 0;
//        while (i < 5) {//遍历游标里的所有数据
//            String titleStr = "测试标题";
//            String contentStr = "测试内容";
//            String pic_pathStr = "null";
//            String video_pathStr = "null";
//            String sound_pathStr = "null";
//            String timeStr = "测试时间";
//            String changeTimeStr = "修改时间";
////            int isChange_int = 1;// 0 为 false ， 1为true
////            boolean isChange = (isChange_int == 1);
//            String ownerStr = NotesDB.LOCAL_OWNER_STRING;
//
//            Notes note = new Notes();
//            note.setTime(timeStr);
//            note.setChange_time(changeTimeStr);
//            note.setIs_change(isChange);
//
//            if (isChange) {
//                note.setTitle(titleStr);
//                note.setContent(contentStr);
//                note.setPic_path(pic_pathStr);
//                note.setVideo_path(video_pathStr);
//                note.setSound_path(sound_pathStr);
//                note.setOwner(ownerStr);
//            }
//
//            notesList.add(note);
//
//            i++;
//        }
//
//        return notesList;
//    }

}
