package ru.eyelog.cowdb;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ListOfFarms extends AppCompatActivity {

    private static final String TAG_OF_CLASS = "ListOfFarms";

    ListView lv_farms;
    ArrayList<HashMap<String, String>> farmsList;
    ArrayList<String> farmsListID, farmsListName;
    HashMap<String, String> hm;

    private static Context context;
    Intent intent;

    DBConnector dbConnector;
    // Create - 0; Read all - 1; Read one - 2; Update - 3; Delete - 4;
    int modePosition;

    AlertDialog.Builder createDialog, del_dialog;
    AlertDialog showDialog;
    LayoutInflater inflater;
    View alertView;
    TextView tvTitle;
    String stNewFarm, stDelTitle;
    EditText etCreateFarm;
    Button btCancel, btCreate, btDelete;
    String stGotName, stEmpty;

    // Блок контекстного меню.
    final int CNX_UPDATE = 100;
    final int CNX_DEL = 101;
    String stUpdate, stDel;
    String mainId;

    // url получения списка всех продуктов
    private static final String url_link = "http://90.156.139.108/ServerCowFarms/farms_connector.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_FARMS = "farms";

    private static final String TAG_ID_FARM = "id_farm";
    private static final String TAG_NAME_FARM = "name_farm";

    // Блок загрузки данных.
    private ProgressDialog pDialog;

    JSONObject json, subJson;
    JSONParser jParser;
    JSONArray farms = null;
    ArrayList<NameValuePair> params;

    String st_id, st_farmName;

    ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_farms);

        context = this;

        lv_farms = findViewById(R.id.id_lv_farms);

        registerForContextMenu(lv_farms);

        farmsList = new ArrayList<HashMap<String, String>>();
        jParser = new JSONParser();


        stEmpty = getString(R.string.emptyLine);
        stUpdate = getString(R.string.cnx_update_farm);
        stDel = getString(R.string.cnx_del_farm);

        ListPresenter();
    }

    public void ListPresenter(){

        dbConnector = new DBConnector();

        // Загружаем продукты в фоновом потоке
        dbConnector.doDBActive(1, null);
        dbConnector.execute();

        // Переход к выбору стада
        lv_farms.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if(farmsListID.get(0)==null){
                    onNewFarm(view);
                }else {
                    // Получаем id фермы из списка.
                    st_id = farmsList.get(position).get(TAG_ID_FARM);

                    // Запускаем новый intent который покажет нам Activity
                    intent = new Intent(getApplicationContext(), ListOfHerds.class);

                    // отправляем id в следующий activity
                    intent.putExtra(TAG_ID_FARM, st_id);
                    intent.putExtra(TAG_NAME_FARM, farmsList.get(position).get(TAG_NAME_FARM));
                    intent.putStringArrayListExtra("array", farmsListID);
                    intent.putStringArrayListExtra("array2", farmsListName);

                    startActivity(intent);
                }
            }
        });

    }

    // Создание контекстного меню
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        if(farmsListID.get(0)!=null){
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
        mainId = farmsList.get(info.position).get(TAG_ID_FARM);

        // Выбор действия для контекстного меню.
        switch (item.getItemId()){
            // Показ статистики персонажа.
            case CNX_UPDATE:

                // Переименовываем ферму на основании её ID.
                // Создаём всплывающее окно для создания новой записи.
                createDialog = new AlertDialog.Builder(context);
                // Подгружаем форму всплывающего окна и инициируем его компоненты.
                inflater = getLayoutInflater();
                alertView = inflater.inflate(R.layout.form_new_farm, null);
                tvTitle = alertView.findViewById(R.id.id_tv_mesg);
                stNewFarm = getString(R.string.cnx_update_farm);
                tvTitle.setText(stNewFarm);
                etCreateFarm = alertView.findViewById(R.id.id_et_newfarm);
                etCreateFarm.setText(farmsList.get(info.position).get(TAG_NAME_FARM));

                btCreate = alertView.findViewById(R.id.id_btn_save);
                btCancel = alertView.findViewById(R.id.id_btn_cancel);

                createDialog.setView(alertView);
                createDialog.setCancelable(false);

                // Добавляем кнопки.
                btCreate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stGotName = etCreateFarm.getText().toString();
                        // Поле ввода названия задачи не должно быть пустым.
                        if (stGotName.equals("")) {
                            etCreateFarm.setError(stEmpty);
                            //Toast.makeText(context, stEmpty, Toast.LENGTH_SHORT).show();
                            //etCreateChar.setError(stEmpty);
                        }else {

                            stGotName = etCreateFarm.getText().toString();

                            dbConnector = new DBConnector();
                            dbConnector.doDBActive(3, stGotName);
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
                // Удаляем ферму на основании её ID.
                // Запрашиваем подтверждение удаления.
                del_dialog = new AlertDialog.Builder(context);

                // Подгружаем форму всплывающего окна и инициируем его компоненты.
                inflater = getLayoutInflater();
                alertView = inflater.inflate(R.layout.form_delete, null);

                tvTitle = alertView.findViewById(R.id.id_tv_mesg);

                btDelete = alertView.findViewById(R.id.id_btn_del);
                btCancel = alertView.findViewById(R.id.id_btn_cancel);

                stDelTitle = getString(R.string.del_farm) + farmsList.get(info.position).get(TAG_NAME_FARM);
                tvTitle.setText(stDelTitle);

                del_dialog.setView(alertView);
                del_dialog.setCancelable(false);

                btDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dbConnector = new DBConnector();
                        dbConnector.doDBActive(4, null);
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

    public void onNewFarm(View view){
        // Создаём всплывающее окно для создания новой записи.
        createDialog = new AlertDialog.Builder(context);
        // Подгружаем форму всплывающего окна и инициируем его компоненты.
        inflater = getLayoutInflater();
        alertView = inflater.inflate(R.layout.form_new_farm, null);
        tvTitle = alertView.findViewById(R.id.id_tv_mesg);
        stNewFarm = getString(R.string.create_farm);
        tvTitle.setText(stNewFarm);
        etCreateFarm = alertView.findViewById(R.id.id_et_newfarm);
        btCreate = alertView.findViewById(R.id.id_btn_save);
        btCancel = alertView.findViewById(R.id.id_btn_cancel);

        createDialog.setView(alertView);
        createDialog.setCancelable(false);

        // Добавляем кнопки.
        btCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stGotName = etCreateFarm.getText().toString();
                // Поле ввода названия задачи не должно быть пустым.
                if (stGotName.equals("")) {
                    etCreateFarm.setError(stEmpty);
                    //Toast.makeText(context, stEmpty, Toast.LENGTH_SHORT).show();
                    //etCreateChar.setError(stEmpty);
                }else {

                    stGotName = etCreateFarm.getText().toString();

                    dbConnector = new DBConnector();
                    dbConnector.doDBActive(0, stGotName);
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
     * Фоновый Async Task для загрузки всех ферм по HTTP запросу
     * */
    class DBConnector extends AsyncTask<String, String, String> {

        private int mode;
        private String stMode;
        private String value;
        private String ST_RES_LOADING;

        private final static String TAG_REQUEST_MODE = "mode";
        private final static String TAG_REQUEST_ID = "id_farm";
        private final static String TAG_REQUEST_VALUE = "name_farm";

        private String MESSAGE_NO_FARMS;

        DBConnector(){
            ST_RES_LOADING = getString(R.string.loading_farms);
            MESSAGE_NO_FARMS = getString(R.string.no_farms);
        }

        public void doDBActive(int mode, String value){
            this.value = value;
            this.mode = mode;
            stMode = String.valueOf(mode);

            switch (mode){
                case 0: // Create a farm
                    ST_RES_LOADING = getString(R.string.creating_farm);
                    break;

                case 1: // Read farms list
                    ST_RES_LOADING = getString(R.string.loading_farms);
                    break;

                case 2: // Read a farm
                    ST_RES_LOADING = getString(R.string.loading_farm);
                    break;

                case 3: // Update a farm
                    ST_RES_LOADING = getString(R.string.updating_farm);
                    break;

                case 4: // Delete a farm
                    ST_RES_LOADING = getString(R.string.deleting_farm);
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
            params.add(new BasicNameValuePair(TAG_REQUEST_ID, mainId));
            params.add(new BasicNameValuePair(TAG_REQUEST_VALUE, value));

            // получаем JSON строк с URL
            json = jParser.makeHttpRequest(url_link, params);

            Log.d(TAG_OF_CLASS, "Server answer: " + json.toString());

            try {
                // Получаем SUCCESS тег для проверки статуса ответа сервера
                int success = json.getInt(TAG_SUCCESS);

                farmsList = new ArrayList<>();
                farmsListID = new ArrayList<>();
                farmsListName = new ArrayList<>();

                if (success == 1) {

                    // В любои случае обносляем список.
                    farms = json.getJSONArray(TAG_FARMS);

                    // перебор всех продуктов
                    for (int i = 0; i < farms.length(); i++) {
                        subJson = farms.getJSONObject(i);

                        // Сохраняем каждый json елемент в переменную
                        st_id = subJson.getString(TAG_ID_FARM);
                        st_farmName = subJson.getString(TAG_NAME_FARM);

                        // Создаем новый HashMap
                        hm = new HashMap<String, String>();

                        // добавляем каждый елемент в HashMap ключ => значение
                        hm.put(TAG_ID_FARM, st_id);
                        hm.put(TAG_NAME_FARM, st_farmName);

                        // добавляем HashList в ArrayList
                        farmsList.add(hm);
                        farmsListID.add(st_id);
                        farmsListName.add(st_farmName);
                    }

                } else {
                    // Фермы не найдены.
                    // Выводим в список единственную строку: "Ферм нет"

                    // Создаем новый HashMap
                    hm = new HashMap<String, String>();

                    // добавляем каждый елемент в HashMap ключ => значение
                    hm.put(TAG_ID_FARM, null);
                    hm.put(TAG_NAME_FARM, MESSAGE_NO_FARMS);

                    // добавляем HashList в ArrayList
                    farmsList.add(hm);
                    farmsListID.add(null);
                    farmsListName.add(null);

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
            // обновляем UI форму в фоновом потоке
            runOnUiThread(new Runnable() {
                public void run() {

                    // В случае любого изменения списка перескачиваем его с базы.
                    if(mode!=1){
                        dbConnector = new DBConnector();
                        dbConnector.doDBActive(1, null);
                        dbConnector.execute();
                    }

                    /**
                     * Обновляем распарсенные JSON данные в ListView
                     * */
                    adapter = new SimpleAdapter(
                            ListOfFarms.this, farmsList,
                            android.R.layout.simple_list_item_1, new String[] {TAG_NAME_FARM},
                            new int[] {android.R.id.text1});
                    // обновляем listview
                    lv_farms.setAdapter(adapter);
                }
            });
        }
    }
}
