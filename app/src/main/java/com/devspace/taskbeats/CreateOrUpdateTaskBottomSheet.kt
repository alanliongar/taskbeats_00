package com.devspace.taskbeats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class CreateOrUpdateTaskBottomSheet(
    private val categoryList: List<CategoryUiData>,
    private val task: TaskUiData? = null,
    private val onCreateClicked: (TaskUiData) -> Unit,
    private val onUpdateClicked: (TaskUiData) -> Unit
) : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.create_or_update_task_bottom_sheet, container, false)
        val btnCreate = view.findViewById<Button>(R.id.btn_task_create)
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val tieTaskName = view.findViewById<TextInputEditText>(R.id.tie_task_name)
        val catSpin = view.findViewById<Spinner>(R.id.category_list)
        var taskCategory: String? = null
        val categoryStr: List<String> = categoryList.map { it.name }
        ArrayAdapter(
            requireActivity().baseContext,
            android.R.layout.simple_spinner_item,
            categoryStr.toList()
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
            catSpin.adapter = adapter
        }
        catSpin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                taskCategory = categoryStr.get(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        btnCreate.setOnClickListener {
            if (taskCategory != null) {
                val name = tieTaskName.text.toString()

                if (task == null) {
                    onCreateClicked.invoke(
                        TaskUiData(
                            id = 11,
                            name = name,
                            category = requireNotNull(taskCategory)
                        )
                    )
                } else {
                    onUpdateClicked.invoke(
                        TaskUiData(
                            id = task.id,
                            name = name,
                            category = requireNotNull(taskCategory)
                        )
                    )
                }


            } else {
                Snackbar.make(btnCreate, "Please select a category", Snackbar.LENGTH_LONG).show()
            }
        }
        if (task == null) {
            tvTitle.setText(R.string.create_task_title)
            btnCreate.setText(R.string.create)
        } else {
            tvTitle.setText(R.string.update_task_title)
            btnCreate.setText(R.string.update)
            tieTaskName.setText(task.name)

            val currentCategory = categoryList.first { it.name == task.category }
            catSpin.setSelection(categoryList.indexOf(currentCategory))
        }
        return view
    }
}