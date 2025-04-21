package mikleaka.utils

import java.util.*
import javax.mail.*
import javax.mail.internet.*

/**
 * Сервис для отправки электронных писем
 */
object EmailService {
    private val host = System.getenv("MAIL_HOST") ?: "smtp.gmail.com"
    private val port = System.getenv("MAIL_PORT") ?: "587"
    private val username = System.getenv("MAIL_USERNAME") ?: "mixa1410buz@gmail.com" // Замените на свой email
    private val password = System.getenv("MAIL_PASSWORD") ?: "gyam dztq nypk gllq" // Замените на свой пароль

    /**
     * Отправляет код верификации на указанный email
     * @param to Email получателя
     * @param code Код верификации
     * @return true если отправка успешна, false в случае ошибки
     */
    fun sendVerificationCode(to: String, code: String): Boolean {
        val subject = "Код подтверждения регистрации в приложении Russian Intonation"
        val body = """
            Здравствуйте!
            
            Ваш код подтверждения для регистрации в приложении Russian Intonation: $code
            
            Пожалуйста, введите этот код в приложении для завершения регистрации.
            
            С уважением,
            Команда Russian Intonation
        """.trimIndent()
        
        return try {
            sendEmail(to, subject, body)
            true
        } catch (e: Exception) {
            println("Ошибка при отправке email: ${e.message}")
            false
        }
    }
    
    /**
     * Отправляет электронное письмо
     * @param to Email получателя
     * @param subject Тема письма
     * @param body Текст письма
     */
    private fun sendEmail(to: String, subject: String, body: String) {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", host)
            put("mail.smtp.port", port)
        }
        
        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })
        
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(username))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            setSubject(subject)
            setText(body, "UTF-8")
        }
        
        Transport.send(message)
    }
    
    /**
     * Генерирует случайный код верификации
     * @return Шестизначный код
     */
    fun generateVerificationCode(): String {
        return (100000..999999).random().toString()
    }
} 