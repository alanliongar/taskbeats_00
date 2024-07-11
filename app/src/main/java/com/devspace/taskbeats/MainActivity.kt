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
            TaskBeatDataBase::class.java, "database-task-beat"
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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        insertDefaultCategory()
        insertDefaultTasks()

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
                        GlobalScope.launch(Dispatchers.IO) {
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
        GlobalScope.launch(Dispatchers.IO) {
            getTasksFromDataBase()
        }
    }

    private fun insertDefaultCategory() {
        val categoriesEntity =
            categories.map { //isso aqui é um tipo de categoryEntity, são objetos sendo transformados em DB
                CategoryEntity(
                    name = it.name,
                    isSelected = it.isSelected
                )
            }
        //Isso aqui em baixo tem a ver com Threads << mas vamos aprender isso mais pra frente.
        GlobalScope.launch(Dispatchers.IO) {
            categoryDao.insetAll(categoriesEntity)
        }
        //basicamente essas duas linhas de código servem pra jogar o processamento do banco pra segundo plano

    }

    private fun insertDefaultTasks() {
        val tasksEntity = tasks.map {
            TaskEntity(
                id = it.id,
                name = it.name,
                category = it.category
            )
        }

        GlobalScope.launch(Dispatchers.IO) {
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

    private fun getCategoriesFromDataBase() {
        val categoriesFromDb: List<CategoryEntity> = categoryDao.getAll()
        categoriesEntity = categoriesFromDb
        GlobalScope.launch(Dispatchers.Main) { //quando for mexer em visualização, precisa estar na MAIN THREAD.
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
        //daqui pra baixo, eu só tô criando o botão de adiçonar
        val list = listOf<CategoryUiData>()
        val mutableList = mutableListOf<CategoryUiData>()
        categoriesUiData.add(CategoryUiData(name = "+", isSelected = false))
        val tempCategory = mutableListOf(CategoryUiData(name = "ALL", isSelected = true))
        tempCategory.addAll(categoriesUiData)
        GlobalScope.launch(Dispatchers.Main) {
            categoriess = tempCategory
            categoryAdapter.submitList(categoriess)
        }
    }

    private fun getTasksFromDataBase() {
        val tasksFromDb: List<TaskEntity> = taskDao.getAll()
        val tasksUiData: List<TaskUiData> = tasksFromDb.map {
            TaskUiData(
                id = it.id,
                name = it.name,
                category = it.category
            )
        }
        GlobalScope.launch(Dispatchers.Main) {
            taskss = tasksUiData
            taskAdapter.submitList(taskss)
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