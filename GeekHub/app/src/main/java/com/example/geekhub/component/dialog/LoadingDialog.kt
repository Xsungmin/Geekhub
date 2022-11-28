package com.example.todayfilm

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.example.geekhub.R

class LoadingDialog constructor(context: Context) : Dialog(context){
    init {
        // 다이얼로그 외부 화면을 터치해도 종료되지 않음
        setCanceledOnTouchOutside(false)
        // 배경 투명화
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_loading)
    }
}