package com.example.mynotes.util;

import android.content.Context;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class FileUtil {//该类用于进行文件的输入输出

    public static void deleteBoth(Context context, String file_path){//该方法用于文件和文件夹的删除。context用于显示提示信息
        File delete_file=new File(file_path);

        if(delete_file.exists()) {//如果该文件存在，则判断是文件夹还是文件
            if (delete_file.isDirectory()){//如果是文件夹，则调用递归删除
                deleteDir(delete_file);
            }else {
                delete_file.delete();
            }
            Toast.makeText(context,delete_file.getAbsolutePath()+"删除成功！",Toast.LENGTH_SHORT ).show();
        }else {
            Toast.makeText(context,delete_file.getAbsolutePath()+"不存在！",Toast.LENGTH_SHORT ).show();
        }

    }


    public static void deleteDir(File dir) {//该方法可以递归删除文件夹里的所有内容和其本身
        if(dir.exists()) {
            File files[]=dir.listFiles();
            for(File fileChild:files) {
                if(fileChild.isDirectory()) {
                    deleteDir(fileChild);
                }else {
                    fileChild.delete();
                }
            }
            dir.delete();
        }
    }

    public static void makeDir(String dir_path) {//该方法可以较安全地创建文件夹
        File dir=new File(dir_path);

        if(!dir.exists()){
            System.out.print(dir_path+"不存在!");
            try{
                System.out.print("现在立即创建该目录!");
                dir.mkdirs();
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                System.out.println((dir.exists()?"创建该目录成功!":"创建该目录失败!"));
            }
        }else {
            System.out.print(dir_path+"已存在!");
        }
    }

    /**
     * 该方法用于将文件转换成Byte数组
     */
    public static byte[] getBytesByFile(String pathStr) {
        File file = new File(pathStr);
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            byte[] data = bos.toByteArray();
            bos.close();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从一个byte[]数组中截取一部分
     * @param src
     * @param begin
     * @param step
     * @return
     */
    public static byte[] subBytes(byte[] src, int begin, int step) {
        byte[] bs = new byte[step];
        for (int i=begin;i<begin+step; i++)
            bs[i-begin] = src[i];
        return bs;
    }

}
