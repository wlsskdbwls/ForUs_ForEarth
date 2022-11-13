package com.example.forusforearth

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.forusforearth.util.PathUtil
import com.example.forusforearth.databinding.ActivityCameraBinding
import com.example.forusforearth.extensions.loadCenterCrop
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding

    private lateinit var cameraExecutor: ExecutorService
    private val cameraMainExecutor by lazy { ContextCompat.getMainExecutor(this) }
    private val cameraProviderFuture by lazy { ProcessCameraProvider.getInstance(this) } // 카메라 얻어오면 이후 실행 리스너 등록

    private lateinit var imageCapture: ImageCapture

    private val displayManager by lazy {
        getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }
    private var displayID:Int=-1

    private var camera: Camera?=null
    private var root: View?= null

    private var isCapturing:Boolean = false

    private val displayListener =object : DisplayManager.DisplayListener{
        override fun onDisplayAdded(displayID: Int) {}

        override fun onDisplayRemoved(displayId: Int) {}

        override fun onDisplayChanged(displayId: Int) {
            if (this@CameraActivity.displayID==displayId){

            }

        }

    }

    private var uriList= mutableListOf<Uri>()  //사진 리스트

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityCameraBinding.inflate(layoutInflater)
        root=binding.root
        setContentView(binding.root)
        if(allPermissionsGranted()){
            startCamera(binding.viewFinder)  //권한이 허용되면 카메라 시작
        } else{
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )

        }

    }



    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED          //카메라 권한
    }
    private fun startCamera(viewFinder: PreviewView){
        displayManager.registerDisplayListener(displayListener,null)
        cameraExecutor = Executors.newSingleThreadExecutor()
        viewFinder.postDelayed({
            displayID=viewFinder.display.displayId
            bindCameraUseCase()
        },10)
    }

    private fun bindCameraUseCase()=with(binding){
        val rotation=viewFinder.display.rotation
        val cameraSelectore= CameraSelector.Builder().requireLensFacing(LENS_FACING).build() // 카메라 설정(후면)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider =cameraProviderFuture.get()
            val preview=Preview.Builder().apply {
                setTargetAspectRatio(AspectRatio.RATIO_4_3)
                setTargetRotation(rotation)
            }.build()
            val imageCaptureBulider=ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(rotation)
                .setFlashMode(ImageCapture.FLASH_MODE_AUTO)

            imageCapture= imageCaptureBulider.build()
            try {
                cameraProvider.unbindAll()// 기존에 바인딩 되어 있는 카메라는 해제
                camera=cameraProvider.bindToLifecycle(
                    this@CameraActivity,cameraSelectore,preview,imageCapture
                )
                preview.setSurfaceProvider(viewFinder.surfaceProvider)
                bindCaptureListener()
                bindPreviewImageViewClickListener()
            } catch (e: Exception){
                e.printStackTrace()
            }
        }, cameraMainExecutor)

    }
    private fun bindCaptureListener() = with(binding){
        captureButton.setOnClickListener{
            if (isCapturing.not()) {
                isCapturing=true
                captureCamera()

            }

        }
    }

    private fun updateSavedImageContent(){
        contentUri?.let{
            isCapturing=try {
                val file = File(PathUtil.getpath(this,it)?:throw FileNotFoundException())
                MediaScannerConnection.scanFile(this, arrayOf(file.path), arrayOf("image/jpeg"),null)
                Handler(Looper.getMainLooper()).post{
                    binding.previewImageVIew.loadCenterCrop(url = it.toString(), corner = 4f)
                }
                uriList.add(it)

                false
            }catch (e:Exception){
                e.printStackTrace()
                Toast.makeText(this,"파일이 존재하지 않습니다", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    private var contentUri: Uri?=null

    private fun captureCamera(){
        if (::imageCapture.isInitialized.not()) return
        val photofile = File(
            PathUtil.getOutputDirectory(this),
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.KOREA
            ).format(System.currentTimeMillis()) + ".jpg"

        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photofile).build()
        imageCapture.takePicture(outputOptions,cameraExecutor, object : ImageCapture.OnImageSavedCallback{
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri= outputFileResults.savedUri ?: Uri.fromFile(photofile)
                contentUri=savedUri
                updateSavedImageContent()
            }

            override fun onError(e: ImageCaptureException) {
                e.printStackTrace()
                isCapturing=false
            }

        })

    }

    private fun bindPreviewImageViewClickListener() = with(binding){
        previewImageVIew.setOnClickListener{
            startActivity(
                ImageListActivity.newIntent(this@CameraActivity,uriList)
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                startCamera(binding.viewFinder)
            }else{
                Toast.makeText(this,"카메라 권한이 없습니다", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }


    companion object{

        private const val FILENAME_FORMAT="yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS= 10
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
        private val LENS_FACING:Int= CameraSelector.LENS_FACING_BACK
    }
}