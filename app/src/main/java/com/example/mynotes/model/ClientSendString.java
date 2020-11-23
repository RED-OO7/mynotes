package com.example.mynotes.model;

/**
 * 该类为客户端发送的字符串的枚举类
 */
public class ClientSendString {
	public static final String CheckNetwork="CheckNetwork";//(检查是否有网络)

	public static final String NotesUpload="NotesUpload";//(客户上传记事到服务器的操作)

	public static final String NoteAdd="NoteAdd"; //(客户添加一条需同步请求）

	public static final String LoginAccount="LoginAccount";//(登录账号)

	public static final String RegisterAccount="RegisterAccount";//(注册账号)
	public static final String ServerAddText="ServerAddText";//(服务器添加一条记录)
	public static final String ServerUpdateText="ServerUpdateText";//(服务器更新某一条记录)
	public static final String ServerDeleteText="ServerDeleteText";//(服务器删除某一条记录，用于登录账户时删除记录用)
}
