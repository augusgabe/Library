package com.example.librarysystem.ui.setting

import android.content.ContentValues
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.example.librarysystem.utils.ToastUtils
import com.example.librarysystem.MainActivity
import com.example.librarysystem.MainViewModel
import com.example.librarysystem.databinding.SettingFragmentBinding
import com.example.librarysystem.ui.check.CheckViewModel
import com.example.librarysystem.ui.database.DBViewModel
import com.example.librarysystem.utils.DataHandle
import java.util.regex.Matcher
import java.util.regex.Pattern

class SettingFragment : Fragment() {

    private var _binding: SettingFragmentBinding? = null

    private val binding get() = _binding!!

    private lateinit var mainActivity : MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SettingFragmentBinding.inflate(inflater, container, false)
        mainActivity = activity as MainActivity
        initView()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //当服务器传来数据帧，receDate值发生改变调用
        MainViewModel.receDate.observe(viewLifecycleOwner, Observer {

            val dataHandle = DataHandle(mainActivity, CheckViewModel.checkView!!)
            if(it != MainViewModel.oldReceDate){
                //弊端：当连续接收同样的数据时，无变化，但是每次的数据都需要处理，导致不能处理无变化的数据
                dataHandle.handle(it)
                MainViewModel.oldReceDate = it

            }

        } )

        binding.connBtn.setOnClickListener {
            val mainActivity = activity as MainActivity

            //服务器IP
            val ip = binding.connEtIp.text.toString().trim()
            //保存到SetViewModel中
            SetViewModel.setIp(ip)
            if (!checkIP(ip)) {
                ToastUtils.showToast(context, "请输入正确的服务器IP")
            }
            //端口号
            val port = binding.connEtPort.text.toString().trim()
            SetViewModel.setPort(port)

            if (TextUtils.isEmpty(port)) {
                ToastUtils.showToast(context, "请输入正确的端口号")
            }
            mainActivity.connectA53Server(ip,port)
        }

        binding.cardBtn.setOnClickListener {
            val cardNum:String = binding.connEtCard.text.toString().trim()

            //将当前卡号存入数据库中
            val values_temp = ContentValues().apply {
                //组装数据
                put("card",cardNum)
            }

            MainViewModel.db?.insert("Card",null,values_temp)
            ToastUtils.showToast(context,"卡号添加成功")
        }

        binding.borrowerBtn.setOnClickListener {
            val borrowerId = binding.borrwerIdIn.text.toString()
            val borrowerName = binding.borrowerNameIn.text.toString()
            val borrowerIdCard = binding.borrowerIdCardIn.text.toString()

            if(borrowerId!="" &&borrowerName!="" && borrowerIdCard!=""){
                val value = ContentValues().apply {
                    put("lenderId",borrowerId)
                    put("idCard",borrowerIdCard)
                    put("lenderName",borrowerName)
                    put("bookNum",0)
                }

                //将借阅者信息登记到数据库中
                MainViewModel.db?.insert("Lender",null,value)

//                //借阅者列表添加，和统计图表相关
//                DBViewModel.borrowerList.add(borrowerName)

                ToastUtils.showToast(context,"借阅者登记成功！")
            }else{
                ToastUtils.showToast(context,"信息不完整！")
            }

        }


    }

    //每次进入界面就会初始化
    fun initView() {
        binding.connEtIp.setText(SetViewModel.getIp())
        binding.connEtPort.setText(SetViewModel.getPort())
    }

    /**
     * 校验服务器IP
     **/
    private fun checkIP(ip:String):Boolean {
        var isIP:Boolean = false

        if (!TextUtils.isEmpty(ip)) {
            //IP地址验证规则
            val regex:String = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."+
                    "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."+
                    "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."+
                    "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$"

            val pattern: Pattern = Pattern.compile(regex)
            val matcher: Matcher = pattern.matcher(ip)
            val rs:Boolean = matcher.matches()// 字符串是否与正则表达式相匹配
            isIP = rs
        }
        return isIP
    }

    override fun onDestroy() {
        super.onDestroy()
        //保证binding有效生命周期是在onCreateView()函数和onDestroyView()函数之间
        _binding = null
    }
}