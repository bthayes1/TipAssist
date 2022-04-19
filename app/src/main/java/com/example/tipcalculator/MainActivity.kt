package com.example.tipcalculator

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat

private const val TAG = "MainActivity"
private const val INIT_TIP_PERCENT = 20

class MainActivity : AppCompatActivity() {
    private lateinit var tvTipPercent: TextView
    private lateinit var tipScroll: SeekBar
    private lateinit var tvTipAmount: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var billAmount: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvTipPercent = findViewById(R.id.tvTipPercent)
        tipScroll = findViewById(R.id.tipPercentBar)
        tvTipAmount = findViewById(R.id.tvTipAmount)
        tvTotalAmount = findViewById(R.id.tvTotal)
        billAmount = findViewById(R.id.tvAmountInput)

        //Initialize tip value at 20%
        tipScroll.progress = INIT_TIP_PERCENT
        tvTipPercent.text = INIT_TIP_PERCENT.toString()
        setColor(INIT_TIP_PERCENT)


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
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                //Changes on edit text are logged and values are calculated
                Log.i(TAG, "Current Input is: $p0")
                tipAndTotalCalc()
            }
        })
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
        val tipAmt = billAmt *  tipPercent/ 100
        val total = tipAmt + billAmt
        //Update UI
        tvTotalAmount.text = "%.2f".format(total)
        tvTipAmount.text = "%.2f".format(tipAmt)
    }
}