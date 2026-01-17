plugins {
    java
    id("com.gradleup.shadow") version "8.3.6"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

group = "com.vendouple.aetherlink"
version = "1.0.0-beta.1"

repositories {
    mavenCentral()
} 

dependencies {
    compileOnly(files("libs/HytaleServer.jar"))
    implementation("net.dv8tion:JDA:5.0.0-beta.20")
}

tasks {
    shadowJar {
        archiveClassifier.set("")

        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")

        relocate("okhttp3", "com.vendouple.aetherlink.lib.okhttp3")
        relocate("okio", "com.vendouple.aetherlink.lib.okio")
        relocate("gnu.trove", "com.vendouple.aetherlink.lib.trove")
        relocate("com.neovisionaries", "com.vendouple.aetherlink.lib.neovisionaries")
        relocate("org.apache.commons", "com.vendouple.aetherlink.lib.commons")
        relocate("com.fasterxml", "com.vendouple.aetherlink.lib.jackson")
    }
}


