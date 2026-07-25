package com.example.lifetimetracker.di

import android.content.Context
import com.example.lifetimetracker.data.local.AppDatabase
import com.example.lifetimetracker.data.local.dao.ActivityLogDao
import com.example.lifetimetracker.data.local.dao.CategoryDao
import com.example.lifetimetracker.data.local.dao.UsageAggregateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideActivityLogDao(database: AppDatabase): ActivityLogDao {
        return database.activityLogDao()
    }

    @Provides
    fun provideUsageAggregateDao(database: AppDatabase): UsageAggregateDao {
        return database.usageAggregateDao()
    }

    @Provides
    fun provideNotificationStateDao(database: AppDatabase): com.example.lifetimetracker.data.local.dao.NotificationStateDao {
        return database.notificationStateDao()
    }
}
