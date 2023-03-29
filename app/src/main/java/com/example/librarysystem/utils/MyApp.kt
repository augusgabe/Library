package com.example.librarysystem.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

//用于全局获取context
class MyApp:Application() {
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}