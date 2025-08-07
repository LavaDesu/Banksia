package moe.lava.banksia.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import moe.lava.banksia.room.Database
import org.koin.core.parameter.ParametersHolder
import org.koin.core.scope.Scope

class AndroidDatabaseBuilder(val ctx: Context) : PlatformDatabaseBuilder {
    override fun getBuilder(): RoomDatabase.Builder<Database> {
        val appContext = ctx.applicationContext
        val dbFile = appContext.getDatabasePath("room.db")
        return Room.databaseBuilder<Database>(
            context = appContext,
            name = dbFile.absolutePath
        )
    }
}

actual fun Scope.provideDatabaseBuilder(p: ParametersHolder): PlatformDatabaseBuilder =
    AndroidDatabaseBuilder(p.get())
