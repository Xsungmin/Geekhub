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
import com.example.geekhub.component.adapter.DeliveryListAddapter
import com.example.geekhub.data.DeliveryList
import com.example.geekhub.data.DeliveryResponse
import com.example.geekhub.data.NextSpotInfo
import com.example.geekhub.databinding.FragmentDeliveryBinding
import com.example.geekhub.databinding.RecyclerDeliveryListBinding
import com.example.geekhub.retrofit.NetWorkInterface
import com.example.todayfilm.LoadingDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


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
//    datas: ArrayList<DeliveryResponse>
    var recdatas : ArrayList<DeliveryResponse> = ArrayList()
    var deldatas : ArrayList<DeliveryResponse> = ArrayList()
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
        getCall()

        binding.allView.setOnTouchListener(object: OnSwipeTouchListener(requireContext()){
            override fun onSwipeBottom() {
                super.onSwipeBottom()
                (activity as MainActivity).changeFragment(7)
            }
            override fun onSwipeLeft() {
                super.onSwipeLeft()
                del()
            }
            override fun onSwipeRight() {
                super.onSwipeRight()
                rec()
            }
        })

        binding.receiveButton.setOnClickListener{
            rec()
        }
        binding.deliveryButton.setOnClickListener {
            del()
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
    }
    fun getCall(){
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
                if (result == null){
                    return
                }
                var recCount = 0
                var recSwitch = true
                for (i in result!!.rec){
                    if(i.status !=0){
                        recdatas!!.add(i)
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
                        deldatas!!.add(i)
                        if(i.status == 1 && delSwitch){
                            saveDeliveryPosition = delCount
                            delSwitch = false
                        }
                        delCount +=1
                    }
                }

                try {
                    binding.deliveryRec.text = "${recdatas!!.size}개"
                    binding.deliveryDel.text = "${deldatas!!.size}개"

                }catch (e : Error){

                }

                if(loadingDialog != null){
                    loadingDialog!!.dismiss()
                    loadingDialog = null
                }

                rec()
            } }
        )
    }

    fun selectDeliveryList(datas: ArrayList<DeliveryResponse>){
        val deliveryListAddapter = DeliveryListAddapter(datas,nowState,(activity as MainActivity),spot!!,userid,delComponentSwitch)
        binding.deliveryListRecycler.adapter = deliveryListAddapter
        binding.deliveryListRecycler.layoutManager =  LinearLayoutManager(requireContext())

        if(nowState == 0){
            binding.deliveryListRecycler.scrollToPosition(saveReceivePosition)
        }else{
            binding.deliveryListRecycler.scrollToPosition(saveDeliveryPosition)
        }

    }

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
    fun rec(){
        getDeliveryList(0)
        selectDeliveryList(recdatas!!)
        nowState = 0
    }

    fun del(){
        getDeliveryList(1)
        selectDeliveryList(deldatas!!)
        nowState = 1
    }





}




