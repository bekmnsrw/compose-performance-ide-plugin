plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "compose.performance.ide.plugin"
version = "1.0.0"

repositories {
    mavenCentral()
}

intellij {
    version.set("2023.1.1.26")
    type.set("AI")
    plugins.set(listOf("Kotlin", "java"))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    }

    patchPluginXml {
        sinceBuild.set("231")
        untilBuild.set("241.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}

dependencies {
    /**
     * Versions
     */
    val okhttp = "4.12.0"
    val gson = "2.9.0"
    val retrofit = "2.11.0"

    /**
     * Dependencies
     */
    implementation("com.squareup.okhttp3:okhttp:$okhttp")
    implementation("com.squareup.retrofit2:converter-gson:$gson")
    implementation("com.squareup.retrofit2:retrofit:$retrofit")
}
