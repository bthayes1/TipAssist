package com.example.tipcalculator.ui.main

import android.animation.ArgbEvaluator
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.cottacush.android.currencyedittext.CurrencyEditText
import com.example.tipcalculator.R
import com.example.tipcalculator.databinding.FragmentMainBinding
import com.example.tipcalculator.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {


    private var binding: FragmentMainBinding? = null
    private val mainViewModel: MainViewModel by activityViewModels()
    private val currencyList = listOf("$", "€", "¥", "£")
    private lateinit var fragmentContext: Context
    private lateinit var currencySelected: String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentMainBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currencySelected = mainViewModel.getCurrencySelected().value ?:
            throw Exception("Currency Not Loaded when ViewModel Initialized")
        // Colors to use to change whether components are enabled.
        val colorDisabled = ContextCompat.getColor(fragmentContext, R.color.disabled)
        val colorEnabled = ContextCompat.getColor(fragmentContext, R.color.enabled)
        binding?.apply {
            Log.i(TAG, "${mainViewModel.getCurrencySelected().value}")
            tipPercentBar.progress = INIT_TIP_PERCENT
            etAmountInput.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {
                    val value = etAmountInput.getNumericValue()
                    Log.i(TAG, "VALUE: $value")
                    mainViewModel.setBillAmount(value)
                }
            })
            tipPercentBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    mainViewModel.setTipPercent(p1)
                }
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })
            mainViewModel.getColor().observe(viewLifecycleOwner){ color ->
                tipPercentBar.thumb.setTint(ContextCompat.getColor(context!!, color))
                tipPercentBar.progressDrawable.setTint(ContextCompat.getColor(context!!, color))
            }
            mainViewModel.getTipPercent().observe(viewLifecycleOwner){tipPercent ->
                tvTipPercent.text = "%$tipPercent"
                val color = ArgbEvaluator().evaluate(
                    tipPercent.toFloat() / tipPercentBar.max,
                    ContextCompat.getColor(context!!, R.color.color_best_tip),
                    ContextCompat.getColor(context!!, R.color.color_worst_tip)
                ) as Int
                tipPercentBar.thumb.setTint(color)
                tipPercentBar.progressDrawable.setTint(color)
            }
            mainViewModel.getTipAmount().observe(viewLifecycleOwner){tipAmount->
                tvTipAmount.text = if (tipAmount > 0.0 ) currencySelected + "%.2f".format(tipAmount) else ""
            }
            mainViewModel.getTotalAmount().observe(viewLifecycleOwner){total->
                tvTotal.text = if (total > 0) currencySelected + "%.2f".format(total) else ""
            }
            mainViewModel.getPartySize().observe(viewLifecycleOwner){size ->
                tvPartySize.text = size.toString()
            }
            mainViewModel.getSplitUpEnabled().observe(viewLifecycleOwner){isEnabled ->
                when(isEnabled){
                    true -> {
                        btnSplit.trackDrawable.setTint(colorEnabled)
                        groupSlitUp.visibility = View.VISIBLE
                    }
                    false -> {
                        btnSplit.trackDrawable.setTint(colorDisabled)
                        groupSlitUp.visibility = View.INVISIBLE
                    }
                }
            }
            mainViewModel.getRoundUpEnabled().observe(viewLifecycleOwner){isEnabled->
                when (isEnabled){
                    true -> btnRound.trackDrawable.setTint(colorEnabled)
                    false -> btnRound.trackDrawable.setTint(colorDisabled)
                }
            }
            mainViewModel.getCurrencySelected().observe(viewLifecycleOwner) { currency ->
                etAmountInput.setCurrencySymbol(currency, false)
                etAmountInput.setText("")
                currencySelected = currency
            }
            btnRound.setOnClickListener {
                mainViewModel.toggleRoundUp()
            }
            btnSplit.setOnClickListener {
                mainViewModel.toggleSplitUp()
            }
            btnAddParty.setOnClickListener {
                mainViewModel.changePartySize(increment = true)
            }
            btnDecParty.setOnClickListener{
                mainViewModel.changePartySize(increment = false)
            }
        }
    }

    companion object {
        private const val TAG = "MainFragment"
    }
}