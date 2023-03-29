package com.example.librarysystem

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

//数据共享处，设置为单一类
object MainViewModel:ViewModel() {

    //接收的数据帧数据
    var receDate = MutableLiveData<String>("")
    //用于做对比，防止默认执行观察
    var oldReceDate = ""

    //数据库
    internal var db: SQLiteDatabase? = null


    fun setReceDate(Date:String){
        receDate.value=Date
    }
}