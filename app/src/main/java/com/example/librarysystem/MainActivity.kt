package com.example.librarysystem

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.librarysystem.utils.ToastUtils
import com.example.librarysystem.utils.MyDatabaseHelper
import com.example.librarysystem.databinding.ActivityMainBinding
import com.example.librarysystem.ui.database.DBViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding

    private var isExit:Boolean = false
    private lateinit var mViewModel: MainViewModel

    //线程池
    private lateinit var mThreadPool: ExecutorService
    internal lateinit var handler: Handler

    //socket
    private var socket: Socket? = null
    // 输入流对象
    private  var isStream: InputStream? = null
    // 输出流对象
    private  var outputStream: OutputStream? = null

    private val CONN_SUCCESS = 1//连接成功
    private val CONN_FAIL = 2//连接失败
    private val CONN_TIME_OUT = 3//连接超时
    private val RECV_DATA = 4;//接收到的数据

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Test","MonCreate")

        //通过binding设置MainActivity能获取各个布局元素中的id来调用，避免写过多的findById啥的
        binding =ActivityMainBinding.inflate(layoutInflater)


        //隐藏原来的标题栏
//        supportActionBar?.hide()
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // 将每个菜单 ID 作为一组 Id 传递，因为应将每个菜单视为顶级目标。
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_check, R.id.navigation_bookIn, R.id.navigation_database,R.id.navigation_setting
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        mViewModel = MainViewModel
        //线程池中开启线程
        mThreadPool = Executors.newCachedThreadPool()
        handler = @SuppressLint("HandlerLeak") object: Handler(){
            override fun handleMessage( msg: Message){
                Log.d("Handler", msg.what.toString())
                when(msg.what){
                    CONN_SUCCESS->{
                        ToastUtils.showToast(this@MainActivity, "连接成功")
                    }

                    CONN_FAIL->{
                        ToastUtils.showToast(this@MainActivity, "连接失败");
                    }
                    CONN_TIME_OUT->{
                        ToastUtils.showToast(this@MainActivity, "连接超时");
                    }
                    // 获取数据
                    RECV_DATA->{
                        Log.d("T","获取的数据"+(msg.obj as String))
                        checkDataShow(msg.obj as String)
                    }
                    //延迟1秒，可调灯再次生效
                    0x117->{
                        val bar = msg.obj as SeekBar
                        bar.isEnabled = true
                    }
                    else ->{        println("未知连接")       }
                }
            }
        }

        //创建或打开数据库和表Sensor
        MainViewModel.db = MyDatabaseHelper(this,"BookStore.db",6).writableDatabase
    }

    /***
     *
     *  发送消息
     *
     * ***/
    internal fun sendMsg( msg:String) {
        Log.d("sendMsg", "sendMsg 发送消息 msg =" + msg);
        // 利用线程池直接开启一个线程 & 执行该线程
        mThreadPool.execute {
            if (socket != null) {
                try {
                    // 步骤1：从Socket 获得输出流对象OutputStream
                    // 该对象作用：发送数据
                    outputStream = socket?.getOutputStream()
                    // 步骤2：写入需要发送的数据到输出流对象中
                    outputStream?.write(msg.toByteArray(Charsets.UTF_8));
                    // 步骤3：发送数据到服务端
                    outputStream?.flush()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun checkDataShow(receDate:String){
        Log.d("Rece","checkDate执行")
        mViewModel.setReceDate(receDate) // 让MainViewModel中数据变化，各Fragment再对receDate进行监听
        Log.d("mViewModel","实例 "+mViewModel)
    }

    /***
     * 连接A53服务器
     * **/
    fun connectA53Server(ip: String,port: String){
        //隐藏软键盘
        hideSoftKeyboard(this@MainActivity)

        Log.d("Main", "ip=$ip")
        Log.d("Main", "port=$port")

        //先关闭socket
        closeSocket()
        //连接socket
        connectSocket(ip, port)
        //接收数据
        recvSocketData()
    }

    /**
     * 连接socket
     ***/
    private fun connectSocket(ip:String, port:String) {
        // 利用线程池直接开启一个线程 & 执行该线程
        mThreadPool.execute {
            try {
                // 创建Socket对象 & 指定服务端的IP 及 端口号
                Log.d("Main","socket创建")
                socket = Socket(ip, Integer.parseInt(port))
                Log.d("Main","socket 指定")
                //如果连接
                if (socket!!.isConnected) {
                    val msg = Message()
                    msg.what = CONN_SUCCESS
                    handler.sendMessage(msg)
                } else {
                    val msg = Message()
                    msg.what = CONN_FAIL
                    handler.sendMessage(msg)
                }

            } catch (e: SocketTimeoutException) {
                val msg = Message()
                msg.what = CONN_TIME_OUT
                handler.sendMessage(msg)
            } catch (e: IOException) {
//                socket = null
                Log.d("Main", "IO错误$socket")
                Log.d("Wrong",e.toString())
                val msg = Message()
                msg.what = CONN_FAIL
                handler.sendMessage(msg)
            }
        }
    }

    /***
     *
     * 关闭socket
     *
     * **/
    private fun closeSocket() {
        try {
            Log.d("Main","socket 关闭")
            //关闭输入流通道
            isStream?.close()
//            关闭输出流通道
            outputStream?.close()
            // 关闭Socket连接
            socket?.close();
        } catch ( e: IOException) {
            e.printStackTrace();
        }

    }

    /**
     * 接受socket数据
     **/
    private fun recvSocketData() {
        // 利用线程池直接开启一个线程 & 执行该线程
        mThreadPool.execute {
            try {
                // 步骤1：创建输入流对象InputStream
                while (true) {
                    socket?.let{
                        //后面的it代表的是socket这个对象
                        // 步骤1：创建输入流对象InputStream
                        isStream = it.getInputStream()
                        // 步骤2：创建输入流读取器对象 并传入输入流对象
                        val inputStream: InputStream = it.getInputStream()
                        val input: DataInputStream =  DataInputStream (inputStream);
                        // 步骤3：接收服务器发送过来的数据
                        val b: ByteArray = ByteArray(150)
                        val length:Int = input.read (b)
                        var recvData:String = ""
                        try {
                            recvData =String (b, 0, length, Charsets.UTF_8)
                        } catch ( e:Exception) {
                            e.printStackTrace()
                        }

                        if (recvData.startsWith("H") && recvData.endsWith("T")) {
                            //分割多条数据帧，存到字符数组
                            val tempStrArray = recvData.split ("T")

                            val tempLength = tempStrArray.size;
                            for (i in 0 until tempLength) {
                                var tempData = tempStrArray [i]
                                if (!tempData.endsWith("T")) {
                                    tempData = tempData + "T"
                                }

                                Log.d("MainActivity", "recvData=" + recvData)

                                if (!TextUtils.isEmpty(tempData) && tempData!="T") {
                                    Log.d("Main",tempData)
                                    //获取数据
                                    val message =  Message()
                                    message.what = RECV_DATA
                                    message.obj = tempData
                                    handler.sendMessage(message)
                                }
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 隐藏软键盘
     */
    private  fun hideSoftKeyboard( activity: Activity) {
        val view: View? = activity.currentFocus;
        if (view != null) {
            val inputMethodManager: InputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     *
     * 退出应用
     *
     * ***/
    private fun exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(this, "在按一次退出程序", Toast.LENGTH_SHORT).show();
            Timer().schedule(object: TimerTask() {
                override fun run() {
                    isExit = false;
                }
            }, 2000)
        } else {
            this.finish();
            exitProcess(0);
        }
    }

    override  fun onKeyDown( keyCode:Int, event: KeyEvent):Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit()
        }
        return false;
    }
}