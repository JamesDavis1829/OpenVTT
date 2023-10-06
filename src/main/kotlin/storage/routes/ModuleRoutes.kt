package storage.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.roll10.plugins.OpenVttSession
import org.roll10.plugins.database
import storage.schemas.ExposedModule
import storage.schemas.ModuleService

fun Application.configureModuleRoutes() {
    val moduleService = ModuleService(database)
    routing {
        post("/module") {
            val session: OpenVttSession = call.sessions.get() ?: return@post call.respond(HttpStatusCode.Unauthorized, "Must be logged in to create modules")
            val module = call.receive<ExposedModule>().copy(userId = session.userId)
            val id = moduleService.create(module)
            call.respond(HttpStatusCode.Created, id)
        }
        get("/module") {
            val session: OpenVttSession = call.sessions.get() ?: return@get call.respond(HttpStatusCode.Unauthorized, "Must be logged in to get your modules")
            val modules = moduleService.getUserModules(session.userId)
            call.respond(HttpStatusCode.OK, modules)
        }
        put("/module/{id}") {
            val session: OpenVttSession = call.sessions.get() ?: return@put call.respond(HttpStatusCode.Unauthorized, "Must be logged in to update modules")
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val module = call.receive<ExposedModule>()
            moduleService.update(id, session.userId, module)
            call.respond(HttpStatusCode.OK)
        }
        delete("/module/{id}") {
            val session: OpenVttSession = call.sessions.get() ?: return@delete call.respond(HttpStatusCode.Unauthorized, "Must be logged in to delete modules")
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            moduleService.delete(id, session.userId)
            call.respond(HttpStatusCode.OK)
        }
    }
}