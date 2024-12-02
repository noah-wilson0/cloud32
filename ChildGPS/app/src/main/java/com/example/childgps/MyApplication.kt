package com.example.childgps

import android.app.Application
import com.kakao.vectormap.KakaoMapSdk

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // KakaoMap SDK 초기화 (앱 실행 시 딱 한 번만 호출됨)
        KakaoMapSdk.init(this, "sdk");
    }
}
