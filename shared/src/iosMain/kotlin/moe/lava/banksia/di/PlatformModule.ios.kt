package moe.lava.banksia.di

import androidx.room.RoomDatabase
import moe.lava.banksia.room.Database
import org.koin.core.parameter.ParametersHolder
import org.koin.core.scope.Scope
import org.koin.dsl.module

class IosDatabaseBuilder() : PlatformDatabaseBuilder {
    override fun getBuilder(): RoomDatabase.Builder<Database> {
        TODO("Not yet implemented")
    }
}

actual fun Scope.provideDatabaseBuilder(p: ParametersHolder): PlatformDatabaseBuilder =
    IosDatabaseBuilder()

internal actual val ExtPlatformModule = module {  }
