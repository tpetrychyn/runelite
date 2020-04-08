plugins {
    java
}

description = "RSPS Map Editor"

dependencies {
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
}