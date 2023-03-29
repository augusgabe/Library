package com.example.librarysystem.popupWindow

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView

import com.example.librarysystem.MainActivity
import com.example.librarysystem.R

class BookDetail(val mainActivity:MainActivity,val bookView: View?,val bookId:String) {

    //弹窗相关
    private var popupWindow: PopupWindow? = null

    //阈值设定界面
    @SuppressLint("InflateParams", "CutPasteId")
    fun start(){
        //创建弹出窗口视图，和阈值设置界面布局联系
        val view: View =
            LayoutInflater.from(mainActivity).inflate(R.layout.book_lend, null)
        popupWindow = PopupWindow(
            view,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true
        )
        //设置弹窗位置
        popupWindow?.showAtLocation(view, Gravity.CENTER, 5, 5)

        //按钮
        val bookLendBtn = view.findViewById<Button>(R.id.bookLend)
        val returnBtn = view.findViewById<ImageView>(R.id.returnBtn)

        val bookText = view.findViewById<TextView>(R.id.book)
        val bookDescription = view.findViewById<TextView>(R.id.description)
        //下标线
        val bookLine = view.findViewById<View>(R.id.book_line)
        val descriptionLine = view.findViewById<View>(R.id.description_line)
        val detailContent = view.findViewById<View>(R.id.detailContent)
        val detailContent2 = view.findViewById<View>(R.id.detailContent2)

        val bookName = view.findViewById<TextView>(R.id.book_name)

        //这个是bookItem的视图，bookView
        val book_name: TextView? = bookView?.findViewById(R.id.bookName)

        //将当前页面的图书名初始化为当前item的书名
        bookName.text = book_name?.text

        //退出按钮 //弹窗消失
        returnBtn.setOnClickListener {
                popupWindow?.dismiss()
                popupWindow = null
        }

        //借阅按钮
        bookLendBtn.setOnClickListener {

            //弹出借阅信息
            val lendRegister = LendInformation(mainActivity, bookName.text.toString(),bookId)
            lendRegister.start()

        }

        bookText.setOnClickListener {
            //设置点击后显示下标线
            bookLine.visibility = View.VISIBLE
            descriptionLine.visibility = View.INVISIBLE

            //设置第一个书详情的布局显示，描述布局消失
            detailContent.visibility =  View.VISIBLE
            detailContent2.visibility =  View.GONE

        }

        bookDescription.setOnClickListener {
            //设置点击后显示下标线
            descriptionLine.visibility = View.VISIBLE
            bookLine.visibility = View.INVISIBLE

            //设置第描述的布局显示，书详情布局消失
            detailContent2.visibility =  View.VISIBLE
            detailContent.visibility =  View.GONE
        }



    }
}