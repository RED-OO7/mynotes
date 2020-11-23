package com.example.mynotes.controller;

import android.os.Handler;

import com.example.mynotes.MainActivity;
import com.example.mynotes.model.ClientSendString;
import com.example.mynotes.view.fragments.ContentFragment;
import com.example.mynotes.view.fragments.LoginFragment;
import com.example.mynotes.model.Account;
import com.example.mynotes.model.ClientReceiveString;
import com.example.mynotes.model.DataJsonPack;
import com.example.mynotes.model.Notes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;


public class TCPConnectController {
    //"10.3.49.170"本地wifi ip地址，"139.224.128.87"是远程服务端的地址
    private static String serverAddress_str = "10.3.49.170";
    private static int PORT = 19200;

    private static Handler mHandler = new Handler();//创建Handler

    public static final String DATA_START = "[--[--[";
    public static final String DATA_END = "]--]--]";
    public static final String DIVIDE_LINE = "---------------------------------------------------";// 分割线

    /**
     * 该方法可以开启新线程并发送数据到服务器端，并接收服务器回传的数据
     *
     * @param sendData 参数是DataJsonPack的对象，是需要发送给服务器的数据对象
     */
    public void sendTCPRequestAndRespone(final DataJsonPack sendData) {

//        mHandler.post(
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            SocketAddress addr = new InetSocketAddress(serverAddress_str, PORT);
                            Socket clientSocket = new Socket();
                            clientSocket.connect(addr, 500);
                            // 设置读取数据超时时间
                            clientSocket.setSoTimeout(5000);

                            // 开启字节输入流(用以接收服务器发来的信息)
                            InputStream serverInputStream = clientSocket.getInputStream();
                            BufferedReader serverBufferedReader = new BufferedReader(new InputStreamReader(serverInputStream, "utf-8"));

                            // 开启字节输出流(用以发送信息到服务器)
                            OutputStream serverOutputStream = clientSocket.getOutputStream();
                            BufferedWriter serverBufferedWriter = new BufferedWriter(new OutputStreamWriter(serverOutputStream, "utf-8"));
                            PrintWriter printWriter = new PrintWriter(serverBufferedWriter, true);

                            String dataSendStr = TCPConnectController.DATA_START + sendData.toString() + TCPConnectController.DATA_END;
                            printWriter.println(dataSendStr);// 发送数据给服务器

                            //开始接收服务器回传的数据
                            StringBuffer dataBuffer = new StringBuffer();
                            String lineStr;
                            while ((lineStr = serverBufferedReader.readLine()) != null) {
                                //该循环有极大可能会导致阻塞，请谨慎判断退出循环的条件

                                dataBuffer.append(lineStr);

                                if (!dataBuffer.toString().startsWith(TCPConnectController.DATA_START)) {
                                    //如果服务器发来的消息不是按照规定的格式，则丢弃
                                    System.out.println("本机：⚠警告！服务器来的消息不以\"[--[--[\"开头！");
                                    break;
                                }

                                if (dataBuffer.toString().endsWith(TCPConnectController.DATA_END)) {
                                    //如果服务器发来的消息末尾是]--]--]则说明该停止了
                                    System.out.println("本机：一个服务器数据包接收完成！");
                                    break;
                                }

                                System.out.println("dataBuffer此时为：" + dataBuffer);
                            }

                            int dataLen = dataBuffer.length();
                            String finalString = dataBuffer.substring(TCPConnectController.DATA_START.length(), (dataLen - TCPConnectController.DATA_END.length()));
//                    System.out.println("服务器端发来的信息："+finalString);
                            resolveRespone(finalString);//解析服务器回传的数据

                        } catch (IOException e) {
                            e.printStackTrace();

                            MainActivity mainActivity = MainActivity.getMainActivityInstance();

                            switch (MainActivity.fragment_type) {
                                case MainActivity.CONTENT_FRAGMENT_CODE:
                                    mainActivity.stopRefresh();//关闭刷新动画
                                    break;
                                case MainActivity.LOGIN_FRAGMENT_CODE:
                                    mainActivity.afterLoginFailed();//失败后也要释放登录碎片的按钮
                                    break;
                                case MainActivity.REGISTER_FRAGMENT_CODE:
                                    mainActivity.afterRegisterFailed();//失败后也要释放注册碎片的按钮
                                    break;
                            }

                            mainActivity.sendToastTextWouldBlock("socket错误，错误为:\n" + e);
                        }
                    }
                }
        ).start();
//        );
    }

    /**
     * 该方法用于解析并操作服务器回传的数据<br/>
     * 注意，该方法不再允许进行网络操作！！！
     */
    public void resolveRespone(final String responeString) {
        try {
            JSONObject sourceJsonObject = new JSONObject(responeString);
            String classStr = sourceJsonObject.getString("CLASSNAME");

            if (classStr.equals(DataJsonPack.CLASSNAME)) {//要json包的验证类名和本类名一致时才能解析该json包
                String operationStr = sourceJsonObject.getString("operation");// 获取操作字符串
                String username;
                System.out.println("该数据包操作类型为:" + operationStr);

                MainActivity mainActivity = MainActivity.getMainActivityInstance();

                switch (operationStr) {
                    case ClientReceiveString.LoginSuccess:// 登录成功后的操作(这里是客户端)
                        JSONObject accountJsonObject = sourceJsonObject.getJSONObject("dataObject");
                        username = accountJsonObject.getString("username");
                        String password = accountJsonObject.getString("password");
                        String email = accountJsonObject.getString("email");

                        Account nowAccount = new Account(username, password, email);
                        MainActivity.setNowAccount(nowAccount);//将登录账户设置为服务端已验证的账户
                        MainActivity.saveAccountToSharePreference(nowAccount, LoginFragment.isRememberAccount);//将账号信息保存到非易失文件中
                        mainActivity.afterLoginSuccess();//执行登录成功后的切换操作
                        mainActivity.sendToastTextWouldBlock("账号和密码跟云端数据库上的一致！");
                        break;

                    case ClientReceiveString.LoginUserDoesNotExist:
                        mainActivity.afterLoginFailed();//关闭
                        mainActivity.sendToastTextWouldBlock("该用户不存在云端数据库！");
                        break;

                    case ClientReceiveString.LoginWrongPassword:
//                        LoginFragement.loginFragementInstance.releaseLoginButton();//只有创建视图的线程才能操作属于它的视图，所以该句会抛出致命异常
                        mainActivity.afterLoginFailed();//关闭
                        mainActivity.sendToastTextWouldBlock("密码错误！");

                        break;

                    case ClientReceiveString.LoginUseResultsetFailed:
                        mainActivity.afterLoginFailed();//关闭
                        mainActivity.sendToastTextWouldBlock("服务器结果集解析异常！");
                        break;

                    case ClientReceiveString.RegisterUsernameExist:
                        mainActivity.afterRegisterFailed();
                        mainActivity.sendToastTextWouldBlock("该用户已存在！");
                        break;

                    case ClientReceiveString.RegisterSuccess:
                        mainActivity.afterRegisterSuccess();
                        mainActivity.sendToastTextWouldBlock("注册成功！");
                        break;

                    case ClientReceiveString.RegisterFailed:
                        mainActivity.afterRegisterFailed();
                        mainActivity.sendToastTextWouldBlock("注册失败！");
                        break;

                    case ClientReceiveString.RegisterUseConnectionFailed:
                        mainActivity.afterRegisterFailed();
                        mainActivity.sendToastTextWouldBlock("服务器连接异常！");
                        break;

                    case ClientReceiveString.UnknownOperation:
                        mainActivity.stopRefresh();//停止刷新
                        mainActivity.sendToastTextWouldBlock("服务器发来了未知的操作指令！");
                        break;
                    case ClientReceiveString.NotesDownload:
                        username = sourceJsonObject.getString("username");
                        JSONArray notesArray = sourceJsonObject.getJSONArray("dataObject");
                        mainActivity.stopRefresh();//停止刷新
                        int n = Notes.updateUserTextNotes(mainActivity.getBaseContext(), notesArray, username);
                        ContentFragment.lastUpdateNum = n;
//                        MainActivity.mainActivityInstance.sendToastTextWouldBlock("同步记录成功，共同步记录"+n+"条");
                        break;
                    case ClientReceiveString.NoteAddSuccess:


                        break;
                    case ClientReceiveString.NoteAddFailed:


                        break;
                    default:
                        mainActivity.stopRefresh();//停止刷新
                        mainActivity.sendToastTextWouldBlock("服务器发来了未知的操作指令！\n" + operationStr);
                        break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            MainActivity mainActivity = MainActivity.getMainActivityInstance();
            mainActivity.sendToastTextWouldBlock("该输入的服务器回传的数据包并非规定的json格式的数据包!!!");
        }

    }

    public void resolveException(Exception e) {

    }

    public void loginSuccess() {

    }

}

