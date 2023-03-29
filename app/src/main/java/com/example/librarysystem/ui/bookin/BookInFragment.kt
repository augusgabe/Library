package com.example.librarysystem.ui.bookin

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.librarysystem.MainActivity
import com.example.librarysystem.MainViewModel
import com.example.librarysystem.databinding.BookInFragmentBinding
import com.example.librarysystem.entity.Book
import com.example.librarysystem.entity.BookAdapter
import com.example.librarysystem.entity.OnItemClickListener
import com.example.librarysystem.popupWindow.BookDetail
import com.example.librarysystem.ui.check.CheckViewModel
import com.example.librarysystem.ui.database.DBViewModel
import com.example.librarysystem.utils.DataHandle
import com.example.librarysystem.utils.ToastUtils

class BookInFragment : Fragment() {

    private var _binding: BookInFragmentBinding? = null

    private val binding get() = _binding!!

    private lateinit var mainActivity : MainActivity

    private lateinit var bookAdapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BookInFragmentBinding.inflate(inflater,container,false)

        BookInViewModel.cursor = MainViewModel.db?.query("Book",null,null,null,null,null,null)

        mainActivity = activity as MainActivity

        initView()

        return binding.root
    }

    @SuppressLint("Range")
    private fun initView(){

        //从数据库中调取书籍数据
        val layoutManager = LinearLayoutManager(context)

        layoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager.stackFromEnd = true;//列表再底部开始展示，反转后由上面开始展示
        layoutManager.reverseLayout = true;//列表翻转

        binding.bookRecyclerview.layoutManager = layoutManager

        //从数据库中加载数据
        BookInViewModel.bookList.clear()

        BookInViewModel.cursor?.let {
            if(it.moveToFirst()){
                do{
                    //遍历cursor对象，取出数据并添加至sensorList中
                    val bookId = it.getString(it.getColumnIndex("bookId")).toString()
                    val bookName = it.getString(it.getColumnIndex("bookName"))
                    val bookPosition = it.getString(it.getColumnIndex("bookPosition"))
                    val bookState = it.getString(it.getColumnIndex("bookState"))

                    BookInViewModel.bookList.add(Book(bookId,bookName,bookPosition,bookState))

                }while(it.moveToNext())
            }

        }

        bookAdapter = BookAdapter(BookInViewModel.bookList)

        bookAdapter.setOnItemClickListener(object :OnItemClickListener{
            override fun onButtonClicked(view: View?, position: Int) {

                val bookId = BookInViewModel.bookList[position].id
                //开启一个弹窗
                val bookDetail  =  BookDetail(mainActivity,view,bookId)
                bookDetail.start()
                Log.d("Test","onButtonClicked")
            }

            override fun onDeleteBtnClicked(view: View?, position: Int) {
                TODO("Not yet implemented")
            }

        })

        binding.bookRecyclerview.adapter = bookAdapter

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

        binding.bookInBtn.setOnClickListener {
            //获取当前书籍信息，然后填入数据库中，并更新到列表里
            val bookId = binding.bookIdIn.text.toString()
            val bookName = binding.bookNameIn.text.toString()
            val bookPosition = binding.bookPositionIn.text.toString()

            if(bookId != "" && bookName != "" && bookPosition != ""){

                //添加到列表中
                BookInViewModel.bookList.add(Book(bookId,bookName,bookPosition,"未借阅"))
                bookAdapter.setList(BookInViewModel.bookList)
                binding.bookRecyclerview.adapter = bookAdapter

                val values_temp = ContentValues().apply {
                    //组装数据
                    put("bookId",bookId)
                    put("bookName",bookName)
                    put("bookPosition",bookPosition)
                    put("bookState","未借阅")
                    put("bookLendNum",0)
                }

                //添加到数据库
                MainViewModel.db?.insert("Book",null,values_temp)

//                //添加书名到列表中，（用于统计图横坐标标签设置）
//                DBViewModel.bookNameList.add(bookName)

                ToastUtils.showToast(context,"图书入库成功！")

            }else{
                ToastUtils.showToast(context,"图书信息有误！")
            }

        }

        //搜索图书
        binding.searchBtn.setOnClickListener {

            //先获取书号加书名
            val book_search_name = binding.bookNameSearch.text.toString()
            val book_search_id = binding.bookIdSearch.text.toString()

            queryBook(book_search_id,book_search_name)

        }

    }

    @SuppressLint("Range")
    fun queryBook(bookId_search:String, bookName_search:String){
        //移除所有list数据
        BookInViewModel.bookList.clear()

        var situation = 0

        //分情况讨论传进来的查询条件
        situation = if(bookId_search == "" && bookName_search ==""){
            1
        }else if (bookId_search != "" && bookName_search==""){
            2
        }else if(bookId_search == "" && bookName_search!="" ){
            3
        }else{
            4
        }

        BookInViewModel.cursor?.let {
            if(it.moveToFirst()){
                do{
                    //遍历cursor对象，取出数据并添加至sensorList中
                    val bookId = it.getString(it.getColumnIndex("bookId"))
                    val bookName = it.getString(it.getColumnIndex("bookName"))

                    val bookPosition = it.getString(it.getColumnIndex("bookPosition"))
                    val bookState = it.getString(it.getColumnIndex("bookState"))

                    when(situation){
                        1->{
                            Log.d("Test","无筛选")
                        }
                        2->{

                            if (bookId == bookId_search){
                                BookInViewModel.bookList.add(Book(bookId,bookName,bookPosition,bookState))
                            }

                        }
                        3->{
                            if (bookName == bookName_search){
                                BookInViewModel.bookList.add(Book(bookId,bookName,bookPosition,bookState))
                            }

                        }
                        4->{
                            if (bookId == bookId_search && bookName == bookName_search){
                                BookInViewModel.bookList.add(Book(bookId,bookName,bookPosition,bookState))
                            }
                        }
                    }

                }while(it.moveToNext())
            }

        }

        //查无结果
        if(BookInViewModel.bookList.size==0){
            ToastUtils.showToast(context,"查询不到该图书！")
        }else{
            bookAdapter.setList(BookInViewModel.bookList)
            binding.bookRecyclerview.adapter = bookAdapter
            ToastUtils.showToast(context,"图书查找成功！")
        }


    }

}