package com.example.geekhub

import OnSwipeTouchListener
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.geekhub.databinding.FragmentNfcBinding

class NfcFragment : Fragment() {
    lateinit var binding : FragmentNfcBinding
    var title:String? = "s"
    var state = 0
    var url:String? = "url"
    var idx:String? = "idx"
    lateinit var userid : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNfcBinding.inflate(inflater,container,false)
        arguments?.let{
            title = it.getString("title")
            url = it.getString("url")
            idx = it.getString("idx")
            binding.nfcTitle.text = title
        }

        binding.nfcInfomation1.setText("아래 버튼을 눌러 nfc를 켜주세요!\nnfc를 꼭 일반모드로 설정해주세요")
        binding.nfcTitle.isSelected = true

        Glide.with(requireContext())
            .load(R.drawable.nfc_animation) // 불러올 이미지 url
            .into(binding.nfcImage) // 이미지를 넣을 뷰

        Glide.with(requireContext())
            .load(url) // 불러올 이미지 url
            .into(binding.logoIcon) // 이미지를 넣을 뷰

        binding.allView.setOnTouchListener(object: OnSwipeTouchListener(requireContext()){
            override fun onSwipeBottom() {
                super.onSwipeBottom()
                (activity as MainActivity).changeFragment(7)
            }
        })
        userid = (activity as MainActivity).getId()
        binding.nfcInfomation1.visibility = View.VISIBLE
        binding.waitingButton.visibility = View.INVISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            binding.nfcInfomation1.visibility = View.INVISIBLE
            binding.waitingButton.visibility = View.VISIBLE
        }, 6000)

        binding.waitingButton.setOnClickListener{
            (activity as MainActivity).sendUserId(idx!!,userid,"비NFC")
        }


        return binding.root
    }



    fun getNfc(){
        try{
            var nfcAdapter = NfcAdapter.getDefaultAdapter(requireActivity().applicationContext)

            if(nfcAdapter.isEnabled){
                binding.nfcInfomation1.setText("스캔준비 완료")
                state = 0

            }else{
                if (state == 0){
                    startActivity(Intent(android.provider.Settings.ACTION_NFC_SETTINGS))
                    state += 1
                }
                binding.nfcInfomation1.setText("NFC가 꺼져있습니다 위 그림을 눌러서 꼭 일반모드로 켜주세요")
                binding.nfcImage.setOnClickListener{
                    startActivity(Intent(android.provider.Settings.ACTION_NFC_SETTINGS))
                }
            }

        }catch (e: Exception){
            Log.e("NFC미지원 단말기",e.toString())
            binding.nfcInfomation1.visibility = View.INVISIBLE
            binding.waitingButton.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        getNfc()
    }


}