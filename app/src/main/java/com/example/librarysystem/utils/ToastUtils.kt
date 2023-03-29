package com.example.librarysystem.utils

import android.content.Context
import android.view.Gravity
import android.widget.Toast

object ToastUtils {

    private var toast: Toast? = null

    /**
     * 常用底部提示
     *
     * @param msg
     */
    fun showToast(mContext: Context?, msg:String){
            toast?.cancel()
            toast = Toast.makeText(mContext,msg,Toast.LENGTH_LONG)
            toast?.show()
    }

    /**
     * 头部提示
     *
     * @param mContext
     * @param msg
     */
    fun showTopToast(mContext:Context,msg:String){

        toast?.cancel()
        toast = Toast.makeText(mContext,msg,Toast.LENGTH_LONG)
        toast?.let{
            it.setGravity(Gravity.TOP, 0, 0)
            it.show()
        }

    }

    /**
     * 中部提示
     *
     * @param mContext
     * @param msg
     */
    fun showCenterToast(mContext:Context?, msg:String) {
        toast?.cancel()
        toast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        toast?.let{
            it.setGravity(Gravity.CENTER, 0, 0)
            it.show()
        }
    }


    //取消提示
    fun cancel(){
        //非空判断
        toast?.cancel();
    }

}