package moe.lava.banksia.client.repository

import moe.lava.banksia.client.datasource.local.StopLocalDataSource
import moe.lava.banksia.client.datasource.remote.StopRemoteDataSource

class StopRepository(
    private val local: StopLocalDataSource,
    private val remote: StopRemoteDataSource,
) {
    suspend fun get(id: String) = local.get(id)?.asModel() ?: remote.get(id)
    suspend fun getByRoute(id: String) =
        local
            .getByRoute(id)
            .map { it.asModel() }
            .ifEmpty { null }
            ?: remote.getByRoute(id)
}
