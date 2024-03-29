package com.brapps.tipassist.ui.main

import android.animation.ArgbEvaluator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.brapps.tipassist.R
import com.brapps.tipassist.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {


    private var binding: FragmentMainBinding? = null
    private val mainViewModel: MainViewModel by activityViewModels()
    private var currencySelected = ""

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
        val colorDisabled = ContextCompat.getColor(requireContext(), R.color.disabled)
        val colorEnabled = ContextCompat.getColor(requireContext(), R.color.enabled)
        binding?.apply {
            tipPercentBar.progress = INIT_TIP_PERCENT

            etAmountInput.doOnTextChanged { text, start, before, count ->
                mainViewModel.setBillAmount(etAmountInput.getNumericValue())
            }

            tipPercentBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    mainViewModel.setTipPercent(p1)
                }
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })
            mainViewModel.getColor().observe(viewLifecycleOwner){ color ->
                tipPercentBar.thumb.setTint(ContextCompat.getColor(requireContext(), color))
                tipPercentBar.progressDrawable.setTint(ContextCompat.getColor(requireContext(), color))
            }
            mainViewModel.getTipPercent().observe(viewLifecycleOwner){tipPercent ->
                tvTipPercent.text = "%$tipPercent"
                val color = ArgbEvaluator().evaluate(
                    tipPercent.toFloat() / tipPercentBar.max,
                    ContextCompat.getColor(requireContext(), R.color.color_best_tip),
                    ContextCompat.getColor(requireContext(), R.color.color_worst_tip)
                ) as Int
                tipPercentBar.thumb.setTint(color)
                tipPercentBar.progressDrawable.setTint(color)
            }
            mainViewModel.getTipAmount().observe(viewLifecycleOwner){tipAmount->
                tvTipAmount.text = tipAmount
            }
            mainViewModel.getTotalAmount().observe(viewLifecycleOwner){total->
                tvTotal.text = total
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
                Log.i(TAG, "onViewCreated: $isEnabled")
                btnRound.isChecked = isEnabled
                when (isEnabled){
                    true -> btnRound.trackDrawable.setTint(colorEnabled)
                    false -> btnRound.trackDrawable.setTint(colorDisabled)
                }
            }
            mainViewModel.getCurrencySelected().observe(viewLifecycleOwner) { currency ->
                Log.i(TAG, "onViewCreated: currency $currency")
                if (currency != currencySelected){
                    etAmountInput.setCurrencySymbol(currency, false)
                    currencySelected = currency
                    etAmountInput.setText("")
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
        }
    }
    companion object {
        private const val TAG = "MainFragment"
    }
}