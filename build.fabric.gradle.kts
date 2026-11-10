plugins {
    id("dev.kikugie.loom-back-compat")
}

val modVersion = providers.gradleProperty("version").orNull ?: sc.properties.get<String>("mod.version")
val modId = sc.properties.get<String>("mod.id")
val modName = sc.properties.get<String>("mod.name")
val mc = sc.current.version

version = modVersion
group = sc.properties.get<String>("mod.group")
base.archivesName = "$modId-fabric-$mc"

val modern = sc.current.parsed >= "26.1"
val requiredJava = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    else -> JavaVersion.VERSION_17
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
    java.exclude("com/workshopcraft/nodeores/neoforge/**", "com/workshopcraft/nodeores/forge/**")
}

dependencies {
    minecraft("com.mojang:minecraft:$mc")
    loomx.applyMojangMappings()
    modImplementation("net.fabricmc:fabric-loader:${sc.properties.get<String>("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${sc.properties.get<String>("deps.fabric_api")}")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")
    if (modern) accessWidenerPath = rootProject.file("src/main/resources/nodeores.accesswidener")
    runConfigs.all {
        runDirectory = rootProject.file("runs/${sc.current.project}")
    }
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
            "loader" to sc.properties.get<String>("deps.fabric_loader"),
            "java" to requiredJava.majorVersion,
            "pack_min" to (runCatching { sc.properties.get<String>("mod.pack_min") }.getOrDefault("")),
            "pack_max" to (runCatching { sc.properties.get<String>("mod.pack_max") }.getOrDefault("")),
            "pack_format" to (runCatching { sc.properties.get<String>("mod.pack_format") }.getOrDefault("")),
        )
        inputs.properties(props)
        filesMatching(listOf("fabric.mod.json", "pack.mcmeta")) { expand(props) }
        exclude("META-INF/neoforge.mods.toml", "META-INF/mods.toml", "META-INF/accesstransformer.cfg", "data/neoforge/**", "data/forge/**")
        if (!modern) {
            exclude("nodeores.accesswidener")
            filesMatching("fabric.mod.json") { filter(StripAccessWidener::class.java) }
        }
        if (sc.current.parsed < "1.21") {
            exclude("data/minecraft/tags/block/**", "data/nodeores/loot_table/**")
        }
        if (sc.current.parsed >= "26.3") exclude("data/nodeores/worldgen/configured_feature/**")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    withType<Test> {
        useJUnitPlatform()
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
            "Implementation-Title" to "fabric",
            "Implementation-Version" to modVersion,
            "Implementation-Vendor" to sc.properties.get<String>("mod.author"),
            "Built-On-Minecraft" to mc,
        )
    }
}
