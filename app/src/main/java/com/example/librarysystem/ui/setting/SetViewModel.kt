package com.example.librarysystem.ui.setting

import androidx.lifecycle.ViewModel

object SetViewModel :ViewModel() {
    private var ip = "192.168.1.200"
    private var port = "6004"

    fun getIp():String{
        return ip
    }

    fun setIp(ip:String){
        SetViewModel.ip = ip
    }

    fun getPort():String{
        return port
    }

    fun setPort(port:String){
        SetViewModel.port = port
    }
}