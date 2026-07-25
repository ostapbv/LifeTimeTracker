package com.example.lifetimetracker.di

import com.example.lifetimetracker.domain.parser.ActivityTextParser
import com.example.lifetimetracker.domain.parser.RuleBasedActivityTextParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SystemModule {

    @Provides
    @Singleton
    fun provideActivityTextParser(): ActivityTextParser {
        return RuleBasedActivityTextParser()
    }
}
