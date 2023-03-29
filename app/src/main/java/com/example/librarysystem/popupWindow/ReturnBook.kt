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
import com.example.librarysystem.utils.ToastUtils
import java.text.SimpleDateFormat
import java.util.*

class ReturnBook (val mainActivity: MainActivity, val bookId:String, val borrowerId:String) {

    //弹窗相关
    private var popupWindow: PopupWindow? = null

    //阈值设定界面
    @SuppressLint("InflateParams", "CutPasteId", "SetTextI18n", "SimpleDateFormat", "Recycle",
        "Range"
    )
    fun start(){
        //创建弹出窗口视图，和阈值设置界面布局联系
        val view: View =
            LayoutInflater.from(mainActivity).inflate(R.layout.returnbook, null)
        popupWindow = PopupWindow(
            view,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        //设置弹窗位置
        popupWindow?.showAtLocation(view, Gravity.BOTTOM, 5, 5)

        //申请借阅按钮
        val toReturnBtn = view.findViewById<Button>(R.id.toReturn)
        val lenderName = view.findViewById<EditText>(R.id.bookLender_return)
        val lenderID = view.findViewById<EditText>(R.id.lenderID_return)
        val returnBtn = view.findViewById<ImageView>(R.id.return_Btn)
        val bookLended = view.findViewById<TextView>(R.id.bookLended_return)

        //依据bookId和borrwerId来到数据库中查找各自信息
        val book_cursor = MainViewModel.db?.query("Book",
            null,"bookId = ?", arrayOf(bookId),null,null,null)

        val borrower_cursor = MainViewModel.db?.query("Lender",
            null,"lenderId = ?", arrayOf(borrowerId),null,null,null)

        book_cursor?.let {
            if(it.moveToFirst()){
                do{
                    //书名
                    bookLended.text  =it.getString(it.getColumnIndex("bookName"))

                }while(it.moveToNext())
            }

        }

        borrower_cursor?.let {
            if(it.moveToFirst()){
                do{
                    //人名
                    lenderName.setText(it.getString(it.getColumnIndex("lenderName")))

                    //身份证
                    lenderID.setText(it.getString((it.getColumnIndex("idCard"))))


                }while(it.moveToNext())
            }


        }


        //返回按钮
        returnBtn.setOnClickListener {
            popupWindow?.dismiss()
            popupWindow = null
        }

        toReturnBtn.setOnClickListener {

            if(lenderName.text.toString()!= "" && lenderID.text.toString()!=""){
                //获取系统当前时间,(数据库相关)
                val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
                val date = Date(System.currentTimeMillis())
                val time = simpleDateFormat.format(date)

                val updateBookSql = "update `Book` set bookState= '未借阅' " +
                        " where bookId = '$bookId'"

                //更新数据库中该位置的数据
                MainViewModel.db?.execSQL(updateBookSql)


                ToastUtils.showToast(MyApp.context,"归还成功！")
                popupWindow?.dismiss()
                popupWindow = null

            }else{

                ToastUtils.showToast(MyApp.context,"信息不完整！")
            }

        }

    }
}