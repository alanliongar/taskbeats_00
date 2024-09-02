package com.devspace.taskbeats
//Onde parei? Terminei aula 24, devo encerrar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings.Global
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private var categoriess = listOf<CategoryUiData>()
    private var categoriesEntity = listOf<CategoryEntity>()
    private var taskss = listOf<TaskUiData>()
    private lateinit var rvCategory: RecyclerView
    private lateinit var ctnEmptyView: LinearLayout
    private lateinit var fabCreateTask: FloatingActionButton
    private val taskAdapter by lazy {
        TaskListAdapter()
    }
    private val categoryAdapter = CategoryListAdapter()
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            TaskBeatDataBase::class.java, "database-taskbeat"
        ).build()
    }

    //TTT
    private val categoryDao: CategoryDao by lazy {
        db.getCategoryDao()
    }

    private val taskDao: TaskDao by lazy {
        db.getTaskDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applicationContext.deleteDatabase("database-taskbeat-v1")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
/*        GlobalScope.launch(Dispatchers.Main) {
            insertDefaultCategory(categories)
            insertDefaultTasks(tasks)
            getTasksFromDataBase()
            getCategoriesFromDataBase()
        }*/ //linhas de código pra adicionar as tarefas e categorias pra testar - fins de desenvolvimento

        rvCategory = findViewById<RecyclerView>(R.id.rv_categories)
        ctnEmptyView = findViewById<LinearLayout>(R.id.ll_empty_view)
        val rvTask = findViewById<RecyclerView>(R.id.rv_tasks)
        fabCreateTask = findViewById<FloatingActionButton>(R.id.fab_create_task)
        val btnCreateEmpty = findViewById<Button>(R.id.btn_create_empty)
        btnCreateEmpty.setOnClickListener {
            showCreateCategoryBottomSheet()
        }

        fabCreateTask.setOnClickListener {
            showCreateUpdateTaskBottomSheet()
        }

        taskAdapter.setOnClickListener { task ->
            showCreateUpdateTaskBottomSheet(task)
        }

        categoryAdapter.setOnLongClickListener { categoryToBeDeleted ->
            if (categoryToBeDeleted.name != "+" && categoryToBeDeleted.name != "ALL") {
                val title: String = this.getString(R.string.category_delete_title)
                val desc: String = this.getString(R.string.category_delete_description)
                val btnText: String = this.getString(R.string.delete)
                showInfoDialog(
                    title, desc, btnText
                )
                {
                    val categoryEntityToBeDeleted =
                        CategoryEntity(categoryToBeDeleted.name, categoryToBeDeleted.isSelected)
                    deleteCategory(categoryEntityToBeDeleted)
                }
            }
        }

        categoryAdapter.setOnClickListener { selected ->
            if (selected.name == "+") {
                showCreateCategoryBottomSheet()
            } else {
                val categoryTemp = categoriess.map { item ->
                    when {
                        item.name == selected.name && item.isSelected -> item.copy(isSelected = true)
                        item.name == selected.name && !item.isSelected -> item.copy(isSelected = true)
                        item.name != selected.name && item.isSelected -> item.copy(isSelected = false)
                        else -> item
                    }
                }

                val taskTemp =
                    if (selected.name != "ALL") {
                        filterTaskByCategoryName(selected.name)
                    } else {
                        GlobalScope.launch(Dispatchers.Main) {
                            getTasksFromDataBase()
                        }
                    }
                categoryAdapter.submitList(categoryTemp)
            }
        }

        rvCategory.adapter = categoryAdapter
        //insertCategory(catEntEmpt)
        GlobalScope.launch(Dispatchers.IO) {
            getCategoriesFromDataBase()
        }
        rvTask.adapter = taskAdapter


        //categoryAdapter.submitList(categories)
        GlobalScope.launch(Dispatchers.Main) {
            getTasksFromDataBase()
        }
    }

    private suspend fun insertDefaultCategory(cats: List<CategoryUiData>) {
        withContext(Dispatchers.IO) {
            val categoriesEntity =
                cats.map { //isso aqui é um tipo de categoryEntity, são objetos sendo transformados em DB
                    CategoryEntity(
                        name = it.name,
                        isSelected = it.isSelected
                    )
                }
            categoryDao.insetAll(categoriesEntity)
        }
    }

    private suspend fun insertDefaultTasks(task: List<TaskUiData>) {
        withContext(Dispatchers.IO) {
            val tasksEntity = task.map {
                TaskEntity(
                    id = it.id,
                    name = it.name,
                    category = it.category
                )
            }
            taskDao.insertAll(tasksEntity)
        }
    }

    private fun showInfoDialog(
        title: String,
        description: String,
        btnText: String,
        onClick: () -> Unit
    ) {
        val infoBottomSheet = InfoBottomSheet(
            title = title,
            description = description,
            btnText = btnText,
            onClick
        )
        infoBottomSheet.show(supportFragmentManager, "infoBottomSheet")
    }

    private suspend fun getCategoriesFromDataBase() {
        withContext(Dispatchers.IO) {
            val categoriesFromDb: List<CategoryEntity> = categoryDao.getAll()
            categoriesEntity = categoriesFromDb
            withContext(Dispatchers.Main) { //quando for mexer em visualização, precisa estar na MAIN THREAD.
                if (categoriesEntity.isEmpty()) {
                    rvCategory.isVisible = false
                    ctnEmptyView.isVisible = true
                    fabCreateTask.isVisible = false

                } else {
                    rvCategory.isVisible = true
                    ctnEmptyView.isVisible = false
                    fabCreateTask.isVisible = true
                }
            }

            val categoriesUiData =
                categoriesFromDb.map { //Aqui é o contrário do que está sendo feito na linha 64: estamos pegando do banco de dados e puxando pra lista de novo.
                    CategoryUiData(name = it.name, isSelected = it.isSelected)
                }.toMutableList() //esse tomutablelist serve pro que tá no nome mutablelist
            categoriesUiData.add(CategoryUiData(name = "+", isSelected = false))
            val tempCategory = mutableListOf<CategoryUiData>()
            if (taskss.isNotEmpty()) {
                tempCategory.add(CategoryUiData(name = "ALL", isSelected = true))
            }
            tempCategory.addAll(categoriesUiData)
            withContext(Dispatchers.Main) {
                categoriess = tempCategory
                categoryAdapter.submitList(categoriess)
            }
        }
    }

    private suspend fun getTasksFromDataBase() {
        withContext(Dispatchers.IO) {
            val tasksFromDb: List<TaskEntity> = taskDao.getAll()
            val tasksUiData: List<TaskUiData> = tasksFromDb.map {
                TaskUiData(
                    id = it.id,
                    name = it.name,
                    category = it.category
                )
            }
            withContext(Dispatchers.Main) {
                taskss = tasksUiData
                taskAdapter.submitList(taskss)
            }
            getCategoriesFromDataBase()
        }
    }

    private fun insertCategory(categoryEntity: CategoryEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            categoryDao.inset(categoryEntity)//ainda precisa atualizar a view pro usuário
            getCategoriesFromDataBase()
        }
    }

    private fun insertTask(taskEntity: TaskEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            taskDao.insert(taskEntity)
            getTasksFromDataBase()
        }
    }

    private fun updateTask(taskEntity: TaskEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            taskDao.update(taskEntity)
            getTasksFromDataBase()
        }
    }

    private fun deleteTask(taskEntity: TaskEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            taskDao.delete(taskEntity)
            getTasksFromDataBase()
        }
    }

    private fun deleteCategory(categoryEntity: CategoryEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            val tasksToBeDeleted = taskDao.getAllByCategoryName(categoryEntity.name)
            taskDao.deleteAll(tasksToBeDeleted)
            categoryDao.delete(categoryEntity)
            getCategoriesFromDataBase()
            getTasksFromDataBase()
        }
    }

    private fun filterTaskByCategoryName(category: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val tasksFromDb: List<TaskEntity> = taskDao.getAllByCategoryName(category)
            val tasksUiData: List<TaskUiData> =
                tasksFromDb.map { TaskUiData(id = it.id, name = it.name, category = it.category) }
            GlobalScope.launch(Dispatchers.Main) {
                taskAdapter.submitList(tasksUiData)
            }
        }
    }

    private fun showCreateUpdateTaskBottomSheet(taskUiData: TaskUiData? = null) {
        val createTaskBottomSheet = CreateOrUpdateTaskBottomSheet(
            task = taskUiData,
            categoryList = categoriesEntity,
            onCreateClicked = { taskToBeCreated ->
                val taskEntityToBeInsert = TaskEntity(
                    name = taskToBeCreated.name,
                    category = taskToBeCreated.category
                )
                insertTask(taskEntityToBeInsert)
            },
            onUpdateClicked = { taskToBeUpdated ->
                val taskEntityToBeUpdate = TaskEntity(
                    id = taskToBeUpdated.id, //cuidado com essa porra
                    name = taskToBeUpdated.name,
                    category = taskToBeUpdated.category
                )

                updateTask(taskEntityToBeUpdate)
            },
            onDeleteClicked = { taskToBeDeleted ->
                val taskEntityToBeDeleted = TaskEntity(
                    id = taskToBeDeleted.id,
                    name = taskToBeDeleted.name,
                    category = taskToBeDeleted.category
                )
                deleteTask(taskEntityToBeDeleted)
            }
        )
        createTaskBottomSheet.show(supportFragmentManager, "createTaskBottomSheet")
    }

    private fun showCreateCategoryBottomSheet() {
        val createCategoryBottomSheet = CreateCategoryBottomSheet { categoryName ->
            val categoryEntity = CategoryEntity(
                name = categoryName,
                isSelected = false
            )
            insertCategory(categoryEntity)
        }
        createCategoryBottomSheet.show(supportFragmentManager, "createCategoryBottomSheet")
    }
}

//
val categories: List<CategoryUiData> = listOf(
    //Lista vazia pra testar se o dado foi pro DB mesmo.
    /*CategoryUiData(
        name = "ALL",
        isSelected = false
    ),*/
    CategoryUiData(
        name = "STUDY",
        isSelected = false
    ),
    CategoryUiData(
        name = "WORK",
        isSelected = false
    ),
    CategoryUiData(
        name = "WELLNESS",
        isSelected = false
    ),
    CategoryUiData(
        name = "HOME",
        isSelected = false
    ),
    CategoryUiData(
        name = "HEALTH",
        isSelected = false
    ),
    CategoryUiData(
        name = "HEALTH",
        isSelected = false
    ),
    CategoryUiData(
        name = "HEALTH",
        isSelected = false
    ),
    CategoryUiData(
        name = "HEALTH",
        isSelected = false
    )
)


//Aula 5 (ao vivo) -
//1 - Inserir essas tasks na base de dados (pelo menos criar uma)
//2 - Ler as tarefas da base de dados

val tasks = listOf(
    TaskUiData(
        0,
        "01 - Ler 10 páginas do livro atual",
        "STUDY",
    ),
    TaskUiData(
        0,
        "02 - 45 min de treino na academia",
        "HEALTH",
    ),
    TaskUiData(
        0,
        "03 - Correr 5km",
        "HEALTH",
    ),
    TaskUiData(
        0,
        "04 - Meditar por 10 min",
        "WELLNESS",
    ),
    TaskUiData(
        0,
        "05 - Silêncio total por 5 min",
        "WELLNESS",
    ),
    TaskUiData(
        0,
        "06 - Descer o livo",
        "HOME",
    ),
    TaskUiData(
        0,
        "07 - Tirar caixas da garagem",
        "HOME",
    ),
    TaskUiData(
        0,
        "08 - Lavar o carro",
        "HOME",
    ),
    TaskUiData(
        0,
        "09 - Gravar aulas DevSpace",
        "WORK",
    ),
    TaskUiData(
        0,
        "10 - Criar planejamento de vídeos da semana",
        "WORK",
    ),
    TaskUiData(
        0,
        "11 - Soltar reels da semana",
        "WORK",
    ),
)