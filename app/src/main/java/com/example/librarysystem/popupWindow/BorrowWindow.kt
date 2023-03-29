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
import com.example.librarysystem.ui.bookin.BookInViewModel
import com.example.librarysystem.utils.MyApp
import com.example.librarysystem.utils.ToastUtils
import java.text.SimpleDateFormat
import java.util.*

class BorrowWindow (val mainActivity: MainActivity, val bookId:String, val borrowerId:String) {

    //弹窗相关
    private var popupWindow: PopupWindow? = null

    //阈值设定界面
    @SuppressLint("InflateParams", "CutPasteId", "SetTextI18n", "SimpleDateFormat", "Recycle",
        "Range"
    )
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

        var bookLendNum = 0//当前书籍被借阅次数
        var bookNum = 0//当前借阅者借阅书籍次数

        //依据bookId和borrwerId来到数据库中查找各自信息
        val book_cursor =MainViewModel.db?.query("Book",
            null,"bookId = ?", arrayOf(bookId),null,null,null)

        val borrower_cursor =MainViewModel.db?.query("Lender",
            null,"lenderId = ?", arrayOf(borrowerId),null,null,null)

//        var bookLendText = ""
//        var lenderNameText = ""
//        var idCardText = ""

        book_cursor?.let {
            if(it.moveToFirst()){
                do{
                    //书名
                    bookLended.text  =it.getString(it.getColumnIndex("bookName"))
                    //书籍被借阅次数
                    bookLendNum = it.getInt(it.getColumnIndex("bookLendNum"))
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

                    //借书数
                    bookNum = it.getInt(it.getColumnIndex("bookNum"))

                }while(it.moveToNext())
            }


        }


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
                    put("lendBookName",bookLended.text.toString())
                    put("lenderName",lenderName.text.toString())
                    put("lendDay",lenderDay.text.toString())
                    put("lendTime",time)

                }

                //添加到数据库
                MainViewModel.db?.insert("LendInfo",null,values_temp)

                //更新当前图书借阅状态,图书借阅次数加1,人借书数加1
                bookLendNum++
                bookNum++

                val updateBookSql = "update `Book` set bookState= '借阅中',bookLendNum =  $bookLendNum " +
                        " where bookId = '$bookId'"

                val updateBorrowerSql = "update `Lender` set bookNum =  $bookNum " +
                        " where lenderId = '$borrowerId'"
                //更新数据库中该位置的数据
                MainViewModel.db?.execSQL(updateBookSql)
                MainViewModel.db?.execSQL(updateBorrowerSql)

                ToastUtils.showToast(MyApp.context,"借阅成功！")
                popupWindow?.dismiss()
                popupWindow = null

            }else{

                ToastUtils.showToast(MyApp.context,"信息不完整！")
            }

        }

    }
}