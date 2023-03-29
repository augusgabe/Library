package com.example.librarysystem.ui.database

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.librarysystem.MainActivity
import com.example.librarysystem.MainViewModel
import com.example.librarysystem.utils.ToastUtils
import com.example.librarysystem.databinding.DatabaseFragmentBinding
import com.example.librarysystem.entity.LendInfo
import com.example.librarysystem.entity.LendInfoAdapter
import com.example.librarysystem.popupWindow.ColumnChart
import com.example.librarysystem.ui.check.CheckViewModel
import com.example.librarysystem.utils.DataHandle
import java.text.SimpleDateFormat
import java.util.*


class DatabaseFragment : Fragment() {

    private var _binding: DatabaseFragmentBinding? = null

    private val binding get() = _binding!!

    private lateinit var mainActivity : MainActivity

    private lateinit var lendInfoAdapter: LendInfoAdapter

    //选择日期Dialog
    private var datePickerDialog: DatePickerDialog? = null

    private val calendar: Calendar  = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DatabaseFragmentBinding.inflate(inflater, container, false)

        DBViewModel.cursor = MainViewModel.db?.query("LendInfo",null,null,null,null,null,null)

        mainActivity = activity as MainActivity

        val layoutManager = LinearLayoutManager(context)

        layoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager.stackFromEnd = true;//列表再底部开始展示，反转后由上面开始展示
        layoutManager.reverseLayout = true;//列表翻转

        binding.tableRecyclerView.layoutManager = layoutManager
        //初始化列表
        initLendInfo()

        return binding.root
    }

    @SuppressLint("Range")
    fun initLendInfo(){
        //从数据库中加载数据
        DBViewModel.lendInfoList.clear()

        DBViewModel.cursor?.let {
            if(it.moveToFirst()){
                do{
                    //遍历cursor对象，取出数据并添加至sensorList中
                    val lenderName = it.getString(it.getColumnIndex("lenderName"))
                    val lenderIdCard = it.getString(it.getColumnIndex("lenderIdCard"))
                    val lendBookName = it.getString(it.getColumnIndex("lendBookName"))
                    val lendDay = it.getString(it.getColumnIndex("lendDay"))
                    val lendTime = it.getString(it.getColumnIndex("lendTime"))

                    //从数据库中取值添加至列表
                    DBViewModel.lendInfoList.add(LendInfo(lenderName,lenderIdCard,lendDay,lendBookName,
                    lendTime))

                }while(it.moveToNext())
            }

        }

        lendInfoAdapter = LendInfoAdapter(DBViewModel.lendInfoList)
        binding.tableRecyclerView.adapter = lendInfoAdapter

    }

    //比较两个日期，通过时间戳
    @SuppressLint("SimpleDateFormat")
    fun compareDate(date1:String,date2:String): Boolean {
        val pattern = "yyyy.MM.dd"
        val simpleDateFormat = SimpleDateFormat(pattern)
        //将日期字符串转换为日期格式
        val d1 = simpleDateFormat.parse(date1)
        val d2 = simpleDateFormat.parse(date2)

        //获取两个日期格式的时间戳
        val timeStamp1 = d1.time
        val timeStamp2 = d2.time

        //若日期1在日期2之后，返回true
        return timeStamp1>=timeStamp2
    }

    //显示日历控件
    private fun showDailog() {
        datePickerDialog = DatePickerDialog(
            requireContext(),
            OnDateSetListener { _, year, monthOfYear, dayOfMonth -> //monthOfYear 得到的月份会减1所以我们要加1
                val time =
                    year.toString() + "." + (monthOfYear + 1).toString() + "." + dayOfMonth.toString()
                Log.d("测试", time)

                //将当前选择的日期显示
                binding.stDateShow.text = time

                //将当前选择的日期存储
                DBViewModel.time = time
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)

        )
        datePickerDialog!!.show()
        //自动弹出键盘问题解决
        datePickerDialog!!.window!!
            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

    }


    override fun onResume() {
        super.onResume()

        //当服务器传来数据帧，receDate值发生改变调用
        MainViewModel.receDate.observe(viewLifecycleOwner, Observer {
            val dataHandle = DataHandle(mainActivity, CheckViewModel.checkView!!)
            if(it != MainViewModel.oldReceDate){
                //弊端：当连续接收同样的数据时，无变化，但是每次的数据都需要处理，导致不能处理无变化的数据
                dataHandle.handle(it)
                MainViewModel.oldReceDate = it

            }
        } )

        //筛选按钮点击事件
        binding.searchBtnLend.setOnClickListener{
                queryFromDate(DBViewModel.time)
        }

        binding.timeSearch.setOnClickListener {
                showDailog()
        }

        binding.deleteBtn.setOnClickListener {
            MainViewModel.db?.delete("LendInfo",null, null)
            initLendInfo()//更新列表

            ToastUtils.showToast(context,"数据删除成功！")
        }

        binding.lenderStatic.setOnClickListener {
            val columnChart = ColumnChart(mainActivity)

            //从数据库中，将当前的借阅者姓名综合成一个arraylist
            findBorrowers()
            columnChart.initxValues(DBViewModel.borrowerList)
            columnChart.inityValues(DBViewModel.borrowerBookNumList)
            columnChart.setxName("借阅者")
            columnChart.setyName("书本")
            columnChart.setyHeight(13f)

            columnChart.showChart()
        }

        binding.bookIdStatic.setOnClickListener {
            val columnChart = ColumnChart(mainActivity)

            findBooks()

            columnChart.initxValues(DBViewModel.bookNameList)
            columnChart.inityValues(DBViewModel.bookNumList)
            columnChart.setxName("书本")
            columnChart.setyName("借阅次数")
            columnChart.setyHeight(13f)

            columnChart.showChart()
        }
    }
    @SuppressLint("Range", "NotifyDataSetChanged")
    private fun queryFromDate(date:String){
        //若日期在所选日期之后，加入list
        Log.d("测试","sensorList:"+DBViewModel.lendInfoList.toString())
        //移除所有list数据
        DBViewModel.lendInfoList.clear()

        var situation = 0
        DBViewModel.lenderName = binding.lenderSearch.text.toString()

        situation = if(DBViewModel.time == "" && DBViewModel.lenderName==""){
            1
        }else if (DBViewModel.time != "" && DBViewModel.lenderName==""){
            2
        }else if (DBViewModel.time == "" && DBViewModel.lenderName!= "" ){
            3
        }else {
            4
        }

        DBViewModel.cursor?.let {

            Log.d("测试","Cursor:执行")
            if(it.moveToFirst()){
                do{
                    //遍历cursor对象，取出数据并添加至lendInfoList中
                    val lenderName = it.getString(it.getColumnIndex("lenderName"))
                    val lenderIdCard = it.getString(it.getColumnIndex("lenderIdCard"))
                    val lendBookName = it.getString(it.getColumnIndex("lendBookName"))
                    val lendDay = it.getString(it.getColumnIndex("lendDay"))
                    val lendTime = it.getString(it.getColumnIndex("lendTime"))

//                    Log.d("database",cursorDate+","+cursorType+","+data)

                    //查询的四种情况
                    when(situation){
                        //什么也没筛选
                        1->{
                            Log.d("测试","无筛选")
                        }
                        //时间为条件筛选
                        2->{
                            //筛选选定日期之后的数据
                            if(compareDate(lendTime,date)){
                                DBViewModel.lendInfoList.add(LendInfo(lenderName,lenderIdCard,lendDay,lendBookName,
                                    lendTime))
                            }
                        }
                        //传感器类型为条件筛选
                        3->{
                            if(lenderName == DBViewModel.lenderName){
                                DBViewModel.lendInfoList.add(LendInfo(lenderName,lenderIdCard,lendDay,lendBookName,
                                    lendTime))
                            }
                        }
                        //两个条件都有筛选
                        4->{
                            if(compareDate(lendTime,date) && lenderName == DBViewModel.lenderName){
                                DBViewModel.lendInfoList.add(LendInfo(lenderName,lenderIdCard,lendDay,lendBookName,
                                    lendTime))
                            }
                        }
                    }

                }while(it.moveToNext())
            }

        }

//        Log.d("测试","sensorList:"+DBViewModel.sensorList.toString())

        //让recyclerView使用自定义的适配器，生成相应列表
        lendInfoAdapter.setList(DBViewModel.lendInfoList)
        binding.tableRecyclerView.adapter = lendInfoAdapter
        ToastUtils.showToast(context,"查找数据成功！")

    }

    @SuppressLint("Range", "Recycle")
    fun findBorrowers(){

        //从数据库中加载数据
        DBViewModel.borrowerList.clear()
        DBViewModel.borrowerBookNumList.clear()

        val cursor = MainViewModel.db?.query("Lender",null,null,null,null,null,null)

        cursor?.let {
            if(it.moveToFirst()){
                do{
                    //遍历cursor对象，取出数据并添加至sensorList中
                    val borrowerName = it.getString(it.getColumnIndex("lenderName"))
                    val borrowerBookNum = it.getInt(it.getColumnIndex("bookNum"))

                    //从数据库中取值添加至列表
                    DBViewModel.borrowerList.add(borrowerName)
                    DBViewModel.borrowerBookNumList.add(borrowerBookNum)

                }while(it.moveToNext())
            }

        }
    }

    @SuppressLint("Range", "Recycle")
    fun findBooks(){

        //从数据库中加载数据
        DBViewModel.bookNameList.clear()
        DBViewModel.bookNumList.clear()

        val cursor = MainViewModel.db?.query("Book",null,null,null,null,null,null)

        cursor?.let {
            if(it.moveToFirst()){
                do{
                    //遍历cursor对象，取出数据并添加至sensorList中
                    val bookName = it.getString(it.getColumnIndex("bookName"))
                    val bookLendNum = it.getInt(it.getColumnIndex("bookLendNum"))

                    //从数据库中取值添加至列表
                    DBViewModel.bookNameList.add(bookName)
                    DBViewModel.bookNumList.add(bookLendNum)

                }while(it.moveToNext())
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //保证binding有效生命周期是在onCreateView()函数和onDestroyView()函数之间
        _binding = null
    }

}