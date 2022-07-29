package com.example.tipcalculator.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tipcalculator.models.UserSettings
import com.example.tipcalculator.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
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
    private val themeSelected = MutableLiveData<String>()
    private val df = DecimalFormat("0")

    init {
        Log.i(TAG, "MainViewModel initialized...")
        tipPercent.value = INIT_TIP_PERCENT
        roundUpEnabled.value = false
        splitUpEnabled.value = false
        partySize.value = INIT_PARTY_SIZE

        //Init format for rounding up
        df.roundingMode = RoundingMode.UP
        loadSettings()
    }

    // Methods to get read-only versions of MutableLiveData
    fun getTipPercent(): LiveData<Int> = tipPercent
    fun getTipAmount(): LiveData<Double> = tipAmount
    fun getTotalAmount(): LiveData<Double> = totalAmount
    fun getColor(): LiveData<Int> = progressColor
    fun getRoundUpEnabled(): LiveData<Boolean> = roundUpEnabled
    fun getSplitUpEnabled(): LiveData<Boolean> = splitUpEnabled
    fun getPartySize(): LiveData<Int> = partySize
    fun getCurrencySelected(): LiveData<String> = currencySelected
    fun getTheme(): LiveData<String> = themeSelected

    //Methods to write new values when changes are made in UI
    fun toggleRoundUp() {
        val roundUp = getRoundUpEnabled().value
        require(roundUp != null)
        roundUpEnabled.value = !roundUp
        calculateTotal()
    }
    fun toggleSplitUp() {
        splitUpEnabled.value = !splitUpEnabled.value!!
        calculateTotal()
    }
    fun setBillAmount(total: Double) {
        billAmt = total
        calculateTotal()
    }
    fun setTipPercent(percent: Int) {
        tipPercent.value = percent
        calculateTotal()
    }
    fun changePartySize(increment: Boolean) {
        val party = getPartySize().value
        require(party != null)
        partySize.value =
            when (increment) {
                true -> {
                    if (party == MAX_PARTY_SIZE) return
                    party.inc()
                }
                false -> {
                    if (party == INIT_PARTY_SIZE) return
                    party.dec()
                }
            }
        calculateTotal()
    }
    fun setTheme(theme: String) {
        themeSelected.value = theme
        saveSettings()
    }
    fun setCurrency(currency: String){
        currencySelected.value = currency
        saveSettings()
        calculateTotal() //Must calculate to update currency
    }

    private fun calculateTotal() {
        val tipPercent = getTipPercent().value
        val roundUp = getRoundUpEnabled().value
        val splitUp = getSplitUpEnabled().value
        val partySize = getPartySize().value

        //If any value to be used in calculation is null, throw error
        require(tipPercent != null)
        require(roundUp != null)
        require(splitUp != null)
        require(partySize != null)

        val bill = if (splitUp) billAmt/partySize else billAmt
        val tip = if (roundUp) {
            df.format(bill * tipPercent / 100).toDouble()
        } else {
            bill * tipPercent / 100
        }
        tipAmount.value = tip
        totalAmount.value = tip + bill

    }
    private fun loadSettings() {
        runBlocking {
            Log.i(TAG, "blocking...")
            settingsRepository.loadSettings().first { userSettings ->
                currencySelected.value = userSettings.currency
                themeSelected.value = userSettings.theme
                true
            }
        }
        Log.i(TAG, "done")
    }
    private fun saveSettings(){
        val userCurrency = getCurrencySelected().value
        val userTheme = getTheme().value
        require(!userCurrency.isNullOrEmpty())
        require(!userTheme.isNullOrEmpty())
        viewModelScope.launch(Dispatchers.IO){
            settingsRepository.saveSettings(
                UserSettings(
                    currency = userCurrency,
                    theme = userTheme
                )
            )
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
        private const val INIT_PARTY_SIZE = 1
        private const val MAX_PARTY_SIZE = 12
    }
}