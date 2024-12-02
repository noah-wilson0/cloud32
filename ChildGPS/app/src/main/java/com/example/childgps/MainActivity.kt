package com.example.childgps

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.childgps.api.ApiClient
import com.kakao.vectormap.*
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.shape.MapPoints
import com.kakao.vectormap.shape.Polyline
import com.kakao.vectormap.shape.PolylineOptions
import com.kakao.vectormap.shape.PolylineStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections.*


class MainActivity : AppCompatActivity() {

    private lateinit var kakaoMap: KakaoMap
    private lateinit var polyline: Polyline // Polyline 객체 선언
    private val apiClient = ApiClient()
    private lateinit var locationUpdateJob: Job
    private lateinit var pathPoints: List<LatLng>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // MapView 초기화
        val mapView = findViewById<MapView>(R.id.map_view)
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {}

            override fun onMapError(error: Exception) {
                // 인증 실패 또는 지도 로드 중 에러 발생 시 처리
                error.printStackTrace()
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(readyMap: KakaoMap) {
                // 지도 준비 완료 시 호출
                kakaoMap = readyMap
                kakaoMap.changeMapType(MapType.NORMAL)
                kakaoMap.minZoomLevel


                // 주기적으로 위치 데이터를 가져오기 시작
                startLocationUpdates()

            } })

        //버튼 이벤트
        val declarationBtn = findViewById<ImageView>(R.id.declaration_btn)
        declarationBtn.setOnClickListener {
            val phoneNumber = "182"
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(intent)
        }
        val callButton = findViewById<ImageView>(R.id.call_btn)
        callButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:")
            }
            startActivity(intent)
        }

        val alarmBtn = findViewById<ImageView>(R.id.alarm_btn)

        alarmBtn.setOnClickListener {
            showConfirmationDialog()
        }


    }

    private fun createMarkar(latlng:LatLng) {
        val labelStyles = LabelStyles.from(
            "mystyle",
            LabelStyle.from(R.drawable.marker).setZoomLevel(8),
            LabelStyle.from(R.drawable.marker).setZoomLevel(11),
            LabelStyle.from(R.drawable.marker).setZoomLevel(15),
            LabelStyle.from(R.drawable.marker).setZoomLevel(32)

        )
        //마커 생성
        kakaoMap.labelManager!!.layer!!.removeAll()
        val pos = latlng
        Log.e("TAG", kakaoMap.zoomLevel.toString()) //15로 출력됨

        val addLabelStyles = kakaoMap.labelManager!!.addLabelStyles(labelStyles)
        kakaoMap.labelManager!!.layer!!.addLabel(
            LabelOptions.from(pos).setStyles(addLabelStyles)
        )
    }


    // 경로를 그리는 함수
    private fun drawRoute(pathPoints: List<LatLng>) {
        Log.e("LocationData","drawRoute시작")
        Log.e("LocationData",pathPoints.toString())
        val polylineOptions = PolylineOptions.from(
            MapPoints.fromLatLng(pathPoints),
            PolylineStyle.from(3f, Color.BLUE)
        )
        //마커 생성
        createMarkar(pathPoints.last())

        // 카메라를 경로의 첫 번째 지점으로 이동
        kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(pathPoints.last()))
        kakaoMap.shapeManager!!.layer.addPolyline(polylineOptions)

    }

    private fun startLocationUpdates() {
        Log.e("LocationData","startLocationUpdates시작")
        // CoroutineScope를 사용해 주기적으로 위치 데이터를 가져옵니다.
        locationUpdateJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                Log.e("LocationData","코루틴 시작")
                try {
                    val locations = apiClient.getAllLocations()
                    if (locations != null) {
                        // LocationData 리스트를 LatLng 리스트로 변환
                        pathPoints = apiClient.convertToLatLngList(locations)
                        Log.e("startLocationUpdates",pathPoints.toString())
                        withContext(Dispatchers.Main) {
                            // UI 스레드에서 Kakao Map에 Polyline 그리기
                            drawRoute(pathPoints)
                        }
                    }else {
                        Log.e("startLocationUpdates", "locations is null")
                    }


                } catch (e: Exception) {
                    Log.e("LocationData", "에러 발생: ${e.message}")
                }
                delay(2000) // 2초마다 요청
            }
        }
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("뱃지에 알림 보내기")
        builder.setMessage("정말로 알림을 보내시겠습니까?")
        // "Yes" 버튼
        builder.setPositiveButton("예") { dialog, which ->
            insertCallValue(1)
        }

        // "No" 버튼
        builder.setNegativeButton("아니오") { dialog, which ->
            dialog.dismiss()  // 다이얼로그 닫기
        }

        val dialog = builder.create()
        dialog.show()
    }
    private fun insertCallValue(callbelValue: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val success = ApiClient().insertCallValue(callbelValue)
            withContext(Dispatchers.Main) {
                if (success) {
                    Log.i("MainActivity", "Successfully inserted callbel value.")
                } else {
                    Log.e("MainActivity", "Failed to insert callbel value.")
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // Activity가 종료되면 Coroutine도 취소하여 메모리 누수 방지
        locationUpdateJob.cancel()
    }

}
