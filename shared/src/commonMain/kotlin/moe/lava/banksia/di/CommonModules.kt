package moe.lava.banksia.di

import moe.lava.banksia.room.Database
import org.koin.dsl.module

val CommonModules = module {
    includes(PlatformModule)

    single { Database.build(get<PlatformDatabaseBuilder>().getBuilder()) }
    single { get<Database>().getRouteDao() }
    single { get<Database>().getShapeDao() }
}
