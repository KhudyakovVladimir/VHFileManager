package com.example.filemanager;

import java.io.File;
import java.util.ArrayList;

public class Model {
    static final String ROOT_PATH = "/";
    public static String mainFileName;
    static File file;
    static boolean flagFirstCreated = true;
    static File[] listOfFiles;
    static ArrayList<String> heap = new ArrayList<>();
    static int count = 0;

    public static File setFileName(String fileName){
        File file = new File(fileName);
        return file;
    }

    public static File[] listOfFiles(String fileName){
        File[] files = new File[]{};
        return files;
    }
}
