package ru.eyelog.cowdb;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class NewCowForm extends AppCompatActivity {

    private static final String TAG_OF_CLASS = "NewCowForm";

    TextView tv_Title, tv_Name;
    EditText et_name;
    Button btDate;

    private static Context context;
    Intent intent;
    int requestCode;

    DBConnector dbConnector;
    // Create - 0; Read all - 1; Read one - 2; Update - 3; Delete - 4;
    TaskFileDelete taskFileDelete;

    // url cow connector
    private static final String url_link = "http://90.156.139.108/ServerCowFarms/cows_connector.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_COWS = "cows";

    private static final  String TAG_ID_FARM = "id_farm";
    private static final String TAG_ID_HERD = "id_herd";
    private static final String TAG_NAME_HERD = "name_herd";
    private static final  String TAG_ID_COW = "id_cow";
    private static final  String TAG_NAME_COW = "name_cow";
    private static final  String TAG_BIRTHDAY_COW = "birthday_cow";
    private static final  String TAG_PHOTO_COW = "photo_cow";

    // Блок загрузки данных.
    private ProgressDialog pDialog;

    JSONObject json, delJson;
    JSONParser jParser, delJParser;
    ArrayList<NameValuePair> params;
    String stGotName, stEmpty;

    HashMap<String, String> hm;

    String stBirthday = null;
    String stCowAge;
    String stMainTitle;
    String st_id_farm, st_id_herd, st_id_cow, st_cowName, st_cow_birthday, st_cow_photo;

    int jsonSuccess;

    // Блок создания и загрузки фотографии.
    ImageView iv_MainPhoto;
    Intent takePictureIntent;

    static final int REQUEST_TAKE_PHOTO = 0;
    static final int REQUEST_TAKE_GALLERY_IMAGE = 1;

    Uri photoURI;
    URL url_image;
    boolean pictureGot = false;
    Bitmap bmp = null;
    boolean gotImage = false;

    public static String mCurrentPhotoPath;
    public static String imageFileName;
    public static File image;

    int serverResponseCode = 0;
    //ProgressDialog dialog = null;

    String upLoadServerUri = "http://90.156.139.108/ServerCowFarms/image_uploader.php";
    private static String uploadFilePath = "/mnt/sdcard/";
    private static String uploadFileName = "service_lifecycle.png";
    String gotLinkPhoto = "http://90.156.139.108/ServerCowFarms/photos/";

    // url photo remover
    private static final String url_photo_remove = "http://90.156.139.108/ServerCowFarms/image_delete.php";
    private static final  String TAG_PHOTO_DELETE = "deleteImage";

    AlertDialog.Builder createDialog;
    AlertDialog showDialog;
    LayoutInflater inflater;
    View alertView;
    Button btCancel, btSave;
    DatePicker datePicker;
    Calendar birthDate;
    Cow_age cow_age;

    CharSequence[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_cow);

        context = this;

        jParser = new JSONParser();
        delJParser = new JSONParser();

        iv_MainPhoto = findViewById(R.id.id_photo);
        tv_Title = findViewById(R.id.id_tv_title);
        tv_Name = findViewById(R.id.id_tv_name);
        et_name = findViewById(R.id.id_et_name);
        datePicker = findViewById(R.id.id_datepicker);
        btDate = findViewById(R.id.button_date);

        intent = getIntent();
        requestCode = intent.getIntExtra("requestCode", 1);
        if(requestCode==1){
            // Создание коровы
            st_id_herd = intent.getStringExtra(TAG_ID_HERD);
            st_id_farm = intent.getStringExtra(TAG_ID_FARM);
        }else{
            // Редактирование коровы.
            st_id_cow = intent.getStringExtra(TAG_ID_COW);
            st_id_herd = intent.getStringExtra(TAG_ID_HERD);
            st_id_farm = intent.getStringExtra(TAG_ID_FARM);


            hm = new HashMap<>();
            hm.put(TAG_ID_COW, st_id_cow);

            dbConnector = new DBConnector();
            dbConnector.doDBActive(2, hm);
            dbConnector.execute();

        }

        stEmpty = getString(R.string.emptyLine);
        if(requestCode==1){
            stMainTitle = getString(R.string.create_cow);
        }else {
            stMainTitle = getString(R.string.update_cow);
        }
        tv_Title.setText(stMainTitle);

        iv_MainPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder builder = new AlertDialog.Builder(NewCowForm.this);
                builder.setTitle(getString(R.string.cnx_get_pick));
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (items[item].equals(items[0])) {
                            takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                // Create the File where the photo should go
                                File photoFile = null;
                                try {
                                    photoFile = createImageFile();
                                } catch (IOException ex) {
                                    // Error occurred while creating the File
                                    Log.e("Got exception", ex.toString());
                                }
                                // Continue only if the File was successfully created
                                if (photoFile != null) {
                                    photoURI = FileProvider.getUriForFile(NewCowForm.this,
                                            "ru.eyelog.cowdb.fileprovider",
                                            photoFile);
                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                                }
                            }

                        } else if (items[item].equals(items[1])) {
                            takePictureIntent = new Intent();
                            takePictureIntent.setType("image/*");
                            takePictureIntent.setAction(Intent.ACTION_GET_CONTENT);//
                            startActivityForResult(takePictureIntent, REQUEST_TAKE_GALLERY_IMAGE);

                        } else if (items[item].equals(items[2])) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });

        items = new CharSequence[]{getString(R.string.cnx_get_pick_cam), getString(R.string.cnx_get_pick_gal),
                getString(R.string.cancel)};
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.e("Got Result", REQUEST_TAKE_PHOTO + "Got" + RESULT_OK);

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            bmp = BitmapFactory.decodeFile(mCurrentPhotoPath);
            int bWidth = bmp.getWidth();
            int bHight = bmp.getHeight();
            Bitmap out = Bitmap.createScaledBitmap(bmp, bWidth/5, bHight/5, false);

            /*
            Bitmap cutBitmap = Bitmap.createBitmap(out.getWidth() / 2,
                    out.getHeight() / 2, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(cutBitmap);
            Rect desRect = new Rect(0, out.getWidth(), 0, out.getWidth());
            Rect srcRect = new Rect(out.getWidth() / 2, 0, out.getWidth(),
                    out.getHeight() / 2);
            canvas.drawBitmap(out, srcRect, desRect, null);
            */

            FileOutputStream fOut;
            try {
                fOut = new FileOutputStream(image);
                out.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.flush();
                fOut.close();
                bmp.recycle();
                out.recycle();
            } catch (Exception e) {
                Log.e("Got exception", e.toString());
            }

            pictureGot = true;

            iv_MainPhoto.setImageURI(photoURI);

        }else if (requestCode == REQUEST_TAKE_GALLERY_IMAGE && resultCode == RESULT_OK) {

            photoURI = data.getData();

            try {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), photoURI);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                image = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileOutputStream out = null;
            try {
                out = new FileOutputStream(image);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            pictureGot = true;

            iv_MainPhoto.setImageBitmap(bmp);
        }

        Log.e("photoURI", photoURI.toString());
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        imageFileName = "farmID_" + st_id_farm + "_herdID_" + st_id_herd + "_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        uploadFilePath = storageDir.getPath();
        uploadFileName = image.getName();

        Log.e("LookImageFile: ", uploadFileName);


        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void onDatePicker(View view){

        cow_age = new Cow_age(context);

        createDialog = new AlertDialog.Builder(context);
        // Подгружаем форму всплывающего окна и инициируем его компоненты.
        inflater = getLayoutInflater();
        alertView = inflater.inflate(R.layout.form_date_picker, null);

        datePicker = alertView.findViewById(R.id.id_datepicker);
        btSave = alertView.findViewById(R.id.id_btn_save);
        btCancel = alertView.findViewById(R.id.id_btn_cancel);

        createDialog.setView(alertView);
        createDialog.setCancelable(false);

        // Добавляем кнопки.
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                birthDate = Calendar.getInstance();
                birthDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

                stBirthday = datePicker.getYear() + "-" + datePicker.getMonth() + "-" + datePicker.getDayOfMonth();

                stCowAge = stBirthday + ", " + cow_age.countAge(birthDate);

                btDate.setText(stCowAge);

                showDialog.cancel();
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog.cancel();
            }
        });

        // И последние штрихи, которые говорят сами за себя.
        showDialog = createDialog.create();
        showDialog.show();

    }

    public void onSaveCow(View view){

        stGotName = et_name.getText().toString();

        if (stGotName.equals("")) {
            et_name.setError(stEmpty);
            Toast.makeText(context, stEmpty, Toast.LENGTH_SHORT).show();
            //etCreateChar.setError(stEmpty);
        }else {

            if(pictureGot){
                // Запускаем процесс загрузки файла на сервер.
                //dialog = ProgressDialog.show(context, "", "Uploading file...", true);

                new Thread(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                //Toast.makeText(context, "uploading started.....", Toast.LENGTH_SHORT).show();
                            }
                        });

                        uploadFilePath = photoURI.toString();
                        uploadFileName = photoURI.getLastPathSegment();

                        uploadFile(mCurrentPhotoPath);

                    }
                }).start();

                gotLinkPhoto += uploadFileName;

                // Если идёт обновление, старый файл удаляем.
                if(requestCode==2){
                    taskFileDelete = new TaskFileDelete();
                    taskFileDelete.doDelete(st_cow_photo);
                    taskFileDelete.execute();
                }


            }else{
                // Значение по умолчанию для фото.
                if(gotImage){
                    // Если мы уже имеем картинку, просто пересохраняем её путь.
                    gotLinkPhoto = st_cow_photo;
                }else {
                    // Если картинки не было, то и фиг с ней.
                    gotLinkPhoto = "nophoto";
                }
            }

            // Значение по умолчанию для дня рождения коровы - текущая дата
            if(stBirthday==null){
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                stBirthday = simpleDateFormat.format(new Date());
            }

            hm = new HashMap<>();
            hm.put(TAG_ID_COW, st_id_cow);
            hm.put(TAG_ID_HERD, st_id_herd);
            hm.put(TAG_NAME_COW, stGotName);
            hm.put(TAG_BIRTHDAY_COW, stBirthday);
            hm.put(TAG_PHOTO_COW, gotLinkPhoto);

            Log.e("HashMapSend", hm.toString());

            if(requestCode==1){
                dbConnector = new DBConnector();
                dbConnector.doDBActive(0, hm);
                dbConnector.execute();
            }else {
                dbConnector = new DBConnector();
                dbConnector.doDBActive(3, hm);
                dbConnector.execute();
            }
        }
    }

    public void onCancel(View view){
        finish();
    }

    public int uploadFile(String sourceFileUri) {


        String fileName = sourceFileUri;
        Log.e("uploadFile", fileName);

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {

            //dialog.dismiss();

            Log.e("uploadFile", "Source File not exist :"
                    +uploadFilePath + "/" + uploadFileName);

            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, "Source File not exist :"
                            +uploadFilePath + "/" + uploadFileName, Toast.LENGTH_SHORT).show();
                }
            });

            return 0;

        }
        else
        {
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){

                    runOnUiThread(new Runnable() {
                        public void run() {

                            String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
                                    +" http://www.androidexample.com/media/uploads/"
                                    +uploadFileName;

                            /*
                            Toast.makeText(context, "File Upload Complete." + msg,
                                    Toast.LENGTH_SHORT).show();
                            */
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                //dialog.dismiss();
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, "MalformedURLException, " + "MalformedURLException Exception : check script url.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                //dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload Exception", "Exception : "
                        + e.getMessage(), e);
            }
            //dialog.dismiss();
            return serverResponseCode;

        } // End else block
    }

    /**
     * Фоновый Async Task для загрузки всех ферм по HTTP запросу
     * */
    class DBConnector extends AsyncTask<String, String, String> {

        private int mode;
        private String stMode;
        private String ST_RES_LOADING;

        HashMap<String, String> do_hm;

        private final static String TAG_REQUEST_MODE = "mode";

        private String MESSAGE_NO_COWS;

        DBConnector(){
            ST_RES_LOADING = getString(R.string.loading_cows);
            MESSAGE_NO_COWS = getString(R.string.no_cows);
        }

        public void doDBActive(int mode, HashMap<String, String> do_hm){
            this.do_hm = new HashMap<>();
            this.do_hm = do_hm;
            this.mode = mode;
            stMode = String.valueOf(mode);

            switch (mode){
                case 0: // Create a cow
                    ST_RES_LOADING = getString(R.string.creating_cow);
                    break;

                case 1: // Read cows list
                    ST_RES_LOADING = getString(R.string.loading_cows);
                    break;

                case 2: // Read a cow
                    ST_RES_LOADING = getString(R.string.loading_cow);
                    break;

                case 3: // Update a cow
                    ST_RES_LOADING = getString(R.string.updating_cow);
                    break;

                case 4: // Delete a cow
                    ST_RES_LOADING = getString(R.string.deleting_cow);
                    break;
            }
        }

        /**
         * Перед началом фонового потока Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(context);
            pDialog.setMessage(ST_RES_LOADING);
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * Получаем все продукт из url
         * */
        protected String doInBackground(String... args) {
            // Будет хранить параметры
            params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(TAG_REQUEST_MODE, stMode));
            params.add(new BasicNameValuePair(TAG_ID_COW, do_hm.get(TAG_ID_COW)));
            params.add(new BasicNameValuePair(TAG_ID_HERD, do_hm.get(TAG_ID_HERD)));
            params.add(new BasicNameValuePair(TAG_NAME_COW, do_hm.get(TAG_NAME_COW)));
            params.add(new BasicNameValuePair(TAG_BIRTHDAY_COW, do_hm.get(TAG_BIRTHDAY_COW)));
            params.add(new BasicNameValuePair(TAG_PHOTO_COW, do_hm.get(TAG_PHOTO_COW)));

            // получаем JSON строк с URL
            json = jParser.makeHttpRequest(url_link, params);

            Log.d(TAG_OF_CLASS, "Server answer: " + json.toString());

            try {
                // Получаем SUCCESS тег для проверки статуса ответа сервера
                jsonSuccess = json.getInt(TAG_SUCCESS);


            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * После завершения фоновой задачи закрываем прогрес диалог
         * **/
        protected void onPostExecute(String file_url) {
            // закрываем прогресс диалог после получение все продуктов
            pDialog.dismiss();
            // обновляем UI форму в фоновом потоке
            runOnUiThread(new Runnable() {
                public void run() {

                    if(mode==0||mode==3){
                        // После создания и редактирования коровы - возвращаемся в предыдущую активность
                        if (jsonSuccess == 1) {
                            // В любои случае обносляем список.
                            setResult(RESULT_OK);
                            finish();

                        } else {
                            // Коровы не найдены.
                            //Toast.makeText(context,"Something wrong", Toast.LENGTH_SHORT).show();
                        }
                    }else if(mode==2){
                        // Если мы получали одну корову - применяем её данные
                        // После создания и редактирования коровы - возвращаемся в предыдущую активность
                        if (jsonSuccess == 1) {

                            try {
                                st_id_cow = json.getString(TAG_ID_COW);
                                st_id_herd = json.getString(TAG_ID_HERD);
                                st_cowName = json.getString(TAG_NAME_COW);
                                stBirthday = json.getString(TAG_BIRTHDAY_COW);
                                st_cow_photo = json.getString(TAG_PHOTO_COW);

                                et_name.setText(st_cowName);

                                cow_age = new Cow_age(context);
                                stCowAge = stBirthday + ", " + cow_age.countAgeFromString(stBirthday);
                                btDate.setText(stCowAge);

                                try {
                                    url_image = new URL(st_cow_photo);
                                    bmp = BitmapFactory.decodeStream(url_image.openConnection().getInputStream());
                                    gotImage = true;
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                    gotImage = false;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    gotImage = false;
                                }finally {
                                    if(gotImage){
                                        iv_MainPhoto.setImageBitmap(bmp);
                                    }else{
                                        iv_MainPhoto.setImageResource(R.mipmap.korova);;
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        } else {
                            // Коровы не найдены.
                            //Toast.makeText(context,"Something wrong", Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            });
        }
    }

    class TaskFileDelete extends AsyncTask<String, String, String> {

        Uri uri;
        String localPath = "photos/";
        String fileName, filePath;

        TaskFileDelete(){

        }

        public void doDelete(String urlLink){
            uri = Uri.parse(urlLink);
            fileName = uri.getLastPathSegment();
            filePath = localPath+fileName;
        }

        /**
         * Перед началом фонового потока Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Получаем все продукт из url
         * */
        protected String doInBackground(String... args) {

            // Удаляем старый файл
            params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(TAG_PHOTO_DELETE, filePath));
            // получаем JSON строк с URL
            delJson = delJParser.makeHttpRequest(url_photo_remove, params);

            Log.d(TAG_OF_CLASS, "What we send: " + url_photo_remove + params);
            Log.d(TAG_OF_CLASS, "File remove: " + delJson.toString());

            return null;
        }

        /**
         * После завершения фоновой задачи закрываем прогрес диалог
         * **/
        protected void onPostExecute(String file_url) {

        }
    }
}