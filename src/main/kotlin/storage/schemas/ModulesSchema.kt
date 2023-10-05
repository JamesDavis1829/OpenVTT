package storage.schemas

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.roll10.plugins.jsonParser

@Serializable data class ExposedModule(val id: Int, val data: ExposedModuleData)
@Serializable data class ExposedModuleData(val name: String)

class ModuleService(database: Database) {
    object Modules: Table() {
        val id = integer("id").autoIncrement()
        val data = text("data")
    }

    init {
        transaction(database) {
            SchemaUtils.create(Modules)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(module: ExposedModule): Int = dbQuery {
        Modules.insert {
            it[data] = Json.encodeToString(module.data)
            it[id] = module.id
        }[UserService.Users.id]
    }

    suspend fun read(id: Int): ExposedModule? {
        return dbQuery {
            Modules.select { Modules.id eq id }
                .map { ExposedModule(it[Modules.id], jsonParser.decodeFromString(it[Modules.data])) }
                .singleOrNull()
        }
    }

    suspend fun update(id: Int, user: ExposedModule) {
        dbQuery {
            Modules.update({ Modules.id eq id }) {
                it[data] = Json.encodeToString(data)
            }
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            Modules.deleteWhere { Modules.id.eq(id) }
        }
    }
}