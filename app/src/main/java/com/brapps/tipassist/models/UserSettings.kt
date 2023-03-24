package com.brapps.tipassist.models

data class UserSettings(
    val currency: String,
    val theme: String,
    val roundUp: Boolean
){
    companion object{
        const val DEFAULT_CURRENCY = "$"
        const val DEFAULT_THEME = "System Default"
        const val DEFAULT_ROUNDUP = false
    }
}