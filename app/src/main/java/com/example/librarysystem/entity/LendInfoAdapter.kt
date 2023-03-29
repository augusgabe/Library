package com.example.librarysystem.entity

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.librarysystem.R

class LendInfoAdapter(var lendInfoList:List<LendInfo>)
    :RecyclerView.Adapter<LendInfoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View):RecyclerView.ViewHolder(view){
        val lendBookName: TextView = view.findViewById(R.id.lendBook_name)
        val lenderName = view.findViewById<TextView>(R.id.lender_name)
        val lendDay = view.findViewById<TextView>(R.id.lendDay_show)
        val lendTime = view.findViewById<TextView>(R.id.lendTime)

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(list: List<LendInfo>){
        this.lendInfoList = list
        //通知数据改变
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.table_item,parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val lendInfo = lendInfoList[position]

        holder.lendBookName.text = lendInfo.lendBookName
        holder.lendDay.text = lendInfo.lendDay
        holder.lendTime.text = lendInfo.lendTime
        holder.lenderName.text = lendInfo.lenderName


    }

    override fun getItemCount()=lendInfoList.size

}