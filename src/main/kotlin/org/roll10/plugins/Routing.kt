package org.roll10.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Application.configureRouting() {
    routing {
        get("/") {
            val session: OpenVttSession = call.sessions.get() ?: return@get call.respondText("Hello World!")
            call.respondText("Hello ${session.username}!")
        }
        staticResources("/static", "static")
    }
}
