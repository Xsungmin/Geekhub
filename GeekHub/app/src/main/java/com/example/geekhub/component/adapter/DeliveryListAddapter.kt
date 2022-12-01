package com.example.geekhub.component.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.geekhub.MainActivity
import com.example.geekhub.NfcFragment
import com.example.geekhub.R
import com.example.geekhub.data.DeliveryResponse
import com.google.android.material.transition.Hold

class DeliveryListAddapter(private var datas : ArrayList<DeliveryResponse>, var nowState:Int, var activity: MainActivity,var spot:String , var userid:String ,var delComponentSwitch:Boolean) :
    RecyclerView.Adapter<DeliveryListAddapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_chatting, parent, false)
        return ViewHolder(inflatedView)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // status 0 : 내용없음  1 : 배달해야함    2 : 완료
        val number = datas[position]

        if(nowState == 0){
            holder.icon.visibility = View.INVISIBLE

        }
        if(number.status == 1){

            visibleList(number,holder)
            holder.title.setTextColor(holder.title.resources.getColor(R.color.gick_blue))
            holder.count.setTextColor(holder.count.resources.getColor(R.color.gick_blue))
            holder.time.setTextColor(holder.time.resources.getColor(R.color.gick_blue))
            holder.main.setOnClickListener{
                if (nowState == 0){
                    activity.sendData(NfcFragment(),number.spotName,number.spotIndex,number.iconUrl)
                }
                else if (nowState == 1 && delComponentSwitch){

                    activity.sendUserId(number.spotIndex,userid,number.spotName)

                }
            }

        } else{
            visibleList(number,holder)
            holder.title.setTextColor(holder.title.resources.getColor(R.color.gray_500))
            holder.count.setTextColor(holder.count.resources.getColor(R.color.gray_500))
            holder.time.setTextColor(holder.time.resources.getColor(R.color.gray_500))
//                holder.icon.visibility = View.VISIBLE
        }

        if(number.spotName == spot){
            holder.title.setTextColor(holder.title.resources.getColor(R.color.dark_red))
            holder.count.setTextColor(holder.count.resources.getColor(R.color.dark_red))
            holder.time.setTextColor(holder.time.resources.getColor(R.color.dark_red))
        }


    }



    //배달쪽 recyclerview
    fun visibleList(number: DeliveryResponse, holder:ViewHolder){
        holder.title.text = number.spotName
        holder.count.text =  "수량 : ${number.count}개"
        holder.time.text = number.expectedTime

        var url = number.iconUrl
//            val url ="https://geekhub.s3.ap-northeast-2.amazonaws.com/logo/burgerkingLogo.png"

//            var requestOption :Option = Option.cir

        Glide.with(activity)
            .load(url) // 불러올 이미지 url
            .placeholder(R.drawable.loading) // 이미지 로딩 시작하기 전 표시할 이미지
            .error(R.drawable.loading) // 로딩 에러 발생 시 표시할 이미지
            .fallback(R.drawable.loading) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
            .into(holder.image) // 이미지를 넣을 뷰

        holder.title.isSelected = true
    }

    override fun getItemCount(): Int {
        return datas.size
    }


    inner class ViewHolder(view:View)
        : RecyclerView.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.delivery_list_title)
        val count = view.findViewById<TextView>(R.id.delivery_list_count)
        val time = view.findViewById<TextView>(R.id.delivery_list_time)
        val main = view.findViewById<TextView>(R.id.delivery_recycler_view)
        val image = view.findViewById<ImageView>(R.id.logo_image)
        val icon = view.findViewById<ImageView>(R.id.logo_icon)

    } }