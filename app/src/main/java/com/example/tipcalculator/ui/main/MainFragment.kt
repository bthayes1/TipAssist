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
        // Colors to use to change whether components are enabled.
        val colorDisabled = ContextCompat.getColor(fragmentContext, R.color.disabled)
        val colorEnabled = ContextCompat.getColor(fragmentContext, R.color.enabled)
        val colorEnabledText = ContextCompat.getColor(fragmentContext, R.color.enabledtext)
        val adapter = ArrayAdapter(fragmentContext, android.R.layout.simple_spinner_item, currencyList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinner.adapter = adapter
//        spinner.setSelection(lastCurr)

        binding?.apply {
            spinner.adapter = adapter
            Log.i(TAG, "${mainViewModel.getCurrencySelected().value}")
            spinner.setSelection(currencyList.indexOf(mainViewModel.getCurrencySelected().value))
            tipPercentBar.progress = INIT_TIP_PERCENT
            etAmountInput.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {
                    mainViewModel.setBillAmount(p0)
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
                Log.i(TAG, "color -> $color")
                tipPercentBar.thumb.setTint(ContextCompat.getColor(context!!, color))
                tipPercentBar.progressDrawable.setTint(ContextCompat.getColor(context!!, color))
            }
            mainViewModel.getTipPercent().observe(viewLifecycleOwner){tipPercent ->
                Log.i(TAG, "tipPercent: $tipPercent")
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
                Log.i(TAG, "tipAmount: $tipAmount")
                tvTipAmount.text = if (tipAmount > 0.0) "$" + "%.2f".format(tipAmount) else ""
            }
            mainViewModel.getTotalAmount().observe(viewLifecycleOwner){total->
                Log.i(TAG, "total: $total")
                tvTotal.text = if (total > 0) "$" + "%.2f".format(total) else ""
            }
            mainViewModel.getPartySize().observe(viewLifecycleOwner){size ->
                tvPartySize.text = size.toString()
            }
            mainViewModel.getSplitUpEnabled().observe(viewLifecycleOwner){isEnabled ->
                Log.i(TAG, "isEnabled: $isEnabled")
                when(isEnabled){
                    true -> {
                        btnSplit.trackDrawable.setTint(colorEnabled)
                        groupSlitUp.visibility = View.VISIBLE
                    }
                    false -> {
                        Log.i(TAG, "isEnabled: set disabled")
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
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    Log.i(TAG, "currency: ${currencyList[p2]}")
                    //mainViewModel.setCurrency(currencyList[p2])
                }
                override fun onNothingSelected(p0: AdapterView<*>?){}
            }
        }
    }

    companion object {
        private const val TAG = "MainFragment"
    }
}