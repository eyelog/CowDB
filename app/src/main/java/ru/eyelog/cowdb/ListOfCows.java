package ru.eyelog.cowdb;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListOfCows extends AppCompatActivity {

    private static final String TAG_OF_CLASS = "ListOfCows";

    TextView tvMainTitle;
    ListView lv_cows;
    List<HashMap<String, String>> cowsList;
    ArrayList<String> herdsListID, herdsListName, cowsListID;
    HashMap<String, String> hm;

    int flipStep, limitOfFlips;

    private static Context context;
    Intent intent;

    DBConnector dbConnector;
    // Create - 0; Read all - 1; Read one - 2; Update - 3; Delete - 4;
    TaskFileDelete taskFileDelete;

    AlertDialog.Builder del_dialog;
    AlertDialog showDialog;
    LayoutInflater inflater;
    View alertView;
    TextView tvTitle;
    String stDelHerd;
    Button btCancel, btDelete;
    String stEmpty;

    // Блок контекстного меню.
    final int CNX_UPDATE = 100;
    final int CNX_DEL = 101;
    String stUpdate, stDel;

    // url получения списка всех продуктов
    private static final String url_link = "http://90.156.139.108/ServerCowFarms/cows_connector.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_COWS = "cows";

    private static final  String TAG_ID_FARM = "id_farm";
    private static final String TAG_NAME_FARM = "name_farm";
    private static final String TAG_ID_HERD = "id_herd";
    private static final String TAG_NAME_HERD = "name_herd";
    private static final  String TAG_ID_COW = "id_cow";
    private static final  String TAG_NAME_COW = "name_cow";
    private static final  String TAG_PHOTO_COW = "photo_cow";

    // Удалени фото с сервера.
    private static final String url_photo_remove = "http://90.156.139.108/ServerCowFarms/image_delete.php";
    private static final  String TAG_PHOTO_DELETE = "deleteImage";

    // Блок загрузки данных.
    private ProgressDialog pDialog;

    JSONObject json, subJson, delJson;
    JSONParser jParser, delJParser;
    JSONArray cows = null;
    ArrayList<NameValuePair> params;

    String stMainTitle;
    String st_id_farm, st_farmName, st_id_herd, st_herdName, st_id_cow, st_cowName, st_cow_photo;

    ListAdapter adapter;

    ViewFlipper flipper;

    Animation animFlipInForward;
    Animation animFlipOutForward;
    Animation animFlipInBackward;
    Animation animFlipOutBackward;

    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_cows);

        context = this;

        tvMainTitle = findViewById(R.id.id_tv_listcows);
        lv_cows = findViewById(R.id.id_lv_cows);
        registerForContextMenu(lv_cows);

        cowsList = new ArrayList<HashMap<String, String>>();
        jParser = new JSONParser();
        delJParser = new JSONParser();

        intent = getIntent();
        st_id_farm = intent.getStringExtra(TAG_ID_FARM);
        st_id_herd = intent.getStringExtra(TAG_ID_HERD);
        st_herdName = intent.getStringExtra(TAG_NAME_HERD);
        st_farmName = intent.getStringExtra(TAG_NAME_FARM);
        herdsListID = intent.getStringArrayListExtra("array");
        herdsListName = intent.getStringArrayListExtra("array2");

        if(herdsListID.size()>1){
            limitOfFlips = herdsListID.size()-1;
            flipStep = 0;
            for(int i=0; i<limitOfFlips+1; i++){
                if(herdsListID.get(i).equals(st_id_herd)){
                    break;
                }else {
                    flipStep++;
                }
            }
        }else {
            limitOfFlips = 0;
        }

        stEmpty = getString(R.string.emptyLine);
        stUpdate = getString(R.string.cnx_update_cow);
        stDel = getString(R.string.cnx_del_cow);

        ListPresenter(st_id_herd);

        flipper = findViewById(R.id.viewflipper);

        animFlipInForward = AnimationUtils.loadAnimation(this, R.anim.flipin);
        animFlipOutForward = AnimationUtils.loadAnimation(this, R.anim.flipout);
        animFlipInBackward = AnimationUtils.loadAnimation(this,
                R.anim.flipin_reverse);
        animFlipOutBackward = AnimationUtils.loadAnimation(this,
                R.anim.flipout_reverse);

    }

    public void ListPresenter(String id){

        // Загружаем продукты в фоновом потоке

        hm = new HashMap<>();
        hm.put(TAG_ID_HERD, id);
        hm.put(TAG_ID_COW, null);
        hm.put(TAG_NAME_COW, null);
        dbConnector = new DBConnector();
        dbConnector.doDBActive(1, hm);
        dbConnector.execute();

        stMainTitle = getString(R.string.list_of_cows);
        stMainTitle += " " + herdsListName.get(flipStep);
        tvMainTitle.setText(stMainTitle);

        // Переход на форму выделенной коровы.
        lv_cows.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if(cowsListID.get(0)==null){
                    onNewCow(view);
                }else {
                    // Получаем id фермы из списка.
                    st_id_cow = cowsList.get(position).get(TAG_ID_COW);

                    // Запускаем новый intent который покажет нам Activity
                    intent = new Intent(getApplicationContext(), ACowTableForm.class);

                    // отправляем id в следующий activity
                    intent.putExtra(TAG_ID_COW, st_id_cow);
                    intent.putExtra(TAG_NAME_FARM, st_farmName);
                    intent.putExtra(TAG_NAME_HERD, st_herdName);
                    intent.putStringArrayListExtra("array", cowsListID);

                    startActivity(intent);
                }
            }
        });

        lv_cows.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

    }

    // Перемотка влево
    private void SwipeLeft() {
        if(flipStep<limitOfFlips){
            flipper.setInAnimation(animFlipInBackward);
            flipper.setOutAnimation(animFlipOutBackward);
            flipper.showPrevious();

            flipStep++;

            ListPresenter(herdsListID.get(flipStep));
        }
    }

    // Перемотка вправо
    private void SwipeRight() {
        if(flipStep>0){
            flipper.setInAnimation(animFlipInForward);
            flipper.setOutAnimation(animFlipOutForward);
            flipper.showNext();

            flipStep--;
            ListPresenter(herdsListID.get(flipStep));
        }
    }

    // Обработка перемотки
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {

            Log.e("Flips: ", "Limit: " + limitOfFlips + ", step: " + flipStep);

            float sensitvity = 50;
            if ((e1.getX() - e2.getX()) > sensitvity) {
                SwipeLeft();
            } else if ((e2.getX() - e1.getX()) > sensitvity) {
                SwipeRight();
            }
            return true;
        }
    };

    GestureDetector gestureDetector = new GestureDetector(getBaseContext(),
            simpleOnGestureListener);

    // Создание контекстного меню
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        if(cowsListID.get(0)!=null){
            menu.add(Menu.NONE, CNX_UPDATE, Menu.NONE, stUpdate);
            menu.add(Menu.NONE, CNX_DEL, Menu.NONE, stDel);
        }
    }

    // Механизм обработки событий контекстного меню.
    @Override
    public boolean onContextItemSelected(MenuItem item){

        // Получаем номер позиции.
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        // Получаем id записи из списка.
        st_id_cow = cowsList.get(info.position).get(TAG_ID_COW);

        // Выбор действия для контекстного меню.
        switch (item.getItemId()){
            // Обновление данных коровы.
            case CNX_UPDATE:

                // Запускаем новый intent который покажет нам Activity
                intent = new Intent(context, NewCowForm.class);

                // отправляем id в следующий activity
                intent.putExtra(TAG_ID_COW, st_id_cow);
                intent.putExtra(TAG_ID_FARM, st_id_farm);
                intent.putExtra(TAG_ID_HERD, st_id_herd);
                intent.putExtra("requestCode", 2);

                startActivityForResult(intent, 2);


                break;

            // Удаление коровы.
            case CNX_DEL:
                // Удаляем корову на основании её ID.
                // Запрашиваем подтверждение удаления.
                del_dialog = new AlertDialog.Builder(context);

                // Подгружаем форму всплывающего окна и инициируем его компоненты.
                inflater = getLayoutInflater();
                alertView = inflater.inflate(R.layout.form_delete, null);

                tvTitle = alertView.findViewById(R.id.id_tv_mesg);

                btDelete = alertView.findViewById(R.id.id_btn_del);
                btCancel = alertView.findViewById(R.id.id_btn_cancel);

                stDelHerd = getString(R.string.del_cow) + cowsList.get(info.position).get(TAG_NAME_COW);
                tvTitle.setText(stDelHerd);

                del_dialog.setView(alertView);
                del_dialog.setCancelable(false);

                st_cow_photo = cowsList.get(info.position).get(TAG_PHOTO_COW);

                btDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        hm = new HashMap<>();
                        hm.put(TAG_ID_COW, st_id_cow);
                        hm.put(TAG_ID_HERD, null);
                        hm.put(TAG_NAME_HERD, null);

                        dbConnector = new DBConnector();
                        dbConnector.doDBActive(4, hm);
                        dbConnector.execute();

                        showDialog.cancel();
                    }
                });


                btCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog.cancel();
                    }
                });

                showDialog = del_dialog.create();
                showDialog.show();

                break;
        }
        return true;
    }

    public void onNewCow(View view){

        // Запускаем новый intent который покажет нам Activity
        intent = new Intent(context, NewCowForm.class);

        // отправляем id в следующий activity
        intent.putExtra(TAG_ID_HERD, st_id_herd);
        intent.putExtra(TAG_ID_FARM, st_id_farm);
        intent.putExtra("requestCode", 1);

        startActivityForResult(intent, 1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            hm = new HashMap<>();
            hm.put(TAG_ID_HERD, st_id_herd);
            hm.put(TAG_ID_COW, null);
            hm.put(TAG_NAME_COW, null);
            dbConnector = new DBConnector();
            dbConnector.doDBActive(1, hm);
            dbConnector.execute();
        }
    }

    /**
     * Фоновый Async Task для загрузки всех коров по HTTP запросу
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
                case 0: // Create a farm
                    ST_RES_LOADING = getString(R.string.creating_cow);
                    break;

                case 1: // Read farms list
                    ST_RES_LOADING = getString(R.string.loading_cows);
                    break;

                case 2: // Read a farm
                    ST_RES_LOADING = getString(R.string.loading_cow);
                    break;

                case 3: // Update a farm
                    ST_RES_LOADING = getString(R.string.updating_cow);
                    break;

                case 4: // Delete a farm
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

            // Ждем ответа сервера в течении 7-ми секунд.
            // И если его не последовало, прерываем загрузку.
            countDownTimer = new CountDownTimer(7000, 1000) {

                public void onTick(long millisUntilFinished) {
                    Log.e("Tik-tak","seconds remaining: " + millisUntilFinished / 1000);
                    // Do nothing
                }

                public void onFinish() {
                    dbConnector.cancel(true);
                }
            };
            countDownTimer.start();
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

            // получаем JSON строк с URL
            json = jParser.makeHttpRequest(url_link, params);

            Log.d(TAG_OF_CLASS, "Server answer: " + json.toString());

            try {
                // Получаем SUCCESS тег для проверки статуса ответа сервера
                int success = json.getInt(TAG_SUCCESS);

                cowsList = new ArrayList<>();
                cowsListID = new ArrayList<>();

                if (success == 1) {

                    // В любои случае обносляем список.
                    cows = json.getJSONArray(TAG_COWS);

                    // перебор всех продуктов
                    for (int i = 0; i < cows.length(); i++) {
                        subJson = cows.getJSONObject(i);

                        // Сохраняем каждый json элемент в переменную
                        st_id_cow = subJson.getString(TAG_ID_COW);
                        st_id_herd = subJson.getString(TAG_ID_HERD);
                        st_cowName = subJson.getString(TAG_NAME_COW);
                        st_cow_photo = subJson.getString(TAG_PHOTO_COW);

                        // Создаем новый HashMap
                        hm = new HashMap<String, String>();

                        // добавляем каждый елемент в HashMap ключ => значение

                        hm.put(TAG_ID_HERD, st_id_herd);
                        hm.put(TAG_ID_COW, st_id_cow);
                        hm.put(TAG_NAME_COW, st_cowName);
                        hm.put(TAG_PHOTO_COW, st_cow_photo);

                        // добавляем HashList в ArrayList
                        cowsList.add(hm);
                        cowsListID.add(st_id_cow);
                    }

                } else {
                    // Фермы не найдены.
                    // Выводим в список единственную строку: "коров нет"

                    cowsList = new ArrayList<>();
                    // Создаем новый HashMap
                    hm = new HashMap<String, String>();

                    // добавляем каждый елемент в HashMap ключ => значение
                    hm.put(TAG_ID_HERD, null);
                    hm.put(TAG_ID_COW, null);
                    hm.put(TAG_NAME_COW, MESSAGE_NO_COWS);

                    // добавляем HashList в ArrayList
                    cowsList.add(hm);
                    cowsListID.add(null);

                }
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
            countDownTimer.cancel();
            // обновляем UI форму в фоновом потоке
            runOnUiThread(new Runnable() {
                public void run() {

                    // В случае любого изменения списка перескачиваем его с базы.
                    if(mode!=1){
                        hm = new HashMap<>();
                        hm.put(TAG_ID_HERD, st_id_herd);
                        hm.put(TAG_ID_COW, null);
                        hm.put(TAG_NAME_COW, null);

                        dbConnector = new DBConnector();
                        dbConnector.doDBActive(1, hm);
                        dbConnector.execute();
                    }

                    if(mode==4){
                        taskFileDelete = new TaskFileDelete();
                        taskFileDelete.doDelete(st_cow_photo);
                        taskFileDelete.execute();
                    }

                    /**
                     * Обновляем распарсенные JSON данные в ListView
                     * */
                    adapter = new SimpleAdapter(
                            context, cowsList,
                            android.R.layout.simple_list_item_1, new String[] {TAG_NAME_COW},
                            new int[] {android.R.id.text1});
                    // обновляем listview
                    lv_cows.setAdapter(adapter);
                }
            });
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(context, "Lost net connection.", Toast.LENGTH_SHORT).show();
            finish();
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

            //Log.d(TAG_OF_CLASS, "What we send: " + url_photo_remove + params);
            //Log.d(TAG_OF_CLASS, "File remove: " + delJson.toString());

            return null;
        }

        /**
         * После завершения фоновой задачи закрываем прогрес диалог
         * **/
        protected void onPostExecute(String file_url) {

        }
    }
}