package com.example.filemanager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class ListItemAdapter extends ArrayAdapter<File> implements View.OnClickListener {
    ViewV viewV;
    LayoutInflater layoutInflater;
    ArrayList<File> listOfFiles;
    int layout;
    File fileForOnClick;
    String fileSize;

    public ListItemAdapter(ViewV viewV1,Context context, int resource, ArrayList<File> listOfFiles) {
        super(context, resource, listOfFiles);
        this.viewV = viewV1;
        context = getContext();
        this.layout = resource;
        this.listOfFiles = listOfFiles;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listOfFiles.size();
    }

    @Override
    public File getItem(int position) {
        return listOfFiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    File getFile(int position) {
        return (getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null){
            view = layoutInflater.inflate(R.layout.list_item, parent, false);
        }

        File file = getItem(position);
        assert file != null;

        fileForOnClick = file;

        ImageView imageView = view.findViewById(R.id.imageView);
        TextView textViewItem = view.findViewById(R.id.textViewItem);
        textViewItem.setOnClickListener(this);
        TextView textViewItem2 = view.findViewById(R.id.textViewItem2);
        CheckBox checkBox = view.findViewById(R.id.checkBoxItem);
        checkBox.setOnCheckedChangeListener(onCheckedChangeListener);

        if(file.isDirectory()){
            imageView.setImageResource(R.drawable.folder);
            //fileSize = getFileSizeMegaBytes(folderSize(file));
            //System.out.println("GET_VIEW : fileSize_isDirectory = " + fileSize);
        }
        if(file.isFile()){
            imageView.setImageResource(R.drawable.file);
            fileSize = fileSize(file.getPath());
            textViewItem2.setText(fileSize);
            //System.out.println("GET_VIEW : fileSize_isFile = " + fileSize);
        }

        textViewItem.setText(parseFolderPath(String.valueOf(file)));
        //textViewItem2.setText(fileSize);
        textViewItem.setTag(fileForOnClick.toString());

        checkBox.setTag(file.toString());

        return view;
    }

    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            System.out.println("CHECKBOX is checked " + buttonView.getTag());
            Model.selectedFile = (String) buttonView.getTag();
        }
    };

    @Override
    public void onClick(View v) {
        Model.mainFileName = (String) v.getTag();
        File file = new File(Model.mainFileName);

        if(file.isDirectory()){
            listUpdate();
        }
        if(file.isFile()){
            viewV.openFile(Model.mainFileName);
        }

        Model.heap.add(Model.mainFileName);
        Model.count++;

        ViewV.textView.setText(Model.mainFileName);
    }

    //вызывает активити из статического контекста
    public static void listUpdate() {
        Model.listOfFiles.clear();
        File[] strings = Model.setFileName(Model.mainFileName).listFiles();
        Model.listOfFiles.addAll(Arrays.asList(strings));
        Arrays.sort(new ArrayList[]{Model.listOfFiles});
    }

    private static String FileSizeMegaBytes(File file) {
        return (double) file.length()/(1024*1024)+" mb";
    }

    private static String getFileSizeMegaBytes(long size) {
        double megaBytes = (double) size / (1024 * 1024);
        String str = String.format("%.2f", megaBytes);
        String result = str + " mb";
        return result;
    }

        public String fileSize(String pathName){
        File file = new File(pathName);
        double megaBytes = (double) file.length() / (1024 * 1024);
        String str = String.format("%.2f", megaBytes);
        String result = str + " mb";
        return result;
    }

    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    String parseFolderPath(String path){
        String str;
        ArrayList<String> arrayList = new ArrayList<>();
        for (String s : path.split("/")) {
            arrayList.add(s);
        }
        str = arrayList.get(arrayList.size() -1);
        return str;
    }
}


