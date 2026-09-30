package com.kerosdev.dex2c.manager.di

import android.content.Context
import com.kerosdev.dex2c.manager.data.obfuscator.BlackObfuscator
import com.kerosdev.dex2c.manager.data.obfuscator.BlackObfuscatorEngine
import com.kerosdev.dex2c.manager.data.obfuscator.ObfuscationExecutor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ObfuscatorModule {

    @Singleton
    @Provides
    fun provideBlackObfuscator(
        @ApplicationContext context: Context
    ): BlackObfuscator = BlackObfuscator(context)

    @Singleton
    @Provides
    fun provideBlackObfuscatorEngine(
        @ApplicationContext context: Context
    ): BlackObfuscatorEngine = BlackObfuscatorEngine(context)

    @Singleton
    @Provides
    fun provideObfuscationExecutor(
        @ApplicationContext context: Context
    ): ObfuscationExecutor = ObfuscationExecutor(context)
}
