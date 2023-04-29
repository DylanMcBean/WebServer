import java.sql.*
import java.util.*
import java.time.Instant
import com.example.EncryptionHandler

data class User(
	val id: UUID,
	val username: String,
	val email: String,
	val hashedPassword: String,
	val createdAt: Timestamp? = null,
	val updatedAt: Timestamp? = null,
	val lastLogin: Timestamp? = null,
	val lastIp: String? = null,
	val lastPasswordUsed: String? = null,
	val loginAttempts: Int = 0,
	val encryptionKey: String? = null
)

class UserDatabase {
	private val conn: Connection = DriverManager.getConnection("jdbc:h2:./users")

	init {
		createTableIfNotExists()
	}

	private fun createTableIfNotExists() {
		val stmt = conn.createStatement()
		stmt.executeUpdate(
			"""
        CREATE TABLE IF NOT EXISTS users (
            id VARCHAR(36) PRIMARY KEY,
            username VARCHAR(255),
            email VARCHAR(255),
            hashed_password VARCHAR(255),
            created_at TIMESTAMP,
            updated_at TIMESTAMP,
            last_login TIMESTAMP,
            last_ip VARCHAR(45),
            last_password_used VARCHAR(255),
            login_attempts INT,
            encryption_key VARCHAR(64)
        )"""
		)
		stmt.close()
	}

	// insert a user into the database, make it return error if the user already exists or there was a problem inserting
	fun insertUser(user: User?): String? {
		if (user == null) {
			return "User is null"
		}

		try {
			// Check if user already exists
			val stmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ? OR email = ?")
			stmt.setString(1, user.username)
			stmt.setString(2, user.email)
			val results = stmt.executeQuery()

			if (results.next() && results.getInt(1) > 0) {
				return "User already exists"
			}
			stmt.close()

			// Generate a unique user ID
			val userId = UUID.randomUUID().toString()

			// Hash the user's password with a strong hashing algorithm and unique salt
			val salt = EncryptionHandler.generateSalt()
			val hashedPassword = EncryptionHandler.hashPassword(user.hashedPassword, salt)

			// Insert the user into the database
			val insertStmt = conn.prepareStatement(
				"""
            INSERT INTO users (id, username, email, hashed_password, created_at, updated_at, last_login, last_ip, last_password_used, login_attempts, encryption_key)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """
			)
			insertStmt.setString(1, userId)
			insertStmt.setString(2, user.username)
			insertStmt.setString(3, user.email)
			insertStmt.setString(4, hashedPassword)
			insertStmt.setTimestamp(5, Timestamp.from(Instant.now()))
			insertStmt.setTimestamp(6, Timestamp.from(Instant.now()))
			insertStmt.setTimestamp(7, Timestamp.from(Instant.now()))
			insertStmt.setString(8, user.lastIp)
			insertStmt.setString(9, hashedPassword)
			insertStmt.setInt(10, 0)
			insertStmt.setString(11, EncryptionHandler.generateEncryptionKey())
			insertStmt.executeUpdate()
			insertStmt.close()

		} catch (e: SQLException) {
			// Log exceptions and errors to a secure location, such as a file that is not accessible from the internet.
			println(e)
			return "Error inserting user"
		}

		return null
	}

	// get a user from the database by username or email and hashed password
	fun getUser(usernameOrEmail: String, password: String): User? {
		if (usernameOrEmail.isEmpty()) {
			return null
		}

		try {
			// Query the database for the user
			val stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ? OR email = ?")
			stmt.setString(1, usernameOrEmail)
			stmt.setString(2, usernameOrEmail)
			val results = stmt.executeQuery()

			if (results.next()) {
				// Retrieve the user's data from the result set
				val user = User(
					id = UUID.fromString(results.getString("id")),
					username = results.getString("username"),
					email = results.getString("email"),
					hashedPassword = results.getString("hashed_password"),
					createdAt = results.getTimestamp("created_at"),
					updatedAt = results.getTimestamp("updated_at"),
					lastLogin = results.getTimestamp("last_login"),
					lastIp = results.getString("last_ip"),
					lastPasswordUsed = results.getString("last_password_used"),
					loginAttempts = results.getInt("login_attempts"),
					encryptionKey = results.getString("encryption_key")
				)

				// Verify the password hash
				if (EncryptionHandler.verifyPassword(password, user.hashedPassword)) {
					stmt.close()
					return user
				}
			}
			stmt.close()
		} catch (e: SQLException) {
			// Log exceptions and errors to a secure location instead of printing them to the console.
			println(e)
		}

		return null
	}

	fun updateUser(user: User, password: String): User? {
		try {
			// Update the user's data in the database
			val stmt = conn.prepareStatement(
				"""
			UPDATE users
			SET username = ?, email = ?, hashed_password = ?, updated_at = ?, last_login = ?, last_ip = ?, last_password_used = ?, login_attempts = ?, encryption_key = ?
			WHERE id = ?
			"""
			)
			stmt.setString(1, user.username)
			stmt.setString(2, user.email)
			stmt.setString(3, user.hashedPassword)
			stmt.setTimestamp(4, Timestamp.from(Instant.now()))
			stmt.setTimestamp(5, Timestamp.from(Instant.now()))
			stmt.setString(6, user.lastIp)
			stmt.setString(7, user.lastPasswordUsed)
			stmt.setInt(8, user.loginAttempts)
			stmt.setString(9, user.encryptionKey)
			stmt.setString(10, user.id.toString())
			stmt.executeUpdate()
			stmt.close()

			// retrieve the updated user from the database
			return getUser(user.username, password)
		} catch (e: SQLException) {
			println(e)
			return null
		}
	}
}