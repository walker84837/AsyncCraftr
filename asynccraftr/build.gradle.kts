plugins {
    `java-library`
    `maven-publish`
}

group = "org.winlogon"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "org.winlogon"
            artifactId = "asynccraftr"
            pom {
                name = "AsyncCraftr"
                description = "Deduplicate asynchronous Folia and non-Folia handling"
                url = "https://github.com/walker84837/AsyncCraftr"
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }
                developers {
                    developer {
                        id = "walker84837"
                        name = "winlogon"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/walker84837/AsyncCraftr.git"
                    developerConnection = "scm:git:ssh://github.com/walker84837/AsyncCraftr.git"
                    url = "https://github.com/walker84837/AsyncCraftr"
                }
            }
        }
    }
    repositories {
        maven {
            name = "winlogon-libs"
            url = uri("https://maven.winlogon.org/releases")
            credentials {
                username = (project.findProperty("reposiliteUser") as String?) ?: System.getenv("MAVEN_USERNAME")
                password = (project.findProperty("reposilitePassword") as String?) ?: System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}
