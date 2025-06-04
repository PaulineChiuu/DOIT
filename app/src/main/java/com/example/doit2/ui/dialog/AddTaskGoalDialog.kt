package com.example.doit2.ui.dialog

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.widget.*
import com.example.doit2.TaskGoal
import com.example.doit2.TaskGoalCategory
import com.example.doit2.TaskGoalPriority
import java.text.SimpleDateFormat
import java.util.*

object AddTaskGoalDialog {

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    fun newInstance(
        context: Context,
        goal: TaskGoal? = null,
        onSave: (String, String, TaskGoalCategory, TaskGoalPriority, Date) -> Unit
    ) {
        val scrollView = ScrollView(context)
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }

        // 標題輸入
        val titleLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        titleLayout.addView(TextView(context).apply {
            text = "目標標題 *"
            textSize = 14f
            setPadding(0, 0, 0, 8)
        })
        val titleEdit = EditText(context).apply {
            hint = "輸入目標標題..."
            setText(goal?.title ?: "")
        }
        titleLayout.addView(titleEdit)

        // 描述輸入
        val descLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16, 0, 0)
        }
        descLayout.addView(TextView(context).apply {
            text = "目標描述"
            textSize = 14f
            setPadding(0, 0, 0, 8)
        })
        val descEdit = EditText(context).apply {
            hint = "詳細描述您的目標..."
            minLines = 2
            maxLines = 4
            setText(goal?.description ?: "")
        }
        descLayout.addView(descEdit)

        // 分類選擇
        val categoryLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16, 0, 0)
        }
        categoryLayout.addView(TextView(context).apply {
            text = "目標分類 *"
            textSize = 14f
            setPadding(0, 0, 0, 8)
        })
        val categorySpinner = Spinner(context)
        val categoryAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            TaskGoalCategory.values().map { "${it.emoji} ${it.displayName}" }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        categorySpinner.adapter = categoryAdapter
        if (goal != null) {
            categorySpinner.setSelection(goal.category.ordinal)
        }
        categoryLayout.addView(categorySpinner)

        // 優先級選擇
        val priorityLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16, 0, 0)
        }
        priorityLayout.addView(TextView(context).apply {
            text = "優先級 *"
            textSize = 14f
            setPadding(0, 0, 0, 8)
        })
        val priorityGroup = RadioGroup(context)
        var selectedPriority = goal?.priority ?: TaskGoalPriority.MEDIUM

        TaskGoalPriority.values().forEach { priority ->
            val radioButton = RadioButton(context).apply {
                text = priority.displayName
                isChecked = priority == selectedPriority
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedPriority = priority
                }
            }
            priorityGroup.addView(radioButton)
        }
        priorityLayout.addView(priorityGroup)

        // 目標日期選擇
        val dateLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16, 0, 0)
        }
        dateLayout.addView(TextView(context).apply {
            text = "目標日期 *"
            textSize = 14f
            setPadding(0, 0, 0, 8)
        })

        var selectedDate = goal?.targetDate ?: Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000) // 預設一週後
        val dateButton = Button(context).apply {
            text = "選擇日期：${dateFormat.format(selectedDate)}"
            setOnClickListener {
                val calendar = Calendar.getInstance()
                calendar.time = selectedDate

                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        calendar.set(year, month, dayOfMonth)
                        selectedDate = calendar.time
                        text = "選擇日期：${dateFormat.format(selectedDate)}"
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
        dateLayout.addView(dateButton)

        // 組裝所有元件
        layout.addView(titleLayout)
        layout.addView(descLayout)
        layout.addView(categoryLayout)
        layout.addView(priorityLayout)
        layout.addView(dateLayout)

        scrollView.addView(layout)

        val title = if (goal != null) "編輯目標" else "新增目標"
        val buttonText = if (goal != null) "更新" else "新增"

        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(scrollView)
            .setPositiveButton(buttonText) { _, _ ->
                val titleText = titleEdit.text.toString().trim()
                val descText = descEdit.text.toString().trim()
                val category = TaskGoalCategory.values()[categorySpinner.selectedItemPosition]

                if (titleText.isEmpty()) {
                    Toast.makeText(context, "請輸入目標標題", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (titleText.length > 100) {
                    Toast.makeText(context, "標題不能超過100字", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (descText.length > 500) {
                    Toast.makeText(context, "描述不能超過500字", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // 檢查日期是否合理
                if (selectedDate.before(Date())) {
                    Toast.makeText(context, "目標日期不能是過去的時間", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                onSave(titleText, descText, category, selectedPriority, selectedDate)
            }
            .setNegativeButton("取消", null)
            .show()
    }
}