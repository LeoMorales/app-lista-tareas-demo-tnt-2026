package com.example.demolistatareas.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.demolistatareas.data.db.entity.TareaEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de acceso a datos provista para Room.
 */
@Dao
interface TareaDao {

    /**
     * El retorno de un Flow indica a Room que debe notificar cualquier alteración
     * en la tabla 'tareas', manteniendo la reactividad del sistema.
     */
    @Query("SELECT * FROM tareas ORDER BY id DESC")
    fun observarTodas(): Flow<List<TareaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(tarea: TareaEntity)

    @Query("UPDATE tareas SET estaCompletada = :estado WHERE id = :tareaId")
    suspend fun actualizarEstado(tareaId: Int, estado: Boolean)

    @Query("SELECT estaCompletada FROM tareas WHERE id = :tareaId LIMIT 1")
    suspend fun obtenerEstadoActual(tareaId: Int): Boolean
}