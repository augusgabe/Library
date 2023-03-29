package com.example.librarysystem.entity

import android.view.View

interface OnItemClickListener {

    fun onButtonClicked(view: View?, position: Int)

    fun onDeleteBtnClicked(view: View?,position: Int)
}