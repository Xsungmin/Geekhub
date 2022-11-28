package com.example.geekhub

import OnSwipeTouchListener
import android.app.Activity
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.geekhub.data.DeliveryList
import com.example.geekhub.data.DeliveryResponse
import com.example.geekhub.data.NextSpotInfo
import com.example.geekhub.databinding.FragmentDeliveryBinding
import com.example.geekhub.databinding.RecyclerDeliveryListBinding
import com.example.geekhub.retrofit.NetWorkInterface
import com.example.todayfilm.LoadingDialog
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.fragment_delivery.*
import kotlinx.android.synthetic.main.fragment_nav.view.*
import kotlinx.coroutines.processNextEventInCurrentThread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import java.security.cert.PKIXRevocationChecker.Option
import java.util.Objects


class DeliveryFragment : Fragment() {
    lateinit var binding : FragmentDeliveryBinding
    var nowState = 0
    var spot : String? = null
    lateinit var pref : SharedPreferences
    lateinit var userid : String
    var nowFocus = 0
    var loadingDialog: LoadingDialog? = null
    var saveReceivePosition = 0
    var saveDeliveryPosition = 0
    var delComponentSwitch = false

    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        loadingDialog = LoadingDialog(requireContext())
        loadingDialog!!.show()

        pref = requireActivity().getSharedPreferences("idKey",0)
        userid = pref.getString("id", "").toString()
        nextSpot(userid)
        binding = FragmentDeliveryBinding.inflate(inflater,container,false)
        if (nowState == 0 ){
//            binding.receiveLine.visibility = View.VISIBLE
            getDeliveryList(0)
        }
        else{
//            binding.deliveryLine.visibility = View.VISIBLE
            getDeliveryList(1)
        }
        binding.allView.setOnTouchListener(object: OnSwipeTouchListener(requireContext()){
            override fun onSwipeBottom() {
                super.onSwipeBottom()
                (activity as MainActivity).changeFragment(7)
            }

            override fun onSwipeLeft() {
                super.onSwipeLeft()
                getDeliveryList(1)
                nowState = 1
            }

            override fun onSwipeRight() {
                super.onSwipeRight()
                getDeliveryList(0)
                nowState = 0
            }

        })

        binding.deliveryListRecycler.setOnTouchListener(object: OnSwipeTouchListener(requireContext()){
            override fun onSwipeLeft() {
                super.onSwipeLeft()
                getDeliveryList(1)
                nowState = 1
            }

            override fun onSwipeRight() {
                super.onSwipeRight()
                getDeliveryList(0)
                nowState = 0
            }

        })

        binding.receiveButton.setOnClickListener{
            getDeliveryList(0)
            nowState = 0
        }
        binding.deliveryButton.setOnClickListener {
            getDeliveryList(1)
            nowState = 1
        }




        return binding.root
    }


    fun getDeliveryList(number : Int){
        if (number == 0){
            binding.receiveButton.setBackgroundResource(R.drawable.del_title)
            binding.deliveryButton.setBackgroundResource(0)
            binding.deliveryRec.setTextColor(resources.getColor(R.color.white))
            binding.deliveryRecTitle.setTextColor(resources.getColor(R.color.white))
            binding.deliveryDel.setTextColor(resources.getColor(R.color.gray_500))
            binding.deliveryDelTitle.setTextColor(resources.getColor(R.color.gray_500))

        }
        else{
            binding.deliveryButton.setBackgroundResource(R.drawable.del_title)
            binding.receiveButton.setBackgroundResource(0)
            binding.deliveryRec.setTextColor(resources.getColor(R.color.gray_500))
            binding.deliveryRecTitle.setTextColor(resources.getColor(R.color.gray_500))
            binding.deliveryDel.setTextColor(resources.getColor(R.color.white))
            binding.deliveryDelTitle.setTextColor(resources.getColor(R.color.white))
        }
        val retrofit = Retrofit.Builder().baseUrl("http://k7c205.p.ssafy.io:9013")
            .addConverterFactory(GsonConverterFactory.create()).build()
        val callData = retrofit.create(NetWorkInterface::class.java)

        val call = callData.getlist(userid.toInt())
        call.enqueue(object : Callback<DeliveryList>{
            override fun onFailure(call: Call<DeliveryList>, t: Throwable) {
                Log.e("안됨",t.toString())
            }

            override fun onResponse(call: Call<DeliveryList>, response: Response<DeliveryList>) {
                val result = response.body()
                println(result)

                if(result?.isFinished == true
                ){
//                    try{(activity as MainActivity).finished()
//
//                    }catch (e:Error){
//
//                    }


                }


                val deldatas : ArrayList<DeliveryResponse> = ArrayList()
                var recdatas : ArrayList<DeliveryResponse> = ArrayList()
                if (result == null){
                    return
                }
                var recCount = 0
                var recSwitch = true
                for (i in result!!.rec){
                    if(i.status !=0){
                        recdatas.add(i)
                        if(i.status == 1 && recSwitch){
                                saveReceivePosition = recCount

                            recSwitch = false
                        }
                        recCount +=1

                        if (recCount == result.rec.size && i.status == 2){
                            delComponentSwitch = true

                        }



                    }
                }
                var delCount = 0
                var delSwitch = true
                for (i in result!!.del){
                    if(i.status !=0){
                        deldatas.add(i)
                        if(i.status == 1 && delSwitch){

                                saveDeliveryPosition = delCount

                            delSwitch = false
                        }
                        delCount +=1
                    }
                }

                try {
                    binding.deliveryRec.text = "${recdatas.size}개"
                    binding.deliveryDel.text = "${deldatas.size}개"

                }catch (e : Error){

                }

                if (number == 0){
                    selectDeliveryList(recdatas)

                }else{
                    selectDeliveryList(deldatas)
                }
                if(loadingDialog != null){
                    loadingDialog!!.dismiss()
                    loadingDialog = null
                }
            } })
    }

    fun selectDeliveryList(datas: ArrayList<DeliveryResponse>){
        val deliveryListAddapter = DeliveryListAddapter(datas,nowState)
        binding.deliveryListRecycler.adapter = deliveryListAddapter
        binding.deliveryListRecycler.layoutManager =  LinearLayoutManager(requireContext())

        if(nowState == 0){
            binding.deliveryListRecycler.scrollToPosition(saveReceivePosition)
        }else{
            binding.deliveryListRecycler.scrollToPosition(saveDeliveryPosition)
        }

    }








    inner class DeliveryListAddapter(private var datas : ArrayList<DeliveryResponse>,nowState:Int) :
        RecyclerView.Adapter<DeliveryListAddapter.ViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = RecyclerDeliveryListBinding.inflate(layoutInflater)
            val holder = ViewHolder(binding)
            val layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                280
            )

            binding.root.layoutParams = layoutParams

            return holder

        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            // status 0 : 내용없음  1 : 배달해야함    2 : 완료
            val number = datas[position]

            if(nowState == 0){
                holder.icon.visibility = View.INVISIBLE

            }




            if(number.status == 1){

                if(nowFocus == 0){
                    //focusingcode
                    nowFocus +=1
                }
                visibleList(number,holder)
                holder.title.setTextColor(resources.getColor(R.color.gick_blue))
                holder.count.setTextColor(resources.getColor(R.color.gick_blue))
                holder.time.setTextColor(resources.getColor(R.color.gick_blue))
//                holder.icon.visibility = View.INVISIBLE
                holder.main.setOnClickListener{
                    if (nowState == 0){
                        (activity as MainActivity).sendData(NfcFragment(),number.spotName,number.spotIndex,number.iconUrl)
                    }
                    else if (nowState == 1 && delComponentSwitch){

                        (activity as MainActivity).sendUserId(number.spotIndex,userid,number.spotName)

                    }
                }

            } else{
                visibleList(number,holder)
                holder.title.setTextColor(resources.getColor(R.color.gray_500))
                holder.count.setTextColor(resources.getColor(R.color.gray_500))
                holder.time.setTextColor(resources.getColor(R.color.gray_500))
//                holder.icon.visibility = View.VISIBLE
            }

            if(number.spotName == spot){
                holder.title.setTextColor(resources.getColor(R.color.dark_red))
                holder.count.setTextColor(resources.getColor(R.color.dark_red))
                holder.time.setTextColor(resources.getColor(R.color.dark_red))
            }


        }



        //배달쪽 recyclerview
        fun visibleList(number: DeliveryResponse,holder:ViewHolder){
            holder.title.text = number.spotName
            holder.count.text =  "수량 : ${number.count}개"
            holder.time.text = number.expectedTime

            var url = number.iconUrl
//            val url ="https://geekhub.s3.ap-northeast-2.amazonaws.com/logo/burgerkingLogo.png"

//            var requestOption :Option = Option.cir

            Glide.with(requireActivity())
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


        inner class ViewHolder(binding: RecyclerDeliveryListBinding)
            : RecyclerView.ViewHolder(binding.root) {
            val title = binding.deliveryListTitle
            val count = binding.deliveryListCount
            val time = binding.deliveryListTime
            val main = binding.deliveryRecyclerView
            val image = binding.logoImage
            val icon = binding.cameraIcon

        } }


    fun nextSpot(userid: String) {
        val retrofit = Retrofit.Builder().baseUrl("http://k7c205.p.ssafy.io:8000/")
            .addConverterFactory(GsonConverterFactory.create()).build()
        val callData = retrofit.create(NetWorkInterface::class.java)
        val call = callData.nextWork(userid.toInt())
        call.enqueue(object : Callback<NextSpotInfo> {
            override fun onFailure(call: Call<NextSpotInfo>, t: Throwable) {
                Log.e("에러났다", t.toString())
            }

            override fun onResponse(call: Call<NextSpotInfo>, response: Response<NextSpotInfo>) {
                spot = response.body()?.spotName



            }
        })
    }

    override fun onPause() {
        super.onPause()
        if(loadingDialog != null){
            loadingDialog!!.dismiss()
            loadingDialog = null
        }
    }





}




