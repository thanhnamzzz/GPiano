package com.gambisoft.gpiano.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gambisoft.gpiano.entities.RecordDataObject

@Database(entities = [RecordDataObject::class], version = 1, exportSchema = false)
abstract class PianoRecordDatabase : RoomDatabase() {
	abstract fun pianoRecordDao(): PianoRecordDao

	companion object {
		private const val DATABASE_NAME = "piano_record_database"

		@Volatile
		private var instance: PianoRecordDatabase? = null
		fun getInstance(context: Context): PianoRecordDatabase {
			return instance ?: synchronized(this) {
				instance ?: Room.databaseBuilder(
					context.applicationContext,
					PianoRecordDatabase::class.java,
					DATABASE_NAME
				).allowMainThreadQueries().build().also { instance = it }
			}
		}
	}
}