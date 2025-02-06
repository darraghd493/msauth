plugins {
    id("java")
}

group = "me.darragh"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val annotationProcessorImplementation: Configuration by configurations.creating {
    configurations.compileOnly.get().extendsFrom(this)
    configurations.testCompileOnly.get().extendsFrom(this)
    configurations.annotationProcessor.get().extendsFrom(this)
    configurations.testAnnotationProcessor.get().extendsFrom(this)
}

dependencies {
    annotationProcessorImplementation("org.projectlombok:lombok:1.18.34")
    implementation("org.jspecify:jspecify:1.0.0")
    implementation("com.google.code.gson:gson:2.12.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
