package com.example.mynotes;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mynotes.view.activities.AddContentActivity;
import com.example.mynotes.util.ThumbLoaderUtil;
import com.example.mynotes.dao.NotesDB;
import com.example.mynotes.view.fragments.ContentFragment;
import com.example.mynotes.view.fragments.LoginFragment;
import com.example.mynotes.view.fragments.RegisterFragment;
import com.example.mynotes.view.fragments.SearchFragment;
import com.example.mynotes.model.Account;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //AppCompatActivity
    public static ContentFragment mainContentFragment;//显示记事内容的碎片
    public static LoginFragment mainLoginFragment;//显示登录内容的碎片
    public static RegisterFragment mainRegisterFragment;//显示注册内容的碎片
    public static ThumbLoaderUtil mainVideoThumbLoader;//用于异步加载缩略图的方法
    
    public static int fragment_type;//该变量为当前碎片的种类，根据不同种类而进行不同的操作

    public static final int CONTENT_FRAGMENT_CODE = 1001;//ContentFragment的代号
    public static final int LOGIN_FRAGMENT_CODE = 1002;//LoginFragment的代号
    public static final int REGISTER_FRAGMENT_CODE = 1003;//RegisterFragment的代号
    public static final int SEARCH_FRAGMENT_CODE = 1004;//SearchFragment的代号

    //    Button bt_text;//输入文字按钮
//    Button bt_pic;//拍照按钮
//    Button bt_video;//录像按钮
//    Button bt_sound;//录音按钮
    Button bt_login;//左侧菜单登录按钮
    Button bt_register;//左侧菜单注册按钮
    Button bt_cancel;//左侧菜单注销按钮
    Button bt_tosearch;//左侧搜索碎片按钮
    Button bt_tomain;//左侧菜单主碎片按钮
    Button bt_callleft;//唤出左菜单的按钮
    Button bt_test1;//测试按钮1
    Button bt_test2;//测试按钮2
    Button bt_test3;//测试按钮3
    Button bt_test4;//测试按钮4

    ListView cellListView;//用于显示记录的listView

    Intent intent;

    //    LinearLayout leftMenuLayout;//该LinearLayout为左侧菜单栏
    DrawerLayout mainDrawer;//该DrawerLayout为主Layout

    private TextView tv_username;//用户名显示框
    private TextView tv_email;//邮箱显示框

    private LinearLayout accountLayout;//该accountLayout用于显示账号信息
    private LinearLayout voidLayout;//该voidLayout用于占位


    private NotesDB notesDB;
    private SQLiteDatabase dbWriter;
    private Cursor cursor;//该游标用于读取数据库

    private static MainActivity mainActivityInstance;
    private static Boolean isLogin = false;//isLogin是用来检测当前用户是否登录用的
    private static Account nowAccount = null;//目前已登录的账号信息

    private static SharedPreferences preferences = null;//保存密码用的非易失文件
    private static SharedPreferences.Editor editor = null;



    public static Account getNowAccount() {
        return nowAccount;
    }

    public static void setNowAccount(Account nowAccount) {
        MainActivity.nowAccount = nowAccount;
    }

    public static Boolean getIsLogin() {
        return isLogin;
    }

    public static void setIsLogin(Boolean isLogin) {
        MainActivity.isLogin = isLogin;
    }

    public static MainActivity getMainActivityInstance() {
        return mainActivityInstance;
    }

    public static void setMainActivityInstance(MainActivity mainActivityInstance) {
        MainActivity.mainActivityInstance = mainActivityInstance;
    }

    /**
     * 该方法用于检查当前用户是否登录并返回当前用户名
     * @return 会返回当前用户名，若当前用户不存在，则会返回本地拥有者
     */
    public static String getNowUsername(){
        String nowUsername = NotesDB.LOCAL_OWNER_STRING;
        if (isLogin){
            if (nowAccount != null){
                nowUsername = nowAccount.getUsername();
            }
        }
        return nowUsername;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 24) {//api版本大于24时则使用渐变色
            setContentView(R.layout.activity_main);
        } else {//否则不使用渐变色
            setContentView(R.layout.activity_main_v21);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");//将工具栏里的标题设置为空
        setSupportActionBar(toolbar);

        mainContentFragment = new ContentFragment();
        replaceMainFragment(mainContentFragment);//更换为主内容碎片

        initView();

        mainActivityInstance = this;//自己的单例实例化完成
        mainVideoThumbLoader = new ThumbLoaderUtil();//初始化缩略图异步加载类
    }


    /**
     * 该方法用于初始化MainActivity的控件们
     */
    public void initView() {
        bt_login = (Button) findViewById(R.id.bt_login);
        bt_register = (Button) findViewById(R.id.bt_register);
        bt_cancel = (Button) findViewById(R.id.bt_cancel);
        bt_tosearch = (Button) findViewById(R.id.bt_tosearch);
        bt_tomain = (Button) findViewById(R.id.bt_tomain);
        bt_callleft = (Button) findViewById(R.id.bt_callleft);
        bt_test1 = (Button) findViewById(R.id.bt_test1);
        bt_test2 = (Button) findViewById(R.id.bt_test2);
        bt_test3 = (Button) findViewById(R.id.bt_test3);
        bt_test4 = (Button) findViewById(R.id.bt_test4);

        accountLayout = findViewById(R.id.show_account);
        voidLayout = findViewById(R.id.show_void);

        tv_username = findViewById(R.id.tv_username);
        tv_email = findViewById(R.id.tv_email);

        bt_login.setOnClickListener(this);
        bt_register.setOnClickListener(this);
        bt_cancel.setOnClickListener(this);
        bt_tosearch.setOnClickListener(this);
        bt_tomain.setOnClickListener(this);
        bt_callleft.setOnClickListener(this);
        bt_test1.setOnClickListener(this);
        bt_test2.setOnClickListener(this);
        bt_test3.setOnClickListener(this);
        bt_test4.setOnClickListener(this);

        mainDrawer = findViewById(R.id.mainDrawer);
        mainDrawer.addDrawerListener(new DrawerLayout.DrawerListener() {//给drawer布局设置监听器
            @Override
            public void onDrawerSlide(View view, float v) {
            }

            @Override
            public void onDrawerOpened(View view) {
//                Toast.makeText(MainActivity.this,"侧拉菜单打开了~",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDrawerClosed(View view) {
//                Toast.makeText(MainActivity.this,"侧拉菜单关闭了...",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDrawerStateChanged(int i) {
//                Toast.makeText(MainActivity.this,"侧拉菜单状态改变了！ ",Toast.LENGTH_LONG).show();
            }
        });

        loginWhenBeginIfRemember();//看看是否记住了密码，如果记住了，就进行无网络验证的登录操作
    }


    @Override
    public void onClick(View view) {//设置点击后的事件
        intent = new Intent(this, AddContentActivity.class);
        switch (view.getId()) {
            case R.id.bt_login:
//                bt_login.setVisibility(View.GONE);//纯粹测试用，记得注释掉
//                afterLogin("red007");//使用red007测试登录
                Toast.makeText(this, "点击了登录按钮", Toast.LENGTH_SHORT).show();
                mainLoginFragment = new LoginFragment();
                replaceMainFragment(mainLoginFragment);//更换为登录碎片
//                replaceFragment.replaceMainFragment(loginFragement);
                break;
            case R.id.bt_register:
                mainRegisterFragment = new RegisterFragment();
                replaceMainFragment(mainRegisterFragment);//更换为注册碎片
                break;
            case R.id.bt_cancel:
                afterLogout();//注销后的操作
                Toast.makeText(this, "注销成功", Toast.LENGTH_SHORT).show();
                break;
            case R.id.bt_tosearch:
                Toast.makeText(this, "点击了搜索记录按钮", Toast.LENGTH_SHORT).show();
                SearchFragment searchFragment = new SearchFragment();
                replaceMainFragment(searchFragment);//更换为搜索碎片
                break;
            case R.id.bt_tomain:
                Toast.makeText(this, "点击了开始记事按钮", Toast.LENGTH_SHORT).show();
                ContentFragment contentFragment = new ContentFragment();
                replaceMainFragment(contentFragment);//更换为主内容碎片
//                replaceFragment.replaceMainFragment(contentFragment);
                break;
            case R.id.bt_callleft://该按钮会唤出左侧菜单栏
                mainDrawer.openDrawer(Gravity.LEFT);
                break;
            case R.id.bt_test1://该按钮用于测试
                ContentValues contentValues1 = new ContentValues();
                //id不需要放入是因为设置了id自增
                contentValues1.put(NotesDB.TITLE, "无序号的空内容记录2");
                contentValues1.put(NotesDB.CONTENT, "");//添加文本输入框里的内容进数据库
                contentValues1.put(NotesDB.TIME, AddContentActivity.getNowTimeStr());//添加当前的时间
                contentValues1.put(NotesDB.CHANGE_TIME, AddContentActivity.getNowTimeStr());//添加修改的时间
                contentValues1.put(NotesDB.PIC_PATH, null + "");//添加图片路径  我怀疑这个路径有问题
                contentValues1.put(NotesDB.VIDEO_PATH, null + "");//添加视频路径
                contentValues1.put(NotesDB.SOUND_PATH, null + "");//添加录音路径
                contentValues1.put(NotesDB.OWNER, NotesDB.LOCAL_OWNER_STRING);//添加当前拥有者名
                contentValues1.put(NotesDB.IS_CHANGE, 1);//添加是否修改标识

                notesDB = new NotesDB(this);
                SQLiteDatabase notesWriter1 = notesDB.getWritableDatabase();
                notesWriter1.insert(NotesDB.TABLE_NAME, null, contentValues1);
//                ContentFragment.contentFragmentInstance.selectNotesDB();//重新查询记录以刷新记录
//                Toast.makeText(this, "这个按钮现在什么都没做", Toast.LENGTH_SHORT).show();
                break;

            case R.id.bt_test2://该按钮用于测试数据库的获取
                NotesDB notesDB = new NotesDB(this);
                dbWriter = notesDB.getReadableDatabase();//获取可写入数据库
                Cursor cursor = dbWriter.query(NotesDB.TABLE_NAME, null, null, null, null, null, null);

                //cursor.moveToFirst();//游标移到第一位;
                String allDataStr = "";
                while (cursor.moveToNext()) {//遍历游标里的所有数据
                    String ownerStr = cursor.getString(cursor.getColumnIndex(NotesDB.OWNER));
                    String timeStr = cursor.getString(cursor.getColumnIndex(NotesDB.TIME));
                    String changeTimeStr = cursor.getString(cursor.getColumnIndex(NotesDB.CHANGE_TIME));
                    int isChange_int = cursor.getInt(cursor.getColumnIndex(NotesDB.IS_CHANGE));// 0 为 false ， 1为true
                    boolean isChange = isChange_int == 1;
                    String contentStr = cursor.getString(cursor.getColumnIndex(NotesDB.CONTENT));//如果输入内容为空，则真的会获取到"null"字符串
                    allDataStr = allDataStr + timeStr + ":  " + contentStr + "\n";
                }

                if ("".equals(allDataStr)) {
                    Toast.makeText(this, "数据库消息为空", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, allDataStr, Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.bt_test3:
//                new TCPConnectUtil().sendTCPRequestAndRespone();
                Toast.makeText(this, "主活动中尝试释放按钮", Toast.LENGTH_SHORT).show();
//                mainLoginFragment.releaseLoginButton();
//                LoginFragement.loginFragementInstance.releaseLoginButton();//尝试释放按钮
                afterLoginFailed();//尝试释放按钮
                break;

            case R.id.bt_test4:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BufferedReader reader = null;

                        try {
                            URL url = new URL("http://10.0.80.64:8080/users");
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            connection.setConnectTimeout(3000);
                            connection.setReadTimeout(3000);

                            InputStream in = new BufferedInputStream(connection.getInputStream());
                            reader = new BufferedReader(new InputStreamReader(in));
                            StringBuffer stringBuffer = new StringBuffer();
                            String line;

                            while ((line = reader.readLine()) != null){
                                stringBuffer.append(line);
                            }

                            sendToastTextWouldBlock(stringBuffer.toString());

                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;

            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        selectNotesDB();
    }


    /**
     * 该方法用于保存账号信息到非易失文件
     * @param account    参数是Account类的账号信息
     * @param isRemember 参数是是否记住密码
     */
    public static void saveAccountToSharePreference(Account account, Boolean isRemember) {
        if (preferences == null) {//若未初始化则初始化
            preferences = MainActivity.mainActivityInstance.getSharedPreferences("AccountPreference", Context.MODE_PRIVATE);
        }
        if (editor == null) {//若未初始化则初始化
            editor = preferences.edit();
        }

        if (isRemember) {//这里是记住密码的情况
            editor.putString("username", account.getUsername());
            editor.putString("password", account.getPassword());
            editor.putString("email", account.getEmail());
            editor.putBoolean("remember_password", true);
        } else {
            editor.putString("username", "");
            editor.putString("password", "");
            editor.putString("email", "");
            editor.putBoolean("remember_password", false);
        }
        editor.apply();//提交更改
    }


    /**
     * 该方法用于把主碎片给替换成其它碎片
     */
    public void replaceMainFragment(Fragment fragment) {
        //判断需要替换的碎片类型并设置碎片的代号
        if (fragment instanceof ContentFragment){
            fragment_type = CONTENT_FRAGMENT_CODE;
        }
        if (fragment instanceof LoginFragment){
            fragment_type = LOGIN_FRAGMENT_CODE;
        }
        if (fragment instanceof RegisterFragment){
            fragment_type = REGISTER_FRAGMENT_CODE;
        }
        if (fragment instanceof SearchFragment){
            fragment_type = SEARCH_FRAGMENT_CODE;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.main_fragment, fragment);//将main_fragement这个id的碎片控件替换为别的碎片
        transaction.commit();//提交更改
    }


    /**
     * 该方法用于执行登录成功后的切换操作<br/>
     * 注意，此方法不能在未初始化nowAccount之前使用！！！<br/>
     * 由于此方法会被网络连接的管理类所调用，同时该方法有执行UI改变操作，所以它必须在.runOnUiThread()方法中执行！
     */
    public void afterLoginSuccess() {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_username.setText(nowAccount.getUsername());//把名字显示在名字框
                tv_email.setText(nowAccount.getEmail());//把邮箱显示在邮箱框

                voidLayout.setVisibility(View.GONE);//不显示占位
                accountLayout.setVisibility(View.VISIBLE);//显示头像
                bt_login.setVisibility(View.GONE);//不显示左侧菜单登录按钮
                bt_cancel.setVisibility(View.VISIBLE);//显示左侧菜单注销按钮

                ContentFragment contentFragment = ContentFragment.getContentFragmentInstance();
                if (contentFragment != null){//如果ContentFragment.contentFragmentInstance不为空，则重复利用
//                    contentFragment = ContentFragment.contentFragmentInstance;
                } else //否则就新造
                    contentFragment = new ContentFragment();

                replaceMainFragment(contentFragment);//将当前界面(登录界面)换成记事内容界面
                isLogin = true;//登录过后登录标识改为 真
            }
        });
    }

    /**
     * 该方法用于执行登录失败后的切换操作<br/>
     * 由于此方法会被网络连接的管理类所调用，同时该方法有执行UI改变操作，所以它必须在.runOnUiThread()方法中执行！<br/>
     */
    public void afterLoginFailed() {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (LoginFragment.loginFragmentInstance != null) {//不为空时才能操作
                    LoginFragment.loginFragmentInstance.releaseLoginButton();//释放按钮
                    isLogin = false;//登录失败后登录标识改为 假
                }
            }
        });
    }

    /**
     * 该方法用于执行注册成功后的处理操作<br/>
     * 由于此方法会被网络连接的管理类所调用，同时该方法有执行UI改变操作，所以它必须在.runOnUiThread()方法中执行！<br/>
     */
    public void afterRegisterSuccess() {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RegisterFragment.registerFragementInstance.releaseRegisterButton();//释放按钮
            }
        });
    }

    /**
     * 该方法用于执行注册异常后的处理操作<br/>
     * 由于此方法会被网络连接的管理类所调用，同时该方法有执行UI改变操作，所以它必须在.runOnUiThread()方法中执行！<br/>
     */
    public void afterRegisterFailed() {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (RegisterFragment.registerFragementInstance != null)//不为空时才能操作
                    RegisterFragment.registerFragementInstance.releaseRegisterButton();//释放按钮
            }
        });
    }


    public void afterLogout() {//该方法用于执行注销后的UI切换操作
        nowAccount = null;//现有登录的账户改为null

        voidLayout.setVisibility(View.VISIBLE);//显示占位
        accountLayout.setVisibility(View.GONE);//不显示头像
        bt_login.setVisibility(View.VISIBLE);//显示左侧菜单登录按钮
        bt_cancel.setVisibility(View.GONE);//不显示左侧菜单注销按钮

        saveAccountToSharePreference(null, false); //并且不保存账号密码
        isLogin = false;//已登录标识改为 false
        nowAccount = null;//然后目前已登录账号改为 null

        ContentFragment contentFragment = new ContentFragment();
        replaceMainFragment(contentFragment);//说白了就是强制刷新显示内容

    }

    /**
     * 该方法是初始化主活动时，在非易失文件判断用户是否已记住密码并登录用的方法
     */
    public void loginWhenBeginIfRemember() {
        preferences = getSharedPreferences("AccountPreference", Context.MODE_PRIVATE);
        String username = "";
        String password = "";
        String email = "";

        boolean isRemember = preferences.getBoolean("remember_password", false);
        if (isRemember) {
            username = preferences.getString("username", "");
            password = preferences.getString("password", "");
            email = preferences.getString("email", "");
            Account tempAccount = new Account(username, password, email);
            nowAccount = tempAccount;//若非易失的记住密码标识为真，则把记住的账号标识加载到当前登录中

            if (username != null && !"".equals(username)) {
                afterLoginSuccess();//登录成功后进行切换操作
            }
        }
    }


    /**
     * 该方法用于外部类调用以发送Toast消息
     * 该方法有极大概率会导致阻塞
     */
    public void sendToastTextWouldBlock(final String str) {
        Looper.prepare();
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        Looper.loop();//这里有极高概率会导致阻塞
    }

    /**
     * 该方法用于控制器类调用以关闭刷新动画，同时还会刷新记录
     */
    public void afterRefreshSucceed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ContentFragment fragment = ContentFragment.getContentFragmentInstance();
                if (fragment != null) {//不为空时才能操作
                    fragment.afterRefresh();//
                }
            }
        });
    }

    /**
     * 该方法用于控制器类调用以关闭刷新动画，同时还会刷新记录
     */
    public void afterRefreshFailed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ContentFragment fragment = ContentFragment.getContentFragmentInstance();
                if (fragment != null) {//不为空时才能操作
                    fragment.stopRefresh();
                }
            }
        });
    }
}