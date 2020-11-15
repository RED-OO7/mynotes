package com.example.mynotes.view.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mynotes.MainActivity;
import com.example.mynotes.R;
import com.example.mynotes.controller.TCPConnectController;
import com.example.mynotes.model.Account;
import com.example.mynotes.model.ClientSendString;
import com.example.mynotes.model.DataJsonPack;

import java.util.Map;

public class LoginFragment extends Fragment implements View.OnClickListener {

    public static LoginFragment loginFragmentInstance = null;//本登录碎片的静态对象

    public static final String btn_login_color = "#3c8dc4";
    public static final String btn_register_color = "#2b7cb3";
    public static final String btn_block_color = "#708090";

    private EditText et_username;
    private EditText et_password;

    public static Boolean isRememberAccount = false;//是否记住登录信息的标识

    private TextView tv_username;//用户名显示框
    private TextView tv_email;//邮箱显示框

    private Button btn_login;//碎片里的登录按钮
    private Button btn_register;//碎片里的注册按钮
    private CheckBox checkbox;

    private Button bt_login;//左侧菜单登录按钮
    private Button bt_cancel;//左侧菜单那注销按钮

    private LinearLayout accountLayout;//该accountLayout用于显示账号信息
    private LinearLayout voidLayout;//该voidLayout用于占位

    private SharedPreferences preferences;//保存密码用的
    private SharedPreferences.Editor editor;

    private Map<String, Account> accountData;//该用于存储用户数据

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragement, container, false);
        //这个view这里使得碎片绑定了login_fragement这个layout
        View view_main = inflater.inflate(R.layout.activity_main, container, false);

        et_username = (EditText) view.findViewById(R.id.et_username);
        et_password = (EditText) view.findViewById(R.id.et_password);
        checkbox = (CheckBox) view.findViewById(R.id.checkBox);
        btn_login = (Button) view.findViewById(R.id.button_login);
        btn_register = (Button) view.findViewById(R.id.button_register);
        bt_login = (Button) view_main.findViewById(R.id.bt_login);
        bt_cancel = (Button) view_main.findViewById(R.id.bt_cancel);

        accountLayout = (LinearLayout) view_main.findViewById(R.id.show_account);
        voidLayout = (LinearLayout) view_main.findViewById(R.id.show_void);

        tv_username = (TextView) view_main.findViewById(R.id.tv_username);
        tv_email = (TextView) view_main.findViewById(R.id.tv_email);

//        tv_email.setText("测试是否获取成功");
//        bt_login.setOnClickListener(new MyButton());

        btn_login.setOnClickListener(this);
        btn_register.setOnClickListener(this);

        preferences = getActivity().getSharedPreferences("AccountPreference", Context.MODE_PRIVATE);
        String username = "";
        String password = "";
        boolean isRememberAccountTemp = preferences.getBoolean("remember_password", false);
        if (isRememberAccountTemp) {//如果配置文件中的[记住密码]为真，则把配置文件中的账号信息显示在编辑框里
            username = preferences.getString("username", "");
            password = preferences.getString("password", "");
            et_username.setText(username);
            et_password.setText(password);
        }

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        accountData = SaveInfo.getSaveInformation(getContext());
//        accountData = SaveInfo.getAccountsMap(getContext());
        loginFragmentInstance = this;//实例化本静态实例
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        String username = et_username.getText().toString().trim();
        String password = et_password.getText().toString().trim();
        switch (v.getId()) {
            //当点击登录按钮时
            case R.id.button_login:
                lockLoginButton();//使登录按钮不可用
//                    Toast.makeText(getContext(), "你输入的\n账号为:" + username + "\n密码为:" + password, Toast.LENGTH_SHORT).show();
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    Toast.makeText(getContext(), "密码或账号不能为空", Toast.LENGTH_SHORT).show();
                    releaseLoginButton();//使登录按钮可用
                } else {//这里是密码和账号不为空的情况

                    if (checkbox.isChecked()) //如果勾选了，则是否保存密码的标识改为真
                        isRememberAccount = true;
                    else //否则，是否保存密码的标识改为假
                        isRememberAccount = false;

                    /* 这里之后是新的登录代码--------------------------------------------- */
                    Account accountLogin = new Account();
                    accountLogin.setUsername(username);
                    accountLogin.setPassword(password);

                    DataJsonPack dataSend = new DataJsonPack();
                    dataSend.setDataObject(accountLogin);
                    dataSend.setOperation(ClientSendString.LoginAccount);//设置操作为登录账户

                    new TCPConnectController().sendTCPRequestAndRespone(dataSend);//发送数据
                }
                break;
            //当点击注册按钮事件时
            case R.id.button_register:
                RegisterFragment registerFragment = new RegisterFragment();
                replaceMainFragment(registerFragment);//更换为注册碎片
                break;
        }
    }

    private void replaceMainFragment(Fragment fragment) {
        MainActivity mainActivity = MainActivity.getMainActivityInstance();
        mainActivity.replaceMainFragment(fragment);//调用主活动的碎片替换方法
    }

    /**
     * 该方法用于使登录按钮不可用，该方法只能在LoginFragment被初始化完成后使用！
     */
    private void lockLoginButton(){
        btn_login.setEnabled(false);//使登录按钮不可用
        btn_login.setBackgroundColor(Color.parseColor(btn_block_color));
    }

    /**
     * 该方法用于使登录按钮可用，该方法只能在LoginFragment被初始化完成后使用！
     */
    public void releaseLoginButton(){
        btn_login.setEnabled(true);//使登录按钮可用
        btn_login.setBackgroundColor(Color.parseColor(btn_login_color));//恢复淡蓝色
    }


}