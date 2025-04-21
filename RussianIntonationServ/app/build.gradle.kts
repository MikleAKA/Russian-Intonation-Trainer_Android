plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

group = "mikleaka"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    sourceSets {
        main {
            kotlin.srcDir("src/main/com")
        }
    }
}

dependencies {
    // Ktor
    val ktorVersion = "2.3.8"
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    
    // База данных
    val exposedVersion = "0.47.0"
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("com.zaxxer:HikariCP:5.1.0")
    
    // Безопасность
    implementation("org.mindrot:jbcrypt:0.4")
    
    // Логирование
    implementation("ch.qos.logback:logback-classic:1.5.1")
    
    // Тестирование
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    
    // JavaMail для отправки электронных писем
    implementation("com.sun.mail:javax.mail:1.6.2")
}

application {
    mainClass.set("mikleaka.ApplicationKt")
}

// Задача для проверки пользователей в базе данных
tasks.register("verifyUsers") {
    group = "verification"
    description = "Проверяет наличие пользователей в базе данных"
    
    doLast {
        javaexec {
            mainClass.set("mikleaka.utils.DbVerifier")
            classpath = sourceSets.main.get().runtimeClasspath
        }
    }
}