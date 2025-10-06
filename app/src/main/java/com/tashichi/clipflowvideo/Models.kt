package com.tashichi.clipflowvideo

import java.util.Date

// MARK: - VideoSegment
// iOS版のVideoSegmentと同等の構造
data class VideoSegment(
    val id: Long,
    val uri: String,
    val timestamp: Date,
    val facing: String, // "back" or "front"
    var order: Int
) {
    companion object {
        fun create(
            uri: String,
            facing: String,
            order: Int,
            id: Long = System.currentTimeMillis(),
            timestamp: Date = Date()
        ): VideoSegment {
            return VideoSegment(id, uri, timestamp, facing, order)
        }
    }
}

// MARK: - Project
// iOS版のProjectと同等の構造
data class Project(
    val id: Long,
    var name: String,
    var segments: MutableList<VideoSegment>,
    val createdAt: Date,
    var lastModified: Date
) {
    companion object {
        fun create(
            name: String,
            id: Long = System.currentTimeMillis(),
            segments: MutableList<VideoSegment> = mutableListOf(),
            createdAt: Date = Date()
        ): Project {
            return Project(id, name, segments, createdAt, createdAt)
        }
    }

    // セグメント追加
    fun addSegment(segment: VideoSegment) {
        segments.add(segment)
        lastModified = Date()
    }

    // セグメント数
    val segmentCount: Int
        get() = segments.size
}

// MARK: - AppScreen
// iOS版のAppScreenと同等
enum class AppScreen {
    PROJECTS,   // プロジェクト一覧
    CAMERA,     // カメラ撮影
    PLAYER      // 動画再生
}