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

class CreateTaskBottomSheet(
    private val categoryList: List<CategoryUiData>,
    private val onCreateClicked: (TaskUiData) -> Unit
) : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.create_task, container, false)
        val btnCreate = view.findViewById<Button>(R.id.btn_task_create)
        val tieTaskName = view.findViewById<TextView>(R.id.tie_task_name)
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
                onCreateClicked.invoke(
                    TaskUiData(
                        id = 11,
                        name = name,
                        category = requireNotNull(taskCategory)
                    )
                )
            } else {
                Snackbar.make(btnCreate, "Please select a category", Snackbar.LENGTH_LONG).show()
            }
        }
        return view
    }
}