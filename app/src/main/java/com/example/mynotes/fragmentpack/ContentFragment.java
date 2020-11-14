package com.example.mynotes.fragmentpack;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mynotes.MainActivity;
import com.example.mynotes.R;
import com.example.mynotes.adapter.ShowListContentApdater;
import com.example.mynotes.control.AddContent;
import com.example.mynotes.control.TCPConnectUtil;
import com.example.mynotes.database.NotesDB;
import com.example.mynotes.model.Account;
import com.example.mynotes.model.ClientSendString;
import com.example.mynotes.model.DataJsonPack;
import com.example.mynotes.model.Notes;
import com.example.mynotes.other_activities.Detail;
import com.example.mynotes.widget.XListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class ContentFragment extends Fragment implements View.OnClickListener, XListView.IXListViewListener {
//
//    @Override
//    public View onCreateView( LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.content_fragment,container,false);
//        //这个view这里使得碎片绑定了content_fragment这个layout
//        return view;
//    }

//    public static String local_owner_name;

    public static ContentFragment contentFragmentInstance = null;
    public static PullToRefreshLayout refreshLayout;//该布局用于下拉刷新(同步)
    public static int lastUpdateNum = 0;//上一次的更新记录条数

    public static boolean isRefreshing = false;//是否正在刷新的标识

    Button bt_text;//输入文字按钮
    Button bt_pic;//拍照按钮
    Button bt_video;//录像按钮
    Button bt_sound;//录音按钮

    ListView cellListView;//用于显示记录的listView
    XListView mListView;//新的测试用listView

    Intent intent;

    private NotesDB notesDB;
    private SQLiteDatabase dbWriter, dbReader;
    private Cursor cursor;//该游标用于读取数据库

    private ShowListContentApdater showListContentApdater;//该适配器用于显示每条记录
    private Handler mHandler;//用于在子线程中更新UI使用

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_fragment, container, false);
        //这个view这里使得碎片绑定了content_fragment这个layout

        cellListView = (ListView) view.findViewById(R.id.cellListView);
        bt_text = (Button) view.findViewById(R.id.bt_text);
        bt_pic = (Button) view.findViewById(R.id.bt_pic);
        bt_video = (Button) view.findViewById(R.id.bt_video);
        bt_sound = (Button) view.findViewById(R.id.bt_sound);
//        refreshLayout = (PullToRefreshLayout) view.findViewById(R.id.content_refresh);

        bt_text.setOnClickListener(this);
        bt_pic.setOnClickListener(this);
        bt_video.setOnClickListener(this);
        bt_sound.setOnClickListener(this);

        //以下部分为新增实验代码
        mHandler = new Handler();

        mListView = view.findViewById(R.id.cellListView);
        mListView.setPullRefreshEnable(true);
        mListView.setPullLoadEnable(false);//使到达底部时能加载更多
        mListView.setAutoLoadEnable(true);//到达底部时自动刷新
        mListView.setXListViewListener(this);
//        mListView.setRefreshTime();
        //以上部分为新增实验代码

        initView();

        selectNotesDB();//获取全部记录的记事信息

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contentFragmentInstance = this;//初始化本实例
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void initView() {//该方法用于初始化cellListView

        notesDB = new NotesDB(getContext());
        dbReader = notesDB.getReadableDatabase();//获取可读数据库
        initCursor();//初始化数据库游标

        cellListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//这部分是点到每一行记录时的操作
                if (position == 0) {//如果位置等于0，则说明是第一位，第一位是刷新位，刷新位必须没有任何点击事件
                    return;//所以选择直接返回
                }

                //先初始化了cursor
//                cursor=dbReader.query(NotesDB.TABLE_NAME,null,null,null,null,null, null);
                cursor.moveToPosition(position - 1);//注意，这里为什么要减1呢？因为，下拉刷新的那一行居然也算是占了一位
                Intent intent = new Intent(getContext(), Detail.class);//跳转意图，用以从main跳转到detail
                intent.putExtra(NotesDB.ID, cursor.getInt(cursor.getColumnIndex(NotesDB.ID)));//意图里装载使用游标查找到的ID
                intent.putExtra(NotesDB.TITLE, cursor.getString(cursor.getColumnIndex(NotesDB.TITLE)));//意图里装载使用游标查找到的标题
                intent.putExtra(NotesDB.CONTENT, cursor.getString(cursor.getColumnIndex(NotesDB.CONTENT)));//意图里装载使用游标查找到的文本
                intent.putExtra(NotesDB.TIME, cursor.getString(cursor.getColumnIndex(NotesDB.TIME)));//意图里装载使用游标查找到的时间
                intent.putExtra(NotesDB.CHANGE_TIME, cursor.getString(cursor.getColumnIndex(NotesDB.CHANGE_TIME)));//意图里装载使用游标查找到的修改时间
                intent.putExtra(NotesDB.PIC_PATH, cursor.getString(cursor.getColumnIndex(NotesDB.PIC_PATH)));//意图里装载使用游标查找到的图片
                intent.putExtra(NotesDB.VIDEO_PATH, cursor.getString(cursor.getColumnIndex(NotesDB.VIDEO_PATH)));//意图里装载使用游标查找到的视频
                intent.putExtra(NotesDB.SOUND_PATH, cursor.getString(cursor.getColumnIndex(NotesDB.SOUND_PATH)));//意图里装载使用游标查找到的录音
                intent.putExtra(NotesDB.OWNER, cursor.getString(cursor.getColumnIndex(NotesDB.OWNER)));//意图里装载使用游标查找到的信息
                startActivity(intent);//跳转到详情页面开始
            }
        });

    }


    @Override
    public void onClick(View view) {//设置点击后的事件
        MainActivity mainActivity = (MainActivity) getActivity();//获取活动，为了获取当前是否有账号登录的标记
        Boolean isLogin = mainActivity.isLogin;//获取是否登录

        intent = new Intent(getContext(), AddContent.class);

        if (isLogin) {//如果已登录，则获取已登录的用户名
            intent.putExtra("owner", MainActivity.getNowAccount().getUsername());
        } else {//如果未登录，则设置为本地用户
            intent.putExtra("owner", NotesDB.LOCAL_OWNER_STRING);
        }

        switch (view.getId()) {
            //flag 1代表文字，2代表图片，3代表视频，4代表录音
            case R.id.bt_text:
                intent.putExtra("flag", "1");
                startActivity(intent);
                break;
            case R.id.bt_pic:
                intent.putExtra("flag", "2");
                startActivity(intent);
                break;
            case R.id.bt_video:
                intent.putExtra("flag", "3");
                startActivity(intent);
                break;
            case R.id.bt_sound:
                intent.putExtra("flag", "4");
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    public void initCursor() {//该方法用于初始化游标
        MainActivity mainActivity = MainActivity.mainActivityInstance;//获取MainActivity
        Boolean isLogin = mainActivity.isLogin;//获取是否登录的标识

        String selectionArgs[] = new String[2];//是显示记录时的参数
        selectionArgs[0] = NotesDB.LOCAL_OWNER_STRING;
        selectionArgs[1] = NotesDB.LOCAL_OWNER_STRING;//初始化
        if (isLogin) {//如果已登录
            Account nowAccount = MainActivity.getNowAccount();//获取获取当前账号实例Account
            String ownerStr = nowAccount.getUsername();//获取账号用户名
            selectionArgs[1] = ownerStr;//用户名作为搜索记录的条件
        } else {//如果未登录，则参数都设置为NotesDB.LOCAL_OWNER_STRING
        }

        //该cursor游标设置为使用NotesDB.OWNER限定搜索结果，再使用NotesDB.TIME排序
        cursor = dbReader.query(NotesDB.TABLE_NAME, null, NotesDB.OWNER + " = ? or " + NotesDB.OWNER + " = ?", selectionArgs, null, null, NotesDB.CHANGE_TIME + " Desc");
    }

    public void selectNotesDB() {//该方法用于查询数据库内容并绑定游标内容到适配器中
        initCursor();//初始化数据库游标

//        ContentFragment contentFragment=new ContentFragment();
        showListContentApdater = new ShowListContentApdater(getContext(), cursor);//将该适配器和该 主页面 绑定游标
//        showListContentApdater.notifyDataSetChanged();//通知数据变化
        cellListView.setAdapter(showListContentApdater);//将显示列表用的View绑定适配器
    }

    @Override
    public void onResume() {
        super.onResume();
        selectNotesDB();//这里用到是为了在别的活动结束后刷新记录列表
    }

    /**
     * 该方法是刷新后的回调方法（该方法隶属于XListView.IXListViewListener）
     */
    @Override
    public void onRefresh() {

        if (!isRefreshing) {//不在刷新时才能进行刷新
            isRefreshing = true;//刷新中设置为true

            if (MainActivity.getIsLogin()){//在登录时才能进行同步
                mHandler.postDelayed(new Runnable() {
//                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        String username = MainActivity.getNowAccount().getUsername();

                        List<Notes> notesList = Notes.getUserTextNotesList(getContext(), username);
                        JSONArray notesJsonArray = DataJsonPack.notesListToJSONArray(notesList);//获取notes的JsonArray数据

                        DataJsonPack dataJsonPack = new DataJsonPack();
                        dataJsonPack.setOperation(ClientSendString.NotesUpload);//设置数据包为上传操作
                        dataJsonPack.setDataObject(notesJsonArray);
                        dataJsonPack.setUsername(username);//设置数据包的用户名

                        new TCPConnectUtil().sendTCPRequestAndRespone(dataJsonPack);
                    }
                },1);
//                });
            }else {
                Toast.makeText(getContext(), "尚未登录，无法同步！", Toast.LENGTH_SHORT).show();
                onLoad();
            }
        } else {
            Toast.makeText(getContext(), "刷新中，请稍后！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 该方法隶属于XListView.IXListViewListener
     */
    @Override
    public void onLoadMore() {
        mListView.stopLoadMore();
    }

    /**
     * 该方法用于关闭"刷新中"动画
     */
    public void onLoad() {
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        mListView.stopRefresh();
        mListView.stopLoadMore();
        isRefreshing = false;//刷新中标识设置为false
        selectNotesDB();
        Toast.makeText(getContext(),"同步记录成功，共同步记录"+lastUpdateNum+"条",Toast.LENGTH_SHORT).show();
    }

    /**
     * 该方法用于在刷新成功后设置刷新时间
     */
    public void setRefreshTime() {
        mListView.setRefreshTime(AddContent.getNowTimeStr().substring(0, 17));
    }

    /**
     * 该方法用于在刷新成功后通知数据改变
     */
    public void notifyChange() {
        showListContentApdater.notifyDataSetChanged();
    }
}
