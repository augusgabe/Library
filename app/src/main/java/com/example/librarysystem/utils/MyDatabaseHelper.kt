package com.example.librarysystem.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class MyDatabaseHelper(val context: Context?, name: String, version: Int) :
    SQLiteOpenHelper(context, name, null, version) {

    //如果数据库中不存在表就创建一个
    private val createBook = "create table if not exists Book (" +
            "bookId text primary key ," +
            "bookName text," +
            "bookPosition text," +
            "bookLendNum integer,"+
            "bookState text) "

    //如果数据库中不存在表就创建一个
    private val createCard = "create table if not exists Card (" +
            "id integer primary key autoincrement," +
            "card text)"

    private val createLendInfo = "create table if not exists LendInfo (" +
            "lendInfoId integer primary key autoincrement," +
            "lenderName text," +
            "lenderIdCard text," +
            "lendBookName text," +
            "lendDay text," +
            "lendTime text" +
            ")"

    private val createLender = "create table if not exists Lender (" +
            "lenderId text primary key," +
            "lenderName text," +
            "idCard text," +
            "bookNum integer)"

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(createBook)
        db?.execSQL(createCard)
        db?.execSQL(createLendInfo)
        db?.execSQL(createLender)
        Log.d("table", "数据表创建成功")
    }

    //更新数据库
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("drop table if exists Book")
        db?.execSQL("drop table if exists Card")
        db?.execSQL("drop table if exists LendInfo")
        db?.execSQL("drop table if exists Lender")
        onCreate(db)
    }

}