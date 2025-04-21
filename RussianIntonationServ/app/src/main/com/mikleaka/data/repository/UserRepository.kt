package mikleaka.data.repository

import mikleaka.data.database.DatabaseFactory.dbQuery
import mikleaka.data.database.Users
import mikleaka.data.models.User
import mikleaka.utils.EmailService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class UserRepository {
    /**
     * Создает предварительную регистрацию пользователя и отправляет код верификации
     * @param username Имя пользователя
     * @param email Email пользователя
     * @param password Пароль
     * @return null если пользователь с таким именем или email уже существует и подтвержден, 
     *         иначе пара (User, String) - созданный/обновленный пользователь и код верификации
     */
    suspend fun registerUser(username: String, email: String, password: String): Pair<User, String>? = dbQuery {
        // Проверяем, существует ли пользователь с таким именем или email
        val existingUser = Users.selectAll()
            .where { (Users.username eq username) or (Users.email eq email) }.firstOrNull()

        // Проверяем если пользователь уже существует и подтвержден
        if (existingUser != null && existingUser[Users.isVerified]) {
            return@dbQuery null
        }

        // Генерируем код верификации
        val verificationCode = EmailService.generateVerificationCode()

        // Хешируем пароль для безопасного хранения
        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())

        val userId: String

        // Если пользователь существует, но не подтвержден - обновляем его
        if (existingUser != null) {
            userId = existingUser[Users.id]
            Users.update({ Users.id eq userId }) {
                it[Users.passwordHash] = passwordHash
                it[Users.verificationCode] = verificationCode
            }
        } else {
            // Создаем нового пользователя
            userId = UUID.randomUUID().toString()
            Users.insert {
                it[id] = userId
                it[Users.username] = username
                it[Users.email] = email
                it[Users.passwordHash] = passwordHash
                it[Users.verificationCode] = verificationCode
                it[Users.isVerified] = false
            }
        }

        // Получаем созданного/обновленного пользователя
        val user = Users.selectAll().where { Users.id eq userId }.map {
            User(
                id = it[Users.id],
                username = it[Users.username],
                email = it[Users.email],
                passwordHash = it[Users.passwordHash],
                isVerified = it[Users.isVerified]
            )
        }.single()

        // Отправляем код верификации на email
        EmailService.sendVerificationCode(email, verificationCode)

        return@dbQuery Pair(user, verificationCode)
    }
    
    /**
     * Подтверждает регистрацию пользователя с помощью кода верификации
     * @param email Email пользователя
     * @param code Код верификации
     * @return true если верификация успешна, false если код неверный или пользователь не найден
     */
    suspend fun verifyUser(email: String, code: String): Boolean = dbQuery {
        Users.selectAll().where { (Users.email eq email) and (Users.verificationCode eq code) }
            .firstOrNull() ?: return@dbQuery false
        
        // Обновляем статус верификации
        Users.update({ Users.email eq email }) {
            it[isVerified] = true
            it[verificationCode] = null
        }
        
        return@dbQuery true
    }
    
    /**
     * Аутентифицирует пользователя по имени пользователя или email и паролю
     * @param usernameOrEmail Имя пользователя или email
     * @param password Пароль
     * @return Пользователь если аутентификация успешна, null в противном случае
     */
    suspend fun authenticate(usernameOrEmail: String, password: String): User? = dbQuery {
        val user = Users.selectAll()
            .where { (Users.username eq usernameOrEmail) or (Users.email eq usernameOrEmail) }
            .firstOrNull() ?: return@dbQuery null
        
        // Проверяем пароль
        if (!BCrypt.checkpw(password, user[Users.passwordHash])) {
            return@dbQuery null
        }
        
        // Проверяем верификацию
        if (!user[Users.isVerified]) {
            return@dbQuery null
        }
        
        return@dbQuery User(
            id = user[Users.id],
            username = user[Users.username],
            email = user[Users.email],
            passwordHash = user[Users.passwordHash],
            isVerified = user[Users.isVerified]
        )
    }
    
    /**
     * Получает пользователя по ID
     * @param id ID пользователя
     * @return Пользователь если найден, null в противном случае
     */
    suspend fun getUserById(id: String): User? = dbQuery {
        Users.selectAll().where { Users.id eq id }.map {
            User(
                id = it[Users.id],
                username = it[Users.username],
                email = it[Users.email],
                passwordHash = it[Users.passwordHash],
                isVerified = it[Users.isVerified]
            )
        }.singleOrNull()
    }

    /**
     * Смена пароля пользователя
     */
    fun changePassword(userId: String, currentPassword: String, newPassword: String): Boolean {
        return transaction {
            val user = Users.selectAll().where { Users.id eq userId }.singleOrNull()

            if (user == null) {
                println("User with ID $userId not found")
                return@transaction false
            }

            val storedHash = user[Users.passwordHash]
            if (!BCrypt.checkpw(currentPassword, storedHash)) {
                println("Invalid current password for user $userId")
                return@transaction false
            }

            val newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt())
            Users.update({ Users.id eq userId }) {
                it[passwordHash] = newPasswordHash
            }

            println("Password successfully changed for user $userId")
            true
        }
    }
} 