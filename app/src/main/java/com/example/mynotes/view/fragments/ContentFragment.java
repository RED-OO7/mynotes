package com.example.mynotes.view.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mynotes.MainActivity;
import com.example.mynotes.R;
import com.example.mynotes.model.Account;
import com.example.mynotes.view.adapter.ShowListContentApdater;
import com.example.mynotes.view.activities.AddContentActivity;
import com.example.mynotes.controller.TCPConnectController;
import com.example.mynotes.dao.NotesDB;
import com.example.mynotes.model.ClientSendString;
import com.example.mynotes.model.DataJsonPack;
import com.example.mynotes.model.Notes;
import com.example.mynotes.view.activities.DetailActivity;
import com.example.mynotes.widget.XListView;

import org.json.JSONArray;

import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

public class ContentFragment extends Fragment implements View.OnClickListener, XListView.IXListViewListener {

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

    private ShowListContentApdater showListContentAdapter;//该适配器用于显示每条记录
    private Handler mHandler;//用于在子线程中更新UI使用

    private static List<Notes> notesList;//这是存储记事信息内容时用的队列
    private static SharedPreferences preferences = null;//保存密码用的非易失文件
    private static SharedPreferences.Editor editor = null;

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

        getRefreshTimeAndShow();//在初始时获取刷新时间并显示
        initView();//初始化cellListView

        notesList = Notes.getNotesListContent(getContext(), MainActivity.getNowUsername());//用游标获取记事记录

        showListContentAdapter = new ShowListContentApdater(getContext(), R.id.cellListView, notesList);//创建适配器
        cellListView.setAdapter(showListContentAdapter);//设置适配器

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getContext(),"11111",Toast.LENGTH_LONG).show();

                    //intent.putExtra("flag", "2");
                    //startActivity(intent);
                }else{
                    Toast.makeText(getContext(), "当前没有录音权限", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }


    }

    @Override
    public void onClick(View view) {//设置点击后的事件
        Boolean isLogin = MainActivity.getIsLogin();//获取是否登录

        intent = new Intent(getContext(), AddContentActivity.class);

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
                if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)!=
                PackageManager.PERMISSION_GRANTED){
                    //Toast.makeText(getContext(),"123",Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CAMERA},1);
                }else{
                    //Toast.makeText(getContext(),"123456",Toast.LENGTH_LONG).show();
                    intent.putExtra("flag", "2");
                    startActivity(intent);
                }
                break;
            case R.id.bt_video:

                if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)!=
                        PackageManager.PERMISSION_GRANTED){
                    //Toast.makeText(getContext(),"123",Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CAMERA},1);
                }else{
                    //Toast.makeText(getContext(),"123456",Toast.LENGTH_LONG).show();
                    intent.putExtra("flag", "3");
                    startActivity(intent);
                }
                break;
            case R.id.bt_sound:

                intent.putExtra("flag", "4");
                startActivity(intent);
                break;
            default:
                break;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        refreshNotesList();//刷新显示列表
    }

    /**
     * 该方法是刷新后的回调方法（该方法隶属于XListView.IXListViewListener）
     */
    @Override
    public void onRefresh() {

        if (!isRefreshing) {//不在刷新时才能进行刷新
            isRefreshing = true;//刷新中设置为true

            if (MainActivity.getIsLogin()) {//在登录时才能进行同步
//                mHandler.postDelayed(new Runnable() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        String username = MainActivity.getNowAccount().getUsername();

                        List<Notes> notesList = Notes.getUserTextNotesList(getContext(), username);
                        JSONArray notesJsonArray = DataJsonPack.notesListToJSONArray(notesList);//获取notes的JsonArray数据

                        DataJsonPack dataJsonPack = new DataJsonPack();
                        dataJsonPack.setOperation(ClientSendString.NotesUpload);//设置数据包为上传操作
                        dataJsonPack.setDataObject(notesJsonArray);
                        dataJsonPack.setUsername(username);//设置数据包的用户名

                        new TCPConnectController().sendTCPRequestAndRespone(dataJsonPack);
                    }
//                },1);
                });
            } else {
                Toast.makeText(getContext(), "尚未登录，无法同步！", Toast.LENGTH_SHORT).show();
                afterRefresh();
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
     * 该方法用于初始化cellListView
     */
    public void initView() {
        cellListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//这部分是点到每一行记录时的操作
                if (position == 0) {//如果位置等于0，则说明是第一位，第一位是刷新位，刷新位必须没有任何点击事件
                    return;//所以选择直接返回
                }

                Intent intent = new Intent(getContext(), DetailActivity.class);//跳转意图，用以从main跳转到detail
                Notes nowNote = notesList.get(position - 1);//注意，这里为什么要减1呢？因为，下拉刷新的那一行居然也算是占了一位
                intent.putExtra(Notes.CLASSNAME, nowNote);

                startActivity(intent);//跳转到详情页面
            }
        });
    }

    /**
     * 该方法用于关闭"刷新中"动画
     */
    public void stopRefresh() {
        mListView.stopRefresh();
        mListView.stopLoadMore();
        isRefreshing = false;//刷新中标识设置为false
    }

    /**
     * 该方法用于刷新记录列表
     */
    public void refreshNotesList() {
        List<Notes> newNotesList = Notes.getNotesListContent(getContext(), MainActivity.getNowUsername());//获取新记录
        notesList.clear();//清除旧记录
        notesList.addAll(newNotesList);//增加新记录
        showListContentAdapter.notifyDataSetChanged();//最后通知适配器数据更新
    }

    /**
     * 该方法用于关闭"刷新中"动画，设置刷新中标识，和刷新记录列表
     */
    public void afterRefresh() {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stopRefresh();//关闭刷新中动画
        setRefreshTime();//设置刷新的时间
        refreshNotesList();//刷新记录列表

        if (lastUpdateNum > 1) {//如果更新记录数大于0，则显示通知记录数
            Toast.makeText(getContext(), "记录同步成功，共同步记录" + lastUpdateNum + "条", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "记录已更新至最新！", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * 该方法用于在刷新成功后设置刷新时间，并保存到非易失文件中
     */
    public void setRefreshTime() {
        String refresh_time = AddContentActivity.getNowTimeStr().substring(0, 17);
        mListView.setRefreshTime(refresh_time);

        if (preferences == null) {//若未初始化则初始化
            preferences = MainActivity.getMainActivityInstance().getSharedPreferences("AccountPreference", Context.MODE_PRIVATE);
        }
        if (editor == null) {//若未初始化则初始化
            editor = preferences.edit();
        }

        editor.putString("refresh_time", refresh_time);
        editor.apply();//提交更改
    }

    /**
     * 该方法是用于在非易失文件中获取上次记录同步时间并显示
     */
    public void getRefreshTimeAndShow() {
        if (preferences == null) {//若未初始化则初始化
            preferences = MainActivity.getMainActivityInstance().getSharedPreferences("AccountPreference", Context.MODE_PRIVATE);
        }
        String refresh_time;
        refresh_time = preferences.getString("refresh_time", "");

        if(!"".equals(refresh_time)){//如果刷新时间不为空
            mListView.setRefreshTime(refresh_time);//则显示出来
        }
    }


}
