package com.example.geekhub.component.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.geekhub.R
import com.example.geekhub.data.messageData

class ChattingAddapter (
    var datas: ArrayList<messageData>, var userid : String
) :
    RecyclerView.Adapter<ChattingAddapter.ViewHolder>() {
    var previewId = ""

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChattingAddapter.ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_chatting, parent, false)
        return ViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = datas[position]
        if (data.userId == userid){
            previewId = data.userId
            holder.oContent.visibility = View.INVISIBLE
            holder.oNickname.visibility = View.INVISIBLE
            holder.oTime.visibility = View.INVISIBLE
            holder.mTime.visibility = View.VISIBLE
            holder.mContent.visibility = View.VISIBLE
            holder.mTime.setText(data.created_at)
            holder.mContent.setText(data.content)
        }else{
            holder.oContent.visibility = View.VISIBLE
            holder.oNickname.visibility = View.VISIBLE
            holder.oTime.visibility = View.VISIBLE
            holder.mTime.visibility = View.INVISIBLE
            holder.mContent.visibility = View.INVISIBLE
            if( position > 0 && datas[position-1].userId == data.userId){
                holder.oNickname.visibility = View.INVISIBLE
                holder.oNickname.setText("")
            }else{
                holder.oNickname.visibility = View.VISIBLE
                holder.oNickname.setText(data.name)
            }
            holder.oContent.setText(data.content)
            holder.oTime.setText(data.created_at)
        }
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    inner class ViewHolder(view:View)
        : RecyclerView.ViewHolder(view) {
        val oNickname = view.findViewById<TextView>(R.id.other_nickname)
        val oContent = view.findViewById<TextView>(R.id.other_nickname_content)
        val oTime = view.findViewById<TextView>(R.id.other_nickname_time)
        val mContent = view.findViewById<TextView>(R.id.my_nickname_content)
        val mTime = view.findViewById<TextView>(R.id.my_nickname_time)
    }
    override fun getItemViewType(position: Int): Int {
        return if(userid == datas[position].userId)1
        else if (position > 0 && datas[position-1].userId == datas[position].userId)2
        else 3
    }
}