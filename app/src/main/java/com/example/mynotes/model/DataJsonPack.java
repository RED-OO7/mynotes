package com.example.mynotes.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * 1.每次通信时，客户端只能发送一个数据包，且该数据包必须是自制的特定格式
 * 该特定格式为 [[[ (json数据格式包) ]]] 。三个"["号在前，三个"]"号在后
 * <p>
 * 2.客户端给服务器发送完数据包之后，服务器一定要回传一个数据包来通知客户端，
 * 无论该回传数据包是否有作用，客户端也必须接收该数据包
 */
public class DataJsonPack {// 该类为封装了类对象数据的json格式传输封装类，传输时会传输该类的对象的json格式的数据
    // 最高级抽象类，可以代表所有数据
    // 包括Account类，和Notes类List对象
    private Object dataObject = null;
    private String operation;// 需要操作的数据
    private String username;//发送数据包的用户的用户名

    public static final String CLASSNAME = "DataJsonPack";//这个为该类名

//    public static final String SyncNotesUpload = "SyncNotesUpload";// SyncNotesUpload(本账号记事全部上传操作)
//    public static final String SyncNotesDownload = "SyncNotesDownload";// SyncNotesDownload(本账号记事全部下载操作)
//    public static final String RegisterAccount = "RegisterAccount";// RegisterAccount(注册账号)
//    public static final String LoginAccount = "LoginAccount";// LoginAccount(登录账号)
//
//    public static final String LoginUseResultsetFailed = "LoginUseResultsetFailed";// LoginUseResultsetFailed(使用Result错误)
//    public static final String LoginUserDoesNotExist = "LoginUserDoesNotExist";// LoginUserDoesNotExist(用户不存在)
//    public static final String LoginWrongPassword = "LoginWrongPassword";// LoginWrongPassword(登录密码错误)
//    public static final String LoginSuccess = "LoginSuccess";// LoginSuccess(登录成功)

    public DataJsonPack() {
        super();
    }

    public DataJsonPack(Object dataObject, String operation) {
        super();
        this.dataObject = dataObject;
        this.operation = operation;
    }

    public Object getDataObject() {
        return dataObject;
    }

    public void setDataObject(Object dataObject) {
        this.dataObject = dataObject;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 该重写方法已可将DataJsonPack类直接输出为json格式的字符串
     */
    @Override
    public String toString() {
        Object sendDataObject = null;//这个是要传入dataJsonObject的dataObject

        JSONObject dataJsonPackObject = new JSONObject();

        try {
            if (dataObject instanceof Account) {// 如果dataObject是账户类的对象的话，dataStr赋值为Account的json字符串
                Account account = (Account) dataObject;

                JSONObject accountJsonObject = new JSONObject();

                accountJsonObject.put("CLASSNAME", Account.CLASSNAME);
                accountJsonObject.put("username", account.getUsername());
                accountJsonObject.put("password", account.getPassword());
                accountJsonObject.put("email", account.getEmail());

                sendDataObject = accountJsonObject;
            }

            if(dataObject instanceof JSONArray){
                sendDataObject = (JSONArray) dataObject;
            }

            dataJsonPackObject.put("CLASSNAME", CLASSNAME);
            dataJsonPackObject.put("dataObject", sendDataObject);
            dataJsonPackObject.put("operation", operation);
            dataJsonPackObject.put("username", username);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String finalStr2 = dataJsonPackObject.toString();

        return finalStr2;
    }

    /**
     * 该函数能根据Notes的列表而返回一个携带Notes信息的简单JSONArray字符串
     */
    public static String simpleJSONArrayString(List<Notes> notesList) {
        String arrayString = "[";

        for (int i = 0; i < notesList.size(); i++) {
            arrayString = arrayString + notesList.get(i).toString();
            if (i<(notesList.size()-1)) {
                arrayString = arrayString+",";
            }
        }
        arrayString = arrayString + "]";

        return arrayString;
    }


    /**
     * 该函数能根据Notes的列表参数而返回一个携带Notes信息的JSONArray
     * @param notesList 是note的列表
     */
    public static JSONArray notesListToJSONArray(List<Notes> notesList) {
//        JSONArray listJsonArray = new JSONArray(notesList);

        StringBuffer arrayStringBuffer = new StringBuffer("[");

        for (int i = 0; i < notesList.size(); i++) {
            arrayStringBuffer.append( notesList.get(i).toString());
            if (i<(notesList.size()-1)) {
                arrayStringBuffer.append( ",");
            }
        }
        arrayStringBuffer.append("]");

        JSONArray listJsonArray = null;
        try {
            listJsonArray = new JSONArray(arrayStringBuffer.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return listJsonArray;
    }


}
