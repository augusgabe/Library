package com.example.librarysystem.ui.bookin

import android.database.Cursor
import androidx.lifecycle.ViewModel
import com.example.librarysystem.entity.Book

object BookInViewModel : ViewModel() {

    var bookList = ArrayList<Book>()

    var cursor: Cursor? = null

    var bookNameBorrowed = ""

    var borrower = ""

    var bookId =""
    var borrowerId =""

    var isReturn = false
}