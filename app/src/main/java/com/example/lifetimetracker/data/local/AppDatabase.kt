package com.example.lifetimetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.lifetimetracker.data.local.dao.ActivityLogDao
import com.example.lifetimetracker.data.local.dao.CategoryDao
import com.example.lifetimetracker.data.local.dao.NotificationStateDao
import com.example.lifetimetracker.data.local.dao.UsageAggregateDao
import com.example.lifetimetracker.data.local.entity.ActivityLogEntity
import com.example.lifetimetracker.data.local.entity.CategoryEntity
import com.example.lifetimetracker.data.local.entity.NotificationStateEntity
import com.example.lifetimetracker.data.local.entity.UsageAggregateEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CategoryEntity::class,
        ActivityLogEntity::class,
        UsageAggregateEntity::class,
        NotificationStateEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun usageAggregateDao(): UsageAggregateDao
    abstract fun notificationStateDao(): NotificationStateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lifetime_tracker_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            seedDatabase()
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            seedDatabase()
        }

        private fun seedDatabase() {
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = database.categoryDao()
                    val currentTime = System.currentTimeMillis()
                    
                    val systemCategories = listOf(
                        CategoryEntity(
                            key = "work",
                            name = "Work",
                            colorHex = "#2196F3",
                            iconName = "work",
                            dailyLimitMinutes = null,
                            isSystem = true,
                            sortOrder = 0,
                            createdAt = currentTime,
                            updatedAt = currentTime
                        ),
                        CategoryEntity(
                            key = "learning",
                            name = "Learning",
                            colorHex = "#4CAF50",
                            iconName = "school",
                            dailyLimitMinutes = null,
                            isSystem = true,
                            sortOrder = 1,
                            createdAt = currentTime,
                            updatedAt = currentTime
                        ),
                        CategoryEntity(
                            key = "personal",
                            name = "Personal",
                            colorHex = "#FF9800",
                            iconName = "person",
                            dailyLimitMinutes = null,
                            isSystem = true,
                            sortOrder = 2,
                            createdAt = currentTime,
                            updatedAt = currentTime
                        ),
                        CategoryEntity(
                            key = "social_video",
                            name = "Social & Video",
                            colorHex = "#E91E63",
                            iconName = "subscriptions",
                            dailyLimitMinutes = 60,
                            isSystem = true,
                            sortOrder = 3,
                            createdAt = currentTime,
                            updatedAt = currentTime
                        ),
                        CategoryEntity(
                            key = "other",
                            name = "Other",
                            colorHex = "#9E9E9E",
                            iconName = "more_horiz",
                            dailyLimitMinutes = null,
                            isSystem = true,
                            sortOrder = 4,
                            createdAt = currentTime,
                            updatedAt = currentTime
                        )
                    )
                    dao.insertCategories(systemCategories)
                }
            }
        }
    }
}
