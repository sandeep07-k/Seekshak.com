package com.example.seekshakcom

import android.app.Application
import com.mappls.sdk.maps.Mappls
import com.mappls.sdk.services.account.MapplsAccountManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        MapplsAccountManager.getInstance().restAPIKey = BuildConfig.MAPPLS_REST_API_KEY
        MapplsAccountManager.getInstance().mapSDKKey = BuildConfig.MAPPLS_MAP_SDK_KEY
        MapplsAccountManager.getInstance().atlasClientId = BuildConfig.MAPPLS_CLIENT_ID
        MapplsAccountManager.getInstance().atlasClientSecret = BuildConfig.MAPPLS_CLIENT_SECRET

        Mappls.getInstance(applicationContext)
    }
}
