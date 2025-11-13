# ClipFlow Android版 - 開発ルール & ベストプラクティス

## 📁 プロジェクト情報

**プロジェクト名:** ClipFlow Video (Android)  
**Package Name:** com.tashichi.clipflowvideo  
**開発環境:** Android Studio + Kotlin + CameraX + ExoPlayer  
**GitHubリポジトリ:** https://github.com/tashichi/clipflow_android  
**プロジェクトパス:** `/Users/tanizawakenji/AndroidStudioProjects/ClipFlowVideo/`

---

## 🎯 開発哲学

### 基本原則
1. **iOS版の設計を最大限参考にする** - 成功した設計パターンを移植
2. **シームレス再生は絶対に妥協しない** - ExoPlayerで実現
3. **安定版を絶対に壊さない** - 動作する版を常に保持
4. **小さな変更の積み重ね** - 一度に1つの機能のみ実装
5. **実機テスト重視** - エミュレーターだけでなく実機で検証

### 避けるべきパターン
- ❌ 複数機能の同時実装（大量エラーの原因）
- ❌ iOS版の優秀な設計を無視した独自実装
- ❌ ExoPlayerを使わない個別セグメント再生（ギャップが発生）
- ❌ 完璧主義（永遠に公開できないアプリ化）

---

## 🤖 Claude Code サブエージェント活用ルール

### 基本ワークフロー（分散並列開発）
```
┌─────────────────────────────────────────┐
│         Phase開始（機能単位）            │
└──────────────┬──────────────────────────┘
               ↓
┌──────────────┴──────────────────────────┐
│    サブエージェント並列実行（最大3-4個）   │
├──────────────┬──────────────────────────┤
│ Agent 1:     │ Agent 2:                 │
│ UI実装       │ ロジック実装              │
│              │                          │
│ Agent 3:     │ Agent 4:                 │
│ データモデル │ テストコード作成          │
└──────────────┴──────────────────────────┘
               ↓
┌──────────────┴──────────────────────────┐
│           統合 & ビルド                  │
│  - コンパイル確認                        │
│  - 依存関係チェック                      │
│  - 初期動作確認                          │
└──────────────┬──────────────────────────┘
               ↓
┌──────────────┴──────────────────────────┐
│      Definition of Done (DoD) 確認      │
│  ✅ コンパイル成功                       │
│  ✅ エミュレーター動作確認                │
│  ✅ 実機テスト完了                       │
│  ✅ 既存機能の動作確認                   │
│  ✅ コードレビュー完了                   │
│  ✅ Gitコミット & プッシュ               │
└──────────────┬──────────────────────────┘
               ↓
┌──────────────┴──────────────────────────┐
│      プロダクションビルド & リリース      │
│  - APK/AAB作成                          │
│  - Google Play内部テスト（必要時）       │
│  - タグ付け (例: v1.0-Feature-Name)     │
└──────────────┬──────────────────────────┘
               ↓
         Phase完了
```

---

## 📋 Definition of Done (DoD) チェックリスト

### 必須項目（Phase完了の条件）

#### 1. ビルド & コンパイル ✅
- [ ] Android Studioでビルド成功（Build → Make Project）
- [ ] Gradle同期成功
- [ ] 警告の確認（Critical警告はゼロ）
- [ ] エラーゼロ

#### 2. エミュレーターテスト ✅
- [ ] Pixel 6（最新エミュレーター）で起動確認
- [ ] 新機能の基本動作確認
- [ ] UI表示確認（レイアウト崩れなし）

#### 3. 実機テスト ✅
- [ ] 実機Android（AQUOS sense5G）で動作確認
- [ ] 新機能の完全動作確認
- [ ] カメラ・ストレージ等の権限動作確認
- [ ] パフォーマンス確認（ラグなし）

#### 4. リグレッションテスト（既存機能の動作確認）✅
- [ ] プロジェクト作成・削除
- [ ] 1秒録画機能
- [ ] ExoPlayerギャップレス連続再生
- [ ] データ永続化（SharedPreferences）

#### 5. コードレビュー ✅
- [ ] コードの可読性確認
- [ ] iOS版の設計パターン踏襲確認
- [ ] メモリリーク確認（onDestroy等のクリーンアップ）
- [ ] エラーハンドリング確認

#### 6. Git管理 ✅
- [ ] 意味のあるコミットメッセージ
- [ ] Gitコミット実行
- [ ] GitHubへプッシュ
- [ ] タグ付け（重要なマイルストーンの場合）

#### 7. ドキュメント更新 ✅
- [ ] README.md更新（機能追加の場合）
- [ ] CHANGELOG.md更新（バージョン管理）
- [ ] プロジェクトナレッジ更新

---

## 🔧 技術スタック & ファイル構成

### 主要ファイル一覧

| ファイル名 | 役割 | iOS版対応 |
|-----------|------|----------|
| `MainActivity.kt` | 画面遷移管理 | MainView.swift |
| `PlayerActivity.kt` | ExoPlayerギャップレス再生 | PlayerView.swift |
| `CameraManager.kt` | カメラ・録画制御 | VideoManager.swift |
| `ProjectManager.kt` | プロジェクトCRUD・永続化 | ProjectManager.swift |
| `Models.kt` | データ構造（Project, VideoSegment） | Models.swift |

### 依存技術
- **Kotlin**: 言語
- **CameraX API**: カメラ録画（1.3.1）
- **ExoPlayer (media3)**: 動画再生（1.2.1）
- **SharedPreferences + Gson**: データ永続化（2.10.1）

---

## 🎯 iOS版からの移植状況

### ✅ 完成済み機能（2025年10月6日時点）
1. **ExoPlayerギャップレス再生**: `setMediaItems()`で実現、iOS版のAVComposition相当
2. **データ構造**: iOS版Models.swiftを完全移植
3. **プロジェクト管理**: 作成・保存・削除・永続化
4. **カメラ録画**: CameraXで音声付き録画成功
5. **基本UI**: プロジェクト一覧→カメラ→再生の3画面構成

### ⚠️ 現在の課題（2025年10月6日時点）
1. **録画時間が短い問題**
   - 症状: 1秒ではなく0.25秒程度
   - 原因: CameraXの録画開始に遅延、`VideoRecordEvent.Start`後にタイマー開始で修正済み
   - 状況: 実機テストで録画時間の確認が必要

2. **CameraXリソース解放エラー**
   - 症状: Logcatに大量のCameraX COREエラー
   - 原因: MainActivity終了時にCameraリソースが解放されていない
   - 解決策: `onDestroy()`でのクリーンアップ処理が必要

### ❌ 未実装機能
- [ ] エクスポート機能
- [ ] フリーミアム制限（3プロジェクト）
- [ ] Google Play課金システム
- [ ] ライト機能（懐中電灯）
- [ ] UI/UXのiOS版レベルへの洗練化

---

## 💡 重要な技術的知見

### ExoPlayerギャップレス再生の実装
```kotlin
val mediaItems = segments.map { MediaItem.fromUri(file.absolutePath) }
player?.setMediaItems(mediaItems)
player?.prepare()
player?.play()
```
- iOS版のAVComposition相当の機能
- `setMediaItems()`で複数動画を一度にセット
- 継ぎ目なしのシームレス再生を実現

### CameraX録画の実装パターン
```kotlin
activeRecording = videoCapture.output
    .prepareRecording(context, outputOptions)
    .withAudioEnabled()
    .start(ContextCompat.getMainExecutor(context)) { event ->
        when (event) {
            is VideoRecordEvent.Start -> { 
                // ここでタイマー開始（重要）
            }
            is VideoRecordEvent.Finalize -> { 
                // 完了処理
            }
        }
    }
```

### データ永続化パターン
```kotlin
// 保存
val json = Gson().toJson(projects)
prefs.edit().putString("projects", json).apply()

// 読み込み
val json = prefs.getString("projects", "[]")
val type = object : TypeToken<List<Project>>() {}.type
projects = Gson().fromJson(json, type)
```

---

## 🚀 サブエージェント活用の具体例

### 例: 「エクスポート機能」実装

#### Phase定義
**目標**: プロジェクトをMP4ファイルとしてギャラリーに保存

#### サブエージェント分担
```
Agent 1 (UI実装): PlayerActivity.kt
- エクスポートボタン追加
- 進捗表示ダイアログ
- 完了トースト表示

Agent 2 (ロジック実装): ProjectManager.kt
- ExoPlayerで統合動画作成
- MediaMuxerでMP4出力
- ギャラリー登録処理

Agent 3 (権限処理): MainActivity.kt
- WRITE_EXTERNAL_STORAGE権限
- Android 10以降のScopedStorage対応

Agent 4 (テスト): 統合テストコード
- エクスポート動作テスト
- ファイル保存確認
- ギャラリー表示確認
```

#### DoD確認
- ✅ ビルド成功
- ✅ エミュレーターでエクスポート動作確認
- ✅ 実機テスト（実際のMP4ファイル作成確認）
- ✅ ギャラリーアプリで再生確認
- ✅ データ保存・復元確認
- ✅ Gitコミット「✨ エクスポート機能実装完了」

---

## 📦 プロダクションビルド手順

### Google Play内部テスト配信
```bash
1. Android Studio: Build → Generate Signed Bundle / APK
2. Android App Bundle (AAB) を選択
3. キーストア作成・選択
4. Google Play Console → 内部テスト
5. AABアップロード
6. 内部テスター追加・テスト実行
```

### バージョン管理
```bash
# 重要なマイルストーン時
git tag -a v1.0-Export-Feature -m "📦 エクスポート機能実装完了"
git push origin v1.0-Export-Feature
```

---

## ⚠️ 重要な注意事項

### コード修正時の原則
1. **必ずGitコミット前の状態を保存**
```bash
   git stash  # 一時保存
```

2. **段階的修正**
   - 一度に1ファイルのみ修正
   - 修正後、即座にビルド確認
   - 問題があれば即座にロールバック

3. **実機テスト必須の機能**
   - カメラ録画
   - ExoPlayer再生
   - ストレージアクセス
   - 権限処理

### Android固有の注意点
- **CameraXライフサイクル管理**: `onDestroy()`でリソース解放必須
- **権限処理**: Android 6.0以降のランタイム権限
- **ScopedStorage**: Android 10以降のストレージアクセス
- **非同期処理**: iOSとタイミングが異なる（`VideoRecordEvent.Start`後処理）

---

## 📊 現在のアプリ状態（2025年11月13日）

### 完成済み機能
- ✅ プロジェクト管理（作成・保存・削除）
- ✅ 1秒動画録画（音声付き、CameraX）
- ✅ ExoPlayerギャップレス再生（iOS版のAVComposition相当）
- ✅ データ永続化（SharedPreferences + Gson）
- ✅ 基本3画面構成

### 今後の実装候補
- [ ] 録画時間問題の解決（実機テスト）
- [ ] CameraXエラー解決（onDestroy()クリーンアップ）
- [ ] エクスポート機能（MP4出力）
- [ ] フリーミアム制限（3プロジェクト）
- [ ] Google Play課金システム
- [ ] ライト機能
- [ ] UI/UX洗練化（iOS版レベル）

---

## 🎓 Android開発の学習ポイント

### CameraXの特性
- 録画開始に遅延がある → `VideoRecordEvent.Start`を待つ
- ライフサイクル管理が重要 → `onDestroy()`で確実にクリーンアップ

### ExoPlayerの優位性
- `setMediaItems()`で簡単にギャップレス再生実現
- iOS版のAVCompositionより実装がシンプル

### Kotlin開発のベストプラクティス
- データクラスでモデル定義（`data class`）
- Null安全性（`?`, `!!`, `?.let`）
- SharedPreferences + Gson でJSON永続化

---

**作成日:** 2025年11月13日  
**最終更新:** 2025年11月13日  
**管理者:** tashichi（谷澤健二）  
**ステータス:** 運用中
