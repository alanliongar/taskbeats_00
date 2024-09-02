package com.devspace.taskbeats

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class CreateOrUpdateTaskBottomSheet(
    private val categoryList: List<CategoryEntity>, //trocamos de categoryuidata pra categoryentity para pegar a informação do banco, ao invés do que "mostra pro usuário".
    private val task: TaskUiData? = null,
    private val onCreateClicked: (TaskUiData) -> Unit,
    private val onUpdateClicked: (TaskUiData) -> Unit,
    private val onDeleteClicked: (TaskUiData) -> Unit
) : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.create_or_update_task_bottom_sheet, container, false)
        val btnCreateOrUpdate = view.findViewById<Button>(R.id.btn_task_create_or_update)
        val btnDelete = view.findViewById<Button>(R.id.btn_task_delete)
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val tieTaskName = view.findViewById<TextInputEditText>(R.id.tie_task_name)
        val catSpin = view.findViewById<Spinner>(R.id.category_list)
        var taskCategory: String? = null
        val categoryListTemp = mutableListOf("Select")
        categoryListTemp.addAll(categoryList.map { it.name })
        val categoryStr: List<String> = categoryListTemp
        ArrayAdapter(
            requireActivity().baseContext,
            R.layout.item_spinner,
            categoryStr.toList()
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.item_spinner)
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



        btnCreateOrUpdate.setOnClickListener {
            val name = tieTaskName.text.toString().trim()
            if (taskCategory != "Select" && name.isNotEmpty()) {
                if (task == null) {
                    onCreateClicked.invoke(
                        TaskUiData(
                            id = 0,
                            name = name,
                            category = requireNotNull(taskCategory)
                        )
                    )
                    dismiss()
                } else {
                    onUpdateClicked.invoke(
                        TaskUiData(
                            id = task.id,
                            name = name,
                            category = requireNotNull(taskCategory)
                        )
                    )
                    dismiss()
                }


            } else {
                Snackbar.make(btnCreateOrUpdate, "Please select a category", Snackbar.LENGTH_LONG)
                    .show()
            }
        }
        if (task == null) {
            btnDelete.isVisible = false
            tvTitle.setText(R.string.create_task_title)
            btnCreateOrUpdate.setText(R.string.create)
        } else {
            tvTitle.setText(R.string.update_task_title)
            btnCreateOrUpdate.setText(R.string.update)
            tieTaskName.setText(task.name)
            btnDelete.isVisible = true
            btnDelete.setOnClickListener {
                if (task != null) {
                    onDeleteClicked.invoke(task)
                    dismiss()
                } else {
                    Log.d("CreateOrUpdateTaskBottomSheet", "Task not found")
                }
            }
            catSpin.setSelection(categoryStr.indexOf(task.category))
        }
        return view
    }
}