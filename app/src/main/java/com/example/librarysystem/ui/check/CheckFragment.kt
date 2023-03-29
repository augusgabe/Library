package com.example.librarysystem.ui.check

import android.annotation.SuppressLint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.librarysystem.utils.DataHandle
import com.example.librarysystem.MainActivity
import com.example.librarysystem.MainViewModel
import com.example.librarysystem.databinding.CheckFragmentBinding
import com.example.librarysystem.utils.DataSend


class CheckFragment : Fragment(){

//    启用ViewBinding功能之后，则必然会生成一个与其对应的CheckFragmentBinding类
    private var _binding: CheckFragmentBinding? = null

    private val binding get() = _binding!!

    private lateinit var mainActivity : MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CheckFragmentBinding.inflate(inflater,container,false)

        mainActivity = activity as MainActivity

        initView()

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    fun initView() {
        //设置保存的值,从共享数据中拿
        binding.connTvInfra.text = CheckViewModel.getInfrared() //有无人
        binding.connTvPm1.text = "${CheckViewModel.getDust1()} ug/m³"
        binding.connTvPm2.text = "${CheckViewModel.getDust2()} ug/m³"

        //声光报警器
        binding.tvAlarm.text = CheckViewModel.getSlNum()
        binding.swAlarm.isChecked = CheckViewModel.sl_alarm_state.value == "on"

        //电磁锁
        binding.tvLock.text = CheckViewModel.getElNum()
        binding.swLock.isChecked = CheckViewModel.el_state.value == "on"

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        CheckViewModel.checkView = requireView()
        val dataHandle = DataHandle(mainActivity, requireView())

        //当服务器传来数据帧，receDate值发生改变调用
        MainViewModel.receDate.observe(viewLifecycleOwner, Observer {

            if(it != MainViewModel.oldReceDate){
                //弊端：当连续接收同样的数据时，无变化，但是每次的数据都需要处理，导致不能处理无变化的数据
                dataHandle.handle(it)
                MainViewModel.oldReceDate = it

            }


        } )

    }

    override fun onResume() {
        super.onResume()

        //数据帧处理类
//        val dataHandle1 = DataHandle(mainActivity, requireView())

        //发送数据帧类
        val dataSend = DataSend(mainActivity)

        //声光报警器点击控制
        binding.swAlarm.setOnClickListener {
            val status = CheckViewModel.sl_alarm_state.value
            val num = CheckViewModel.getSlNum()
            val type = "sl"

            //发送相应数据帧
            if (status != null) {
                dataSend.sendCtrlCmd(num,status,type)
            }
        }

        //电磁锁点击控制
        binding.swLock.setOnClickListener {
            val status = CheckViewModel.el_state.value
            val num = CheckViewModel.getElNum()
            val type = "el"
            //发送相应数据帧
            if (status != null) {
                dataSend.sendCtrlCmd(num,status,type)
            }
        }

        //开馆按钮
        binding.openLib.setOnClickListener {
            //图书馆当前状态更新
            CheckViewModel.isOpenLibraries = true

            //红外设备开启
            binding.infraState.text = "未开启"
            CheckViewModel.infra_state.value = "off"

        }

        //闭馆按钮
        binding.closeLib.setOnClickListener {
            //图书馆当前状态更新
            CheckViewModel.isOpenLibraries = false

            //红外设备开启
            binding.infraState.text = "已开启"
            CheckViewModel.infra_state.value = "on"

            //电子锁自动开启
            binding.swLock.isChecked = true
            CheckViewModel.el_state.value = "on"

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        //保证binding有效生命周期是在onCreateView()函数和onDestroyView()函数之间
        _binding = null
    }


}