package ru.startandroid.currencyconverter

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.startandroid.currencyconverter.models.Results
import java.lang.reflect.Field
import kotlin.reflect.full.memberProperties
import android.text.Editable
import android.view.View
import com.google.gson.annotations.SerializedName
import android.widget.AdapterView
import android.widget.Toast
import android.widget.AdapterView.OnItemSelectedListener
import io.realm.Realm
import android.net.NetworkInfo
import android.content.Context.CONNECTIVITY_SERVICE
import android.support.v4.content.ContextCompat.getSystemService
import android.net.ConnectivityManager
import io.realm.RealmList


class MainActivity : AppCompatActivity() {

    var request: Disposable? = null
    var rate: Double = 0.0
    lateinit var toast: Toast
    lateinit var spinnerFrom: Spinner
    lateinit var spinnerTo: Spinner
    lateinit var editTextFrom: EditText
    lateinit var editTextTo: EditText
    lateinit var currencyFrom: String
    lateinit var currencyTo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val text = "Offline mode. No connection"
        toast = Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT)

        val currencyList = getAllCurrencies(Results().javaClass)

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, currencyList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerFrom = findViewById(R.id.spinner_from)
        spinnerTo = findViewById(R.id.spinner_to)

        spinnerFrom.adapter = adapter
        spinnerTo.adapter = adapter

        spinnerFrom.setSelection(currencyList.indexOf("USD"))
        spinnerTo.setSelection(currencyList.indexOf("RUB"))

        spinnerFrom.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currencyFrom = spinnerFrom.selectedItem.toString()
            }
        }

        spinnerTo.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currencyTo = spinnerTo.selectedItem.toString()
            }
        }

        editTextFrom = findViewById(R.id.editText_from)
        editTextTo = findViewById(R.id.editText_to)

        editTextFrom.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (editTextFrom.isFocused) {

                    editTextTo.text.clear()


                    val query = currencyFrom + "_" + currencyTo
                    val url = "https://free.currencyconverterapi.com/api/v6/convert?q=$query&compact=ultra"

                    val observable = createRequest(url)
                        .map { it -> it.substring(it.indexOf(":") + 1, it.length - 1) }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())

                    request?.dispose()

                    request = observable.subscribe({

                        rate = it.toDouble()
                        editTextTo.setText((s.toString().toDouble() * rate).toString())

                    }, {
                        if (!s.isNullOrEmpty()) {
                            toast.show()
                        }
                    })
                }
            }
        })

        editTextTo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (editTextTo.isFocused) {

                    editTextFrom.text.clear()

                    val query = currencyTo + "_" + currencyFrom
                    val url = "https://free.currencyconverterapi.com/api/v6/convert?q=$query&compact=ultra"

                    val observable = createRequest(url)
                        .map { it -> it.substring(it.indexOf(":") + 1, it.length - 1) }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())

                    request?.dispose()

                    request = observable.subscribe({

                        rate = it.toDouble()
                        editTextFrom.setText((s.toString().toDouble() * rate).toString())

                    }, {
                        if (!s.isNullOrEmpty()) {
                            toast.show()
                        }
                    })
                }
            }
        })
    }

    override fun onDestroy() {
        request?.dispose()
        super.onDestroy()
    }
}
