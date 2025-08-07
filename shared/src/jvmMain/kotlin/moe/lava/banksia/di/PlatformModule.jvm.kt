package moe.lava.banksia.di

import androidx.room.Room
import androidx.room.RoomDatabase
import moe.lava.banksia.room.Database
import org.koin.core.parameter.ParametersHolder
import org.koin.core.scope.Scope
import java.io.File

class JvmDatabaseBuilder() : PlatformDatabaseBuilder {
    override fun getBuilder(): RoomDatabase.Builder<Database> {
        val dbFile = File(System.getProperty("java.io.tmpdir"), "my_room.db")
        return Room.databaseBuilder<Database>(
            name = dbFile.absolutePath,
        )
    }
}

actual fun Scope.provideDatabaseBuilder(p: ParametersHolder): PlatformDatabaseBuilder =
    JvmDatabaseBuilder()
