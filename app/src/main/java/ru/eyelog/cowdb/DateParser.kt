package ru.eyelog.cowdb


// Данная функция убирает часы из даты и усредняет данные для дублируемых дат

fun dateParser(listDate: ArrayList<String>, listValues: ArrayList<String>): ArrayList<HashMap<String, String>> {

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

    return parsedList
}

