package com.example.doit2

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.doit2.databinding.ActivityTaiwanTourMapBinding
import androidx.lifecycle.lifecycleScope
import com.example.doit2.data.database.AppDatabase
import com.example.doit2.data.database.PomodoroDao
import kotlinx.coroutines.launch
import android.view.LayoutInflater
import android.view.View

class TaiwanTourMapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaiwanTourMapBinding

    data class Region(
        val name: String,
        val unlockThreshold: Int,
        val imageResId: Int,
        val description: String
    )

    private lateinit var pomodoroDao: PomodoroDao

    private val regions = listOf(
        Region("淡江大學", 25, R.drawable.tamkang, "淡江大學位於新北市淡水區，是一所歷史悠久的私立大學，以優秀的國際交流聞名。"),
        Region("台北101", 50, R.drawable.taipei_101, "台北101是台灣最高的摩天大樓，以其獨特的設計和觀景台吸引大量遊客。"),
        Region("木柵動物園", 90, R.drawable.zoo, "木柵動物園是台北市最大的動物園，擁有豐富的動物種類和教育展示。"),
        Region("日月潭", 130, R.drawable.sun_moon_lake, "日月潭是台灣最大的湖泊，以美麗的湖光山色和環湖自行車道著稱。"),
        Region("九份老街", 180, R.drawable.jiufen, "九份老街以復古的街道、特色小吃及海景聞名，是熱門旅遊勝地。"),
        Region("阿里山", 240, R.drawable.alishan, "阿里山著名於壯麗的日出、雲海及阿里山鐵路，是台灣山林美景代表。"),
        Region("赤崁樓", 320, R.drawable.chihkan, "赤崁樓是台南的古蹟，融合了荷蘭與中國傳統建築風格，富有歷史意義。"),
        Region("綠島溫泉", 400, R.drawable.green_island, "綠島以溫泉及海底生態聞名，是潛水及放鬆的絕佳地點。"),
        Region("武嶺", 550, R.drawable.wuling, "武嶺為台灣公路最高點，風景壯麗，是許多自行車手挑戰的目標。"),
        Region("花蓮太魯閣", 650, R.drawable.taroko, "太魯閣國家公園以峽谷、岩壁及豐富的原住民文化著稱，是台灣必訪景點。"),
        Region("台東熱氣球", 750, R.drawable.balloon, "台東熱氣球嘉年華吸引無數旅客，在湛藍天空下乘坐繽紛熱氣球。"),
        Region("🏁 全台完成", 800, R.drawable.taiwan_complete, "恭喜你完成全台環島之旅！探索了台灣最美的景點與文化。")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaiwanTourMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "台灣環島旅程"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        pomodoroDao = AppDatabase.getDatabase(this).pomodoroDao()

        lifecycleScope.launch {
            val totalMinutes = pomodoroDao.getTotalFocusMinutes() ?: 0
            //可以改 val totalMinutes = 300  // 測試用，手動設定進度

            runOnUiThread {
                binding.tvFocusMinutes.text = "累積專注時間：$totalMinutes 分鐘"

                val maxMinutes = 800
                val progressPercent = (totalMinutes.toFloat() / maxMinutes * 100).toInt().coerceIn(0, 100)
                binding.progressTour.progress = progressPercent

                val currentRegionIndex = regions.indexOfLast { totalMinutes >= it.unlockThreshold }
                val currentRegionName = when {
                    currentRegionIndex >= 0 && currentRegionIndex < regions.size - 1 ->
                        "${regions[currentRegionIndex].name} → ${regions[currentRegionIndex + 1].name}"
                    currentRegionIndex == regions.size - 1 ->
                        "全台完成！"
                    else -> "尚未開始旅程"
                }
                binding.tvCurrentProgress.text = "目前進度：$currentRegionName"

                populateBadgeGrid(totalMinutes)
                createExplorationButtons(totalMinutes)
            }
        }
    }

    private fun populateBadgeGrid(totalMinutes: Int) {
        val inflater = LayoutInflater.from(this)
        binding.gridBadges.removeAllViews()

        regions.forEach { region ->
            val view = inflater.inflate(R.layout.item_badge, binding.gridBadges, false)

            val iv = view.findViewById<ImageView>(R.id.ivBadge)
            val tv = view.findViewById<TextView>(R.id.tvBadgeName)
            val lockTv = view.findViewById<TextView>(R.id.tvLock)

            iv.setImageResource(region.imageResId)
            tv.text = region.name

            val unlocked = totalMinutes >= region.unlockThreshold
            if (!unlocked) {
                iv.setColorFilter(0x99000000.toInt())
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
                iv.alpha = 0.5f
                lockTv.visibility = View.VISIBLE
            } else {
                iv.clearColorFilter()
                tv.setTextColor(ContextCompat.getColor(this, android.R.color.black))
                iv.alpha = 1f
                lockTv.visibility = View.GONE
            }

            binding.gridBadges.addView(view)
        }
    }

    private fun createExplorationButtons(totalMinutes: Int) {
        binding.llExplorationSpots.removeAllViews()

        regions.forEach { region ->
            val button = Button(this).apply {
                text = region.name
                isEnabled = totalMinutes >= region.unlockThreshold
                setOnClickListener {
                    if (isEnabled) {
                        AlertDialog.Builder(this@TaiwanTourMapActivity)
                            .setTitle(region.name)
                            .setMessage(region.description)
                            .setPositiveButton("關閉", null)
                            .show()
                    }
                }
            }
            binding.llExplorationSpots.addView(button)
        }
    }
}
