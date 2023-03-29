package com.example.librarysystem.popupWindow

import android.annotation.SuppressLint
import android.content.ContentValues
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.librarysystem.MainActivity
import com.example.librarysystem.MainViewModel
import com.example.librarysystem.R
import com.example.librarysystem.utils.MyApp
import com.example.librarysystem.utils.MyApp.Companion.context
import com.example.librarysystem.utils.ToastUtils
import java.text.SimpleDateFormat
import java.util.*

class LendInformation(val mainActivity: MainActivity, val bookName:String,val bookId:String) {

    //弹窗相关
    private var popupWindow: PopupWindow? = null

    //阈值设定界面
    @SuppressLint("InflateParams", "CutPasteId", "SetTextI18n", "SimpleDateFormat")
    fun start(){
        //创建弹出窗口视图，和阈值设置界面布局联系
        val view: View =
            LayoutInflater.from(mainActivity).inflate(R.layout.lender, null)
        popupWindow = PopupWindow(
            view,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        //设置弹窗位置
        popupWindow?.showAtLocation(view, Gravity.BOTTOM, 5, 5)

        //申请借阅按钮
        val toLendBtn = view.findViewById<Button>(R.id.toLend)
        val lenderName = view.findViewById<EditText>(R.id.bookLender)
        val lenderID = view.findViewById<EditText>(R.id.lenderID)
        val lenderDay = view.findViewById<EditText>(R.id.lendDay)
        val returnBtn = view.findViewById<ImageView>(R.id.return_lend_Btn)
        val bookLended = view.findViewById<TextView>(R.id.bookLended)

        bookLended.text = bookName

        //返回按钮
        returnBtn.setOnClickListener {
            popupWindow?.dismiss()
            popupWindow = null
        }

        toLendBtn.setOnClickListener {

            if(lenderName.text.toString()!="" && lenderDay.text.toString()!=""
                && lenderID.text.toString()!=""){
                //获取系统当前时间,(数据库相关)
                val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
                val date = Date(System.currentTimeMillis())
                val time = simpleDateFormat.format(date)

                //存储借阅信息到数据库中
                val values_temp = ContentValues().apply {
                    //组装数据
                    put("lenderIdCard",lenderID.text.toString())
                    put("lendBookName",bookName)
                    put("lenderName",lenderName.text.toString())
                    put("lendDay",lenderDay.text.toString())
                    put("lendTime",time)

                }

                //添加到数据库
                MainViewModel.db?.insert("LendInfo",null,values_temp)

                //更新当前图书借阅状态
                val updateSql = "update `Book` set bookState= '借阅中' where bookId = ${bookId.toInt()};"
                //更新数据库中该位置的数据
                MainViewModel.db?.execSQL(updateSql)

                ToastUtils.showToast(context,"借阅成功！")
                popupWindow?.dismiss()
                popupWindow = null


            }else{

                ToastUtils.showToast(context,"信息不完整！")
            }

        }

    }
}