package org.roll10.plugins

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.headers
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class OpenVttSession(val accessToken: String, val username: String)
@Serializable
data class UserProfile(val email: String)
val apacheClient = HttpClient(Apache)
val jsonParser = Json {
    ignoreUnknownKeys = true
}

fun Application.configureSecurity() {
    authentication {
        oauth("auth-oauth-google") {
            urlProvider = { "http://localhost:8080/redirect" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "google",
                    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                    accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = System.getenv("OPEN_VTT_GOOGLE_CLIENT_ID"),
                    clientSecret = System.getenv("OPEN_VTT_GOOGLE_CLIENT_SECRET"),
                    defaultScopes = listOf("https://www.googleapis.com/auth/userinfo.email")
                )
            }
            client = apacheClient
        }
    }
    install(Sessions) {
        cookie<OpenVttSession>("OPEN_VTT_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }
    routing {
        authenticate("auth-oauth-google") {
            get("login") {
                call.respondRedirect("/redirect")
            }

            get("/redirect") {
                val principal: OAuthAccessTokenResponse.OAuth2? = call.authentication.principal()
                val token = principal?.accessToken ?: ""
                val userInfoPayload: String = apacheClient.get("https://www.googleapis.com/oauth2/v3/userinfo") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }.body()
                val userProfile: UserProfile = jsonParser.decodeFromString(userInfoPayload)
                call.sessions.set(OpenVttSession(token, userProfile.email))
                call.respondRedirect("/")
            }
        }
    }
}
