package com.apk.agrostore

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for AgroStore.
 * This class is annotated with @HiltAndroidApp to trigger Hilt's code generation.
 */
@HiltAndroidApp
class AgroStoreApp : Application()