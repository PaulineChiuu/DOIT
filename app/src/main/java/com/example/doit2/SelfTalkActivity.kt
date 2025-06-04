package com.example.doit2

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doit2.databinding.ActivitySelfTalkBinding
import com.example.doit2.ui.adapter.SelfTalkAdapter
import com.example.doit2.ui.dialog.AddSelfTalkDialog
import java.text.SimpleDateFormat
import java.util.*

class SelfTalkActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelfTalkBinding
    private lateinit var selfTalkAdapter: SelfTalkAdapter
    private val selfTalkList = mutableListOf<SelfTalkEntry>()
    private val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("SelfTalkActivity", "=== 自我對話 Activity 開始 ===")

        binding = ActivitySelfTalkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        loadSelfTalkEntries()
        updateUI()

        android.util.Log.d("SelfTalkActivity", "=== 自我對話 Activity 完成 ===")
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "🗣️ 自我對話"
    }

    private fun setupRecyclerView() {
        selfTalkAdapter = SelfTalkAdapter(
            onEditClick = { entry ->
                showEditDialog(entry)
            },
            onDeleteClick = { entry ->
                showDeleteDialog(entry)
            }
        )

        binding.rvSelfTalk.apply {
            layoutManager = LinearLayoutManager(this@SelfTalkActivity)
            adapter = selfTalkAdapter
        }
    }

    private fun setupClickListeners() {
        // 新增對話按鈕
        binding.fabAddSelfTalk.setOnClickListener {
            showAddDialog()
        }

        // 清除全部按鈕
        binding.btnClearAll.setOnClickListener {
            showClearAllDialog()
        }
    }

    private fun loadSelfTalkEntries() {
        // 從 SharedPreferences 載入
        val prefs = getSharedPreferences("self_talk_data", MODE_PRIVATE)
        val entriesJson = prefs.getString("entries", "[]")

        // 簡單的 JSON 解析（可以用更複雜的方式）
        try {
            selfTalkList.clear()

            // 載入一些示例數據（首次使用）
            if (entriesJson == "[]") {
                selfTalkList.addAll(getDefaultEntries())
                saveSelfTalkEntries()
            } else {
                // 這裡可以實作 JSON 解析，暫時用預設數據
                selfTalkList.addAll(getDefaultEntries())
            }

            android.util.Log.d("SelfTalkActivity", "載入 ${selfTalkList.size} 條對話記錄")
        } catch (e: Exception) {
            android.util.Log.e("SelfTalkActivity", "載入對話記錄失敗", e)
            selfTalkList.addAll(getDefaultEntries())
        }
    }

    private fun getDefaultEntries(): List<SelfTalkEntry> {
        return listOf(
            SelfTalkEntry(
                id = 1,
                content = "今天完成了一個重要的專案，感覺很有成就感！繼續保持這個狀態。",
                mood = SelfTalkMood.HAPPY,
                createdAt = Date(System.currentTimeMillis() - 2 * 60 * 60 * 1000) // 2小時前
            ),
            SelfTalkEntry(
                id = 2,
                content = "最近壓力有點大，需要調整一下工作節奏，記得要適時休息。",
                mood = SelfTalkMood.THOUGHTFUL,
                createdAt = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000) // 1天前
            ),
            SelfTalkEntry(
                id = 3,
                content = "學會使用番茄鐘技巧後，專注力明顯提升了！這個方法真的很有效。",
                mood = SelfTalkMood.EXCITED,
                createdAt = Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000) // 3天前
            )
        )
    }

    private fun saveSelfTalkEntries() {
        val prefs = getSharedPreferences("self_talk_data", MODE_PRIVATE)
        // 簡化存儲，實際可以用 JSON 或資料庫
        prefs.edit().putInt("entries_count", selfTalkList.size).apply()
        android.util.Log.d("SelfTalkActivity", "保存 ${selfTalkList.size} 條對話記錄")
    }

    private fun showAddDialog() {
        val dialog = AddSelfTalkDialog.newInstance { content, mood ->
            addSelfTalkEntry(content, mood)
        }
        dialog.show(supportFragmentManager, "AddSelfTalkDialog")
    }

    private fun showEditDialog(entry: SelfTalkEntry) {
        val dialog = AddSelfTalkDialog.newInstance(entry) { content, mood ->
            editSelfTalkEntry(entry.id, content, mood)
        }
        dialog.show(supportFragmentManager, "EditSelfTalkDialog")
    }

    private fun showDeleteDialog(entry: SelfTalkEntry) {
        AlertDialog.Builder(this)
            .setTitle("刪除對話記錄")
            .setMessage("確定要刪除這條對話記錄嗎？")
            .setPositiveButton("刪除") { _, _ ->
                deleteSelfTalkEntry(entry)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showClearAllDialog() {
        if (selfTalkList.isEmpty()) {
            Toast.makeText(this, "沒有對話記錄可以清除", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("清除全部記錄")
            .setMessage("確定要清除所有對話記錄嗎？此操作無法撤銷。")
            .setPositiveButton("清除") { _, _ ->
                clearAllEntries()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun addSelfTalkEntry(content: String, mood: SelfTalkMood) {
        val newEntry = SelfTalkEntry(
            id = System.currentTimeMillis(),
            content = content,
            mood = mood,
            createdAt = Date()
        )

        selfTalkList.add(0, newEntry) // 新的放在最前面
        saveSelfTalkEntries()
        updateUI()

        Toast.makeText(this, "對話記錄已新增", Toast.LENGTH_SHORT).show()
        android.util.Log.d("SelfTalkActivity", "新增對話記錄: $content")
    }

    private fun editSelfTalkEntry(id: Long, content: String, mood: SelfTalkMood) {
        val index = selfTalkList.indexOfFirst { it.id == id }
        if (index != -1) {
            selfTalkList[index] = selfTalkList[index].copy(
                content = content,
                mood = mood
            )
            saveSelfTalkEntries()
            updateUI()
            Toast.makeText(this, "對話記錄已更新", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteSelfTalkEntry(entry: SelfTalkEntry) {
        selfTalkList.remove(entry)
        saveSelfTalkEntries()
        updateUI()
        Toast.makeText(this, "對話記錄已刪除", Toast.LENGTH_SHORT).show()
    }

    private fun clearAllEntries() {
        selfTalkList.clear()
        saveSelfTalkEntries()
        updateUI()
        Toast.makeText(this, "所有對話記錄已清除", Toast.LENGTH_SHORT).show()
    }

    private fun updateUI() {
        // 更新列表
        selfTalkAdapter.submitList(selfTalkList.toList())

        // 更新統計
        binding.tvTotalEntries.text = selfTalkList.size.toString()

        val todayEntries = selfTalkList.count { entry ->
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val entryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(entry.createdAt)
            today == entryDate
        }
        binding.tvTodayEntries.text = todayEntries.toString()

        // 顯示/隱藏空狀態
        if (selfTalkList.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvSelfTalk.visibility = View.GONE
            binding.btnClearAll.isEnabled = false
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvSelfTalk.visibility = View.VISIBLE
            binding.btnClearAll.isEnabled = true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

/**
 * 自我對話記錄
 */
data class SelfTalkEntry(
    val id: Long,
    val content: String,
    val mood: SelfTalkMood,
    val createdAt: Date
)

/**
 * 心情類型
 */
enum class SelfTalkMood(val emoji: String, val label: String, val color: String) {
    HAPPY("😊", "開心", "#4CAF50"),
    EXCITED("🤩", "興奮", "#FF9800"),
    THOUGHTFUL("🤔", "思考", "#2196F3"),
    CALM("😌", "平靜", "#9C27B0"),
    WORRIED("😟", "擔心", "#FF5722"),
    GRATEFUL("🙏", "感恩", "#795548")
}