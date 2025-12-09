plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka") version "1.9.20"
}

android {
    namespace = "com.eclypses.socketx_client_android"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    // The modern way to declare what to publish.
    // This automatically handles task dependencies for sources and javadocs.
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

group = "com.eclypses"
version = "1.0.2"

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = project.group.toString()
                artifactId = project.name
                version = project.version.toString()

                from(components["release"])

                pom {
                    name.set("SocketX Client for Android")
                    description.set("A client library for connecting to a SocketX server, providing MTE-secured WebSockets.")
                    url.set("https://github.com/eclypses/socketx-client-android")
                    licenses {
                        license {
                            name.set("The MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    developers {
                        developer {
                            id.set("eclypses")
                            name.set("Eclypses, Inc.")
                            email.set("info@eclypses.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/eclypses/socketx-client-android.git")
                        developerConnection.set("scm:git:ssh://github.com/eclypses/socketx-client-android.git")
                        url.set("https://github.com/eclypses/socketx-client-android")
                    }
                }
            }
        }
        repositories {
            maven {
                name = "MavenCentral"
                url = uri("https://central.sonatype.com/api/v1/publisher")
                credentials {
                    username = project.findProperty("ossrhUsername") as String? ?: ""
                    password = project.findProperty("ossrhPassword") as String? ?: ""
                }
            }
        }
    }
    signing {
        useGpgCmd()
        sign(publishing.publications["release"])
    }
}

dependencies {
    implementation("com.eclypses:eclypses-aws-mte-relay-client-android-release:4.2.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
