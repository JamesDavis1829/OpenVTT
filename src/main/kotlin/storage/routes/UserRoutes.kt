package storage.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.roll10.plugins.OpenVttSession
import org.roll10.plugins.database
import storage.schemas.ExposedUser
import storage.schemas.UserService

fun Application.configureUserRoutes() {
    val userService = UserService(database)
    routing {
        // Update user
        put("/users/{id}") {
            val session: OpenVttSession = call.sessions.get() ?: return@put call.respond(HttpStatusCode.Unauthorized, "Must be logged in to update user.")
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            if(id != session.userId)
                return@put call.respond(HttpStatusCode.Unauthorized, "Cannot update another user.")
            val user = call.receive<ExposedUser>()
            userService.update(id, user)
            call.respond(HttpStatusCode.OK)
        }
    }
}