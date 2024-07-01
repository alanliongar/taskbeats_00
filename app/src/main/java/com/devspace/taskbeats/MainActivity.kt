package com.devspace.taskbeats
//Onde parei? Terminei aula 10, ver aula 11
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings.Global
import android.service.controls.actions.FloatAction
import android.util.Log
import android.widget.Button
import androidx.core.app.TaskStackBuilder
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.comunidadedevspace.taskbeats.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var categoriess = listOf<CategoryUiData>()
    private var taskss = listOf<TaskUiData>()

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

        val rvCategory = findViewById<RecyclerView>(R.id.rv_categories)
        val rvTask = findViewById<RecyclerView>(R.id.rv_tasks)
        val fabCreateTask = findViewById<FloatingActionButton>(R.id.fab_create_task)
        val taskAdapter = TaskListAdapter()
        fabCreateTask.setOnClickListener {
            val createTaskBottomSheet = CreateTaskBottomSheet(categoriess)
            { taskToBeCreated ->
            }

            createTaskBottomSheet.show(supportFragmentManager,"createTaskBottomSheet")
        }

        categoryAdapter.setOnClickListener { selected ->
            if (selected.name == "+") {
                val createCategoryBottomSheet = CreateCategoryBottomSheet { categoryName ->
                    val categoryEntity = CategoryEntity(
                        name = categoryName,
                        isSelected = false
                    )
                    insertCategory(categoryEntity)
                }
                createCategoryBottomSheet.show(supportFragmentManager, "createCategoryBottomSheet")
            } else {
                val categoryTemp = categoriess.map { item ->
                    when {
                        item.name == selected.name && !item.isSelected -> item.copy(
                            isSelected = true
                        )

                        item.name == selected.name && item.isSelected -> item.copy(isSelected = false)
                        else -> item
                    }
                }

                val taskTemp =
                    if (selected.name != "ALL") {
                        taskss.filter { it.category == selected.name }
                    } else {
                        taskss
                    }
                taskAdapter.submitList(taskTemp)
                categoryAdapter.submitList(categoryTemp)
            }
        }

        rvCategory.adapter = categoryAdapter
        //insertCategory(catEntEmpt)
        GlobalScope.launch(Dispatchers.IO) {
            getCategoriesFromDataBase()
        }
        //categoryAdapter.submitList(categories)

        rvTask.adapter = taskAdapter
        getTasksFromDataBase(taskAdapter)
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
                name = it.name,
                category = it.category
            )
        }

        GlobalScope.launch(Dispatchers.IO) {
            taskDao.insertAll(tasksEntity)
        }

    }

    private fun getCategoriesFromDataBase() {
        val categoriesFromDb: List<CategoryEntity> = categoryDao.getAll()
        val categoriesUiData =
            categoriesFromDb.map { //Aqui é o contrário do que está sendo feito na linha 64: estamos pegando do banco de dados e puxando pra lista de novo.
                CategoryUiData(name = it.name, isSelected = it.isSelected)
            }.toMutableList() //esse tomutablelist serve pro que tá no nome mutablelist

        //daqui pra baixo, eu só tô criando o botão de adiçonar
        val list = listOf<CategoryUiData>()
        val mutableList = mutableListOf<CategoryUiData>()
        categoriesUiData.add(CategoryUiData(name = "+", isSelected = false))
        GlobalScope.launch(Dispatchers.Main) {
            categoriess = categoriesUiData
            categoryAdapter.submitList(categoriesUiData)
        }

    }

    private fun getTasksFromDataBase(adapter: TaskListAdapter) {
        GlobalScope.launch(Dispatchers.IO) {
            val tasksFromDb: List<TaskEntity> = taskDao.getAll()
            val tasksUiData = tasksFromDb.map {
                TaskUiData(
                    name = it.name,
                    category = it.category
                )
            }.toMutableList()
            GlobalScope.launch(Dispatchers.Main) {
                taskss = tasksUiData
                adapter.submitList(tasksUiData)
            }
        }
    }

    private fun insertCategory(categoryEntity: CategoryEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            categoryDao.inset(categoryEntity)//ainda precisa atualizar a view pro usuário
            getCategoriesFromDataBase()
        }

    }
}

val categories: List<CategoryUiData> = listOf(
    //Lista vazia pra testar se o dado foi pro DB mesmo.
    CategoryUiData(
        name = "ALL",
        isSelected = false
    ),
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
    ),
    CategoryUiData(
        name = "HEALTH",
        isSelected = false
    ),
)


//Aula 5 (ao vivo) -
//1 - Inserir essas tasks na base de dados (pelo menos criar uma)
//2 - Ler as tarefas da base de dados

val tasks = listOf(
    TaskUiData(
        "Ler 10 páginas do livro atual",
        "STUDY"
    ),
    TaskUiData(
        "45 min de treino na academia",
        "HEALTH"
    ),
    TaskUiData(
        "Correr 5km",
        "HEALTH"
    ),
    TaskUiData(
        "Meditar por 10 min",
        "WELLNESS"
    ),
    TaskUiData(
        "Silêncio total por 5 min",
        "WELLNESS"
    ),
    TaskUiData(
        "Descer o livo",
        "HOME"
    ),
    TaskUiData(
        "Tirar caixas da garagem",
        "HOME"
    ),
    TaskUiData(
        "Lavar o carro",
        "HOME"
    ),
    TaskUiData(
        "Gravar aulas DevSpace",
        "WORK"
    ),
    TaskUiData(
        "Criar planejamento de vídeos da semana",
        "WORK"
    ),
    TaskUiData(
        "Soltar reels da semana",
        "WORK"
    ),
)