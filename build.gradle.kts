plugins {
    id("java")
    id("maven-publish")
}

// Toolchains:
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

// Dependencies:
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
    // Lombok
    annotationProcessorImplementation("org.projectlombok:lombok:1.18.34")

    // JSpecify
    implementation("org.jspecify:jspecify:1.0.0")

    // Gson
    implementation("com.google.code.gson:gson:2.12.1")
}

// Tasks:
tasks.compileJava {
    options.encoding = "UTF-8"
}

tasks.javadoc {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    isFailOnError = false
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

// Publishing:
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "me.darragh"
            artifactId = "msauth"
            version = project.version.toString()

            pom {
                name.set("msauth")
                description.set("A simple library for authenticating with Microsoft for Minecraft")
                url.set("https://github.com/darraghd493/msauth")
                properties.set(mapOf(
                    "java.version" to "17",
                    "project.build.sourceEncoding" to "UTF-8",
                    "project.reporting.outputEncoding" to "UTF-8"
                ))
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/darraghd493/msauth/blob/main/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("darraghd493")
                        name.set("Darragh")
                    }
                }
                organization {
                    name.set("darragh.website")
                    url.set("https://darragh.website")
                }
                scm {
                    connection.set("scm:git:git://github.com/darraghd493/msauth.git")
                    developerConnection.set("scm:git:ssh://github.com/darraghd493/msauth.git")
                    url.set("https://github.com/darraghd493/msauth")
                }
            }

            java {
                withSourcesJar()
                withJavadocJar()
            }
        }
    }
    repositories {
        mavenLocal()
        maven {
            name = "darraghsRepo"
            url = uri("https://repo.darragh.website/releases")
            credentials {
                username = System.getenv("REPO_TOKEN")
                password = System.getenv("REPO_SECRET")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}
