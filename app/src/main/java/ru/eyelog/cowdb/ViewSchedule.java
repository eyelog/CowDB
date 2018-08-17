package ru.eyelog.cowdb;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

public class ViewSchedule extends View {

    // Класс для создания пользовательского графика.
    // Данный класс находится в разработке


    Paint paint, paintLine;
    Path path;

    float screenX, screenY, textSize;

    int textColor = Color.BLACK;

    boolean showSignal = false;
    boolean screenGet = true;
    boolean portraitMode;


    // План позиций
    float[] positions_x;
    float[] positions_y;

    int position_x, length;
    static int minValue, maxValue, sectorValue, sectorDate;
    int[] arrayValues;

    ArrayList<HashMap<String, String>> dataList;

    public ViewSchedule(Context context) {
        super(context);
        init();
    }

    public ViewSchedule(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewSchedule(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){
        if(screenX>screenY){
            portraitMode=false;
            textSize = screenX/36;
        }else{
            portraitMode=true;
            textSize = screenY/24;
        }

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(textSize);
        paint.setColor(textColor);

        paintLine = new Paint();
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setColor(textColor);
        paintLine.setStrokeWidth(3);

        path = new Path();
    }

    public void drawUpdater(ArrayList<HashMap<String, String>> dataList, boolean showSignal){

        this.dataList = new ArrayList<>();
        this.dataList = dataList;
        this.showSignal = showSignal;

        // Расчитываем колличество ячеек для дат.
        sectorDate = dataList.size();
        positions_x = new float[sectorDate+4];

        // Рассчитываем колличество ячеек для значений.
        // Для этого нам сначала нужно получить минимальное и максимальное значения.
        arrayValues = new int[sectorDate];
        for(int i=0; i<sectorDate; i++){
            arrayValues[i] = Integer.parseInt(dataList.get(i).get("value"));
        }
        minMaxValGetter(arrayValues);

        // Далее получаем диапазон значений - столько ячеек и будет
        sectorValue = maxValue - minValue;
        positions_y = new float[sectorValue+6];

        // Далее размечаем экран.
        if(portraitMode){
            for(int i=0; i<positions_x.length; i++){
                positions_x[i] = (screenX/positions_x.length)*i;
            }

            for(int i=0; i<positions_y.length; i++){
                positions_y[i] = (screenY/positions_y.length)*i;
            }
        }else{
            for(int i=0; i<positions_x.length; i++){
                positions_x[i] = (screenY/positions_x.length)*i;
            }

            for(int i=0; i<positions_y.length; i++){
                positions_y[i] = (screenX/positions_y.length)*i;
            }
        }


        // Сразу готовим диарамму.
        path.reset();
        path.moveTo(positions_x[1], positions_y[maxValue-arrayValues[0]+3]);
        for (int i=1; i<sectorDate; i++){
            path.lineTo(positions_x[i+1], positions_y[maxValue-arrayValues[i]+3]);
        }

    }

    public void minMaxValGetter(int[] array){

        minValue = array[0];
        maxValue = array[0];
        for(int i=1; i<array.length; i++){
            if(minValue>array[i]){
                minValue=array[i];
            }
            if(maxValue<array[i]){
                maxValue=array[i];
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if(screenGet){
            screenX = getWidth();
            screenY = getHeight();

            screenGet = false;
            init();
            invalidate();
        }

        // Всегда рисуем координатные прямые.
        // Координатная прямая -x
        canvas.drawLine(10, screenY-10,screenX-10, screenY-10, paintLine);
        canvas.drawText("dates", screenX-100, screenY-15, paint);

        // Координатная прямая -y
        canvas.drawLine(10, screenY-10,10, 10, paintLine);
        canvas.drawText("values", 15, 30, paint);

        if(showSignal){
            canvas.drawPath(path, paintLine);

            for (int i=0; i<sectorDate; i++){
                canvas.drawCircle(positions_x[i+1], positions_y[maxValue-arrayValues[i]+3], 5, paintLine);
            }
        }
    }
}
