package mikleaka.data.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.sql.DriverManager

object DatabaseFactory {
    fun init() {
        try {
            // Введите здесь пароль, который вы установили при создании PostgreSQL
            val dbPassword = "MikleAKA200e"
            
            // Проверяем соединение с PostgreSQL (без указания конкретной базы данных)
            Class.forName("org.postgresql.Driver")
            val mainConnection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/", 
                "postgres", 
                dbPassword
            )
            
            // Проверяем, существует ли база данных
            val databaseExists = checkDatabaseExists(mainConnection, "russian_intonation")
            
            if (!databaseExists) {
                println("База данных 'russian_intonation' не существует. Создаем...")
                // Создаем базу данных
                val statement = mainConnection.createStatement()
                statement.execute("CREATE DATABASE russian_intonation")
                statement.close()
                println("База данных 'russian_intonation' успешно создана.")
            }
            
            // Закрываем основное соединение
            mainConnection.close()
            
            // Подключаемся к нашей базе данных
            val config = HikariConfig().apply {
                driverClassName = "org.postgresql.Driver"
                jdbcUrl = "jdbc:postgresql://localhost:5432/russian_intonation"
                username = "postgres"
                password = dbPassword
                maximumPoolSize = 3
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            }
            
            val dataSource = HikariDataSource(config)
            val database = Database.connect(dataSource)
            
            // Создаем таблицу Users, если она не существует
            transaction(database) {
                SchemaUtils.create(Users)
            }
            
            println("Подключение к базе данных успешно установлено")
        } catch (e: Exception) {
            println("Ошибка подключения к базе данных: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun checkDatabaseExists(connection: Connection, dbName: String): Boolean {
        val resultSet = connection.metaData.catalogs
        val databaseExists = ArrayList<String>()
        
        while (resultSet.next()) {
            databaseExists.add(resultSet.getString(1))
        }
        
        resultSet.close()
        return databaseExists.contains(dbName)
    }
    
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
} 