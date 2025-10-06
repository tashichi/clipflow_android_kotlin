package com.tashichi.clipflowvideo

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ProjectManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("ClipFlowPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    var projects: MutableList<Project> = mutableListOf()
        private set

    init {
        loadProjects()
    }

    // プロジェクト作成
    fun createNewProject(): Project {
        val projectName = "Project ${projects.size + 1}"
        val project = Project.create(name = projectName)
        projects.add(project)
        saveProjects()
        println("新規プロジェクト作成: $projectName")
        return project
    }

    // プロジェクト更新
    fun updateProject(updatedProject: Project) {
        val index = projects.indexOfFirst { it.id == updatedProject.id }
        if (index != -1) {
            projects[index] = updatedProject
            saveProjects()
            println("プロジェクト更新: ${updatedProject.name}, セグメント数: ${updatedProject.segmentCount}")
        }
    }

    // プロジェクト削除
    fun deleteProject(project: Project) {
        projects.remove(project)
        saveProjects()
        println("プロジェクト削除: ${project.name}")
    }

    // プロジェクト保存
    private fun saveProjects() {
        val json = gson.toJson(projects)
        sharedPreferences.edit().putString("projects", json).apply()
        println("プロジェクト保存完了: ${projects.size}件")
    }

    // プロジェクト読み込み
    private fun loadProjects() {
        val json = sharedPreferences.getString("projects", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<Project>>() {}.type
            projects = gson.fromJson(json, type) ?: mutableListOf()
            println("プロジェクト読み込み完了: ${projects.size}件")
        } else {
            println("保存されたプロジェクトなし")
        }
    }
}