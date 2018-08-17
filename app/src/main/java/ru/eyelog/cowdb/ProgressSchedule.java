package ru.eyelog.cowdb;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ProgressSchedule extends AppCompatActivity {

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

    private static final  String TAG_NAME_COW = "name_cow";

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

            parsedList = DateParserKt.dateParser(listGraphDates, listGraphValues);

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
}
