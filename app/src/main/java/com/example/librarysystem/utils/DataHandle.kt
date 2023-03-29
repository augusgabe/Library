package com.example.librarysystem.utils

import android.annotation.SuppressLint
import android.content.ContentValues
import android.util.Log
import android.view.View
import android.widget.Switch
import android.widget.TextView
import com.example.librarysystem.ui.check.CheckViewModel
import com.example.librarysystem.MainActivity
import com.example.librarysystem.MainViewModel
import com.example.librarysystem.R
import com.example.librarysystem.entity.LendInfo
import com.example.librarysystem.popupWindow.BorrowWindow
import com.example.librarysystem.popupWindow.LendInformation
import com.example.librarysystem.popupWindow.ReturnBook
import com.example.librarysystem.ui.bookin.BookInViewModel
import com.example.librarysystem.ui.database.DBViewModel
import com.example.librarysystem.utils.MyApp.Companion.context
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.*

class DataHandle(private val mainActivity: MainActivity, val checkView:View) {

    //数据展示
    private val CARD = "cc" //卡号
    private val SENSOR_DA = "da"
    private val SENSOR_DB = "db"
    private val SENSOR_HI = "hi"
    private val RFID = "ID"


    //控制
    private val CONTROL_SLA = "sl" //声光报警器
    private val CONTROL_EL = "el" //电磁锁


@SuppressLint("SimpleDateFormat", "SetTextI18n", "UseSwitchCompatOrMaterialCode", "Recycle",
    "Range"
)

fun handle(recvData: String){
        try {
            val type = recvData.substring(3, 5) //设备类型
            //获取系统当前时间,(数据库相关)
            val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss") //
            val date = Date(System.currentTimeMillis())
            val time = simpleDateFormat.format(date)

            val dataSend = DataSend(mainActivity)

            Log.d("databaseTest",time)
            when {
                // 粉尘1
                type.endsWith(SENSOR_DA) -> {

                    //获取烟雾1数据展示的控件
                    val connTvDust1 = checkView.findViewById<TextView>(R.id.conn_tv_pm1)

                    //Hwdda0139PM1:00100T
                    val pm1:String = recvData.substring(13, 18).toInt().toString()//去除数据前头的0

                    CheckViewModel.setDust1(pm1) //保存传来的值
                    connTvDust1?.text = "$pm1 ug/m³"

                }

                // 粉尘2
                type.endsWith(SENSOR_DB) -> {

                    //获取烟雾1数据展示的控件
                    val connTvDust2 = checkView.findViewById<TextView>(R.id.conn_tv_pm2)

                    //Hwdda0139PM1:00100T
                    val pm2:String = recvData.substring(13, 18).toFloat().toString()//去除数据前头的0

                    CheckViewModel.setDust1(pm2) //保存传来的值
                    connTvDust2?.text = "$pm2 ug/m³"

                }

                //红外Hwdhi01011T（有人）                Hwdhi01010T（没人）
                type.endsWith(SENSOR_HI) -> {

                    //当红外设备开启时才接收数据
                    if(CheckViewModel.infra_state.value=="on"){

                        //获取红外数据展示的控件
                        val connTvInfra = checkView.findViewById<TextView>(R.id.conn_tv_infra)
                        val swAlarm = checkView.findViewById<Switch>(R.id.swAlarm)
                        val tvAlarm = checkView.findViewById<TextView>(R.id.tv_alarm)
                        val infra:String = recvData[9].toString()

                        when(infra){
                            "0"->{
                                CheckViewModel.setInfrared("无人")
                                //在报警之后，监测到无人之后，关闭报警器
                                if(CheckViewModel.sl_alarm_state.value.toString()=="on")
                                {
                                    dataSend.sendCtrlCmd(CheckViewModel.getSlNum(),
                                        "on","sl")
                                    swAlarm.isChecked = false
                                }


                            }
                            "1"->{
                                CheckViewModel.setInfrared("有人")

                                //依据电磁锁是否关闭判定是否非法闯入,若开启则为非法闯入，关闭为正常进入
                                CheckViewModel.isLegalIn = CheckViewModel.el_state.value.toString()=="off"

                                //监测到有人，若非法闯入则开启声光报警器
                                if(!CheckViewModel.isLegalIn){
                                    dataSend.sendCtrlCmd(CheckViewModel.getSlNum(),
                                        "off","sl")
                                    swAlarm.isChecked = true
                                }

                            }
                        }

                        connTvInfra.text = CheckViewModel.getInfrared()
                    }

                }

                //声光报警器
                type.endsWith(CONTROL_SLA) -> {
                    val swAlarm = checkView.findViewById<Switch>(R.id.swAlarm)
                    val tvAlarm = checkView.findViewById<TextView>(R.id.tv_alarm)

                    val num:String= recvData.substring(5, 7)
                    val state = recvData.substring(9,11)

                    CheckViewModel.setSlNum(num)
                    tvAlarm?.text = num
                    Log.d("Check","receDate执行了")
                    //判断声光报警器当前状态
                    if (state == "on"){
                        CheckViewModel.sl_alarm_state.value = "on"
                        swAlarm?.isChecked = true
                    }else {
                        CheckViewModel.sl_alarm_state.value = "off"
                        swAlarm?.isChecked = false
                    }
                }

                //卡号
                type.endsWith(CARD) ->{
                    val cardNum = recvData.substring(7,13)
                    val cardAction = recvData[13].toString()

                    when(cardAction){
                        //删除该数据帧上存的卡号
                        "d"->{
                            MainViewModel.db?.delete("Card","card = ?", arrayOf(cardNum))
                        }

                        //保存该数据帧上存的卡号
                        "s"->{
                            //将当前卡号存入数据库中
                            val cardValue = ContentValues().apply {
                                //组装数据
                                put("card",cardNum)
                            }

                            MainViewModel.db?.insert("Card",null,cardValue)
                        }

                        //查找并匹配该数据帧上存的卡号
                        "f"->{
                            val cursor_temp= MainViewModel.db?.query("Card",null,"card = ?",
                                arrayOf(cardNum),null,null,null)
                            Log.d("card",""+cursor_temp)

                            //若查询数据库结果不为空就发送电磁锁控制数据帧
                            cursor_temp?.let{
                                if(it.moveToFirst()){
                                    do{
                                        //遍历cursor对象，取出数据并添加至sensorList中
                                        val cursorCard = it.getString(it.getColumnIndex("card"))
                                        Log.d("card",""+cursorCard)
                                        if(cursorCard!=null){
                                            val order = MessageFormat.format("Hwcel{0}02onT", CheckViewModel.getElNum())
                                            dataSend.sendFrame(order)

                                            //更改并保存当前电磁锁状态
                                            val swLock = checkView.findViewById<Switch>(R.id.swLock)
                                            CheckViewModel.el_state.value = "on"
                                            swLock?.isChecked = true
                                        }
                                    }while(it.moveToNext())
                                }

                            }
                        }
                    }


                }

                //电磁锁
                type.endsWith(CONTROL_EL) -> {
                    val swLock = checkView.findViewById<Switch>(R.id.swLock)
                    val tvLock = checkView.findViewById<TextView>(R.id.tv_lock)

                    val num:String= recvData.substring(5, 7)
                    val state = recvData.substring(9,11)

                    CheckViewModel.setElNum(num)
                    tvLock?.text = num
                    //判断电磁锁当前状态
                    if (state == "on"){
                        CheckViewModel.el_state.value = "on"
                        swLock?.isChecked = true
                    }else {
                        CheckViewModel.el_state.value = "off"
                        swLock?.isChecked = false

                    }
                }

                //RFID上传借阅人信息或图书
                //HwdIDborr0508C8D5621HT
                type.endsWith(RFID) ->{
                    Log.d("Test","执行rfid的数据帧")
                    val info:String= recvData.substring(5, 9)
                    val num_id = recvData.substring(13,21)

                    when(info){
                        "book"->{
                            //找到该编号
                            val cursor_temp= MainViewModel.db?.query("Book",null,"bookId = ?",
                                arrayOf(num_id),null,null,null)

                            if (cursor_temp==null){
                                ToastUtils.showToast(context,"查无此书编号！")
                            }else{//弹出窗口
                                //先将查询正确的bookId暂存
                                BookInViewModel.bookId = num_id
                                var bookName = ""

                                cursor_temp.let {
                                    if(it.moveToFirst()){
                                        do{
                                            bookName = it.getString( it.getColumnIndex("bookName") )
                                            val bookState = it.getString( it.getColumnIndex("bookState") )

                                            //判断是否是归还
                                            when(bookState){
                                                "借阅中"->{
                                                    BookInViewModel.isReturn = true
                                                }

                                                "未借阅"->{
                                                    BookInViewModel.isReturn =false
                                                }
                                            }

                                        }while(it.moveToNext())
                                    }
                                }

                                Log.d("Test","书的名字"+bookName)

                                //暂存获得的书名
                                BookInViewModel.bookNameBorrowed = bookName

                                //只有当书名和人名都有，也就是两次卡都滴成功了，才开启借阅界面
                                if(BookInViewModel.bookNameBorrowed != "" && BookInViewModel.borrower!=""){

                                    //归还图书
                                    if(BookInViewModel.isReturn){
                                        val returnBook = ReturnBook(mainActivity,
                                            BookInViewModel.bookId,BookInViewModel.borrowerId)

                                        returnBook.start()

                                    }else{//开借阅弹窗
                                        //开弹窗
                                        val borrowWindow = BorrowWindow(mainActivity,
                                            BookInViewModel.bookId,BookInViewModel.borrowerId)

                                        borrowWindow.start()
                                    }

                                    //清空,说明当前的两个不匹配，需要重新滴卡2个
                                    BookInViewModel.bookId=""
                                    BookInViewModel.borrowerId=""
                                    BookInViewModel.bookNameBorrowed = ""
                                    BookInViewModel.borrower = ""
                                }

                            }

                        }

                        "borr"->{
                            //找到该编号
                            val cursor_temp= MainViewModel.db?.query("Lender",null,"lenderId = ?",
                                arrayOf(num_id),null,null,null)

                            if (cursor_temp==null){
                                ToastUtils.showToast(context,"查无此人编号！")
                            }else{//弹出窗口

                                BookInViewModel.borrowerId = num_id

                                var lenderName = ""

                                cursor_temp.let {
                                    if(it.moveToFirst()){
                                        do{
                                            lenderName = it.getString( it.getColumnIndex("lenderName") )

                                        }while(it.moveToNext())
                                    }
                                }
                                Log.d("Test","书的名字"+lenderName)
                                BookInViewModel.borrower = lenderName

                                if(BookInViewModel.bookNameBorrowed != "" && BookInViewModel.borrower!=""){
                                    //归还图书
                                    if(BookInViewModel.isReturn){
                                        val returnBook = ReturnBook(mainActivity,
                                            BookInViewModel.bookId,BookInViewModel.borrowerId)

                                        returnBook.start()

                                    }else{//开借阅弹窗
                                        //开弹窗
                                        val borrowWindow = BorrowWindow(mainActivity,
                                            BookInViewModel.bookId,BookInViewModel.borrowerId)

                                        borrowWindow.start()
                                    }
                                    //清空
                                    BookInViewModel.bookId=""
                                    BookInViewModel.borrowerId=""
                                    BookInViewModel.bookNameBorrowed = ""
                                    BookInViewModel.borrower = ""
                                }

                            }
                        }
                    }
                }


            }
        } catch ( e:Exception) {
            e.printStackTrace()
        }
    }

}