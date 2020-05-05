plugins {
    java
    id("org.openjfx.javafxplugin") version "0.0.8"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

description = "RSPS Map Editor"

dependencies {
    api(project(":runelite-api"))
    api(project(":cache"))

    annotationProcessor(Libraries.lombok)

    compileOnly(Libraries.lombok)
    api("com.google.inject:guice:4.2.3")

    implementation(Libraries.findbugs)
    implementation(Libraries.guava)
    implementation(Libraries.apacheCommonsText)
    implementation(Libraries.apacheCommonsCompress)
    implementation(Libraries.slf4jApi)

    implementation(Libraries.jogampJogl)
    implementation(Libraries.jogampGluegen)
    runtimeOnly(Libraries.jogampGluegenLinuxAmd64)
    runtimeOnly(Libraries.jogampGluegenLinuxI586)
    runtimeOnly(Libraries.jogampGluegenWindowsAmd64)
    runtimeOnly(Libraries.jogampGluegenWindowsI586)
    runtimeOnly(Libraries.jogampJoglLinuxAmd64)
    runtimeOnly(Libraries.jogampJoglLinuxI586)
    runtimeOnly(Libraries.jogampJoglWindowsAmd64)
    runtimeOnly(Libraries.jogampJoglWindowsI586)

    api(files("lib/jogamp-fat.jar"))
    api(files("lib/jimObjModelImporterJFX.jar"))
    implementation("com.jfoenix:jfoenix:9.0.9")

    api(group = "org.joml", name = "joml", version = "1.9.12")
    api(group = "de.javagl", name = "obj", version = "0.3.0")

    testImplementation(Libraries.junit)
}

javafx {
    version = "14"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics")
}