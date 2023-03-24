package com.brapps.tipassist.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brapps.tipassist.models.UserSettings
import com.brapps.tipassist.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.text.DecimalFormat
import javax.inject.Inject

const val INIT_TIP_PERCENT = 20
@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
    ): ViewModel() {

    private var initialCurrency = ""
    private var billAmt = MutableLiveData(0.00)
    private val tipPercent = MutableLiveData<Int>()
    private val tipAmount = MutableLiveData<String>()
    private val totalAmount = MutableLiveData<String>()
    private val progressColor = MutableLiveData<Int>()
    private val roundUpEnabled = MutableLiveData<Boolean>()
    private val splitUpEnabled = MutableLiveData<Boolean>()
    private val partySize = MutableLiveData<Int>()
    private val currencySelected = MutableLiveData<String>()
    private val themeSelected = MutableLiveData<String>()
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val df = DecimalFormat("0")

    init {
        Log.i(TAG, "MainViewModel initialized...")
        tipPercent.value = INIT_TIP_PERCENT
        splitUpEnabled.value = false
        partySize.value = INIT_PARTY_SIZE
        _isLoading.value = true
        //Init format for rounding up
        df.roundingMode = RoundingMode.UP
        loadSettings()
    }

    // Methods to get read-only versions of MutableLiveData
    fun getBillAmt(): LiveData<Double> = billAmt
    fun getTipPercent(): LiveData<Int> = tipPercent
    fun getTipAmount(): LiveData<String> = tipAmount
    fun getTotalAmount(): LiveData<String> = totalAmount
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
        Log.i(TAG, "toggleRoundUp: saving roundup...")
        saveSettings()
    }

    fun toggleSplitUp() {
        splitUpEnabled.value = !splitUpEnabled.value!!
        calculateTotal()
    }

    fun setBillAmount(total: Double) {
        billAmt.value = total
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
            Log.i(TAG, "setTheme: saving theme...")
            saveSettings()
    }

    fun setCurrency(currency: String) {
        currencySelected.value = currency
        calculateTotal() //Must calculate to update currency
        currencySelected.value = currency
        if (currency != initialCurrency){
            Log.i(TAG, "setCurrency: saving currency...")
            saveSettings()
        }
    }

    private fun calculateTotal() {
        val billAmt = getBillAmt().value ?: 0.00
        val tipPercent = getTipPercent().value
        val roundUp = getRoundUpEnabled().value
        val splitUp = getSplitUpEnabled().value
        val partySize = getPartySize().value
        val currency = getCurrencySelected().value ?: "$"

        //If any value to be used in calculation is null, throw error
        require(tipPercent != null)
        require(splitUp != null)
        require(partySize != null)

        val bill = if (splitUp) billAmt/partySize else billAmt
        var tip = bill * tipPercent / 100
        val unroundedTotal = bill + tip
        Log.i(TAG, "calculateTotal: $roundUp")
        val total = if (roundUp != null && roundUp){
            val roundedTotal = df.format(bill + tip).toDouble()
            val difference = roundedTotal - unroundedTotal
            tip += difference
            roundedTotal
        }else{
            unroundedTotal
        }

        Log.i(TAG, "calculateTotal: bill $bill, tip $tip, total $total")
        when (bill>0){
            true -> {
                tipAmount.value = currency + "%.2f".format(tip)
                totalAmount.value = currency + "%.2f".format(total)
            }
            false -> {
                tipAmount.value = ""
                totalAmount.value = ""
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch{
            settingsRepository.loadSettings().collect { userSettings ->
                Log.i(TAG, "loadSettings: $userSettings")
                themeSelected.value = userSettings.theme
                currencySelected.value = userSettings.currency
                roundUpEnabled.value = userSettings.roundUp
                initialCurrency = userSettings.currency
            }
        }
    }

    private fun saveSettings(){
        val userCurrency = getCurrencySelected().value
        val userTheme = getTheme().value
        val roundUp = getRoundUpEnabled().value

        require(!userCurrency.isNullOrEmpty())
        require(!userTheme.isNullOrEmpty())
        require(roundUp != null)

        viewModelScope.launch(Dispatchers.IO){
            Log.i(TAG, "saveSettings: saving...")
            settingsRepository.saveSettings(
                UserSettings(
                    currency = userCurrency,
                    theme = userTheme,
                    roundUp = roundUp
                )
            )
        }
    }

    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    companion object {
        private const val TAG = "MainViewModel"
        private const val INIT_PARTY_SIZE = 1
        private const val MAX_PARTY_SIZE = 12
    }
}

