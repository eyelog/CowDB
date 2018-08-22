package ru.eyelog.cowdb;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;
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

public class ListOfHerds extends AppCompatActivity {

    private static final String TAG_OF_CLASS = "ListOfHerds";

    TextView tvMainTitle;
    ListView lv_herds;
    List<HashMap<String, String>> herdsList;
    ArrayList<String> farmsListID, farmsListName, herdsListID, herdsListName;
    HashMap<String, String> hm;

    int flipStep, limitOfFlips;

    private static Context context;
    Intent intent;

    DBConnector dbConnector;
    // Create - 0; Read all - 1; Read one - 2; Update - 3; Delete - 4;

    AlertDialog.Builder createDialog, del_dialog;
    AlertDialog showDialog;
    LayoutInflater inflater;
    View alertView;
    TextView tvTitle;
    String stNewHerd, stDelHerd;
    EditText etCreateHerd;
    Button btCancel, btCreate, btDelete;
    String stGotName, stEmpty;

    // Блок контекстного меню.
    final int CNX_UPDATE = 100;
    final int CNX_DEL = 101;
    String stUpdate, stDel;

    // url получения списка всех продуктов
    private static final String url_link = "http://90.156.139.108/ServerCowFarms/herds_connector.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_HERDS = "herds";

    private static final String TAG_ID_FARM = "id_farm";
    private static final String TAG_NAME_FARM = "name_farm";
    private static final String TAG_ID_HERD = "id_herd";
    private static final String TAG_NAME_HERD = "name_herd";

    // Блок загрузки данных.
    private ProgressDialog pDialog;
    CountDownTimer countDownTimer;

    JSONObject json, subJson;
    JSONParser jParser;
    JSONArray herds = null;
    ArrayList<NameValuePair> params;
    int timeout;

    String stMainTitle;
    String st_id_farm, st_farmName, st_id_herd, st_herdName;

    ListAdapter adapter;

    ViewFlipper flipper;

    Animation animFlipInForward;
    Animation animFlipOutForward;
    Animation animFlipInBackward;
    Animation animFlipOutBackward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_herds);

        context = this;

        tvMainTitle = findViewById(R.id.id_tv_listherds);
        lv_herds = findViewById(R.id.id_lv_herds);

        registerForContextMenu(lv_herds);

        herdsList = new ArrayList<HashMap<String, String>>();
        jParser = new JSONParser();

        intent = getIntent();
        st_id_farm = intent.getStringExtra(TAG_ID_FARM);
        st_farmName = intent.getStringExtra(TAG_NAME_FARM);
        farmsListID = intent.getStringArrayListExtra("array");
        farmsListName = intent.getStringArrayListExtra("array2");

        stEmpty = getString(R.string.emptyLine);
        stUpdate = getString(R.string.cnx_update_herd);
        stDel = getString(R.string.cnx_del_herd);
        timeout = getResources().getInteger(R.integer.timeout);

        if(farmsListID.size()>1){
            limitOfFlips = farmsListID.size()-1;
            flipStep = 0;
            for(int i=0; i<limitOfFlips+1; i++){
                if(farmsListID.get(i).equals(st_id_farm)){
                    break;
                }else {
                    flipStep++;
                }
            }
        }else {
            limitOfFlips = 0;
        }

        ListPresenter(st_id_farm);

        flipper = findViewById(R.id.viewflipper);

        animFlipInForward = AnimationUtils.loadAnimation(this, R.anim.flipin);
        animFlipOutForward = AnimationUtils.loadAnimation(this, R.anim.flipout);
        animFlipInBackward = AnimationUtils.loadAnimation(this,
                R.anim.flipin_reverse);
        animFlipOutBackward = AnimationUtils.loadAnimation(this,
                R.anim.flipout_reverse);
    }

    public void ListPresenter(String id){

        // Загружаем стада в фоновом потоке

        hm = new HashMap<>();
        hm.put(TAG_ID_FARM, id);
        hm.put(TAG_ID_HERD, null);
        hm.put(TAG_NAME_HERD, null);
        dbConnector = new DBConnector();
        dbConnector.doDBActive(1, hm);
        dbConnector.execute();

        stMainTitle = getString(R.string.list_of_herds);
        stMainTitle += " " + farmsListName.get(flipStep);
        tvMainTitle.setText(stMainTitle);

        // на выбор одного стада
        lv_herds.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if(herdsListID.get(0)==null){
                    onNewHerd(view);
                }else {
                    // Получаем id фермы из списка.
                    st_id_herd = herdsList.get(position).get(TAG_ID_HERD);
                    st_herdName = herdsList.get(position).get(TAG_NAME_HERD);

                    // Запускаем новый intent который покажет нам Activity
                    intent = new Intent(context, ListOfCows.class);

                    // отправляем id в следующий activity
                    intent.putExtra(TAG_ID_FARM, st_id_farm);
                    intent.putExtra(TAG_NAME_FARM, st_farmName);
                    intent.putExtra(TAG_ID_HERD, st_id_herd);
                    intent.putExtra(TAG_NAME_HERD, st_herdName);
                    intent.putStringArrayListExtra("array", herdsListID);
                    intent.putStringArrayListExtra("array2", herdsListName);

                    startActivity(intent);
                }
            }
        });

        lv_herds.setOnTouchListener(new View.OnTouchListener() {
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

            ListPresenter(farmsListID.get(flipStep));
        }
    }

    // Перемотка вправо
    private void SwipeRight() {
        if(flipStep>0){
            flipper.setInAnimation(animFlipInForward);
            flipper.setOutAnimation(animFlipOutForward);
            flipper.showNext();

            flipStep--;
            ListPresenter(farmsListID.get(flipStep));
        }
    }

    // Обработка флипа
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
        if(herdsListID.get(0)!=null){
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
        st_id_farm = herdsList.get(info.position).get(TAG_ID_FARM);
        st_id_herd = herdsList.get(info.position).get(TAG_ID_HERD);

        // Выбор действия для контекстного меню.
        switch (item.getItemId()){
            // Показ статистики персонажа.
            case CNX_UPDATE:

                // Переименовываем стадо на основании его ID.
                // Создаём всплывающее окно для создания новой записи.
                createDialog = new AlertDialog.Builder(context);
                // Подгружаем форму всплывающего окна и инициируем его компоненты.
                inflater = getLayoutInflater();
                alertView = inflater.inflate(R.layout.form_new_farm, null);
                tvTitle = alertView.findViewById(R.id.id_tv_mesg);
                stNewHerd = getString(R.string.cnx_update_herd);
                tvTitle.setText(stNewHerd);
                etCreateHerd = alertView.findViewById(R.id.id_et_newfarm);
                etCreateHerd.setText(herdsList.get(info.position).get(TAG_NAME_HERD));

                btCreate = alertView.findViewById(R.id.id_btn_save);
                btCancel = alertView.findViewById(R.id.id_btn_cancel);

                createDialog.setView(alertView);
                createDialog.setCancelable(false);

                // Добавляем кнопки.
                btCreate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stGotName = etCreateHerd.getText().toString();
                        // Поле ввода названия задачи не должно быть пустым.
                        if (stGotName.equals("")) {
                            etCreateHerd.setError(stEmpty);
                            //Toast.makeText(context, stEmpty, Toast.LENGTH_SHORT).show();
                            //etCreateChar.setError(stEmpty);
                        }else {

                            stGotName = etCreateHerd.getText().toString();

                            hm = new HashMap<>();
                            hm.put(TAG_ID_FARM, st_id_farm);
                            hm.put(TAG_ID_HERD, st_id_herd);
                            hm.put(TAG_NAME_HERD, stGotName);

                            dbConnector = new DBConnector();
                            dbConnector.doDBActive(3, hm);
                            dbConnector.execute();

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


                break;

            // Удаление Персонажа.
            case CNX_DEL:
                // Удаляем стадо на основании его ID.
                // Запрашиваем подтверждение удаления.
                del_dialog = new AlertDialog.Builder(context);

                // Подгружаем форму всплывающего окна и инициируем его компоненты.
                inflater = getLayoutInflater();
                alertView = inflater.inflate(R.layout.form_delete, null);

                tvTitle = alertView.findViewById(R.id.id_tv_mesg);

                btDelete = alertView.findViewById(R.id.id_btn_del);
                btCancel = alertView.findViewById(R.id.id_btn_cancel);

                stDelHerd = getString(R.string.del_herd) + herdsList.get(info.position).get(TAG_NAME_HERD);
                tvTitle.setText(stDelHerd);

                del_dialog.setView(alertView);
                del_dialog.setCancelable(false);

                btDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        hm = new HashMap<>();
                        hm.put(TAG_ID_FARM, st_id_farm);
                        hm.put(TAG_ID_HERD, st_id_herd);
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

    public void onNewHerd(View view){
        // Создаём всплывающее окно для создания новой записи.
        createDialog = new AlertDialog.Builder(context);
        // Подгружаем форму всплывающего окна и инициируем его компоненты.
        inflater = getLayoutInflater();
        alertView = inflater.inflate(R.layout.form_new_farm, null);
        tvTitle = alertView.findViewById(R.id.id_tv_mesg);
        stNewHerd = getString(R.string.create_herd);
        tvTitle.setText(stNewHerd);
        etCreateHerd = alertView.findViewById(R.id.id_et_newfarm);
        btCreate = alertView.findViewById(R.id.id_btn_save);
        btCancel = alertView.findViewById(R.id.id_btn_cancel);

        createDialog.setView(alertView);
        createDialog.setCancelable(false);

        // Добавляем кнопки.
        btCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stGotName = etCreateHerd.getText().toString();
                // Поле ввода названия задачи не должно быть пустым.
                if (stGotName.equals("")) {
                    etCreateHerd.setError(stEmpty);
                    //Toast.makeText(context, stEmpty, Toast.LENGTH_SHORT).show();
                    //etCreateChar.setError(stEmpty);
                }else {

                    hm = new HashMap<>();
                    hm.put(TAG_ID_FARM, st_id_farm);
                    hm.put(TAG_ID_HERD, null);
                    hm.put(TAG_NAME_HERD, stGotName);

                    dbConnector = new DBConnector();
                    dbConnector.doDBActive(0, hm);
                    dbConnector.execute();

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

    /**
     * Фоновый Async Task для загрузки всех стадов (стад =)) по HTTP запросу
     * */
    class DBConnector extends AsyncTask<String, String, String> {

        private int mode;
        private String stMode;
        private String ST_RES_LOADING;

        HashMap<String, String> do_hm;

        private final static String TAG_REQUEST_MODE = "mode";

        private String MESSAGE_NO_HERDS;

        DBConnector(){
            ST_RES_LOADING = getString(R.string.loading_herds);
            MESSAGE_NO_HERDS = getString(R.string.no_herds);
        }

        public void doDBActive(int mode, HashMap<String, String> do_hm){
            this.do_hm = new HashMap<>();
            this.do_hm = do_hm;
            this.mode = mode;
            stMode = String.valueOf(mode);

            switch (mode){
                case 0: // Create a farm
                    ST_RES_LOADING = getString(R.string.creating_herd);
                    break;

                case 1: // Read farms list
                    ST_RES_LOADING = getString(R.string.loading_herds);
                    break;

                case 2: // Read a farm
                    ST_RES_LOADING = getString(R.string.loading_herd);
                    break;

                case 3: // Update a farm
                    ST_RES_LOADING = getString(R.string.updating_herd);
                    break;

                case 4: // Delete a farm
                    ST_RES_LOADING = getString(R.string.deleting_herd);
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

            // Ждем ответа сервера в течении заданного периода.
            // И если его не последовало, прерываем загрузку.
            countDownTimer = new CountDownTimer(timeout, 1000) {

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
         * Получаем все стада из url
         * */
        protected String doInBackground(String... args) {
            // Будет хранить параметры
            params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(TAG_REQUEST_MODE, stMode));
            params.add(new BasicNameValuePair(TAG_ID_FARM, do_hm.get(TAG_ID_FARM)));
            params.add(new BasicNameValuePair(TAG_ID_HERD, do_hm.get(TAG_ID_HERD)));
            params.add(new BasicNameValuePair(TAG_NAME_HERD, do_hm.get(TAG_NAME_HERD)));

            // получаем JSON строк с URL
            json = jParser.makeHttpRequest(url_link, params);

            Log.d(TAG_OF_CLASS, "Server answer: " + json.toString());

            try {
                // Получаем SUCCESS тег для проверки статуса ответа сервера
                int success = json.getInt(TAG_SUCCESS);

                herdsList = new ArrayList<>();
                herdsListID = new ArrayList<>();
                herdsListName = new ArrayList<>();

                if (success == 1) {

                    // В любои случае обносляем список.
                    herds = json.getJSONArray(TAG_HERDS);

                    // перебор всех продуктов
                    for (int i = 0; i < herds.length(); i++) {
                        subJson = herds.getJSONObject(i);

                        // Сохраняем каждый json элемент в переменную
                        st_id_herd = subJson.getString(TAG_ID_HERD);
                        st_herdName = subJson.getString(TAG_NAME_HERD);

                        // Создаем новый HashMap
                        hm = new HashMap<String, String>();

                        // добавляем каждый елемент в HashMap ключ => значение
                        hm.put(TAG_ID_FARM, st_id_farm);
                        hm.put(TAG_ID_HERD, st_id_herd);
                        hm.put(TAG_NAME_HERD, st_herdName);

                        // добавляем HashList в ArrayList
                        herdsList.add(hm);
                        herdsListID.add(st_id_herd);
                        herdsListName.add(st_herdName);
                    }

                } else {
                    // Стада не найдены.
                    // Выводим в список единственную строку: "Пусто"

                    // Создаем новый HashMap
                    hm = new HashMap<String, String>();

                    // добавляем каждый елемент в HashMap ключ => значение
                    hm.put(TAG_ID_FARM, null);
                    hm.put(TAG_ID_HERD, null);
                    hm.put(TAG_NAME_HERD, MESSAGE_NO_HERDS);

                    // добавляем HashList в ArrayList
                    herdsList.add(hm);
                    herdsListID.add(null);
                    herdsListName.add(null);

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
                        hm.put(TAG_ID_FARM, st_id_farm);
                        hm.put(TAG_ID_HERD, null);
                        hm.put(TAG_NAME_HERD, null);

                        dbConnector = new DBConnector();
                        dbConnector.doDBActive(1, hm);
                        dbConnector.execute();
                    }

                    /**
                     * Обновляем распарсенные JSON данные в ListView
                     * */
                    adapter = new SimpleAdapter(
                            ListOfHerds.this, herdsList,
                            android.R.layout.simple_list_item_1, new String[] {TAG_NAME_HERD},
                            new int[] {android.R.id.text1});
                    // обновляем listview
                    lv_herds.setAdapter(adapter);
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
}
