group = "me.xemor"
version = "1.0-SNAPSHOT"
description = "EndRebalancing"
java.sourceCompatibility = JavaVersion.VERSION_25

plugins {
    java
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.gradleup.shadow") version("9.4.2")
}

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven { url = uri("https://mvn-repo.arim.space/lesser-gpl3") }
    maven { url = uri("https://repo.xemor.zip/releases")}
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    compileOnly("com.fasterxml.jackson.core:jackson-core:2.18.0")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    compileOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.7.0")
    shadow("space.arim.morepaperlib:morepaperlib:0.4.3")
    shadow("me.xemor:foliahacks:1.7.4")
    shadow("io.papermc:paperlib:1.0.7")
}

java {
    configurations.shadow.get().dependencies.remove(dependencies.gradleApi())
}

tasks.shadowJar {
    minimize()
    relocate("space.arim.morepaperlib", "me.xemor.endrebalancing.morepaperlib")
    relocate("me.xemor.foliahacks", "me.xemor.endrebalancing.foliahacks")
    relocate("io.papermc.paperlib", "me.xemor.endrebalancing.paperlib")
    configurations = listOf(project.configurations.shadow.get())
    val folder = System.getenv("pluginFolder")
    destinationDirectory.set(file(folder))
}

tasks {
    runServer {
        minecraftVersion("26.1.2")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        inputs.property("version", rootProject.version)
        expand("version" to rootProject.version)
    }

    withType<JavaCompile>() {
        options.encoding = "UTF-8"
    }
}
