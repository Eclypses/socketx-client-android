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

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

group = "com.eclypses"
version = "2.0.8"

afterEvaluate {
    publishing {
        repositories {
            maven {
                name = "localDirectory"
                url = uri(layout.buildDirectory.dir("repo"))
            }
        }
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = project.group.toString()
                artifactId = "socketx-client-android"
                version = project.version.toString()

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
    }

    signing {
        useGpgCmd()
        sign(publishing.publications["release"])
    }
}

dependencies {
    api("com.eclypses:eclypses-aws-mte-relay-client-android-release:4.2.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
