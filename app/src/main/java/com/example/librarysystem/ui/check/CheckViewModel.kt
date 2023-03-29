package com.example.librarysystem.ui.check

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

@SuppressLint("StaticFieldLeak")
object CheckViewModel:ViewModel() {

    private var infrared ="无人"//人体红外
    private var dust1 ="0"//粉尘
    private var dust2 ="0"//粉尘

    //是否处于图书馆开放时段
    var isOpenLibraries = false

    //是否非法闯入
    var isLegalIn = false

    var checkView:View? = null

    //各折线图
    var isTempLineChartOpen = false
    var isHumLineChartOpen = false
    var isIlluLineChartOpen = false
    var isO2LineChartOpen = false
    var isCo2LineChartOpen = false
    var isBpLineChartOpen = false

    //控制设备的编号
    private var sl_alarm_num = "" //声光报警器编号
    private var el_num = "" //电磁锁编号

    //控制设备的状态
    var sl_alarm_state = MutableLiveData<String>("off")  //声光报警器状态
    var el_state = MutableLiveData<String>("off") //电磁锁状态
    var infra_state = MutableLiveData<String>("off") //红外设备状态

    //控制设备的获取与设置
    fun getSlNum():String{
        return sl_alarm_num
    }
    fun setSlNum(slNum:String){
        sl_alarm_num = slNum
    }

    fun getElNum():String{
        return el_num
    }
    fun setElNum(elNum:String){
        el_num = elNum
    }

    //常用展示数据的获取与设置
    fun getInfrared():String {
        return infrared
    }
    fun setInfrared( infrared:String) {
        CheckViewModel.infrared = infrared
    }

    fun getDust1():String {
        return dust1
    }
    fun setDust1( dust1:String) {
        CheckViewModel.dust1 = dust1
    }

    fun getDust2():String {
        return dust2
    }
    fun setDust2( dust2:String) {
        CheckViewModel.dust2 = dust2
    }



}