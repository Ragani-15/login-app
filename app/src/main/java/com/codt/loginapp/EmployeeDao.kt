package com.codt.loginapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EmployeeDao {

    @Insert
    suspend fun insert(employee: EmployeeEntity)

    @Query("SELECT * FROM employees")
    suspend fun getAll(): List<EmployeeEntity>
}
