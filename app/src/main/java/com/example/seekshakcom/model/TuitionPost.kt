package com.example.seekshakcom.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TuitionPost(
    val _id: String,
    val userId: String,
    val className: String,
    val subject: String,
    val educationBoard: String?,
    val fee: String?,
    val duration: String?,
    val classSchedule: String?,
    val classTiming: String?,
    val gender: String?,
    val demoClassDate: String?,
    val modeOfClass: String?,
    val qualification: String?,
    val specialReq: String?,
    val sublocality: String?,
    val area: String?,
    val city: String?,
    val state: String?,
    val country: String?,
    val postedDate: String?,
    val status: String?,
    val tuitionCode: Int?,
    val distanceInKm: Double?,
    val distanceInMeters: Double?
) : Parcelable

