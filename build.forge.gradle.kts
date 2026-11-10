plugins {
    id("net.neoforged.moddev.legacyforge")
    id("neoforge-mutex")
}

val modVersion = providers.gradleProperty("version").orNull ?: sc.properties.get<String>("mod.version")
val modId = sc.properties.get<String>("mod.id")
val modName = sc.properties.get<String>("mod.name")
val mc = sc.current.version

version = modVersion
group = sc.properties.get<String>("mod.group")
base.archivesName = "$modId-forge-$mc"

val requiredJava = JavaVersion.VERSION_17

sourceSets.main {
    resources.srcDir(rootProject.file("src/eras/1.20.1"))
    resources.srcDir(rootProject.file("src/legacy/1.20.1"))
    java.exclude("com/workshopcraft/nodeores/fabric/**", "com/workshopcraft/nodeores/neoforge/**")
}

legacyForge {
    version = sc.properties.get<String>("deps.forge")
    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
    runs {
        register("client") {
            gameDirectory = rootProject.file("runs/${sc.current.project}")
            client()
        }
        register("server") {
            gameDirectory = rootProject.file("runs/${sc.current.project}-server")
            server()
        }
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
    toolchain.languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
}

tasks {
    processResources {
        val props = mapOf(
            "id" to modId,
            "name" to modName,
            "version" to modVersion,
            "author" to sc.properties.get<String>("mod.author"),
            "license" to sc.properties.get<String>("mod.license"),
            "description" to sc.properties.get<String>("mod.description"),
            "minecraft" to sc.properties.get<String>("mod.mc_compat"),
            "pack_format" to sc.properties.get<String>("mod.pack_format"),
        )
        inputs.properties(props)
        filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) { expand(props) }
        exclude(
            "fabric.mod.json",
            "META-INF/neoforge.mods.toml",
            "data/neoforge/**",
            "data/minecraft/tags/block/**",
            "data/nodeores/loot_table/**",
            "nodeores.accesswidener",
            "META-INF/accesstransformer.cfg",
        )
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    withType<Test> {
        useJUnitPlatform()
        classpath += sourceSets.main.get().compileClasspath
    }

    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        val name = modName
        inputs.property("mod_name", name)
        from(rootProject.file("LICENSE")) { rename { "${it}_$name" } }
    }

    jar {
        manifest.attributes(
            "Specification-Title" to modName,
            "Specification-Vendor" to sc.properties.get<String>("mod.author"),
            "Specification-Version" to modVersion,
            "Implementation-Title" to "forge",
            "Implementation-Version" to modVersion,
            "Implementation-Vendor" to sc.properties.get<String>("mod.author"),
            "Built-On-Minecraft" to mc,
        )
    }
}
