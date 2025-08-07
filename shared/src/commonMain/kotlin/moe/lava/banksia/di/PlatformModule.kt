package moe.lava.banksia.di

import androidx.room.RoomDatabase
import moe.lava.banksia.room.Database
import org.koin.core.parameter.ParametersHolder
import org.koin.core.scope.Scope
import org.koin.dsl.module

interface PlatformDatabaseBuilder {
    fun getBuilder(): RoomDatabase.Builder<Database>
}

expect fun Scope.provideDatabaseBuilder(p: ParametersHolder): PlatformDatabaseBuilder

internal val PlatformModule = module {
    single { provideDatabaseBuilder(it) }
}
