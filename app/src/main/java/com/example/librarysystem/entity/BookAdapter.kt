package com.example.librarysystem.entity

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.librarysystem.R

class BookAdapter(var bookList: List<Book>):
    RecyclerView.Adapter<BookAdapter.ViewHolder>(){

    //设置接口监听
    private var mOnItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(clickListener: OnItemClickListener?) {
        mOnItemClickListener = clickListener
    }

    //内部类
    inner class ViewHolder(view: View,onClickListener: OnItemClickListener):
        RecyclerView.ViewHolder(view){

        val bookId: TextView = view.findViewById(R.id.bookId)
        val bookName:TextView = view.findViewById(R.id.bookName)
        val bookPosition:TextView = view.findViewById(R.id.bookPosition)
        val bookState:TextView = view.findViewById(R.id.bookState)

        init {
            itemView.setOnClickListener {

                Log.d("Test","item点击")
                val position = adapterPosition
                //确保position值有效
                if (position != RecyclerView.NO_POSITION) {
                    onClickListener.onButtonClicked(view, position)
                }

            }
        }


    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(list: List<Book>){
        this.bookList = list
        //通知数据改变
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.book_item,parent,false)

        val viewHolder = mOnItemClickListener?.let { ViewHolder(view, it) }
        return viewHolder!!
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = bookList[position]
        holder.bookId.text = book.id
        holder.bookName.text = book.bookName
        holder.bookPosition.text = book.bookPosition
        holder.bookState.text = book.bookState


    }

    override fun getItemCount() = bookList.size
}