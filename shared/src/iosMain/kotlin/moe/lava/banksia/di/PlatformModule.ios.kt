package moe.lava.banksia.di

import androidx.room.RoomDatabase
import moe.lava.banksia.room.Database
import org.koin.core.parameter.ParametersHolder
import org.koin.core.scope.Scope

class IosDatabaseBuilder() : PlatformDatabaseBuilder {
    override fun getBuilder(): RoomDatabase.Builder<Database> {
        TODO("Not yet implemented")
    }
}

actual fun Scope.provideDatabaseBuilder(p: ParametersHolder): PlatformDatabaseBuilder =
    IosDatabaseBuilder()
