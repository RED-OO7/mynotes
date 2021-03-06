package com.example.mynotes.view.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mynotes.MainActivity;
import com.example.mynotes.R;
import com.example.mynotes.view.adapter.ShowListContentApdater;
import com.example.mynotes.dao.NotesDB;
import com.example.mynotes.model.Notes;
import com.example.mynotes.view.activities.DetailActivity;

import java.util.List;

public class SearchFragment extends Fragment implements View.OnClickListener{

    private Button bt_search;//该按钮用于搜索后执行
    private EditText et_search;//搜索输入框

    private ListView cellListView;//用于显示记录的listView

    private NotesDB notesDB;
    private SQLiteDatabase dbReader;
    private Cursor cursor;//该游标用于读取数据库

//    private ShowListContentApdater showListContentApdater;
    private ShowListContentApdater showListContentAdapter;//该适配器用于显示每条记录

    private static SearchFragment searchFragmentInstance = null;
    private static List<Notes> notesList;//这是存储记事信息内容时用的队列

    private SharedPreferences preferences;//保存搜索关键字用的
    private SharedPreferences.Editor editor;

    public static SearchFragment getSearchFragmentInstance() {
        return searchFragmentInstance;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment,container,false);
        //这个view这里使得碎片绑定了content_fragment这个layout

        et_search = (EditText) view.findViewById(R.id.et_search);
        bt_search = (Button)  view.findViewById(R.id.bt_search);
        bt_search.setOnClickListener(this);

        cellListView = (ListView) view.findViewById(R.id.cellListView);

        notesList = Notes.getNotesListContent(getContext(), MainActivity.getNowUsername());//用游标获取记事记录

        showListContentAdapter = new ShowListContentApdater(getContext(), R.id.cellListView, notesList);//创建适配器
        cellListView.setAdapter(showListContentAdapter);//设置适配器

        initView();

//        preferences = getActivity().getSharedPreferences("AccountPreference", Context.MODE_PRIVATE);
//        editor = preferences.edit();
//        String search_get = preferences.getString("search_str", "");
//        if (search_get!=null&&!"".equals(search_get)){//不为空时执行搜素
//            et_search.setText(search_get);//将搜索文字设置到搜索框
//            refreshNotesList(search_get);//根据条件搜索记录
//        }else {//为空时搜索全部
//            refreshNotesList();//获取全部记录的记事信息
//        }
        refreshNotesList();

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchFragmentInstance = this;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void initView(){//该方法用于初始化cellListView

//        notesDB=new NotesDB(getContext());
//        dbReader=notesDB.getReadableDatabase();//获取可读数据库
//
//        cellListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//这部分是点到每一行记录时的操作
//                //先初始化了cursor
//                cursor=dbReader.query(NotesDB.TABLE_NAME,null,null,null,null,null,null);
//                cursor.moveToPosition(position);
//                Intent intent = new Intent(getContext(), DetailActivity.class);//跳转意图，用以从main跳转到detail
//                intent.putExtra(NotesDB.ID,cursor.getInt(cursor.getColumnIndex(NotesDB.ID)));//意图里装载使用游标查找到的ID
//                intent.putExtra(NotesDB.CONTENT,cursor.getString(cursor.getColumnIndex(NotesDB.CONTENT)));//意图里装载使用游标查找到的文本
//                intent.putExtra(NotesDB.TIME,cursor.getString(cursor.getColumnIndex(NotesDB.TIME)));//意图里装载使用游标查找到的时间
//                intent.putExtra(NotesDB.PIC_PATH,cursor.getString(cursor.getColumnIndex(NotesDB.PIC_PATH)));//意图里装载使用游标查找到的图片
//                intent.putExtra(NotesDB.VIDEO_PATH,cursor.getString(cursor.getColumnIndex(NotesDB.VIDEO_PATH)));//意图里装载使用游标查找到的视频
//                intent.putExtra(NotesDB.SOUND_PATH,cursor.getString(cursor.getColumnIndex(NotesDB.SOUND_PATH)));//意图里装载使用游标查找到的录音
//                startActivity(intent);//跳转到详情页面开始
//            }
//        });
        cellListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//这部分是点到每一行记录时的操作
                Intent intent = new Intent(getContext(), DetailActivity.class);//跳转意图，用以从main跳转到detail
                Notes nowNote = notesList.get(position);
                intent.putExtra(Notes.CLASSNAME, nowNote);//传递记事对象到详情活动

                startActivity(intent);//跳转到详情页面
            }
        });
    }


    public void initView(final String search_str){//该方法用于初始化cellListView
//
//        notesDB=new NotesDB(getContext());
//        dbReader=notesDB.getReadableDatabase();//获取可读数据库
//
//        cellListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//这部分是点到每一行记录时的操作
//                String search_sql = NotesDB.CONTENT + " LIKE '%"+search_str+"%' "+" OR "+ NotesDB.TIME + " LIKE '%"+search_str+"%' " ;
//                //先初始化了cursor
//                cursor=dbReader.query(NotesDB.TABLE_NAME, null, search_sql, null, null, null, null);
//                cursor.moveToPosition(position);
//                Intent intent = new Intent(getContext(), DetailActivity.class);//跳转意图，用以从main跳转到detail
//                intent.putExtra(NotesDB.ID,cursor.getInt(cursor.getColumnIndex(NotesDB.ID)));//意图里装载使用游标查找到的ID
//                intent.putExtra(NotesDB.CONTENT,cursor.getString(cursor.getColumnIndex(NotesDB.CONTENT)));//意图里装载使用游标查找到的文本
//                intent.putExtra(NotesDB.TIME,cursor.getString(cursor.getColumnIndex(NotesDB.TIME)));//意图里装载使用游标查找到的时间
//                intent.putExtra(NotesDB.PIC_PATH,cursor.getString(cursor.getColumnIndex(NotesDB.PIC_PATH)));//意图里装载使用游标查找到的图片
//                intent.putExtra(NotesDB.VIDEO_PATH,cursor.getString(cursor.getColumnIndex(NotesDB.VIDEO_PATH)));//意图里装载使用游标查找到的视频
//                intent.putExtra(NotesDB.SOUND_PATH,cursor.getString(cursor.getColumnIndex(NotesDB.SOUND_PATH)));//意图里装载使用游标查找到的录音
//                startActivity(intent);//跳转到详情页面开始
//            }
//        });
        cellListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//这部分是点到每一行记录时的操作
                Intent intent = new Intent(getContext(), DetailActivity.class);//跳转意图，用以从main跳转到detail
                Notes nowNote = notesList.get(position);
                intent.putExtra(Notes.CLASSNAME, nowNote);//传递记事对象到详情活动

                startActivity(intent);//跳转到详情页面
            }
        });
    }

    @Override
    public void onClick(View view) {//设置点击后的事件
        switch (view.getId()) {
            case R.id.bt_search:
                String search_str=et_search.getText().toString().trim();
                editor.putString("search_str", search_str);//将搜索文存入配置中
                editor.apply();
                refreshNotesList();
                break;
            default:
                break;
        }
    }

    /**
     * 该方法用于刷新记录列表
     */
    public void refreshNotesList() {
        preferences = getActivity().getSharedPreferences("AccountPreference", Context.MODE_PRIVATE);
        editor = preferences.edit();
        String search_get = preferences.getString("search_str", "");
        List<Notes> newNotesList;
        if (!"".equals(search_get)){//如果搜索不为空
            newNotesList = Notes.getNotesListContent(getContext(), MainActivity.getNowUsername(),search_get);//获取新记录
        }else {
            newNotesList = Notes.getNotesListContent(getContext(), MainActivity.getNowUsername());//获取新记录
        }

        notesList.clear();//清除旧记录
        notesList.addAll(newNotesList);//增加新记录
        showListContentAdapter.notifyDataSetChanged();//最后通知适配器数据更新
    }

    /**
     * 该方法用于刷新记录列表
     */
//    public void refreshNotesList(String search_str) {
//        List<Notes> newNotesList = Notes.getNotesListContent(getContext(), MainActivity.getNowUsername(),search_str);//获取新记录
//        notesList.clear();//清除旧记录
//        notesList.addAll(newNotesList);//增加新记录
//        showListContentAdapter.notifyDataSetChanged();//最后通知适配器数据更新
//    }

    @Override
    public void onResume() {
        super.onResume();
        preferences = getActivity().getSharedPreferences("AccountPreference", Context.MODE_PRIVATE);
        String search_get = preferences.getString("search_str", "");
//        if (search_get!=null&&!"".equals(search_get)){//不为空时执行搜素
//            et_search.setText(search_get);//将搜索文字设置到搜索框
//            refreshNotesList();//根据条件搜索记录
//        }else {//为空时搜索全部
//            refreshNotesList();//获取全部记录的记事信息
//        }
        refreshNotesList();
    }


//    public void selectNotesDB(){//该方法用于查询
//        //该cursor游标暂不初始化
//        cursor=dbReader.query(NotesDB.TABLE_NAME,null,null,null,null,null,null);
////        SearchFragment searchFragment =new SearchFragment();
//        List notesList = Notes.getNotesListContent(getContext(), MainActivity.getNowUsername());
//        showListContentApdater = new ShowListContentApdater(getContext(),R.id.cellListView,notesList);//将该适配器和该 主页面 绑定游标
//        cellListView.setAdapter(showListContentApdater);//将显示列表用的View绑定适配器
//    }
//
//    public void selectNotesDB(String search_str){//该方法用于字符串查询
//        initView(search_str);
//
//        String search_sql = NotesDB.CONTENT + " LIKE '%"+search_str+"%' "+" OR "+ NotesDB.TIME + " LIKE '%"+search_str+"%' " ;
////        Toast.makeText(getContext(),search_sql,Toast.LENGTH_SHORT).show();//测试用，可注释
//        final Cursor[] cursor = {dbReader.query(NotesDB.TABLE_NAME, null, search_sql, null, null, null, null)};
//        SearchFragment searchFragment =new SearchFragment();
//        List<Notes> notesList = Notes.getNotesListContent(getContext(),MainActivity.getNowUsername());
//        showListContentApdater = new ShowListContentApdater(getContext(), R.id.cellListView,notesList );//将该适配器和该 主页面 绑定游标
//        cellListView.setAdapter(showListContentApdater);//将显示列表用的View绑定适配器
//
//    }


}
