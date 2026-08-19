package com.example.rodeoserver.devices

import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import retrofit2.Response

interface WorkDao {

  @Insert
  suspend fun add(work: Work)

  @Query("select * from work order by id")
  suspend fun getAll(): List<Work>

  @Query("delete from work where id = :id")
  suspend fun delete(id: Long)

}

@Entity
data class Work(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val workType: WorkType,
  val param1: String? = null,
  val param2: String? = null,
  val param3: String? = null,
)

enum class WorkType {
  ToggleDevice
}