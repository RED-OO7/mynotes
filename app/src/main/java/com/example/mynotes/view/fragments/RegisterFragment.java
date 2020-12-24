package com.example.mynotes.view.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterFragment extends Fragment implements View.OnClickListener {

    public static RegisterFragment registerFragementInstance = null;//本注册碎片的静态对象

    public static final String reg_btn_sure_color = "#74e674";
    public static final String reg_btn_login_color = "#63d563";
    public static final String btn_block_color = "#708090";

    private EditText reg_username;
    private EditText reg_password;
    private EditText reg_password2;
    private EditText reg_mail;
    private Button reg_btn_sure;
    private Button reg_btn_login;
    Pattern p;
    Matcher m;
    Matcher m1;
    String str;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register_fragment, container, false);
        //这个view这里使得碎片绑定了login_fragement这个layout

        reg_username = (EditText) view.findViewById(R.id.reg_username);
        reg_password = (EditText) view.findViewById(R.id.reg_password);
        reg_password2 = (EditText) view.findViewById(R.id.reg_password2);
        reg_mail = (EditText) view.findViewById(R.id.reg_mail);
        reg_btn_sure = (Button) view.findViewById(R.id.reg_btn_sure);
        reg_btn_login = (Button) view.findViewById(R.id.reg_btn_login);
        reg_btn_sure.setOnClickListener(this);
        reg_btn_login.setOnClickListener(this);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerFragementInstance = this;//初始化本静态实例
    }

    @Override
    public void onClick(View v) {
        String username = reg_username.getText().toString().trim();
        String password = reg_password.getText().toString().trim();
        String password2 = reg_password2.getText().toString().trim();
        String mail = reg_mail.getText().toString().trim();
        switch (v.getId()) {
            //注册开始，判断注册条件
            case R.id.reg_btn_sure:
                if (!TextUtils.isEmpty(mail)) {
                    //判断邮箱的正则表达式
                    str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
                    p = Pattern.compile(str);
                    m = p.matcher(mail);
                    if (m.matches() == false) {
                        Toast.makeText(getContext(), "邮箱地址格式有误", Toast.LENGTH_SHORT).show();
                    }
                }
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(password2) || TextUtils.isEmpty(mail)) {
                    Toast.makeText(getContext(), "各项均不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    //判断用户名的正则表达式(只能以字母开头，且6-20位的数字或字母字符)
                    str = "^[a-zA-Z][a-zA-Z0-9]{6,20}$";
                    p = Pattern.compile(str);
                    m = p.matcher(username);
                    if (m.matches() == false) {//如果账号格式有误，则不能继续注册
                        Toast.makeText(getContext(), "账号格式有误\n(只能以字母开头，且6-20位的数字或字母字符)", Toast.LENGTH_SHORT).show();
                    } else {//否则账号格式无误，允许继续注册
                        if (TextUtils.equals(password, password2)) {
                            lockRegisterButton();//先锁住按钮

                            //执行注册操作
                            Account accountRegister = new Account();
                            accountRegister.setUsername(username);
                            accountRegister.setPassword(password);
                            accountRegister.setEmail(mail);

                            DataJsonPack dataSend = new DataJsonPack();
                            dataSend.setDataObject(accountRegister);
                            dataSend.setOperation(ClientSendString.RegisterAccount);//设置操作为注册账户

                            new TCPConnectController().sendTCPRequestAndRespone(dataSend);//发送数据
                            //Toast.makeText(getContext(), "注册成功,请返回登录", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "两次输入的密码不一样", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
                break;

            case R.id.reg_btn_login:
                LoginFragment loginFragment = new LoginFragment();
                replaceMainFragment(loginFragment);
                break;

        }
    }


    private void replaceMainFragment(Fragment fragment) {
        MainActivity mainActivity = MainActivity.getMainActivityInstance();
        mainActivity.replaceMainFragment(fragment);//调用主活动的碎片替换方法
    }

    /**
     * 该方法用于使注册按钮和跳转按钮不可用，该方法只能在RegisterFragment被初始化完成后使用！
     */
    public void lockRegisterButton() {
        reg_btn_sure.setEnabled(false);//使注册按钮不可用
        reg_btn_login.setEnabled(false);//使跳转按钮不可用

        reg_btn_sure.setBackgroundColor(Color.parseColor(btn_block_color));
        reg_btn_login.setBackgroundColor(Color.parseColor(btn_block_color));
    }

    /**
     * 该方法用于使使注册按钮和跳转按钮可用，该方法只能在RegisterFragment被初始化完成后使用！
     */
    public void releaseRegisterButton() {
        reg_btn_sure.setEnabled(true);//使注册按钮可用
        reg_btn_login.setEnabled(true);//使跳转按钮可用

        reg_btn_sure.setBackgroundColor(Color.parseColor(reg_btn_sure_color));//恢复淡绿色
        reg_btn_login.setBackgroundColor(Color.parseColor(reg_btn_login_color));//恢复绿色
    }


}
