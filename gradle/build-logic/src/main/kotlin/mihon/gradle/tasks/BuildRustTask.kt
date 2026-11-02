package mihon.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFile
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.LocalState
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class BuildRustTask @Inject constructor(private val exec: ExecOperations) : DefaultTask() {
    @get:Internal
    abstract val sourceDirectory: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val sources: FileTree
        get() = sourceDirectory.asFileTree.matching {
            include("Cargo.toml", "Cargo.lock", "src/**", "benches/**")
        }

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:LocalState
    abstract val targetDirectory: DirectoryProperty

    @get:Internal
    abstract val ndkDirectory: DirectoryProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    val ndkProperties: RegularFile
        get() = ndkDirectory.file("source.properties").get()

    @TaskAction
    fun build() {
        exec.exec {
            workingDir(sourceDirectory.get().asFile)
            environment("ANDROID_NDK_HOME", ndkDirectory.get().asFile.absolutePath)
            environment("CARGO_TARGET_DIR", targetDirectory.get().asFile.absolutePath)
            commandLine(
                "cargo", "ndk", "-t", "armeabi-v7a", "-t", "arm64-v8a", "-t", "x86", "-t", "x86_64",
                "--platform", "26", "-o", outputDirectory.get().asFile.absolutePath,
                "build", "--release", "--locked",
            )
        }
    }
}
