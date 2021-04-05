package model.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ReferencesEntity::class], version = 1)
abstract class ReferencesDataBase : RoomDatabase() {
    abstract fun referencesDao() : ReferencesDao
}