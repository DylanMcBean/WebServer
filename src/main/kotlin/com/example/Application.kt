package com.example


import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.routes.*
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import java.sql.Timestamp

data class UserSessionData(val data: String)
// create a data class that will hold things a user would need to be validated, also make it so it can be serialized

@Serializable
data class SessionData(
	val userHash: Int,
	val lastIp: String,
	val lastLogin: Timestamp
)

fun main() {
	embeddedServer(Netty, port=8080, host = "0.0.0.0", module=Application::module).start(wait=true)
}

fun Application.module() {
	install(Sessions) {
		cookie<UserSessionData>("user_session") {
			cookie.path = "/"
			cookie.maxAgeInSeconds = 100
			cookie.domain = "localhost"
			cookie.httpOnly = true
			cookie.extensions["SameSite"] = "lax"
		}
	}
	configureRouting()
}