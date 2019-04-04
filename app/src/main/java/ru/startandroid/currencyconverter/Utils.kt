package ru.startandroid.currencyconverter

import io.reactivex.Observable
import java.net.HttpURLConnection
import java.net.URL
import kotlin.reflect.full.memberProperties

fun createRequest(url: String) = Observable.create<String> {
    val urlConnection = URL(url).openConnection() as HttpURLConnection
    try {
        urlConnection.connect()
        if (urlConnection.responseCode != HttpURLConnection.HTTP_OK) {
            it.onError(RuntimeException(urlConnection.responseMessage))
        } else {
            val str = urlConnection.inputStream.bufferedReader().readText()
            it.onNext(str)
        }
    } finally {
        urlConnection.disconnect()
    }
}

fun getAllCurrencies(clazz: Class<*>): List<String> {
    val fields = clazz.kotlin.memberProperties

    val currencies = ArrayList<String>()

    for (field in fields) {
        currencies.add(field.name)
    }

    return currencies
}