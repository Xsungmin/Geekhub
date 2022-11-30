package com.example.geekhub

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.geekhub.data.SpotBody
import com.example.geekhub.databinding.FragmentCameraxBinding
import com.example.geekhub.retrofit.NetWorkInterface
import com.example.todayfilm.LoadingDialog
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit

private var imageCapture: ImageCapture? = null
private lateinit var cameraExecutor: ExecutorService
private const val TAG = "카메라"
var loadingDialog: LoadingDialog? = null

class CameraxFragment : Fragment() {
    lateinit var binding : FragmentCameraxBinding
    private lateinit var callback: OnBackPressedCallback
    lateinit var savedUri:Uri
    lateinit var imageFile:File
    var userid :String? = null
    private var spot :String? = "안바뀌었습니다"
    var title :String? = null


    private val REQUEST_CODE_PERMISSIONS = 10
    private val REQUIRED_PERMISSIONS =
        mutableListOf (
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    //권한

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCameraxBinding.inflate(inflater,container,false)
        arguments?.let{
            title = it.getString("title")
            spot = it.getString("spot")
            userid = it.getString("userid")
        }
        if (title != "널" && title != "비NFC"){
            binding.cameraTitle.setText("배달지는 ${title}입니다")
        }else if (title != "비NFC"){
            binding.sendPicture.setText("수령 완료")
        }
        else{
            binding.sendPicture.setText("수령 완료")
        }

        requestPermission()

        // Set up the listeners for take photo and video capture buttons
        binding.imageCaptureButton.setOnClickListener { takePhoto() }
        cameraExecutor = Executors.newSingleThreadExecutor()
        binding.reTakePicture.setOnClickListener{
            cameraExecutor.shutdown()
            binding.viewFinder.visibility = View.VISIBLE
            binding.imageCaptureButton.visibility = View.VISIBLE
            binding.cameraButtons.visibility = View.INVISIBLE
            binding.imageViewPreview.visibility = View.INVISIBLE
            binding.cameraBackground.setBackgroundColor(getResources().getColor(R.color.black))
            startCamera()
        }

        binding.sendPicture.setOnClickListener {
            changeState()
            loadingDialog = LoadingDialog(requireContext())
            loadingDialog!!.show()
            send()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 거절된 상태
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
                // 1. 사용자가 승인 거절을 누른경우
            } else {
                // 2. 사용자가 승인 거절과 동시에 다시 표시하지 않기 옵션을 선택한 경우
                // 3. 혹은 아직 승인요청을 한적이 없는 경우
                val snackBar = Snackbar.make(binding.permissionCheck, "카메라 권한을 사용하지 않으면 사진을 찍을 수 없어요!", Snackbar.LENGTH_LONG)
                snackBar.setAction("허락하러 가기") {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", requireContext().packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
                snackBar.show()
            }
        } else {
            // 4. 권한이 승인된 상태
        }
        startCamera()
    }
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val time = System.currentTimeMillis()
        val destPath = requireActivity().externalCacheDir.toString() + "/${time}cache_file.png"
        imageFile = File(destPath)
        imageFile.createNewFile()
        savedUri = Uri.fromFile(imageFile)
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(imageFile)
            .build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireActivity()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                }
                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){
                    binding.viewFinder.visibility = View.INVISIBLE
                    binding.imageCaptureButton.visibility = View.INVISIBLE
                    binding.cameraButtons.visibility = View.VISIBLE
                    binding.imageViewPreview.visibility = View.VISIBLE
                    binding.imageViewPreview.setImageURI(savedUri )
                    binding.cameraBackground.setBackgroundColor(getResources().getColor(R.color.white))
                }
            }
        )
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
//                .setTargetAspectRatio(RATIO_4_3)
                .setTargetResolution(Size(960, 960))
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                    })
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture,imageAnalyzer)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishFragment()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    fun finishFragment() {
        val fragmentManager = requireActivity().supportFragmentManager
        fragmentManager.beginTransaction().remove(this@CameraxFragment).commit()
        fragmentManager.popBackStack()
        cameraExecutor.shutdown()
    }

    fun changeState(){
        val retrofit = Retrofit.Builder().baseUrl("http://k7c205.p.ssafy.io:9013/")
            .addConverterFactory(GsonConverterFactory.create()).build()
        val callData = retrofit.create(NetWorkInterface::class.java)
        val Body = SpotBody()
        Body.spotId = spot!!

        val call = callData.changestate(Body)
        call.enqueue(object :Callback<String>{
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("에러입니다",t.toString())
            }
            override fun onResponse(call: Call<String>, response: Response<String>) {
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun send(){
        var time = (activity as MainActivity).getTime()
        val body = RequestBody.create(MediaType.parse("image/*"),imageFile)
        val image = MultipartBody.Part.createFormData("image",imageFile.name,body)
        val userBody = RequestBody.create(MediaType.parse("text/plain"), userid)
        val spotBody = RequestBody.create(MediaType.parse("text/plain"), spot)
        var timeBody = RequestBody.create(MediaType.parse("text/plain"), time)
        val retrofit = Retrofit.Builder().baseUrl("http://k7c205.p.ssafy.io:9012/")
            .addConverterFactory(GsonConverterFactory.create()).build()
        val callData = retrofit.create(NetWorkInterface::class.java)
        val call = callData.sendimage(image,userBody,spotBody,timeBody)
        call.enqueue(object : Callback<String>{
            override fun onFailure(call: Call<String>, t: Throwable) {
                finishFragment()
                (activity as MainActivity).next(userid!!)
                (activity as MainActivity).findPath()
                (activity as MainActivity).finishCheck(userid!!)
            }
            override fun onResponse(call: Call<String>, response: Response<String>) {
                finishFragment()
                (activity as MainActivity).next(userid!!)
                (activity as MainActivity).findPath()
                (activity as MainActivity).finishCheck(userid!!)
            }
        })}

    private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        override fun analyze(image: ImageProxy) {
            val buffer = image.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()
            listener(luma)
            image.close()
        }
    }

    fun requestPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED

        ) {
            ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onStop() {
        super.onStop()
        if(loadingDialog !=null){
            loadingDialog!!.dismiss()
        } }
}