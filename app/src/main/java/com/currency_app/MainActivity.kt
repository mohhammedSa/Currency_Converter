package com.currency_app

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.currency_app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var codesList: ArrayList<String>

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        codesList = ArrayList()

        val myAdapter = ArrayAdapter(this, R.layout.list_item_layout, getCurrenciesCode())
        binding.fromEditText.setAdapter(myAdapter)
        binding.toEditText.setAdapter(myAdapter)
        countResult()

        binding.fromEditText.addTextChangedListener {
            countResult()
        }
        binding.toEditText.addTextChangedListener {
            countResult()
        }
        binding.amountEditText.addTextChangedListener {
            if (binding.amountEditText.text!!.isNotEmpty())
                countResult()
            else
                binding.amountEditText.error = "Required"
        }
    }

    private fun countResult() {
        val from = getThreeLetterCurrencyCode(binding.fromEditText.text.toString())
        val to = getThreeLetterCurrencyCode(binding.toEditText.text.toString())
        val amount = binding.amountEditText.text.toString().toFloat()
        getCurrenciesValueAndChangeResultEditText(from, to, amount)
    }

    private fun getThreeLetterCurrencyCode(str: String): String {
        return str.substring(0, 3)
    }

    private fun getCurrenciesCode(): ArrayList<String> {
        val url = "https://v6.exchangerate-api.com/v6/9d558c6419be1bfd5e302dd8/codes"
        val queue = Volley.newRequestQueue(this)
        val jsonRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            {
                val codesArray = it.getJSONArray("supported_codes")
                for (i in 0 until codesArray.length()) {
                    val codeChildArr = codesArray.getJSONArray(i)
                    val codeStr = codeChildArr.getString(0)
                    val codeCountryStr = codeChildArr.getString(1)
                    codesList.add("$codeStr, $codeCountryStr")
                }
            },
            {
                Toast.makeText(applicationContext, "${it.message}", Toast.LENGTH_LONG).show()
            }
        )
        queue.add(jsonRequest)
        return codesList
    }

    private fun getCurrenciesValueAndChangeResultEditText(from: String, to: String, amount: Float) {
        val url = "https://v6.exchangerate-api.com/v6/9d558c6419be1bfd5e302dd8/latest/USD"
        val queue = Volley.newRequestQueue(this)
        val jsonRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            {
                val currencyFrom = it.getJSONObject("conversion_rates").getString(from)
                val currencyTo = it.getJSONObject("conversion_rates").getString(to)
                val result = currencyTo.toFloat() * amount / currencyFrom.toFloat()
                val formattedResult = String.format("%.2f",result)
                binding.resultEditText.text = Editable.Factory().newEditable(formattedResult)
            },
            { }
        )
        queue.add(jsonRequest)
    }
}
