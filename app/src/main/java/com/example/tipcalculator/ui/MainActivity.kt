package com.example.tipcalculator.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
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
    private lateinit var header: View
    private lateinit var nvDrawer: NavigationView
    private lateinit var darkModeItem: MenuItem
    private lateinit var menu: Menu
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Set a Toolbar to replace the ActionBar.
        toolbar = findViewById(R.id.my_toolbar)
        setSupportActionBar(toolbar)

        // Find our drawer view
        drawer = findViewById(R.id.drawer_layout)
        nvDrawer = findViewById(R.id.nvDrawer)
        menu = nvDrawer.menu
        darkModeItem = menu.findItem(R.id.swNightMode)
        val actionView = darkModeItem.actionView
        val swDarkMode: SwitchCompat = actionView.findViewById(R.id.swActionView)
        swDarkMode.setOnClickListener{
            Log.i(TAG, "${swDarkMode.isChecked}")
            when(swDarkMode.isChecked){
                true-> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                false-> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        val headerLayout = nvDrawer.inflateHeaderView(R.layout.nav_header)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.settings -> {
                //nav drawer opens
                drawer.openDrawer(GravityCompat.END)
                Log.i(TAG, "Settings Clicked")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    companion object{
        private const val TAG = "MainActivity"
    }
}