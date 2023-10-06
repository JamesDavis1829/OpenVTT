package org.roll10.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import storage.routes.configureModuleRoutes
import storage.routes.configureUserRoutes

//val database = Database.connect(
//    url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
//    user = "root",
//    driver = "org.h2.Driver",
//    password = ""
//)

val database = Database.connect("jdbc:sqlite:/data/data.db", "org.sqlite.JDBC")

fun Application.configureDatabases() {
    configureUserRoutes()
    configureModuleRoutes()
}
