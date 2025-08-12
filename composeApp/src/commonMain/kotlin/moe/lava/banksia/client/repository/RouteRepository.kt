package moe.lava.banksia.client.repository

import moe.lava.banksia.client.datasource.local.RouteLocalDataSource
import moe.lava.banksia.client.datasource.remote.RouteRemoteDataSource

class RouteRepository(
    private val local: RouteLocalDataSource,
    private val remote: RouteRemoteDataSource,
) {
    suspend fun getAll() =
        local
            .getAll()
            .map { it.asModel() }
            .ifEmpty {
                remote
                    .getAll()
                    .also { local.save(*it.toTypedArray()) }
            }

    suspend fun get(id: String) = local.get(id)?.asModel() ?: remote.get(id)
}
