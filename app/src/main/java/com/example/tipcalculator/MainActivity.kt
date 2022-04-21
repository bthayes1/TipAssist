package com.example.tipcalculator

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
// Finish Splitup feature and Roundup feature

private const val TAG = "MainActivity"
private const val INIT_TIP_PERCENT = 20

class MainActivity : AppCompatActivity() {
    private lateinit var tvTipPercent: TextView
    private lateinit var tipScroll: SeekBar
    private lateinit var tvTipAmount: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var billAmount: EditText
    private lateinit var spinner: Spinner
    private lateinit var tvSign : TextView
    private lateinit var roundUp : Switch
    private lateinit var splitUp : Switch
    private lateinit var addParty : Button
    private lateinit var decParty : Button

    private var currencyList = listOf("$", "€", "¥", "£")

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvTipPercent = findViewById(R.id.tvTipPercent)
        tipScroll = findViewById(R.id.tipPercentBar)
        tvTipAmount = findViewById(R.id.tvTipAmount)
        tvTotalAmount = findViewById(R.id.tvTotal)
        billAmount = findViewById(R.id.tvAmountInput)
        decParty = findViewById(R.id.decParty)
        addParty = findViewById(R.id.addParty)
        roundUp = findViewById(R.id.butRound)
        splitUp = findViewById(R.id.butSplit)

        tvSign = findViewById(R.id.tvSign)        //The currency symbol next to amounts
        spinner = findViewById(R.id.spinner)      //The dropdown menu
        val lastCurr = loadData()                 //Load the index value of the last currency

        // Setup an array adapter to choose different currencies
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencyList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(lastCurr)

        //Initialize tip value at 20%
        tipScroll.progress = INIT_TIP_PERCENT
        tvTipPercent.text = "$INIT_TIP_PERCENT%"
        setColor(INIT_TIP_PERCENT)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                Log.i(TAG, currencyList[p2])
                tvSign.text = currencyList[p2] // Change symbol next to EditText
                saveData(p2) //Save the index of the selected item to SharedPreference
                tipAndTotalCalc()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        tipScroll.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                //When change occurs on scrollbar, info is logged, and values are calculated
                Log.i(TAG, "Current Progress is: $p1")
                setColor(p1)
                tvTipPercent.text = "$p1%"
                tipAndTotalCalc()
            }

            //No logic needed for these functions
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
        billAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun afterTextChanged(p0: Editable?) {
                //Changes on edit text are logged and values are calculated
                Log.i(TAG, "Current Input is: $p0")
                tipAndTotalCalc()
            }
        })
        roundUp.setOnClickListener {
            Toast.makeText(this, "I have been clicked", Toast.LENGTH_SHORT)
        }
    }

    private fun saveData(currencyIndex: Int) {
        val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("lastCurrency", currencyIndex)
        editor.apply()
    }

    private fun loadData(): Int {
        val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
        return sharedPreferences.getInt("lastCurrency", 0)
    }


    private fun setColor(p1: Int) {
        val color = ArgbEvaluator().evaluate(
            p1.toFloat() / tipScroll.max,
            ContextCompat.getColor(this, R.color.color_best_tip),
            ContextCompat.getColor(this, R.color.color_worst_tip)
        ) as Int
        tipScroll.thumb.setTint(color)
        tipScroll.progressDrawable.setTint(color)
    }

    @SuppressLint("SetTextI18n")
    private fun tipAndTotalCalc() {
        //If there is no characters, return
        if (billAmount.text.isEmpty()) {
            tvTipAmount.text = ""
            tvTotalAmount.text = ""
            return
        }

        //Calculate total and tip amounts
        val billAmt = billAmount.text.toString().toDouble()
        val tipPercent = tipScroll.progress
        val tipAmt = billAmt * tipPercent / 100
        val total = tipAmt + billAmt

        //Update UI
        tvTotalAmount.text = spinner.selectedItem.toString() + "%.2f".format(total)
        tvTipAmount.text = spinner.selectedItem.toString() + "%.2f".format(tipAmt)
    }
}