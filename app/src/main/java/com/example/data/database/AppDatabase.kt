package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.models.*

@Database(
    entities = [
        UserEntity::class,
        ClassRoomEntity::class,
        StudentEntity::class,
        AttendanceEntity::class,
        DisciplineEntity::class,
        RewardEntity::class,
        StudentNoteEntity::class,
        AssignmentEntity::class,
        QuestionEntity::class,
        SubmissionEntity::class,
        SubmissionAnswerEntity::class,
        SystemSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "huy_duong_edu.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
