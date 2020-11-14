package com.example.mynotes.control;

import android.content.Context;
import android.widget.Toast;

import com.example.mynotes.other_activities.Detail;

import java.io.File;

public class FileIO {//该类用于进行文件的输入输出

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
}
