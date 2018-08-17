package ru.eyelog.cowdb;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ViewFlipper;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ACowTableForm extends AppCompatActivity {

    private static final String TAG_OF_CLASS = "ACowTableForm";

    // Блок основной информации.
    DBConnector dbConnector;
    // Create - 0; Read all - 1; Read one - 2; Update - 3; Delete - 4;
    ImageView iv_MainPhoto;
    TextView tv_main, tv_id, tv_herd, tv_farm;
    String stTitleID, stTitleHerd, stTitleFarm;

    ArrayList<String> cowsListID;
    int flipStep, limitOfFlips;

    // Блоки данных.
    ListView[] lv_values = new ListView[3];
    int[] resListViews = new int[]{R.id.id_lv_value, R.id.id_lv_weight, R.id.id_lv_temp};
    ArrayList<ArrayList<HashMap<String, String>>> listValues;
    ArrayList<HashMap<String, String>> sublistValues;
    HashMap<String, String> hm;
    SimpleDateFormat simpleDateFormat;
    DBConnector_values dbConnector_values;
    DataForm dataForm;

    Intent intent;
    Context context;

    // url получения списка всех продуктов
    private static final String url_link = "http://90.156.139.108/ServerCowFarms/cows_connector.php";

    private static String[] url_cow_values = new String[]{"http://90.156.139.108/ServerCowFarms/cows_values_connector.php",
            "http://90.156.139.108/ServerCowFarms/cows_weights_connector.php",
            "http://90.156.139.108/ServerCowFarms/cows_temps_connector.php"};

    private static final String TAG_SUCCESS = "success";

    private static final String TAG_NAME_FARM = "name_farm";
    private static final String TAG_ID_HERD = "id_herd";
    private static final String TAG_NAME_HERD = "name_herd";
    private static final  String TAG_ID_COW = "id_cow";
    private static final  String TAG_NAME_COW = "name_cow";
    private static final  String TAG_BIRTHDAY_COW = "birthday_cow";
    private static final  String TAG_PHOTO_COW = "photo_cow";

    private static final String[] TAG_VALUES = new String[]{"values", "weights", "temps"};

    private static final String[] TAG_ID_VALUES = new String[]{"id_value", "id_weight", "id_temp"};
    private static final String[] TAG_DATE_VALUES = new String[]{"date_cow", "date_cow", "date_cow"};
    private static final String[] TAG_VALUE_VALUES = new String[]{"value_cow", "weight_cow", "temp_cow"};

    // Блок загрузки данных.
    private ProgressDialog pDialog;

    JSONObject json, subJson;
    JSONParser jParser;

    ArrayList<NameValuePair> params;
    int success;
    JSONArray values_data = null;

    public static String stMainTitle;
    public static String st_farmName, st_id_herd, st_herdName, st_id_cow, st_cowName, st_cow_birthday, st_age, st_cow_photo;

    public static String st_value_id, st_value_date, st_value_value;

    Cow_age cow_age;

    public static URL url_image;

    AlertDialog.Builder createDialog;
    AlertDialog showDialog;
    LayoutInflater inflater;
    View alertView;
    TextView tvTitle, tvSubTitle;
    String stNewData, stDataDetails;
    EditText etCreateData;
    Button btCancel, btCreate;
    String stGotData, stEmpty;

    SimpleAdapter adapter;

    ViewFlipper flipper;

    Animation animFlipInForward;
    Animation animFlipOutForward;
    Animation animFlipInBackward;
    Animation animFlipOutBackward;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cow_tableform);

        iv_MainPhoto = findViewById(R.id.id_photo);
        tv_main = findViewById(R.id.id_tv_title_main);
        tv_id = findViewById(R.id.id_tv_title_id);
        tv_herd = findViewById(R.id.id_tv_title_herd);
        tv_farm = findViewById(R.id.id_tv_title_farm);
        for(int i=0; i<3; i++){
            lv_values[i] = findViewById(resListViews[i]);
            lv_values[i].setOnTouchListener(new View.OnTouchListener() {
                // Setting on Touch Listener for handling the touch inside ScrollView
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // Disallow the touch request for parent scroll on touch of child view
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
            lv_values[i].setOnItemLongClickListener(new ContextListMenu(i));
        }

        intent = getIntent();
        st_id_cow = intent.getStringExtra(TAG_ID_COW);
        st_farmName = intent.getStringExtra(TAG_NAME_FARM);
        st_herdName = intent.getStringExtra(TAG_NAME_HERD);
        cowsListID = intent.getStringArrayListExtra("array");

        if(cowsListID.size()>1){
            limitOfFlips = cowsListID.size()-1;
            flipStep = 0;
            for(int i=0; i<limitOfFlips+1; i++){
                if(cowsListID.get(i).equals(st_id_cow)){
                    break;
                }else {
                    flipStep++;
                }
            }
        }else {
            limitOfFlips = 0;
        }

        stTitleID = getString(R.string.title_id);
        stTitleHerd = getString(R.string.title_herd);
        stTitleFarm = getString(R.string.title_farm);

        context = this;
        jParser = new JSONParser();

        cow_age = new Cow_age(context);

        ListPresenter(st_id_cow);

        flipper = findViewById(R.id.viewflipper);

        animFlipInForward = AnimationUtils.loadAnimation(this, R.anim.flipin);
        animFlipOutForward = AnimationUtils.loadAnimation(this, R.anim.flipout);
        animFlipInBackward = AnimationUtils.loadAnimation(this,
                R.anim.flipin_reverse);
        animFlipOutBackward = AnimationUtils.loadAnimation(this,
                R.anim.flipout_reverse);
    }

    public void ListPresenter(String id){

        // Загружаем данные в фоновом потоке

        listValues = new ArrayList<>();
        hm = new HashMap<>();
        hm.put(TAG_ID_COW, id);
        dbConnector = new DBConnector();
        dbConnector.doDBActive(2, hm);
        dbConnector.execute();

    }

    private void SwipeLeft() {
        if(flipStep<limitOfFlips){
            flipper.setInAnimation(animFlipInBackward);
            flipper.setOutAnimation(animFlipOutBackward);
            flipper.showPrevious();

            flipStep++;

            ListPresenter(cowsListID.get(flipStep));
        }
    }

    private void SwipeRight() {
        if(flipStep>0){
            flipper.setInAnimation(animFlipInForward);
            flipper.setOutAnimation(animFlipOutForward);
            flipper.showNext();

            flipStep--;
            ListPresenter(cowsListID.get(flipStep));
        }
    }

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

    public void onNewValue(View view){
        dataForm = new DataForm(0, 0);
    }

    public void onNewWeight(View view){
        dataForm = new DataForm(1, 0);
    }

    public void onNewTemp(View view){
        dataForm = new DataForm(2, 0);
    }

    class ContextListMenu implements AdapterView.OnItemLongClickListener {

        String stUpdateTitle, stUpdateDelete;

        int section;
        PopupMenu popupMenu;
        DataForm dataForm;

        ContextListMenu(int section){
            this.section = section;
            stUpdateTitle = getString(R.string.cnx_update_value);
            stUpdateDelete = getString(R.string.cnx_del_value);
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

            popupMenu = new PopupMenu(ACowTableForm.this, view);
            popupMenu.getMenu().add(Menu.NONE, 3, 1, stUpdateTitle);
            popupMenu.getMenu().add(Menu.NONE, 4, 2, stUpdateDelete);
            popupMenu.show();

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    int i = item.getItemId();

                    hm = new HashMap<>();

                    st_value_id = listValues.get(section).get(position).get(TAG_ID_VALUES[section]);
                    st_value_date = listValues.get(section).get(position).get(TAG_DATE_VALUES[section]);
                    st_value_value = listValues.get(section).get(position).get(TAG_VALUE_VALUES[section]);

                    dataForm = new DataForm(section, i);

                    return true;
                }

            });

            return true;
        }
    }

    class DataForm{

        private int sub_mode;

        protected DataForm(final int section, final int mode) {

            sub_mode = mode;

            // Создаём всплывающее окно для создания новой записи.
            createDialog = new AlertDialog.Builder(context);
            // Подгружаем форму всплывающего окна и инициируем его компоненты.
            inflater = getLayoutInflater();
            alertView = inflater.inflate(R.layout.form_new_data, null);
            tvTitle = alertView.findViewById(R.id.id_tv_mesg);
            tvSubTitle = alertView.findViewById(R.id.id_tv_mesg2);
            etCreateData = alertView.findViewById(R.id.id_et_newdata);
            btCreate = alertView.findViewById(R.id.id_btn_save);
            btCancel = alertView.findViewById(R.id.id_btn_cancel);

            createDialog.setView(alertView);
            createDialog.setCancelable(false);

            // mode: 0 - create, 3 - update, 4 - delete;
            // sections: 0 - values, 1 - weights, 2 - temps;

            switch (sub_mode){
                case 0: // Create

                    switch (section){
                        case 0: // Values
                            stNewData = getString(R.string.data_values);
                            break;

                        case 1: // Weights
                            stNewData = getString(R.string.data_weights);
                            break;

                        case 2: // Temps
                            stNewData = getString(R.string.data_temps);
                            break;
                    }

                    stDataDetails = getString(R.string.data_date);
                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd (HH:mm)");
                    st_value_date = simpleDateFormat.format(new Date());
                    stDataDetails += st_value_date;

                    break;

                case 3: // Update

                    switch (section){
                        case 0: // Values
                            stNewData = getString(R.string.data_values);
                            break;

                        case 1: // Weights
                            stNewData = getString(R.string.data_weights);
                            break;

                        case 2: // Temps
                            stNewData = getString(R.string.data_temps);
                            break;
                    }

                    stDataDetails = getString(R.string.data_date);
                    stDataDetails += st_value_date;

                    etCreateData.setText(st_value_value);

                    btCreate.setText(getString(R.string.cnx_update_value));

                    break;

                case 4: // Delete

                    switch (section){
                        case 0: // Values
                            stNewData = getString(R.string.data_values);
                            break;

                        case 1: // Weights
                            stNewData = getString(R.string.data_weights);
                            break;

                        case 2: // Temps
                            stNewData = getString(R.string.data_temps);
                            break;
                    }

                    stDataDetails = getString(R.string.data_date);
                    stDataDetails += st_value_date;

                    etCreateData.setVisibility(View.GONE);

                    btCreate.setText(getString(R.string.cnx_del_value));

                    break;
            }

            tvTitle.setText(stNewData);
            tvSubTitle.setText(stDataDetails);

            // Добавляем кнопки.
            btCreate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stGotData = etCreateData.getText().toString();
                    // Поле ввода названия задачи не должно быть пустым.
                    if (stGotData.equals("")&&sub_mode!=4) {
                        etCreateData.setError(stEmpty);
                        //Toast.makeText(context, stEmpty, Toast.LENGTH_SHORT).show();
                    }else {

                        hm = new HashMap<>();
                        hm.put(TAG_ID_COW, st_id_cow);
                        hm.put(TAG_ID_VALUES[section], st_value_id);
                        hm.put(TAG_DATE_VALUES[section], st_value_date);
                        hm.put(TAG_VALUE_VALUES[section], stGotData);

                        dbConnector_values = new DBConnector_values();
                        dbConnector_values.doDBActive(section, sub_mode, hm, true, 1);
                        dbConnector_values.execute();

                        showDialog.cancel();

                    }
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
    }

    /**
     * Фоновый Async Task для загрузки данных коровы по HTTP запросу
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
                case 0: // Create a data
                    ST_RES_LOADING = getString(R.string.creating_data);
                    break;

                case 1: // Read data list
                    ST_RES_LOADING = getString(R.string.loading_datas);
                    break;

                case 2: // Read a data
                    ST_RES_LOADING = getString(R.string.loading_data);
                    break;

                case 3: // Update a data
                    ST_RES_LOADING = getString(R.string.updating_data);
                    break;

                case 4: // Delete a data
                    ST_RES_LOADING = getString(R.string.deleting_data);
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

            // получаем JSON строк с URL
            json = jParser.makeHttpRequest(url_link, params);

            Log.d(TAG_OF_CLASS, "Server answer: " + json.toString());

            try {
                // Получаем SUCCESS тег для проверки статуса ответа сервера
                int success = json.getInt(TAG_SUCCESS);

                Log.e("success: ", String.valueOf(success));

                if (success == 1) {
                    st_id_cow = json.getString(TAG_ID_COW);
                    st_id_herd = json.getString(TAG_ID_HERD);
                    st_cowName = json.getString(TAG_NAME_COW);
                    st_cow_birthday = json.getString(TAG_BIRTHDAY_COW);
                    st_cow_photo = json.getString(TAG_PHOTO_COW);

                } else {
                    // Коровы не найдены.
                    Log.e("No cows", "actually not");

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

            // обновляем UI форму в фоновом потоке
            runOnUiThread(new Runnable() {
                public void run() {

                    Log.e("st_cow_birthday: ", st_cow_birthday);

                    st_age = cow_age.countAgeFromString(st_cow_birthday);
                    stMainTitle = st_cowName + " (" + st_age + ")";
                    tv_main.setText(stMainTitle);
                    tv_id.setText(stTitleID + st_id_cow);
                    tv_herd.setText(stTitleHerd + st_herdName);
                    tv_farm.setText(stTitleFarm + st_farmName);

                    Bitmap bmp = null;
                    boolean gotImage = true;
                    try {
                        url_image = new URL(st_cow_photo);
                        bmp = BitmapFactory.decodeStream(url_image.openConnection().getInputStream());
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

                    hm = new HashMap<>();
                    hm.put(TAG_ID_COW, st_id_cow);
                    dbConnector_values = new DBConnector_values();
                    dbConnector_values.doDBActive(0,1, hm, false, 0);
                    dbConnector_values.execute();

                }
            });
        }
    }

    /**
     * Фоновый Async Task для загрузки данных по удоям по HTTP запросу
     * */
    class DBConnector_values extends AsyncTask<String, String, String> {

        private int mode;
        private int section;
        private String stMode;
        private String ST_RES_LOADING;

        HashMap<String, String> do_hm;

        private final static String TAG_REQUEST_MODE = "mode";

        private String MESSAGE_NO_DATA;

        boolean newStart = false;

        // Режимы использования Task-а
        // 0 - Каскадное чтение, 1 - чтение одного списка.
        int DBConnector_values_reg;

        DBConnector_values(){
            ST_RES_LOADING = getString(R.string.loading_data);
            MESSAGE_NO_DATA = getString(R.string.no_data);
        }

        public void doDBActive(int section, int mode, HashMap<String, String> do_hm, boolean newStart, int DBConnector_values_reg){
            this.do_hm = new HashMap<>();
            this.do_hm = do_hm;
            this.mode = mode;
            this.section = section;
            this.newStart = newStart;
            this.DBConnector_values_reg = DBConnector_values_reg;
            stMode = String.valueOf(mode);

            switch (mode){
                case 0: // Create a data
                    ST_RES_LOADING = getString(R.string.creating_data);
                    break;

                case 1: // Read data list
                    ST_RES_LOADING = getString(R.string.loading_datas);
                    break;

                case 2: // Read a data
                    ST_RES_LOADING = getString(R.string.loading_data);
                    break;

                case 3: // Update a data
                    ST_RES_LOADING = getString(R.string.updating_data);
                    break;

                case 4: // Delete a data
                    ST_RES_LOADING = getString(R.string.deleting_data);
                    break;
            }
        }

        /**
         * Перед началом фонового потока Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(newStart){
                pDialog = new ProgressDialog(context);
                pDialog.setMessage(ST_RES_LOADING);
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                pDialog.show();
            }
        }

        /**
         * Получаем все продукт из url
         * */
        protected String doInBackground(String... args) {
            // Будет хранить параметры
            params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(TAG_REQUEST_MODE, stMode));
            params.add(new BasicNameValuePair(TAG_ID_COW, do_hm.get(TAG_ID_COW)));
            params.add(new BasicNameValuePair(TAG_ID_VALUES[section], do_hm.get(TAG_ID_VALUES[section])));
            params.add(new BasicNameValuePair(TAG_DATE_VALUES[section], do_hm.get(TAG_DATE_VALUES[section])));
            params.add(new BasicNameValuePair(TAG_VALUE_VALUES[section], do_hm.get(TAG_VALUE_VALUES[section])));

            // получаем JSON строк с URL
            json = jParser.makeHttpRequest(url_cow_values[section], params);

            Log.d(TAG_OF_CLASS, "Server answer: " + json.toString());

            try {
                // Получаем SUCCESS тег для проверки статуса ответа сервера
                int success = json.getInt(TAG_SUCCESS);

                Log.e("success: ", String.valueOf(success));
                sublistValues = new ArrayList<>();

                if (success == 1) {

                    // В любом случае обносляем список.
                    values_data = json.getJSONArray(TAG_VALUES[section]);

                    // перебор всех продуктов
                    for (int i = 0; i < values_data.length(); i++) {
                        subJson = values_data.getJSONObject(i);

                        // Сохраняем каждый json элемент в переменную
                        st_id_cow = subJson.getString(TAG_ID_COW);
                        st_value_id = subJson.getString(TAG_ID_VALUES[section]);
                        st_value_date= subJson.getString(TAG_DATE_VALUES[section]);
                        st_value_value = subJson.getString(TAG_VALUE_VALUES[section]);

                        // Создаем новый HashMap
                        hm = new HashMap<String, String>();

                        // добавляем каждый елемент в HashMap ключ => значение

                        hm.put(TAG_ID_COW, st_id_cow);
                        hm.put(TAG_ID_VALUES[section], st_value_id);
                        hm.put(TAG_DATE_VALUES[section], st_value_date);
                        hm.put(TAG_VALUE_VALUES[section], st_value_value);

                        // добавляем HashList в ArrayList
                        sublistValues.add(hm);
                    }

                } else {
                    // данные не найдены.
                    // Выводим в список единственную строку: "Ферм нет"

                    // Создаем новый HashMap
                    hm = new HashMap<String, String>();

                    // добавляем каждый елемент в HashMap ключ => значение
                    hm.put(TAG_ID_COW, null);
                    hm.put(TAG_ID_VALUES[section], null);
                    hm.put(TAG_DATE_VALUES[section], MESSAGE_NO_DATA);
                    hm.put(TAG_VALUE_VALUES[section], "");

                    // добавляем HashList в ArrayList
                    sublistValues.add(hm);

                }

                if(DBConnector_values_reg==0){
                    listValues.add(sublistValues);
                }else {
                    listValues.set(section, sublistValues);
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

            if (DBConnector_values_reg==0){
                // Каскадное чтение списков

                if(section==0){
                    hm = new HashMap<>();
                    hm.put(TAG_ID_COW, st_id_cow);
                    dbConnector_values = new DBConnector_values();
                    dbConnector_values.doDBActive(1, 1, hm, false,0);
                    dbConnector_values.execute();
                }else if(section==1){
                    hm = new HashMap<>();
                    hm.put(TAG_ID_COW, st_id_cow);
                    dbConnector_values = new DBConnector_values();
                    dbConnector_values.doDBActive(2, 1, hm, false,0);
                    dbConnector_values.execute();
                }else {
                    pDialog.dismiss();
                }

            }else {

                if(newStart){
                    // Работа с одним списком после изменения
                    hm = new HashMap<>();
                    hm.put(TAG_ID_COW, st_id_cow);
                    dbConnector_values = new DBConnector_values();
                    dbConnector_values.doDBActive(section,1, hm, false, 1);
                    dbConnector_values.execute();
                }else {
                    pDialog.dismiss();
                }
            }

            // обновляем UI форму в фоновом потоке
            runOnUiThread(new Runnable() {
                public void run() {

                    /**
                     * Обновляем распарсенные JSON данные в ListView
                     * */
                    adapter = new SimpleAdapter(
                            context, listValues.get(section),
                            R.layout.item_data, new String[] {TAG_DATE_VALUES[section], TAG_VALUE_VALUES[section]},
                            new int[] {R.id.textView, R.id.textView2});
                    // обновляем listview
                    lv_values[section].setAdapter(adapter);
                }
            });
        }
    }
}