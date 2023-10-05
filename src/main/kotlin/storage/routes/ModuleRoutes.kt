package storage.routes

import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.roll10.plugins.database
import storage.schemas.ModuleService

fun Application.configureModuleRoutes() {
    val moduleService = ModuleService(database)
    routing {

    }
}