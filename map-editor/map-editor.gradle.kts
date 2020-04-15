plugins {
    java
    id("org.openjfx.javafxplugin") version "0.0.8"
}

repositories {
    maven("https://jitpack.io")
}

description = "RSPS Map Editor"

dependencies {
    api(project(":runelite-api"))
    api(project(":cache"))
    implementation(project(":http-api"))

    annotationProcessor(Libraries.lombok)

    compileOnly(Libraries.lombok)

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
    implementation("com.jfoenix:jfoenix:9.0.9")

    api("com.github.elect86:glm:471c2fd5d2002696e2721dde19fded16c01fab78")
}

javafx {
    version = "14"
    modules = listOf("javafx.controls", "javafx.fxml")
}