package com.tashichi.clipflowvideo

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.File

class PlayerActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private var projectId: Long = 0
    private lateinit var projectManager: ProjectManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        projectId = intent.getLongExtra("PROJECT_ID", 0)
        projectManager = ProjectManager(this)

        val project = projectManager.projects.find { it.id == projectId }

        if (project == null) {
            Toast.makeText(this, "Project not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        createUI(project)
        initializePlayer(project)
    }

    private fun createUI(project: Project) {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        playerView = PlayerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        mainLayout.addView(playerView)

        val infoText = android.widget.TextView(this).apply {
            text = "${project.name}\n${project.segmentCount} segments"
            textSize = 16f
            setPadding(16, 16, 16, 16)
        }
        mainLayout.addView(infoText)

        val backButton = Button(this).apply {
            text = "Back"
            setOnClickListener {
                finish()
            }
        }
        mainLayout.addView(backButton)

        setContentView(mainLayout)
    }

    private fun initializePlayer(project: Project) {
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        // セグメントをorder順にソート
        val sortedSegments = project.segments.sortedBy { it.order }

        println("再生準備: ${sortedSegments.size}セグメント")

        // 各セグメントのMediaItemを作成
        val mediaItems = sortedSegments.mapNotNull { segment ->
            val file = File(filesDir, segment.uri)
            if (file.exists()) {
                println("セグメント追加: ${segment.order} - ${segment.uri}")
                MediaItem.fromUri(file.absolutePath)
            } else {
                println("ファイル未発見: ${segment.uri}")
                null
            }
        }

        if (mediaItems.isEmpty()) {
            Toast.makeText(this, "No video files found", Toast.LENGTH_SHORT).show()
            return
        }

        // ExoPlayerにセグメントを設定（ギャップレス再生）
        player?.setMediaItems(mediaItems)
        player?.prepare()
        player?.play()

        println("ギャップレス再生開始: ${mediaItems.size}セグメント")
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }
}