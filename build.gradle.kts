plugins {
    kotlin("jvm")
    application
}

group = "np.com.sudanchapagain"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("np.com.sudanchapagain.MainKt")
}
