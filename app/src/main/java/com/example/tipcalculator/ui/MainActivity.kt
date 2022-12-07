package com.example.tipcalculator.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This line makes the keyboard not shift edit text view out of view
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.isLoading.value == true
            }
        }
        setContentView(R.layout.activity_main)

        initActivity()
        //Find the text views in drawer used to show current theme and currency selection
        nvDrawer.inflateHeaderView(R.layout.nav_header)
        val themeActionView = themeSelection.actionView
        val currencyActionView = currencySelection.actionView
        val tvCurrentTheme: TextView = themeActionView.findViewById(R.id.tvActionView)
        val tvCurrentCurrency: TextView = currencyActionView.findViewById(R.id.tvActionViewCurrency)
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
            Log.i(TAG, "theme: $theme")
            tvCurrentTheme.text = theme
            themeSelected = theme
            changeTheme()
        }
        viewModel.getCurrencySelected().observe(this){viewModelCurrency->
            tvCurrentCurrency.text = viewModelCurrency
            currency = viewModelCurrency
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
   }

    private fun changeTheme() {
        when (themeSelected){
            "Dark Mode" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                return
            }
            "Light Mode" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                return
            }
            else -> {
                //Unexpected Value for theme. This should not happen and should be reported
                if (themeSelected != "System Default"){
                    Log.e(TAG, "Value of theme is not valid, defaulting to system")
                    viewModel.setTheme("System Default")
                }
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
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