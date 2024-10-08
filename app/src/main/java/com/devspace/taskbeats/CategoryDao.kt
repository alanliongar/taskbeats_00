package com.devspace.taskbeats

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

//maneira como vai ACESSAR a base de dados
@Dao
interface CategoryDao {
    @Query("Select * From categoryentity")
    fun getAll(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insetAll(categoryEntity: List<CategoryEntity>) //removemos o vararg e usamos a lista
    //mesmo com a documentação dizendo pra usar vararg e nao usar lista.

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inset(categoryEntity: CategoryEntity)

}