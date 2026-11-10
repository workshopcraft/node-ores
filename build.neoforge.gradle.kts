plugins {
    id("net.neoforged.moddev")
    id("neoforge-mutex")
}

val modVersion = providers.gradleProperty("version").orNull ?: sc.properties.get<String>("mod.version")
val modId = sc.properties.get<String>("mod.id")
val modName = sc.properties.get<String>("mod.name")
val mc = sc.current.version

version = modVersion
group = sc.properties.get<String>("mod.group")
base.archivesName = "$modId-neoforge-$mc"

val modern = sc.current.parsed >= "26.1"
val requiredJava = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    else -> JavaVersion.VERSION_21
}

val era = when {
    sc.current.parsed >= "26.3" -> "26.3"
    sc.current.parsed >= "26.1" -> "26.1"
    sc.current.parsed >= "1.21" -> "1.21.1"
    else -> "1.20.1"
}

sourceSets.main {
    resources.srcDir(rootProject.file("src/eras/$era"))
    if (sc.current.parsed < "1.21") resources.srcDir(rootProject.file("src/legacy/1.20.1"))
    if (sc.current.parsed >= "26.3") resources.srcDir(rootProject.file("src/legacy/26.3"))
    java.exclude("com/workshopcraft/nodeores/fabric/**", "com/workshopcraft/nodeores/forge/**")
}

neoForge {
    version = sc.properties.get<String>("deps.neo_loader")
    if (modern) accessTransformers.files.setFrom(rootProject.file("src/main/resources/META-INF/accesstransformer.cfg"))
    else accessTransformers.files.setFrom()
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
            "neoforge" to sc.properties.get<String>("deps.neo_compat"),
            "fml" to sc.properties.get<String>("deps.neo_fml_compat"),
            "pack_min" to (runCatching { sc.properties.get<String>("mod.pack_min") }.getOrDefault("")),
            "pack_max" to (runCatching { sc.properties.get<String>("mod.pack_max") }.getOrDefault("")),
            "pack_format" to (runCatching { sc.properties.get<String>("mod.pack_format") }.getOrDefault("")),
        )
        inputs.properties(props)
        filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) { expand(props) }
        exclude("fabric.mod.json", "META-INF/mods.toml", "data/forge/**")
        if (!modern) exclude("nodeores.accesswidener", "META-INF/accesstransformer.cfg")
        else exclude("nodeores.accesswidener")
        if (sc.current.parsed < "1.21") {
            exclude("data/minecraft/tags/block/**", "data/nodeores/loot_table/**")
        }
        if (sc.current.parsed >= "26.3") exclude("data/nodeores/worldgen/configured_feature/**")
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
            "Implementation-Title" to "neoforge",
            "Implementation-Version" to modVersion,
            "Implementation-Vendor" to sc.properties.get<String>("mod.author"),
            "Built-On-Minecraft" to mc,
        )
    }
}
