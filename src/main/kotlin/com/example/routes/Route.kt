package com.example.routes

import User
import UserDatabase
import com.example.EncryptionHandler
import com.example.SessionData
import com.example.UserSessionData
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.sessions.*
import java.util.*
import java.time.Instant

fun Application.configureRouting() {
	routing {
		intercept(ApplicationCallPipeline.Call) {
			call.response.headers.append(HttpHeaders.Date, Instant.now().toString())
			call.response.headers.append(HttpHeaders.Server, "Test Server")
		}

		get("/") {
			call.respondRedirect("/login", permanent = false)
		}

		get("/forgot-password") {
			call.respondFile(
				java.io.File("src/main/resources/static/forgot-password.html")
			)
		}

		get("/styles.css") {
			call.respondFile(
				java.io.File("src/main/resources/static/styles.css")
			)
		}

		// LOGIN
		get("/login") {

			call.respondFile(
				java.io.File("src/main/resources/static/login.html")
			)
		}

		get("/login.js") {
			call.respondFile(
				java.io.File("src/main/resources/static/login.js")
			)
		}

		post("/login") {
			val postParameters = call.receiveParameters()
			val usernameOrEmail = postParameters["username"]
			val password = postParameters["password"]

			val db = UserDatabase()
			var user = db.getUser(usernameOrEmail!!, password!!)
			if (user == null) {
				// respond with status code and error message
				call.respond(HttpStatusCode.Unauthorized, "Invalid username or password")
			} else {

				user = db.updateUser(user, password)

				if (user == null) {
					call.respond(HttpStatusCode.InternalServerError, "Something went wrong")
					return@post
				}

				val userSession = SessionData(
					userHash = user.hashCode(),
					lastIp = user.lastIp!!,
					lastLogin = user.lastLogin!!
				).toString()

				call.sessions.set(UserSessionData(EncryptionHandler.encryptCookie(userSession,user.encryptionKey)))
				call.respond(HttpStatusCode.OK, "Logged in successfully")
			}
		}

		// REGISTER
		get("/register") {
			call.respondFile(
				java.io.File("src/main/resources/static/register.html")
			)
		}

		get("/register.js") {
			call.respondFile(
				java.io.File("src/main/resources/static/register.js")
			)
		}

		post("/register") {
			val postParameters = call.receiveParameters()
			val username = postParameters["username"]
			val email = postParameters["email"]
			val password = postParameters["password"]

			val db = UserDatabase()
			val user = User(
				id = UUID.randomUUID(),
				username = username !!,
				email = email !!,
				hashedPassword = password !!,
				lastIp = call.request.local.remoteHost
			)

			val error = db.insertUser(user)
			if (error != null) call.respond(HttpStatusCode.BadRequest, error)
			else call.respond(HttpStatusCode.OK, "Registered successfully")
		}

		get("/home") {
			val userSession = call.sessions.get("user_session")
			if (userSession == null)
			{
				call.respondRedirect("/login")
			} else {
				call.respondFile(
					java.io.File("src/main/resources/static/home.html")
				)
			}
		}
	}
}
