package com.example.lifetimetracker.di

import com.example.lifetimetracker.data.local.dao.ActivityLogDao
import com.example.lifetimetracker.data.local.dao.CategoryDao
import com.example.lifetimetracker.data.repository.ActivityRepositoryImpl
import com.example.lifetimetracker.data.repository.CategoryRepositoryImpl
import com.example.lifetimetracker.data.repository.UsageRepositoryImpl
import com.example.lifetimetracker.domain.repository.ActivityRepository
import com.example.lifetimetracker.domain.repository.CategoryRepository
import com.example.lifetimetracker.domain.repository.UsageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideCategoryRepository(dao: CategoryDao): CategoryRepository {
        return CategoryRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideActivityRepository(dao: ActivityLogDao): ActivityRepository {
        return ActivityRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideUsageRepository(
        usageAggregateDao: com.example.lifetimetracker.data.local.dao.UsageAggregateDao,
        categoryDao: com.example.lifetimetracker.data.local.dao.CategoryDao,
        usageStatsDataSource: com.example.lifetimetracker.data.system.UsageStatsDataSource
    ): UsageRepository {
        return UsageRepositoryImpl(usageAggregateDao, categoryDao, usageStatsDataSource)
    }
}
