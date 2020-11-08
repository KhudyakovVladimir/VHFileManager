package com.example.filemanager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.*;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import java.io.File;
import java.util.Arrays;
import androidx.core.content.FileProvider;

public class ViewV extends ListActivity implements AdapterView.OnItemLongClickListener {
    TextView textView;
    ListView listView;
    ArrayAdapter<File> arrayAdapter;
    String mainFileName;
    String folderName;
    private static final int REQUEST_PERMISSION_WRITE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewv);

        textView = findViewById(R.id.textView);
        listView = findViewById(android.R.id.list);

        textView.setText(Model.mainFileName);
        checkPermissions();

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            System.out.println("SD-карта не доступна: " + Environment.getExternalStorageState());
        }else {
            File sdPath = Environment.getExternalStorageDirectory();
        }

        if (Model.flagFirstCreated){
            //получаем корневую папку и сортируем список
            Model.listOfFiles = Model.setFileName(Model.ROOT_PATH).listFiles();
            //Model.listOfFiles = Model.setFileName(sdDir).listFiles();
            assert Model.listOfFiles != null;
            Arrays.sort(Model.listOfFiles);

            //передаём его адаптеру и добавляем в список
            arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Model.listOfFiles);
            Model.heap.add(Model.ROOT_PATH);
            //Model.heap.add(sdDir);
            Model.flagFirstCreated = false;
        }

        else {
            Model.listOfFiles = Model.setFileName(Model.mainFileName).listFiles();

            if(Model.listOfFiles != null) {
                Arrays.sort(Model.listOfFiles);
                arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Model.listOfFiles);
            }else {
                Toast.makeText(this, "No files here.", Toast.LENGTH_SHORT).show();
            }
        }

        listView.setAdapter(arrayAdapter);
    }

    @Override
    protected void onListItemClick(ListView l, android.view.View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        //получаем позицию списка, добавляем её в лист и выводим в textview
        //String folderName = l.getItemAtPosition(position).toString();
        folderName = l.getItemAtPosition(position).toString();
        Uri intentUri = Uri.parse(folderName);

        File file = new File(folderName);

        if(file.isDirectory()){

        }
        if(file.isFile()){
            //определяем мим тип и запускаем соответствующий экшн(не релизовано)
            //String stringForIntentSetType = getMimeType(folderName);

            //Intent intent = new Intent(Intent.ACTION_VIEW);
            //intent.setDataAndType(intentUri, "*/*");
            //Intent chosenIntent = Intent.createChooser(intent, "Please, choose the app");
            //startActivity(chosenIntent);
            openFile(folderName);
        }
        Model.mainFileName = folderName;

        Model.heap.add(folderName);
        Model.count++;
        System.out.println("onItemClick : " + Model.count + " " + Model.heap.toString());

        textView.setText(folderName);
        openFolder(mainFileName);
    }



    void openFolder(String folderName){
        recreate();
    }

    @Override
    public void onBackPressed() {
        if(Model.heap.size() > 1){
            Model.count--;
            Model.heap.remove(Model.heap.size() - 1);
            int position = Model.heap.size();
            Model.mainFileName = Model.heap.get(position - 1);
            openFolder(Model.mainFileName);
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

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        String itemName = (String) listView.getItemAtPosition(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберете действие");
        //builder.setPositiveButton("open",)
        return false;
    }

    void openFile(String localUri){
        //тут уже как хотите так и формируйте путь, хоть через Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + имя файла
        File file = new File(localUri);
        Uri contentUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
        Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
        //openFileIntent.setDataAndTypeAndNormalize(contentUri, "image/*");
        openFileIntent.setDataAndTypeAndNormalize(contentUri, getMimeType(folderName));
        openFileIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(openFileIntent);
    }
}
