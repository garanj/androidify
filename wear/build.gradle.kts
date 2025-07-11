import java.io.ByteArrayOutputStream
import java.util.regex.Pattern

evaluationDependsOn(":wear:watchface")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "com.android.developers.androidify"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.wearMinSdk.get().toInt()
        applicationId = "com.android.developers.androidify"
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get())
    }
    kotlinOptions {
        jvmTarget = libs.versions.jvmTarget.get()
    }
    buildFeatures {
        compose = true
    }
    sourceSets {
        getByName("release") {
            assets.srcDirs(layout.buildDirectory.dir("intermediates/watchfaceAssets/release"))
            res.srcDirs(layout.buildDirectory.file("generated/wfTokenRes/release/res/"))
        }
        getByName("debug") {
            assets.srcDirs(layout.buildDirectory.dir("intermediates/watchfaceAssets/debug"))
            res.srcDirs(layout.buildDirectory.file("generated/wfTokenRes/debug/res/"))
        }
    }
}

configurations {
    create("cliToolConfiguration") {
        isCanBeConsumed = false
        isCanBeResolved = true
    }
}

dependencies {
    implementation(projects.wear.common)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.material)
    implementation(libs.androidx.wear.compose.ui.tooling)
    implementation(libs.androidx.activity.compose)
    implementation(libs.play.services.wearable)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.watchface.push)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.androidx.wear)
    implementation(libs.androidx.wear.remote.interactions)
    implementation(libs.horologist.compose.layout)
    implementation(libs.accompanist.permissions)

    "cliToolConfiguration"(libs.validator.push.cli)
}

androidComponents.onVariants { variant ->
    val capsVariant = variant.name.replaceFirstChar { it.uppercase() }

    val copyTaskProvider = tasks.register<Copy>("copyWatchface${capsVariant}Output") {
        val wfTask = project(":wear:watchface").tasks.named("assemble$capsVariant")
        dependsOn(wfTask)
        val buildDir = project(":wear:watchface").layout.buildDirectory.asFileTree.matching {
            include("**/*.apk")
        }
        from(buildDir)
        into(layout.buildDirectory.dir("intermediates/watchfaceAssets/${variant.name}"))

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        eachFile {
            path = "default_watchface.apk"
        }
        includeEmptyDirs = false
    }

    val tokenTask = tasks.register<ProcessFilesTask>("generateToken${capsVariant}Res") {
        val tokenFile =
            layout.buildDirectory.file("generated/wfTokenRes/${variant.name}/res/values/wf_token.xml")

        inputFile.from(copyTaskProvider.map { it.outputs.files.singleFile })
        outputFile.set(tokenFile)
        cliToolClasspath.set(project.configurations["cliToolConfiguration"])
    }

    afterEvaluate {
        tasks.named("pre${capsVariant}Build").configure {
            dependsOn(tokenTask)
        }
    }
}

abstract class ProcessFilesTask : DefaultTask() {
    @get:InputFiles
    abstract val inputFile: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val cliToolClasspath: Property<FileCollection>

    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    fun taskAction() {
        val apkFile = inputFile.singleFile.resolve("default_watchface.apk")

        val stdOut = ByteArrayOutputStream()
        val stdErr = ByteArrayOutputStream()

        execOperations.javaexec {
            classpath = cliToolClasspath.get()
            mainClass = "com.google.android.wearable.watchface.validator.cli.DwfValidation"

            args(
                "--apk_path=${apkFile.absolutePath}",
                "--package_name=com.android.developers.androidify",
            )
            standardOutput = stdOut
            errorOutput = stdErr
            isIgnoreExitValue = true
        }

        val outputAsText = stdOut.toString()
        val errorAsText = stdErr.toString()

        if (outputAsText.contains("Failed check")) {
            println(outputAsText)
            if (errorAsText.isNotEmpty()) {
                println(errorAsText)
            }
            throw GradleException("Watch face validation failed")
        }

        val match = Pattern.compile("generated token: (\\S+)").matcher(stdOut.toString())
        if (match.find()) {
            val token = match.group(1)
            val output = outputFile.get().asFile
            output.parentFile.mkdirs()
            val tokenResText = """<resources>
                         |    <string name="default_wf_token">$token</string>
                         |</resources>
                       """.trimMargin()
            output.writeText(tokenResText)
        } else {
            throw TaskExecutionException(
                this,
                GradleException("No token generated for watch face!"),
            )
        }
    }
}