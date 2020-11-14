package com.example.mynotes.model;

/**
 * 该类为客户端接收的字符串的枚举类
 */
public class ClientReceiveString {
	public static final String LoginUseResultsetFailed="LoginUseResultsetFailed";//(使用Result错误)
	public static final String LoginUserDoesNotExist="LoginUserDoesNotExist";//(用户不存在)
	public static final String LoginWrongPassword="LoginWrongPassword";//(登录密码错误)
	public static final String LoginSuccess="LoginSuccess";//(登录成功)

	public static final String RegisterUseConnectionFailed ="RegisterUseConnectionFailed";//(注册使用连接错误)
	public static final String RegisterSuccess="RegisterSuccess";//(注册成功)
	public static final String RegisterFailed="RegisterFailed";//(注册失败)
	public static final String RegisterUsernameExist="RegisterUsernameExist";//(注册用户名已存在)

	public static final String NotesDownload="NotesDownload";//(客户端接收记事的下载操作)

	public static final String UnknownOperation = "UnknownOperation";//(未知的接收操作)
}
