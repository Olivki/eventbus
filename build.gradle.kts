plugins {
    id("me.him188.maven-central-publish") version "1.0.0-dev-3"
    kotlin("jvm") version "1.7.10"
}

val kotestVersion: String by project

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "me.him188.maven-central-publish")
    apply(plugin = "kotlin")

    group = "net.ormr.eventbus"

    repositories {
        mavenCentral()
    }

    kotlin {
        explicitApi()
    }

    dependencies {
        testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
        testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
        testImplementation("io.kotest:kotest-property:$kotestVersion")
    }

    mavenCentralPublish {
        artifactId = "eventbus-${project.name}"
        useCentralS01()
        singleDevGithubProject("Olivki", "eventbus")
        licenseApacheV2()
    }

    tasks {
        test {
            useJUnitPlatform()
        }

        compileKotlin {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
        compileTestKotlin {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
}