package mikleaka.data.database

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = varchar("id", 128)
    val username = varchar("username", 25).uniqueIndex()
    val email = varchar("email", 35).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val verificationCode = varchar("verification_code", 6).nullable()
    val isVerified = bool("is_verified").default(false)
    
    override val primaryKey = PrimaryKey(id)
} 