package com.tashichi.clipflowvideo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var projectManager: ProjectManager
    private lateinit var cameraManager: CameraManager
    private lateinit var projectListContainer: LinearLayout

    private var currentProject: Project? = null
    private var currentScreen: AppScreen = AppScreen.PROJECTS

    private val CAMERA_PERMISSION_REQUEST = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        projectManager = ProjectManager(this)
        cameraManager = CameraManager(this)

        showProjectList()
    }

    private fun showProjectList() {
        currentScreen = AppScreen.PROJECTS

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val titleText = TextView(this).apply {
            text = "ClipFlow Video"
            textSize = 24f
            setPadding(0, 0, 0, 32)
        }
        mainLayout.addView(titleText)

        val newProjectButton = Button(this).apply {
            text = "New Project"
            setOnClickListener {
                val project = projectManager.createNewProject()
                currentProject = project
                showCameraScreen()
            }
        }
        mainLayout.addView(newProjectButton)

        projectListContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        displayProjects()

        val scrollView = ScrollView(this).apply {
            addView(projectListContainer)
        }
        mainLayout.addView(scrollView)

        setContentView(mainLayout)
    }

    private fun displayProjects() {
        projectListContainer.removeAllViews()

        for (project in projectManager.projects) {
            val projectView = TextView(this).apply {
                text = "${project.name}\nSegments: ${project.segmentCount}"
                textSize = 18f
                setPadding(16, 16, 16, 16)
                setBackgroundColor(0xFFEEEEEE.toInt())
                setOnClickListener {
                    currentProject = project
                    showCameraScreen()
                }
            }

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }

            projectListContainer.addView(projectView, layoutParams)
        }
    }

    private fun showCameraScreen() {
        currentScreen = AppScreen.CAMERA

        if (!cameraManager.hasCameraPermission()) {
            requestCameraPermission()
            return
        }

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val previewView = PreviewView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        mainLayout.addView(previewView)

        val recordButton = Button(this).apply {
            text = "REC (1 second)"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            isEnabled = false
            setOnClickListener {
                recordSegment()
            }
        }
        mainLayout.addView(recordButton)

        val playButton = Button(this).apply {
            text = "Play (${currentProject?.segmentCount ?: 0} segments)"
            setOnClickListener {
                currentProject?.let { project ->
                    if (project.segmentCount > 0) {
                        val intent = Intent(this@MainActivity, PlayerActivity::class.java)
                        intent.putExtra("PROJECT_ID", project.id)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@MainActivity, "No segments to play", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        mainLayout.addView(playButton)

        val backButton = Button(this).apply {
            text = "Back to Projects"
            setOnClickListener {
                showProjectList()
            }
        }
        mainLayout.addView(backButton)

        setContentView(mainLayout)

        runOnUiThread {
            cameraManager.setupCamera(
                previewView,
                this,
                ContextCompat.getMainExecutor(this)
            ) {
                recordButton.isEnabled = true
                Toast.makeText(this, "Camera Ready", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun recordSegment() {
        val project = currentProject ?: return

        cameraManager.recordOneSecond(
            project = project,
            onComplete = { segment ->
                project.addSegment(segment)
                projectManager.updateProject(project)

                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Segment ${segment.order} recorded",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onError = { error ->
                runOnUiThread {
                    Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ),
            CAMERA_PERMISSION_REQUEST
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                showCameraScreen()
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_LONG).show()
            }
        }
    }
}