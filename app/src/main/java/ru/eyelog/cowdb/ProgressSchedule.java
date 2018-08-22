package ru.eyelog.cowdb;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ProgressSchedule extends AppCompatActivity {

    // Данный класс находится в разработке

    // Вывод графика статистических данных
    // Используется сторонняя библиотека.

    static Intent intent;

    TextView tvTitle;
    String stTitle;
    Button button;

    public static ViewSchedule viewSchedule;
    ViewRefresher viewRefresher;

    static ArrayList<String> listGraphDates;
    static ArrayList<String> listGraphValues;
    static ArrayList<HashMap<String, String>> parsedList;
    public static int screenX, screenY;

    public static int period = 0;

    private static final  String TAG_NAME_COW = "name_cow";

    AlertDialog.Builder contextDialog;
    AlertDialog showDialog;
    LayoutInflater inflater;
    View alertView;
    TextView[] tvList = new TextView[7];
    int[] res_tvList = new int[]{R.id.tv_00, R.id.tv_01, R.id.tv_02,
            R.id.tv_03, R.id.tv_04, R.id.tv_05, R.id.tv_06};
    String stNewFarm, stDelTitle;
    EditText etCreateFarm;
    Button btCancel, btCreate, btDelete;
    String stGotName, stEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_schedule);

        intent = getIntent();

        stTitle = intent.getStringExtra(TAG_NAME_COW);
        tvTitle = findViewById(R.id.id_tv_graphTitle);
        tvTitle.setText(stTitle);

        button = findViewById(R.id.button);

        viewSchedule = findViewById(R.id.id_viewScedule);
        viewRefresher = new ViewRefresher();
        viewRefresher.execute();

    }

    static class ViewRefresher extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected String doInBackground(String... args) {

            listGraphDates = intent.getStringArrayListExtra("array_date");
            listGraphValues = intent.getStringArrayListExtra("array_value");

            parsedList = DateParserKt.dateParser(listGraphDates, listGraphValues, period);

            viewSchedule.init();
            viewSchedule.invalidate();

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {

            viewSchedule.drawUpdater(parsedList, true);
            viewSchedule.invalidate();

        }
    }

    public void onGetPeriod(View view){
        finish();
    }

    class ContextMenuListener implements View.OnClickListener{

        int position;

        ContextMenuListener(int position){
            this.position = position;
        }

        @Override
        public void onClick(View view) {

        }
    }

    public void onBack(View view){
        finish();
    }
}
