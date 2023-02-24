package com.example.tipcalculator.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.tipcalculator.R
import com.example.tipcalculator.ui.main.MainViewModel
import com.google.android.gms.ads.*
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var drawer: DrawerLayout
    private lateinit var nvDrawer: NavigationView
    private lateinit var themeSelection: MenuItem
    private lateinit var currencySelection: MenuItem
    private lateinit var drawerMenu: Menu
    private var themeSelected: String = ""
    private lateinit var currency: String
    private lateinit var themes: Array<String>
    private lateinit var currencyList: Array<String>
    private lateinit var adView: AdView
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This line makes the keyboard not shift edit text view out of view
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.isLoading.value == true
            }
            // On exit listener fixed bug causing theme to not change before UI content was displayed
            setOnExitAnimationListener {
                it.remove()
            }
        }
        setContentView(R.layout.activity_main)

        initActivity()
        //Find the text views in drawer used to show current theme and currency selection
        nvDrawer.inflateHeaderView(R.layout.nav_header)
        val themeActionView = themeSelection.actionView
        val currencyActionView = currencySelection.actionView
        val tvCurrentTheme: TextView = themeActionView!!.findViewById(R.id.tvActionView)
        val tvCurrentCurrency: TextView = currencyActionView!!.findViewById(R.id.tvActionViewCurrency)
        themeSelection.setOnMenuItemClickListener {
            themeDialog()
            true
        }
        currencySelection.setOnMenuItemClickListener {
            currencyDialog()
            true
        }
        //Observers for changes in theme and currency
        viewModel.getTheme().observe(this) { theme ->
            Log.i(TAG, "ThemeObserver: theme -> $theme")
            tvCurrentTheme.text = theme
            themeSelected = theme
            changeTheme()
            viewModel.setLoading(false)
        }
        viewModel.getCurrencySelected().observe(this){viewModelCurrency->
            tvCurrentCurrency.text = viewModelCurrency
            currency = viewModelCurrency
        }
        startAds()
    }

    private fun startAds() {
//        Log.i(TAG, "Google Mobile Ads SDK Version: " + MobileAds.getVersion())
        MobileAds.initialize(this) {}
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder().setTestDeviceIds(listOf("ABCDEF012345")).build()
        )
        val adRequest = AdRequest.Builder().build()
//        Log.i(TAG, "Ad URL: $adRequest")
        adView.loadAd(adRequest)
        adView.adListener = object: AdListener() {
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }
            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
            override fun onAdFailedToLoad(adError : LoadAdError) {
                // Code to be executed when an ad request fails.
  //              Log.i(TAG, "onAdFailedToLoad: $adError")
            }
            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.i(TAG, "onAdLoaded: ")
            }
            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Log.i(TAG, "onAdOpened:")
            }
        }
    }

    private fun initActivity() {
        //Get arrays for currency and themes
        themes = resources.getStringArray(R.array.themes)
        currencyList = resources.getStringArray(R.array.currencies)

        // Set a Toolbar to replace the ActionBar.
        toolbar = findViewById(R.id.my_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Find drawer views and menu items
        drawer = findViewById(R.id.drawer_layout)
        nvDrawer = findViewById(R.id.nvDrawer)
        drawerMenu = nvDrawer.menu
        currencySelection = drawerMenu.findItem(R.id.currencySelection)
        themeSelection = drawerMenu.findItem(R.id.themeSelection)
        adView = findViewById(R.id.adView)
   }

    private fun changeTheme() {
        Log.i(TAG, "changeTheme: theme change -> $themeSelected")
        when (themeSelected){
            "Dark Mode" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                return
            }
            "Light Mode" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                return
            }
            "System Default" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            //Unexpected theme value, default to system, save theme and notify console
            else -> {
                Log.i(TAG, "changeTheme: Unexpected theme -> $themeSelected")
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                viewModel.setTheme("System Default")
            }
        }
    }

    private fun themeDialog() {
        var newThemeSelected = themeSelected
        val checkedPosition = themes.indexOf(newThemeSelected)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Set Theme")
            .setSingleChoiceItems(themes, checkedPosition){ window, position ->
                newThemeSelected = themes[position]
                if (newThemeSelected != themeSelected) viewModel.setTheme(newThemeSelected)
                window.dismiss()
            }
            .setNegativeButton("Close", null)
            .show()
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener{
            dialog.dismiss()
        }
    }

    private fun currencyDialog(){
        val dialog = AlertDialog.Builder(this)
            .setTitle("Select Currency")
            .setItems(currencyList){ _, position ->
                val currencySelected = currencyList[position]
                Log.i(TAG, "New Currency: $currencySelected")
                if (currencySelected != currency) viewModel.setCurrency(currencySelected)
            }
            .setNegativeButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
        dialog.show()
    }

    //Inflate Menu for Support Bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                //nav drawer opens
                drawer.openDrawer(GravityCompat.END)
                Log.i(TAG, "Settings Clicked")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}