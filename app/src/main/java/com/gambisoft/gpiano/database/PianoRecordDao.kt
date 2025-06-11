package com.gambisoft.gpiano.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gambisoft.gpiano.entities.RecordDataObject

@Dao
interface PianoRecordDao {
	@Query("SELECT * FROM PianoRecord")
	suspend fun getListPianoRecorded(): MutableList<RecordDataObject>

	@Insert
	suspend fun addPianoRecord(recordDataObject: RecordDataObject)

	@Delete
	suspend fun deletePianoRecord(recordDataObject: RecordDataObject)

	@Query("SELECT count(*) FROM PianoRecord")
	fun getCountItem(): Int

	@Update
	suspend fun updatePianoRecord(recordDataObject: RecordDataObject)

	@Query("SELECT * FROM PianoRecord where id = :id")
	suspend fun getPianoRecordById(id: Int): RecordDataObject

	@Query("SELECT EXISTS(SELECT 1 FROM PianoRecord where id = :id)")
	suspend fun hasInDatabase(id: Int): Boolean
}