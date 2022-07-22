package com.example.tipcalculator.ui.main

import android.content.Context
import android.text.Editable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tipcalculator.models.UserSettings
import com.example.tipcalculator.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.text.DecimalFormat
import javax.inject.Inject

const val INIT_TIP_PERCENT = 20

@HiltViewModel
class MainViewModel@Inject constructor(private val settingsRepository: SettingsRepository) : ViewModel() {
    private var billAmt: Double = 0.0
    private val tipPercent = MutableLiveData<Int>()
    private val tipAmount = MutableLiveData<Double>()
    private val totalAmount = MutableLiveData<Double>()
    private val progressColor = MutableLiveData<Int>()
    private val roundUpEnabled = MutableLiveData<Boolean>()
    private val splitUpEnabled = MutableLiveData<Boolean>()
    private val partySize = MutableLiveData<Int>()
    private val currencySelected = MutableLiveData<String>()

    init {
        Log.i(TAG, "MainViewModel initialized...")
        tipPercent.value = INIT_TIP_PERCENT
        roundUpEnabled.value = false
        splitUpEnabled.value = false
        partySize.value = INIT_PARTY_SIZE
        loadSettings()
    }

    fun getTipPercent(): LiveData<Int> = tipPercent
    fun getTipAmount(): LiveData<Double> = tipAmount
    fun getTotalAmount(): LiveData<Double> = totalAmount
    fun getColor(): LiveData<Int> = progressColor
    fun getRoundUpEnabled(): LiveData<Boolean> = roundUpEnabled
    fun getSplitUpEnabled(): LiveData<Boolean> = splitUpEnabled
    fun getPartySize(): LiveData<Int> = partySize
    fun getCurrencySelected(): LiveData<String> = currencySelected

    fun toggleRoundUp() {
        roundUpEnabled.value = !roundUpEnabled.value!!
        calculateTotal()
    }

    fun toggleSplitUp() {
        splitUpEnabled.value = !splitUpEnabled.value!!
        calculateTotal()
    }

    fun setBillAmount(p0: Editable?) {
        billAmt = if (p0.isNullOrEmpty()) 0.0 else p0.toString().toDouble()
        calculateTotal()
        Log.i(TAG, "billAmt: $billAmt")
    }

    fun setTipPercent(percent: Int) {
        tipPercent.value = percent
        calculateTotal()
    }

    fun changePartySize(increment: Boolean) {
        partySize.value =
            when (increment) {
                true -> {
                    if (partySize.value == MAX_PARTY_SIZE) return
                    partySize.value!!.inc()
                }
                false -> {
                    if (partySize.value == INIT_PARTY_SIZE) return
                    partySize.value!!.dec()
                }
            }
        calculateTotal()
    }

    fun setCurrency(currency: String){
        currencySelected.value = currency
        saveSettings()
    }

    private fun calculateTotal() {
        val df = DecimalFormat("0")
        df.roundingMode = RoundingMode.UP

        val tipPercent = tipPercent.value!!.toInt()
        val tip = if (roundUpEnabled.value!!) {
            df.format(billAmt * tipPercent / 100).toDouble()
        } else {
            billAmt * tipPercent / 100
        }
        val total = tip + billAmt
        if (splitUpEnabled.value!!) {
            tipAmount.value = tip / partySize.value!!
            totalAmount.value = total / partySize.value!!
        } else {
            tipAmount.value = tip
            totalAmount.value = total
        }
    }

    private fun loadSettings(){
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.loadSettings().collect { userSettings->
                Log.i(TAG, "Loaded settings: ${userSettings.currency}")
                currencySelected.postValue(userSettings.currency)
            }
        }
    }

    private fun saveSettings(){
        require(!currencySelected.value.isNullOrEmpty())
        viewModelScope.launch(Dispatchers.IO){
            Log.i(TAG, "saving settings: ${currencySelected.value}")
            settingsRepository.saveSettings(
                UserSettings(currency = currencySelected.value!!)// !! because non null currency is required
            )
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
        private const val INIT_PARTY_SIZE = 1
        private const val MAX_PARTY_SIZE = 12
    }
}