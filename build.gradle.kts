plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.17.2"
    id("org.jetbrains.grammarkit") version "2022.3.2.2"
}

group = "org.conilang"
version = "0.0.1"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
intellij {
    version.set("2023.2.5")
    type.set("IC") // IntelliJ IDEA Community Edition
    plugins.set(listOf())
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("242.*")
    }

    generateLexer {
        sourceFile.set(file("src/main/grammars/Coni.flex"))
        targetOutputDir.set(file("src/main/gen/org/conilang/lexer"))
        purgeOldFiles.set(true)
    }

    compileKotlin {
        dependsOn(generateLexer)
    }
}

sourceSets {
    main {
        java.srcDirs("src/main/gen", "src/main/kotlin")
    }
}
