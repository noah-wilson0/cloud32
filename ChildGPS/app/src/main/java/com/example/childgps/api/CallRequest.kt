package com.example.childgps.api

data class CallRequest(
    val action: String, // 예: "insertCall"
    val value: Int      // 삽입할 callbel 값
)
