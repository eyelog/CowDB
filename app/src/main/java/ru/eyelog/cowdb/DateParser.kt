package ru.eyelog.cowdb

import android.util.Log
import java.util.*

// Данный класс находится в разработке



// Данная функция убирает часы из даты и усредняет данные для дублируемых дат
// Даллее собираем календарный период, если есть пропуски по датам - дублируем предыдущие даты
// И в зависимости от заданного параметра собираем пользовательский период
// Параметры запроса:
// 0 - Последние 7 дней
// 1 - Текущий месяц
// 2 - Последние 30 дней
// 3 - Последние 180 дней
// 4 - Текущий год
// 5 - Весь срок
// 6 - Заданный период



fun dateParser(listDate: ArrayList<String>, listValues: ArrayList<String>, period: Int): ArrayList<HashMap<String, String>> {

    // Сначала убираем часы из даты и усредняем данные для дублируемых дат
    val parsedList = arrayListOf<kotlin.collections.HashMap<String, String>>()

    var i = 0
    var map = hashMapOf("date" to listDate[i].substring(0, 10), "value" to listValues[i])
    parsedList.add(map)

    do {

        i++

        if(i < listDate.size){
            if (parsedList[parsedList.lastIndex]["date"] == listDate[i].substring(0, 10)) {

                val middleValue = ((parsedList[parsedList.lastIndex]["value"]?.toInt()?.plus(listValues[i].toInt()))?.div(2)).toString()
                map = hashMapOf("date" to listDate[i].substring(0, 10), "value" to middleValue)
                parsedList[parsedList.lastIndex] = map

            } else {
                map = hashMapOf("date" to listDate[i].substring(0, 10), "value" to listValues[i])
                parsedList.add(map)

            }
        }

    }
    while (i < listDate.size)

    // Далее собираем полный календарь.
    var calendarList = arrayListOf<CalendarObject>()
    var calendar: Calendar? = null
    for(calObj in parsedList){
        calendar?.set(calObj.get("date")?.substring(0, 4)!!.toInt(),    // Год
                calObj.get("date")?.substring(5, 7)!!.toInt(),          // Месяц
                calObj.get("date")?.substring(8, 10)!!.toInt())         // Число
        var calendarObject = CalendarObject(calendar!!, calObj.get("value")!!.toInt())
        calendarList.add(calendarObject)
    }

    // Даллее выявляемм пропущенные даты и заполняем соответствующие данные
    for (outLine in calendarList){
        Log.e("outLine", outLine.toString())
    }


    return parsedList
}

// Кастомный объект для списка
class CalendarObject (calendar: Calendar, dateValue: Int){
    val calendarOdj: Calendar = calendar
    val dateValueOdj = dateValue
}



