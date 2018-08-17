package ru.eyelog.cowdb;

import android.content.Context;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Cow_age {

    // Отдельный класс для расчёта возраста коровы.

    Context context;
    String gotDate, st_age;
    String [] stArrayBirthDay = new String[3];
    int [] itArrayBirthDay = new int[3];
    int yearBirth, yearNow, monthBirth, monthNow, dayBirth, dayNow;
    int yearAge, monthAge, dayAge;
    Calendar dateNow, birthDay;
    int years, months;
    Date date;

    SimpleDateFormat simpleDateFormat;

    public Cow_age(Context context){
        this.context = context;
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }

    public String countAge(Calendar gotDate){

        dateNow = Calendar.getInstance();

        years = dateNow.get(Calendar.YEAR) - gotDate.get(Calendar.YEAR);
        if (dateNow.get(Calendar.DAY_OF_YEAR) < gotDate.get(Calendar.DAY_OF_YEAR)){
            years--;
        }

        months = dateNow.get(Calendar.MONTH) - gotDate.get(Calendar.MONTH);
        if(dateNow.get(Calendar.MONTH) < gotDate.get(Calendar.MONTH)){
            months += 12;
        }

        Log.e("dateNow", dateNow.toString());
        Log.e("gotDate", gotDate.toString());

        st_age = years + " Лет, " + months + " Месяцев.";

        return st_age;
    }

    public String countAgeFromString(String gotDate){

        this.gotDate = gotDate;
        Log.e("gotDate: ", this.gotDate);
        birthDay = Calendar.getInstance();
        try {
            birthDay.setTime(simpleDateFormat.parse(gotDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        dateNow = Calendar.getInstance();

        years = dateNow.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);
        if (dateNow.get(Calendar.DAY_OF_YEAR) < birthDay.get(Calendar.DAY_OF_YEAR)){
            years--;
        }

        months = dateNow.get(Calendar.MONTH) - birthDay.get(Calendar.MONTH);
        if(dateNow.get(Calendar.MONTH) < birthDay.get(Calendar.MONTH)){
            months += 12;
        }

        Log.e("dateNow", dateNow.toString());
        Log.e("gotDate", gotDate.toString());

        st_age = years + " Лет, " + months + " Месяцев.";


        return st_age;
    }
}
