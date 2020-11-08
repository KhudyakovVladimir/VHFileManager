package com.example.filemanager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.*;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;

public class ViewV extends AppCompatActivity {
    static TextView textView;
    static ListView listView;
    static ListItemAdapter listItemAdapter;
    LinearLayout linearLayout;
    EditText editText;
    EditText editText2;
    Button buttonDialog;
    Dialog dialog;
    final int DIALOG_RENAME = 1;
    final int DIALOG_CREATE_NEW_FOLDER = 2;
    private static final int REQUEST_PERMISSION_WRITE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewv);
        System.out.println("ON_CREATE " + Model.mainFileName);

        textView = findViewById(R.id.textView);
        listView = findViewById(R.id.listView);

        textView.setText(Model.mainFileName);
        checkPermissions();

        if(checkPermissions()){
            Model.ROOT_PATH = "/sdcard";
        }

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            System.out.println("SD-карта не доступна: " + Environment.getExternalStorageState());
        }else {
            File sdPath = Environment.getExternalStorageDirectory();
        }

        if (Model.flagFirstCreated){
            //получаем корневую папку и сортируем список
            //Model.listOfFiles = Model.setFileName(Model.ROOT_PATH).listFiles();
            File[] strings = Model.setFileName(Model.ROOT_PATH).listFiles();
            assert strings != null;
            Model.listOfFiles = new ArrayList<>();
            Model.listOfFiles.addAll(Arrays.asList(strings));

            assert Model.listOfFiles != null;
            Arrays.sort(new ArrayList[]{Model.listOfFiles});

            //передаём его адаптеру и добавляем в список
            listItemAdapter = new ListItemAdapter(this,this, R.layout.list_item, Model.listOfFiles);
            listView.setAdapter(listItemAdapter);

            Model.heap.add(Model.ROOT_PATH);
            Model.flagFirstCreated = false;

            //Model.mainFileName = "/sdcard/HfileSize/folder/_DSC0079.jpg";
            //openFile("/sdcard/HfileSize/folder/_DSC0079.jpg");
        }

        else {
            //Model.listOfFiles = Model.setFileName(Model.mainFileName).listFiles();
            File[] strings = Model.setFileName(Model.ROOT_PATH).listFiles();
            assert strings != null;
            Model.listOfFiles.addAll(Arrays.asList(strings));

            if(Model.listOfFiles != null) {
                Arrays.sort(new ArrayList[]{Model.listOfFiles});
                listItemAdapter = new ListItemAdapter(this,this, R.layout.list_item, Model.listOfFiles);
                listView.setAdapter(listItemAdapter);
            }else {
                Toast.makeText(this, "No files here.", Toast.LENGTH_SHORT).show();
            }
        }

        if(Model.mainFileName != null){
            File file = new File(Model.mainFileName);
            if(file.isFile()){
                openFile(file.toString());
            }
        }

        //System.out.println("MODEL_listOfFiles_onCreate = " + Arrays.toString(Model.listOfFiles));

        final Handler mHandler = new Handler();
        ViewV.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //listItemAdapter = new ListItemAdapter(this, R.layout.list_item, Model.listOfFiles);
                //updatedData(Model.listOfFiles);
                listItemAdapter.notifyDataSetChanged();
                //System.out.println("MODEL_listOfFiles_runOnUiThread = " + Arrays.toString(Model.listOfFiles));
                //System.out.println("listItemAdapter.listOfFiles = " + Arrays.toString(listItemAdapter.listOfFiles));
                mHandler.postDelayed(this, 1000);
                }
        });
    }

    @Override
    public void onBackPressed() {
        if(Model.heap.size() > 1){
            Model.count--;
            Model.heap.remove(Model.heap.size() - 1);
            int position = Model.heap.size();
            Model.mainFileName = Model.heap.get(position - 1);
            //openDirectory(Model.mainFileName);
            ListItemAdapter.listUpdate();
            System.out.println("onBackPressed : " + Model.count + " " + Model.heap.toString());
        }
    }


    //метод определяет MIME-Type и возвращает его в String
    public static String getMimeType(String url) {
        String extension = url.substring(url.lastIndexOf("."));
        String mimeTypeMap = MimeTypeMap.getFileExtensionFromUrl(extension);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(mimeTypeMap);
        System.out.println("MIME-TYPE is : " + mimeType);
        return mimeType;
    }

    private boolean checkPermissions(){

        if(!isExternalStorageReadable() || !isExternalStorageWriteable()){
            Toast.makeText(this, "Внешнее хранилище не доступно", Toast.LENGTH_LONG).show();
            return false;
        }
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE);
            return false;
        }
        return true;
    }

    // проверяем, доступно ли внешнее хранилище для чтения и записи
    public boolean isExternalStorageWriteable(){
        String state = Environment.getExternalStorageState();
        return  Environment.MEDIA_MOUNTED.equals(state);
    }
    // проверяем, доступно ли внешнее хранилище хотя бы только для чтения
    public boolean isExternalStorageReadable(){
        String state = Environment.getExternalStorageState();
        return  (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }

    void openDirectory(String folderName){
        System.out.println("OPEN_DIRECTORY " + Model.heap.toString());
        //recreate();
        ListItemAdapter.listUpdate();
    }

    public void openFile(String localUri){
        System.out.println("OPEN_FILE");
        File file = new File(localUri);
        System.out.println("FILE_LOCAL_URI = " + localUri);
        //Uri contentUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
        //next Line is work fine
        //Uri contentUri = Uri.parse("content://com.example.filemanager.provider/root/storage/emulated/0/" + parseFolderPath(Model.mainFileName));
        Uri contentUri = Uri.parse("content://com.example.filemanager.provider/root/storage/emulated/0/" + parseUri(Model.mainFileName));
        System.out.println("OPEN_FILE : contentUri = " + contentUri);
        Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
        openFileIntent.setDataAndTypeAndNormalize(contentUri, getMimeType(Model.mainFileName));
        openFileIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(openFileIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        switch (Model.codeForMenu) {
            case 0 : {
                getMenuInflater().inflate(R.menu.action_bar_menu, menu);
                break;
            }
            case 1 : {
                getMenuInflater().inflate(R.menu.action_bar_menu_paste, menu);
                break;
            }
            case 2 : {
                getMenuInflater().inflate(R.menu.action_menu_remove_paste, menu);
                break;
            }
        }
        return true;
    }
    //обработчик нажатия на пунк actionBar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.copy : {
                Model.codeForMenu = 1;
                String openDirectoryPath = Model.heap.get(Model.heap.size() - 1);
                Model.copyFile = new File(Model.selectedFile);
                System.out.println("COPYFILE === " + Model.copyFile.toString());
                Model.mainFileName = openDirectoryPath;
                openDirectory(openDirectoryPath);
                invalidateOptionsMenu();
                break;
            }
            case R.id.delete : {
                String openDirectoryPath = Model.heap.toString();
                delete(Model.selectedFile);
                System.out.println("DELETE_MODEL_selectedFile = " + Model.selectedFile);
                openDirectory(openDirectoryPath);
                invalidateOptionsMenu();
                break;
            }
            case R.id.remove : {
                Model.codeForMenu = 2;
                System.out.println("REMOVE");
                Model.replaceFile = new File(Model.selectedFile);
                String openDirectoryPath = Model.heap.toString();
                openDirectory(openDirectoryPath);
                invalidateOptionsMenu();
                break;
            }
            case R.id.rename : {
                System.out.println("RENAME");
                showDialog(DIALOG_RENAME);
                break;
            }
            case R.id.paste : {
                Model.codeForMenu = 0;
                System.out.println("PASTE");
                Model.pasteFile = new File(parseString(Model.selectedFile));
                System.out.println("PASTE === " + Model.pasteFile);
                copy(Model.copyFile, Model.pasteFile);
                Toast.makeText(this, "PASTE", Toast.LENGTH_SHORT).show();
                String openDirectoryPath = Model.heap.toString();
                openDirectory(openDirectoryPath);
                invalidateOptionsMenu();
                break;
            }
            case R.id.removePaste : {
                Model.codeForMenu = 0;
                System.out.println("REMOVE_PASTE");
                String openDirectoryPath = Model.heap.get(Model.heap.size() - 1);
                String fileDest = openDirectoryPath + "/" + parseFolderPath(Model.replaceFile.toString());
                Model.replaceFile.renameTo(new File(fileDest));
                Model.mainFileName = openDirectoryPath;
                openDirectory(openDirectoryPath);
                invalidateOptionsMenu();
                break;
            }
            case R.id.createNewFolder : {
                System.out.println("CREATE_NEW_FOLDER");
                showDialog(DIALOG_CREATE_NEW_FOLDER);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //реализовать логику методов
    void copy(File fileSource, File fileDestination){
        if(!fileDestination.exists()){
            try {
                fileDestination.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileChannel fileChannelSource = null;
        FileChannel fileChannelDestination = null;

        try {
            fileChannelSource = new FileInputStream(fileSource).getChannel();
            fileChannelDestination = new FileOutputStream(fileDestination).getChannel();
            fileChannelDestination.transferFrom(fileChannelSource, 0, fileChannelSource.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fileChannelSource != null){
                try {
                    fileChannelSource.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fileChannelDestination != null){
                try {
                    fileChannelDestination.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    void delete(String fileName){
        File file = new File(fileName);
        if(file.isFile()){
            file.delete();
        }
        if(file.isDirectory()){
            for (File c : file.listFiles()) {
                delete(String.valueOf(c));
            }
            file.delete();
        }

    }

    String parseString(String name){
        ArrayList<String> arrayList = new ArrayList<>();
        for (String str : name.split("\\.", 2)) {
            arrayList.add(str);
            System.out.println("STR === " + str);
        }
        String tmp = arrayList.get(0);
        ArrayList<String> arrayList2 = new ArrayList<>();
        for (String s : tmp.split("/")) {
            arrayList2.add(s);
        }
        System.out.println("OUT_FILE_NAME = " + Model.heap.get(Model.heap.size() -1) + "/" + arrayList2.get(arrayList2.size() - 1) + "(copy)." + arrayList.get(1));
        return Model.heap.get(Model.heap.size() -1) + "/" + arrayList2.get(arrayList2.size() - 1) + "(copy)." + arrayList.get(1);
    }


    String parseUri(String name){
        ArrayList<String> arrayList = new ArrayList<>();
        for (String str : name.split("\\.", 2)) {
            arrayList.add(str);
            System.out.println("STR === " + str);
        }
        String tmp = arrayList.get(0);
        ArrayList<String> arrayList2 = new ArrayList<>();
        for (String s : tmp.split("/")) {
            arrayList2.add(s);
            System.out.println("s === " + s);
        }
        arrayList2.remove(0);
        arrayList2.remove(0);
        System.out.println(arrayList2);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < arrayList2.size(); i++) {
            stringBuilder.append(arrayList2.get(i));
            stringBuilder.append("/");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        String result = stringBuilder.toString() + "." + arrayList.get(1);
        return result;
    }

    String parseFolderPath(String path){
        String str;
        ArrayList<String> arrayList = new ArrayList<>();
        for (String s : path.split("/")) {
            arrayList.add(s);
        }
        str = arrayList.get(arrayList.size() -1);
        System.out.println("PARSE_FOLDER_STR === " + str);
        return str;
    }

    String parseStringForRename(String name, String newFileName){
        //String text = "text";
        ArrayList<String> arrayList = new ArrayList<>();
        for (String str : name.split("\\.", 2)) {
            arrayList.add(str);
            System.out.println("STR === " + str);
        }
        String tmp = arrayList.get(0);
        System.out.println("tmp = " + parseFolderPath(tmp));
        ArrayList<String> arrayList2 = new ArrayList<>();
        for (String s : tmp.split("/")) {
            arrayList2.add(s);
        }
        System.out.println("OUT_FILE_NAME = " + Model.heap.get(Model.heap.size() -1) + "/" + newFileName +"." + arrayList.get(1));
        return newFileName +"." + arrayList.get(1);
    }

    String parseFileName(String name){
        String result = "";
        ArrayList<String> arrayList = new ArrayList<>();
        for (String str : name.split("\\.", 2)) {
            arrayList.add(str);
            System.out.println("STR === " + str);
        }
        String tmp = arrayList.get(0);
        result = parseFolderPath(tmp);
        System.out.println("result = " + parseFolderPath(tmp));
        return result;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        if(id == DIALOG_RENAME) {
            adb = new AlertDialog.Builder(this);
            adb.setTitle("New filename");
            linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog, null);
            adb.setView(linearLayout);
            editText = linearLayout.findViewById(R.id.editText);
            System.out.println("ON_CREATE_DIALOG : Model_selectedFile = " + Model.selectedFile);
            editText.setText(parseFileName(Model.selectedFile));
        }

        if(id == DIALOG_CREATE_NEW_FOLDER) {
            adb = new AlertDialog.Builder(this);
            adb.setTitle("New folder name");
            linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_2, null);
            adb.setView(linearLayout);
            editText2 = linearLayout.findViewById(R.id.editText2);
        }
        return adb.create();
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        if(id == DIALOG_RENAME){
            this.dialog = dialog;
            buttonDialog = dialog.getWindow().findViewById(R.id.buttonDialog);
            editText.setText(parseFileName(Model.selectedFile));
        }
        if(id == DIALOG_CREATE_NEW_FOLDER){
            this.dialog = dialog;
            buttonDialog = dialog.getWindow().findViewById(R.id.buttonDialog2);
        }
    }

    public void buttonDialogOnClick(View view) {
        System.out.println("BUTTON_DIALOG_ON_CLICK");
        Model.newFileName = editText.getText().toString();
        Toast.makeText(ViewV.this, "OK", Toast.LENGTH_SHORT).show();

        String openDirectoryPath = Model.heap.get(Model.heap.size() - 1);
        Model.replaceFile = new File(Model.selectedFile);
        String fileDest = openDirectoryPath + "/" + parseStringForRename(Model.replaceFile.toString(), Model.newFileName);
        Model.replaceFile.renameTo(new File(fileDest));
        Model.mainFileName = openDirectoryPath;

        dialog.dismiss();
        ListItemAdapter.listUpdate();
    }

    public void buttonDialogOnClick2(View view) {
        System.out.println("BUTTON_DIALOG_ON_CLICK_2");
        String openDirectoryPath = Model.heap.get(Model.heap.size() - 1);
        Model.newFolderName = editText2.getText().toString();
        File file = new File(openDirectoryPath + "/" + Model.newFolderName);
        file.mkdir();
        Model.mainFileName = openDirectoryPath;
        dialog.dismiss();
        editText2.setText("");
        ListItemAdapter.listUpdate();

        Toast.makeText(ViewV.this, "OK", Toast.LENGTH_SHORT).show();
    }

    public String fileSize(String pathName){
        File file = new File(pathName);
        double megaBytes = (double) file.length() / (1024 * 1024);
        String result = String.format("%.2f", megaBytes);
        return result;
    }
}
