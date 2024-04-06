plugins {
    id("java-library")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.12")
    api(project(":ksignal"))
    api(project(":ksigui"))
    api("com.google.guava:guava:32.1.3-jre")
}