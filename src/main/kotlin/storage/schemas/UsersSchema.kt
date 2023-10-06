package storage.schemas

import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*

@Serializable
data class ExposedUser(val id: Int, val email: String)
class UserService(database: Database) {
    object Users : Table() {
        val id = integer("id").autoIncrement()
        val email = varchar("email", length = 100)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(user: ExposedUser): ExposedUser = dbQuery {
        Users.insert {
            it[email] = user.email
        }[Users.id]
        user
    }

    suspend fun read(email: String): ExposedUser? {
        return dbQuery {
            Users.select { Users.email eq email }
                .map { ExposedUser(it[Users.id], it[Users.email]) }
                .singleOrNull()
        }
    }

    suspend fun update(id: Int, user: ExposedUser) {
        dbQuery {
            Users.update({ Users.id eq id }) {
                it[email] = user.email
            }
        }
    }
}
