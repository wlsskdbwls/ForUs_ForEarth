package com.example.forusforearth

import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig

class cameraapp :Application(),CameraXConfig.Provider{
    override fun getCameraXConfig(): CameraXConfig = Camera2Config.defaultConfig()
}