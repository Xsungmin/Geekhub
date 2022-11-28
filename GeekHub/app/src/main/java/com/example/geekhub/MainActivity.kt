package com.example.geekhub

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NfcF
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.example.geekhub.data.DeliveryList
import com.example.geekhub.data.LocationInfo
import com.example.geekhub.data.NextSpotInfo
import com.example.geekhub.databinding.ActivityMainBinding
import com.example.geekhub.retrofit.NetWorkInterface
import com.example.todayfilm.LoadingDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.skt.Tmap.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(), TMapGpsManager.onLocationChangedCallback {

    private lateinit var binding: ActivityMainBinding
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var tMapView: TMapView
    lateinit var tMapTapi: TMapTapi
    lateinit var bitmap: Bitmap
    var gps : TMapGpsManager? = null
    var nextSpotInfo : NextSpotInfo? = null
    var isNext = 0
    var cnt = 0
    var focusStatus = 1
    val bundle = Bundle()
    lateinit var userid: String
    lateinit var pref : SharedPreferences
    var backKeyPressedTime:Long= 0
    var loadingDialog: LoadingDialog? = null
    var checkBack = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cnt = 0
        focusStatus = 1
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pref = getSharedPreferences("idKey", 0)
        userid = pref.getString("id", "").toString()
        finishCheck(userid)

        binding.liveFocusButton.setOnClickListener{
            tMapView.setCenterPoint(gps!!.location.longitude, gps!!.location.latitude)
        }

        binding.goMainLogo.setOnClickListener{
            changeFragment(7)
        }

        next(userid)


        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<DeliveryFragment>(R.id.main_container_view)
//                add<CameraxFragment>(R.id.camera_view)
            }
        }

        val MY_PERMISSION_ACCESS_ALL = 100

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            var permissions = arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSION_ACCESS_ALL)
        } // 퍼미션

        initialize(userid)

        val permissionCheck : Int = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0);
        }
        gps = TMapGpsManager(this)
        gps!!.minTime = 20000
        gps!!.minDistance = 0F
        gps!!.provider = TMapGpsManager.GPS_PROVIDER
        gps!!.OpenGps()
        gps!!.provider = TMapGpsManager.NETWORK_PROVIDER
        gps!!.OpenGps()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener {
                location : Location? ->
            if (location != null) {
                tMapView.setCenterPoint(location.longitude, location.latitude)
            }
        }

        binding.liveFocusButton.setOnClickListener{
            tMapView.setCenterPoint(gps!!.location.longitude, gps!!.location.latitude)
        }


        activeChat()

        // 채팅버튼
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        //nfc
    }

    public override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    public override fun onResume() {
        super.onResume()
        cnt = 0
        val intent: Intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        var pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try {
                addDataType("*/*")    /* Handles all MIME based dispatches.
                                     You should specify only the ones that you need. */
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("fail", e)
            }
        }

        binding.navButton.setOnClickListener {

            Toast.makeText(this,"네비게이션을 로드하고 있습니다",Toast.LENGTH_SHORT).show()
            loadingDialog = LoadingDialog(this)
            loadingDialog!!.show()
            goNav()
        }

        binding.focusButton.setOnClickListener {
            if (focusStatus == 0){
                binding.focusButton.setImageResource(R.drawable.auto_on)
                focusStatus = 1
            } else{
                binding.focusButton.setImageResource(R.drawable.auto)
                focusStatus = 0
            }

        }

//        binding.rootButton.setOnClickListener {
//            findPath()
//        }

        var intentFiltersArray = arrayOf(ndef)
        var techListsArray = arrayOf(arrayOf<String>(NfcF::class.java.name))
        nfcAdapter!!.enableForegroundDispatch(
            this,
            pendingIntent,
            intentFiltersArray,
            techListsArray
        )
        // nfc
    }

//    public override fun onNewIntent(intent: Intent) {
//        val tagFromIntent: Tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
//        //do something with tagFromIntent
//    }

    private fun initialize(userid: String) {
        tMapView = TMapView(this)
        tMapView.setSKTMapApiKey("l7xx04b0f932024e4b34acb560a2d2d3a2dc")
//        l7xx04b0f932024e4b34acb560a2d2d3a2dc

        val linearLayoutTmap: LinearLayout =
            findViewById<LinearLayout>(R.id.linearLayoutTmap)
        linearLayoutTmap.addView(tMapView)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onLocationChange(location: Location?) {
        if (location != null) {
            sendLocation()
            addMarker()
//            tMapPointStart = gps!!.location
            if (focusStatus == 1){
                tMapView.setCenterPoint(gps!!.location.longitude, gps!!.location.latitude)
            }
            findPath()

        }
    }


    fun changeFragment(index: Int) {
        when (index) {
            1 -> {
                moveFragment(DeliveryFragment())
            }


            3 -> {
                moveFragment(NfcFragment())
            }

            4 -> {
                clearBackStack()
                moveFragment(DeliveryFragment())

            }

            5 -> {
                clearBackStack()
                moveFragment(DeliveryFragment())
            }
            6 -> {
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    add<CameraxFragment>(R.id.camera_view)
                }
            }
            7 -> {
                clearBackStack()
                moveFragment(NavFragment())
            }
        }
    }

    fun activeChat() {

        binding.goChatting.setOnClickListener{
            moveFragment(ChattingFragment())
        }


    }


    private fun moveFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_container_view, fragment)
            .addToBackStack(null)
            .commit()

    }


    fun clearBackStack() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pref = getSharedPreferences("idKey", 0)
        userid = pref.getString("id", "").toString()
        var tagFromIntent: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        if (tagFromIntent != null) {
            var data = Ndef.get(tagFromIntent)
            println("정체")
            var message : NdefMessage? = null
            try{
                data.connect()
                message = data.ndefMessage
            }catch (e:Exception){


            }

            if (message != null){
                var record = message.records
                for (records in record) {
                    var convert = String(records.payload, StandardCharsets.UTF_8)
                    var spot = convert.substring(3)
                    sendUserId(spot,userid,"널")
                }

            }

        }

    }

    fun findPath() {
        if (nextSpotInfo != null) {
            thread {
                try {
                    var tMapPolyLine: TMapPolyLine = TMapData().findPathDataWithType(
                        TMapData.TMapPathType.CAR_PATH,
                        TMapPoint(gps!!.location.latitude, gps!!.location.longitude),
                        TMapPoint(nextSpotInfo!!.lat, nextSpotInfo!!.lon)
                    )
                    tMapPolyLine.lineColor = Color.rgb(14,23,55)
                    tMapPolyLine.outLineColor = Color.rgb(14,23,55)
                    tMapPolyLine.outLineWidth = 20F

                    tMapPolyLine.setLineWidth(0F)
                    tMapView.addTMapPolyLine("Line1", tMapPolyLine)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            var markerItem2 = TMapMarkerItem()
            markerItem2.tMapPoint = TMapPoint(nextSpotInfo!!.lat, nextSpotInfo!!.lon)
            markerItem2.name = "도착지"
            bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.arrive)
            markerItem2.icon = bitmap
            markerItem2.setPosition(0.5F, 0.5F)
            tMapView.addMarkerItem("도착지", markerItem2)
        }
    }

    private fun goNav() {
        tMapTapi = TMapTapi(this)
        if (tMapTapi.isTmapApplicationInstalled) {
            tMapTapi.invokeRoute(nextSpotInfo!!.spotName, nextSpotInfo!!.lon.toFloat(),
                nextSpotInfo!!.lat.toFloat()
            )
        } else {
            var uri = Uri.parse(tMapTapi.tMapDownUrl[0])
            var intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }


    fun sendData(fragment: Fragment, title: String,idx: String,url:String) {
        bundle.putString("title", title)
        bundle.putString("idx",idx)
        bundle.putString("url",url)
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container_view, fragment)
            .addToBackStack(null)
            .commit()
    }


    fun sendUserId(spot: String,userid:String,title: String) {
        val fragment = CameraxFragment()
        bundle.putString("spot", spot)
        bundle.putString("userid",userid)
        bundle.putString("title",title)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .remove(DeliveryFragment()).commit()

        supportFragmentManager.beginTransaction().add(R.id.camera_view,fragment).addToBackStack(null).commit()
    }

    private fun addMarker() {
        var markerItem = TMapMarkerItem()
        markerItem.tMapPoint = TMapPoint(gps!!.location.latitude, gps!!.location.longitude)
        markerItem.name = "현위치"
//        markerItem.visible = TMapMarkerItem.VISIBLE
        bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.pin_r_rn_1)
        markerItem.icon = bitmap
        markerItem.setPosition(0F, 0F)
        tMapView.addMarkerItem("현위치", markerItem)
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun sendLocation() {
        val retrofit = Retrofit.Builder().baseUrl("http://k7c205.p.ssafy.io:8000/")
            .addConverterFactory(GsonConverterFactory.create()).build()
        val callData = retrofit.create(NetWorkInterface::class.java)
        val locationBody = LocationInfo()

        pref = getSharedPreferences("idKey", 0)
        userid = pref.getString("id", "").toString()



        locationBody.driver = userid
        locationBody.longitude = gps!!.location.longitude.toString()
        locationBody.latitude = gps!!.location.latitude.toString()
        locationBody.timestamp = getTime()

        val call = callData.sendLocationLog(locationBody)
        call.enqueue(object : Callback<String?>{
            override fun onFailure(call: Call<String?>, t: Throwable) {
                Log.e("에러났다", t.toString())
            }

            override fun onResponse(call: Call<String?>, response: Response<String?>) {

            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getTime() : String {
        val now = System.currentTimeMillis()
        val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd.HH:mm", Locale.KOREA).format(now)
        val date : String = simpleDateFormat
        return date
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getLocalTime() : LocalDateTime {
        val now = LocalDateTime.now()
        return now
    }



    fun next(userid: String) {
        val retrofit = Retrofit.Builder().baseUrl("http://k7c205.p.ssafy.io:8000/")
            .addConverterFactory(GsonConverterFactory.create()).build()
        val callData = retrofit.create(NetWorkInterface::class.java)
        val call = callData.nextWork(userid.toInt())
        call.enqueue(object : Callback<NextSpotInfo>{
            override fun onFailure(call: Call<NextSpotInfo>, t: Throwable) {
                Log.e("에러났다", t.toString())
            }
            override fun onResponse(call: Call<NextSpotInfo>, response: Response<NextSpotInfo>) {
                nextSpotInfo = response.body()


            }
        })
    }
    fun cntClear() {
        cnt = 0
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 0) {

            if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
                backKeyPressedTime = System.currentTimeMillis();
                Toast.makeText(this,"뒤로가기를 한번 더 누르시면 종료됩니다.",Toast.LENGTH_SHORT).show()
                return;
            } else{
                super.onBackPressed()
            }
        }
        super.onBackPressed()

    }

    fun finished() {
//        startActivity(Intent(this@MainActivity,ReadyActivity::class.java))
        finish()

    }

    fun lockedChat() {
        binding.goChatting.setOnClickListener{
            Toast.makeText(applicationContext,"이미 채팅방이 켜져있습니다",Toast.LENGTH_SHORT).show()
        }}

    fun finishCheck(number:String) {


        val retrofit = Retrofit.Builder().baseUrl("http://k7c205.p.ssafy.io:9013/")
            .addConverterFactory(GsonConverterFactory.create()).build()
        val callData = retrofit.create(NetWorkInterface::class.java)
        val call = callData.getlist(number.toInt())



        call.enqueue(object : Callback<DeliveryList> {
            override fun onFailure(call: Call<DeliveryList>, t: Throwable) {
                Log.e("안됨",t.toString())
            }

            override fun onResponse(call: Call<DeliveryList>, response: Response<DeliveryList>) {
                val result = response.body()

                if (result!!.isFinished == true){
                    clearBackStack()
                    checkBack =1
                    val intent = Intent(applicationContext, ReadyActivity::class.java)
                    startActivity(intent)
                    finish()

                    return
                }

                if (result!!.del.size + result!!.rec.size == 0){
//                    clearBackStack()
//                    checkBack =1
                    val intent = Intent(applicationContext, ReadyActivity::class.java)
                    startActivity(intent)
                    finish()
                    return
                }

                changeFragment(4)


            } })


    }

    override fun onRestart() {
        super.onRestart()
        if (loadingDialog != null){
            loadingDialog!!.dismiss()
        }

    }

    }






