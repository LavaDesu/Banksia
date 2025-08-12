package moe.lava.banksia.di

import androidx.room.RoomDatabase
import moe.lava.banksia.room.Database
import org.koin.core.module.Module
import org.koin.core.parameter.ParametersHolder
import org.koin.core.scope.Scope
import org.koin.dsl.module

interface PlatformDatabaseBuilder {
    fun getBuilder(): RoomDatabase.Builder<Database>
}

expect fun Scope.provideDatabaseBuilder(p: ParametersHolder): PlatformDatabaseBuilder

internal expect val ExtPlatformModule: Module

internal val PlatformModule = module {
    includes(ExtPlatformModule)
    single { provideDatabaseBuilder(it) }
}
