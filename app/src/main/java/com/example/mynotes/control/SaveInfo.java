package com.example.mynotes.control;

import android.content.Context;
import android.util.Log;

import com.example.mynotes.model.Account;
import com.google.android.material.tabs.TabLayout;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class SaveInfo {
    public static String xmlFilePath = "accounts.xml";

    public static boolean SaveInformation(Context context, String username, String password, String password2, String mail) {
        try {
            FileOutputStream fos = context.openFileOutput("AccountData.txt", Context.MODE_APPEND);
//            BufferedWriter bw = new BufferedWriter(fos);

            System.out.println("名字是:" + username + "，密码是:" + password);
            Log.d("MainActivity", "名字是:" + username + "，密码是:" + password);
            fos.write(("用户名:" + username + "密码:" + password + "邮箱:" + mail + "\n").getBytes());
//            fos.newLine();//换行
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static Map<String, Account> getSaveInformation(Context context) {
        try {
            FileInputStream fis = context.openFileInput("AccountData.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
//            FileReader fr = new FileReader(fis);
//            BufferedReader bf = new BufferedReader(fr);
            String str;
            Map<String, Account> map = new HashMap<String, Account>();
            while ((str = br.readLine()) != null) {
                String[] infos = str.split("用户名:|密码:|邮箱:");

                Account account = new Account();//创建一个账户类
                account.setUsername(infos[1]);
                account.setPassword(infos[2]);
                account.setEmail(infos[3]);

                map.put(infos[1], account);//把信息中的第一位(用户名)，作为键
            }
//            fis.close();
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<String, Account>();
        }
    }


    /**
     * 该方法可获取xml文件中的account对象数组
     */
    public static Map<String, Account> getAccountsMap(Context context) {
        Element rootElement;
        try {
            SAXReader saxReader = new SAXReader();
//            InputStream inputStream = context.openFileInput(xmlFilePath);
            InputStream inputStream = context.openFileInput(xmlFilePath);
            File xmlFile = context.getFileStreamPath(xmlFilePath);
//            Document document = saxReader.read(inputStream);
            Document document = saxReader.read(inputStream);
            rootElement = document.getRootElement();

            Log.d("MainActivity", "rootElement is " + rootElement.asXML());

            List<Element> booksElement = rootElement.elements("account");

            Map<String, Account> map = new HashMap<String, Account>();

            for (Element bookElement : booksElement) {
                String usernameString = bookElement.elementText("username");
                String passwordString = bookElement.elementText("password");
                String emailString = bookElement.elementText("email");

                Account account = new Account(usernameString, passwordString, emailString);
                map.put(usernameString, account);//将账户加入hashmap中
                System.out.println(account);
            }

            return map;

        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d("error:", Log.getStackTraceString(e));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 该方法可将账户对象添加到xml文件中，该方法较为安全
     */
    public static void accountsAddToXml(Context context, List<Account> accountsList) {
        File xmlFile = context.getFileStreamPath(xmlFilePath);
        Document document = null;
        // 自定义xml样式
        OutputFormat format = new OutputFormat();
        format.setIndentSize(4); // 行缩进
        format.setNewlines(true); // 一个结点为一行
        format.setTrimText(true); // 去重空格
        format.setPadText(true);
//		format.setNewLineAfterDeclaration(false); // 放置xml文件中第二行为空白行

        try {

            if (!xmlFile.exists()) {// 若该xml文件不存在则创建，并初始化xml文件
                xmlFile.getParentFile().mkdirs();
                xmlFile.createNewFile();

                document = DocumentHelper.createDocument();
                Element rootElement = document.addElement("accounts");

//                FileOutputStream fos = context.openFileOutput("AccountData.txt", Context.MODE_PRIVATE);
//                FileOutputStream outputStream = new FileOutputStream(xmlFile);
                FileOutputStream outputStream = context.openFileOutput(xmlFilePath, Context.MODE_PRIVATE);
//                FileOutputStream outputStream = context.getAssets().;
                XMLWriter xmlWriter = new XMLWriter(outputStream, format);
                xmlWriter.write(document);// 输出xml文件
                outputStream.close();//关闭输入流
            }

            SAXReader saxReader = new SAXReader();
//            document = saxReader.read(xmlFilePath);
            InputStream inputStream = context.openFileInput(xmlFilePath);
            document = saxReader.read(inputStream);

            Element rootElement = document.getRootElement();
            List<Element> elementList = rootElement.elements("account");//从根元素中获取所有的元素

//			System.out.println(rootElement.asXML());

            for (Account account : accountsList) {

                Boolean stopFlag = false;
                for (Element element : elementList) {// 在添加书本内容前必须做检查
                    if (account.getUsername().equals(element.elementText("username"))) {
                        element.element("password").setText(account.getPassword());
                        element.element("email").setText(account.getEmail());
                        stopFlag = true;//该信号用于停止直接把账号加入的操作
                        break;
                    }
                }
                if (stopFlag) // 如果停止信号为真，则进行下一次
                    continue;

                Element bookElement = rootElement.addElement("account");
                bookElement.addElement("username").addText(account.getUsername());
                bookElement.addElement("password").addText(account.getPassword());
                bookElement.addElement("email").addText(account.getEmail());
            }

            FileOutputStream outputStream = context.openFileOutput(xmlFilePath, Context.MODE_PRIVATE);
            XMLWriter xmlWriter = new XMLWriter(outputStream, format);
            xmlWriter.write(document);// 输出xml文件

//            outputStream.write(document.asXML().getBytes());//将document转换成字符串再输入进文件

            outputStream.close();//关闭输入流
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}
