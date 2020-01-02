package com.truongkhanh.supernote.service

import android.content.Context
import android.os.AsyncTask
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.truongkhanh.supernote.model.*

@Database(
    entities = [Todo::class,
        TagType::class,
        TodoTagList::class,
        Evaluate::class,
        DraftNote::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ApplicationDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun tagTypeDao(): TagTypeDao
    abstract fun todoTagListDao(): TodoTagListDao
    abstract fun evaluateDao(): EvaluateDao
    abstract fun draftNoteDao(): DraftNoteDao

    companion object {
        @Volatile
        private var INSTANCE: ApplicationDatabase? = null

        fun getInstance(context: Context): ApplicationDatabase
            = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }

        private fun buildDatabase(context: Context): ApplicationDatabase
            = Room.databaseBuilder(context.applicationContext,
            ApplicationDatabase::class.java,
            "super_note_database"
        ).addCallback(object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                AsyncTask.execute {
                    val dbInstance = getInstance(context)

                    val tagDao = dbInstance.tagTypeDao()
                    tagDao.createTagType(TagType(1, "Work", "#33B5E5"))
                    tagDao.createTagType(TagType(2, "Study", "#FFBB33"))
                    tagDao.createTagType(TagType(3, "hobby", "#99CC00"))
                }
            }
        }).build()
    }
}