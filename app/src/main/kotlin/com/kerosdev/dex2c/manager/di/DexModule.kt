package com.kerosdev.dex2c.manager.di

import android.content.Context
import com.kerosdev.dex2c.manager.data.dex.DexExtractor
import com.kerosdev.dex2c.manager.data.dex.FilterRuleBuilder
import com.kerosdev.dex2c.manager.data.processor.Dex2cExecutor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DexModule {

    @Singleton
    @Provides
    fun provideDexExtractor(
        @ApplicationContext context: Context
    ): DexExtractor = DexExtractor(context)

    @Singleton
    @Provides
    fun provideFilterRuleBuilder(): FilterRuleBuilder = FilterRuleBuilder()

    @Singleton
    @Provides
    fun provideDex2cExecutor(
        @ApplicationContext context: Context
    ): Dex2cExecutor = Dex2cExecutor(context)
}
