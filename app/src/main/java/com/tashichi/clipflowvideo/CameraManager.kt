package com.tashichi.clipflowvideo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.video.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.concurrent.Executor

class CameraManager(private val context: Context) {
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null

    var isSetupComplete = false
        private set

    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun setupCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        executor: Executor,
        onComplete: () -> Unit
    ) {
        println("🔧 setupCamera() 開始")

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                println("✅ CameraProvider取得成功")

                val preview = androidx.camera.core.Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)
                println("✅ プレビュー設定完了")

                val recorder = Recorder.Builder()
                    .setQualitySelector(QualitySelector.from(Quality.HD))
                    .build()
                videoCapture = VideoCapture.withOutput(recorder)
                println("✅ Recorder設定完了")

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture
                )
                println("✅ カメラバインド完了")

                isSetupComplete = true
                println("✅ カメラセットアップ完全完了")

                onComplete()

            } catch (e: Exception) {
                println("❌ カメラセットアップエラー: ${e.message}")
                e.printStackTrace()
            }
        }, executor)
    }

    fun recordOneSecond(
        project: Project,
        onComplete: (VideoSegment) -> Unit,
        onError: (String) -> Unit
    ) {
        val videoCapture = this.videoCapture ?: run {
            onError("カメラが初期化されていません")
            return
        }

        val timestamp = System.currentTimeMillis()
        val filename = "segment_${timestamp}.mp4"
        val outputFile = File(context.filesDir, filename)

        val outputOptions = FileOutputOptions.Builder(outputFile).build()

        println("🎬 録画開始: $filename")

        var recordingStartTime: Long = 0

@Suppress("MissingPermission")

        activeRecording = videoCapture.output
            .prepareRecording(context, outputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        println("📹 録画実際に開始")
                        recordingStartTime = System.currentTimeMillis()

                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            stopRecording()
                        }, 1000)
                    }
                    is VideoRecordEvent.Finalize -> {
                        val recordingDuration = System.currentTimeMillis() - recordingStartTime
                        println("⏱️ 実際の録画時間: ${recordingDuration}ms")

                        if (event.hasError()) {
                            println("❌ 録画エラー: ${event.error}")
                            onError("録画エラー: ${event.error}")
                        } else {
                            println("✅ 録画完了: $filename")

                            val segment = VideoSegment.create(
                                uri = filename,
                                facing = "back",
                                order = project.segmentCount + 1
                            )

                            onComplete(segment)
                        }
                        activeRecording = null
                    }
                }
            }
    }

    private fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
        println("🛑 録画停止")
    }
}