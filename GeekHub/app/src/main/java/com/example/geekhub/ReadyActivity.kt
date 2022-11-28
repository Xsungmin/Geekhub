package com.example.geekhub

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.geekhub.data.DeliveryList
import com.example.geekhub.databinding.ActivityReadyBinding
import com.example.geekhub.retrofit.NetWorkInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ReadyActivity : AppCompatActivity() {
    lateinit var binding: ActivityReadyBinding
    lateinit var pref : SharedPreferences
    lateinit var userid : String
    var backKeyPressedTime:Long= 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pref = getSharedPreferences("idKey", 0)
        userid = pref.getString("id", "").toString()
        getDeliveryList(userid.toInt())
        Glide.with(this)
            .load(R.drawable.empty) // 불러올 이미지 url
            .into(binding.deliveryDobby) // 이미지를 넣을 뷰
    }

    fun getDeliveryList(number : Int){
        val retrofit = Retrofit.Builder().baseUrl("http://k7c205.p.ssafy.io:9013/")
            .addConverterFactory(GsonConverterFactory.create()).build()
        val callData = retrofit.create(NetWorkInterface::class.java)
        val call = callData.getlist(number)

        call.enqueue(object : Callback<DeliveryList> {
            override fun onFailure(call: Call<DeliveryList>, t: Throwable) {
                Log.e("안됨",t.toString())
            }

            override fun onResponse(call: Call<DeliveryList>, response: Response<DeliveryList>) {
                val result = response.body()
                if (result!!.isFinished == true){
                    binding.deliveryFree.setText("오늘의 일을 완료했어요!")
                    Toast.makeText(applicationContext,"업무가 끝났습니다 수고하셨습니다",Toast.LENGTH_SHORT).show()

                    return
                }

                if (result!!.del.size + result!!.rec.size == 0){
                    binding.deliveryFree.setText("오늘은 업무가 없어요 ~_~")

                    return
                }
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)


            } })
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 0) {

            if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
                backKeyPressedTime = System.currentTimeMillis();
                Toast.makeText(this,"뒤로가기를 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
                return;
            } else{
                super.onBackPressed()
            }
        }
        super.onBackPressed()

    }


}