package com.example.librarysystem.utils

import android.annotation.SuppressLint
import android.text.TextUtils
import com.example.librarysystem.utils.MyApp.Companion.context
import com.example.librarysystem.MainActivity
import com.example.librarysystem.ui.check.CheckViewModel
import java.text.MessageFormat

class DataSend(val mainActivity: MainActivity) {

    private val CONTROL_SLA = "sl" //声光报警器
    private val CONTROL_EL = "el" //电磁锁

    /***
     *
     * 发送命令
     *
     * **/
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    fun sendCtrlCmd(num:String, status:String, type:String) {
        when(type){
            //声光报警器
            CONTROL_SLA ->{
                if (!TextUtils.isEmpty(num)) {
                    if (status == "on") {
                        //关闭
                        val order = MessageFormat.format("Hwcsl{0}03offT", num);
                        mainActivity.sendMsg(order)
                        CheckViewModel.sl_alarm_state.value="off"
                        ToastUtils.showToast(context, "声光报警器关闭指令已发送") //此处的context是引入了MyApp里的context
                    } else if (status == "off") {
                        //打开
                        val order = MessageFormat.format("Hwcsl{0}02onT", num)
                        mainActivity.sendMsg(order)
                        CheckViewModel.sl_alarm_state.value="on"
                        ToastUtils.showToast(context, "声光报警器打开指令已发送")
                    }
                } else {
                    ToastUtils.showToast(context, "设备编号获取中...")
                }
            }

            //电磁锁
            CONTROL_EL->{
                if (!TextUtils.isEmpty(num)) {
                    if (status == "on") {
                        //关闭
                        val order = MessageFormat.format("Hwcel{0}03offT", num);
                        mainActivity.sendMsg(order)
                        CheckViewModel.el_state.value="off"
                        ToastUtils.showToast(context, "电磁器关闭指令已发送")
                    } else if (status == "off") {
                        //打开
                        val order = MessageFormat.format("Hwcel{0}02onT", num)
                        mainActivity.sendMsg(order)
                        CheckViewModel.el_state.value="on"
                        ToastUtils.showToast(context, "电磁器打开指令已发送")
                    }
                } else {
                    ToastUtils.showToast(context, "设备编号获取中...")
                }
            }

        }
    }

    //发送任意数据帧
    fun sendFrame(dataFrame:String){
        mainActivity.sendMsg(dataFrame)
    }
}