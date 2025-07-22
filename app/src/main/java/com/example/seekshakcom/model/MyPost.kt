package com.example.seekshakcom.model

import java.io.Serializable

data class MyPost(
    val _id: String,
    val userId: String,
    val tuitionCode: Int,
    val className: String,
    val subject: String,
    val educationBoard: String,
    val fee: String,
    val duration: String,
    val classSchedule: String,
    val classTiming: String,
    val gender: String,
    val demoClassDate: String,
    val modeOfClass: String,
    val qualification: String,
    val specialReq: String,
    val latitude: Double?,
    val longitude: Double?,
    val sublocality: String?,
    val area: String?,
    val city: String?,
    val state: String?,
    val country: String?,
    val createdAt: String,
    val status: String = "active"
) : Serializable
