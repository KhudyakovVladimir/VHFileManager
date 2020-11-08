package com.example.filemanager;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.util.ArrayList;

public class Model {
    static String ROOT_PATH = "/";
    //static final String ROOT_PATH_2 = "/sdcard";
    public static String mainFileName;
    static File copyFile;
    static File pasteFile;
    static String selectedFile;
    static String newFileName;
    static String newFolderName;
    static File replaceFile;
    static boolean flagFirstCreated = true;
    static int codeForMenu = 0;
    static ArrayList<File> listOfFiles;
    static ArrayList<String> heap = new ArrayList<>();
    static int count = 0;

    public static File setFileName(String fileName){
        File file = new File(fileName);
        return file;
    }
}
