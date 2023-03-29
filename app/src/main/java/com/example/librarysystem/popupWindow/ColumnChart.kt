package com.example.librarysystem.popupWindow

import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import com.example.librarysystem.MainActivity
import com.example.librarysystem.R
import com.example.librarysystem.utils.MyApp.Companion.context
import com.example.librarysystem.utils.ToastUtils
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.util.ChartUtils
import lecho.lib.hellocharts.view.ColumnChartView


class ColumnChart(val mainActivity: MainActivity) {


    //弹窗相关
    private var popupWindow: PopupWindow? = null

    private lateinit var mColumnChartView: ColumnChartView

    /*========== 数据相关 ==========*/
    private var mColumnChartData : ColumnChartData? = null  //柱状图数据

    val columnList: MutableList<Column> = ArrayList() //柱子列表

    lateinit var subcolumnValueList: MutableList<SubcolumnValue?> //子柱列表（即一个柱子，因为一个柱子可分为多个子柱）

    val axisValues = ArrayList<AxisValue>() //自定义横轴坐标值

    val axisY: Axis = Axis().setHasLines(true)
    //将自定义x轴显示值传入构造函数
    val axisX = Axis(axisValues)
    var xValues = arrayListOf("1","2","3","4","5","6")//x轴标签,默认
    var yValues = arrayListOf(1,2,3,4,5,6)

    var xName = ""
    var yName = ""
    var yHeight= 103f

    fun showChart() {
        //创建弹出窗口视图，和阈值设置界面布局联系
        val view: View =
            LayoutInflater.from(mainActivity).inflate(R.layout.chart, null)
        popupWindow = PopupWindow(
            view,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        //设置弹窗位置
        popupWindow?.showAtLocation(view, Gravity.CENTER, 5, 5)

        Log.d("Test","show")

        mColumnChartView = view.findViewById(R.id.columnChart)

        initView()

        //柱状图柱子点击事件
        mColumnChartView.onValueTouchListener = object : ColumnChartOnValueSelectListener{
            override fun onValueDeselected() {
                TODO("Not yet implemented")
            }

            override fun onValueSelected(
                columnIndex: Int,
                subcolumnIndex: Int,
                value: SubcolumnValue?
            ) {
                ToastUtils.showToast(context, xValues[columnIndex] + "借阅数 : " +
                        value!!.value.toInt())
            }

        }

        //关闭按钮
        val imageView = view.findViewById<View>(R.id.iv_close_light) as ImageView

        imageView.setOnClickListener {

            //弹窗消失
            popupWindow?.dismiss()
            popupWindow = null


        }

    }


    fun setxName(Xname:String){
        this.xName = Xname
    }

    fun setyName(yName:String){
        this.yName = yName
    }

    fun setyHeight(height:Float){
       this.yHeight = height
    }

    //初始化x坐标标签
    fun initxValues(xValues1:ArrayList<String>){
          xValues = xValues1
//        xValues = DBViewModel.xValues
    }

    fun inityValues(yValues1:ArrayList<Int>){
        yValues = yValues1
    }

    fun initView(){

        /*========== 柱状图数据填充 ==========*/
        //这个里面每个主柱只有一个子柱
        for (i in 0 until xValues.size) {

            subcolumnValueList = ArrayList() //每次循环初始化一下，这样就让每个主柱只会有一个子柱

            //添加子柱列表的数据
            subcolumnValueList.add(
                SubcolumnValue(
                    yValues[i].toFloat(),//柱子高度的值
                    ChartUtils.pickColor()
                )
            )

            //主柱
            val column = Column(subcolumnValueList)
            columnList.add(column)
            column.setHasLabels(true);//☆☆☆☆☆设置列标签
            //只有当点击时才显示列标签
//            column.setHasLabelsOnlyForSelected(true);

            //设置横轴坐标值
            axisValues.add( AxisValue(i.toFloat()).setLabel(xValues[i]) )
        }

        mColumnChartData = ColumnChartData(columnList) //设置数据

        /*===== 坐标轴相关设置 =====*/
        axisX.name = xName //设置横轴名称
        axisY.name = yName //设置竖轴名称
        mColumnChartData!!.axisXBottom = axisX //设置横轴
        mColumnChartData!!.axisYLeft = axisY //设置竖轴

        //以上所有设置的数据、坐标配置都已存放到mColumnChartData中，接下来给mColumnChartView设置这些配置
        mColumnChartView.columnChartData = mColumnChartData

        //设置竖轴最大高度
        val v = mColumnChartView.currentViewport //获得当前柱形图视图窗口
        v.top = yHeight
        mColumnChartView.maximumViewport = v
        mColumnChartView.currentViewport = v

    }
}