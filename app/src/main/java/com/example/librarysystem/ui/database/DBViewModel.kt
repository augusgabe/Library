package com.example.librarysystem.ui.database

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.ViewModel
import com.example.librarysystem.entity.LendInfo

object DBViewModel:ViewModel() {

    var time=""

    var lenderName=""

    //查询的数据库结果
    var cursor:Cursor? = null

    var lendInfoList= ArrayList<LendInfo>()
    var borrowerList= ArrayList<String>()
    var borrowerBookNumList = ArrayList<Int>()

    var bookNameList = ArrayList<String>()
    var bookNumList = ArrayList<Int>()

    var xValues = ArrayList<String>()

}