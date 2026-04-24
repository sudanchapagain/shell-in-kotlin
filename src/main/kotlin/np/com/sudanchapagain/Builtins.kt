package np.com.sudanchapagain

import java.io.File
import java.io.IOException
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class Builtins(private val shell: Shell) {
    fun echo(arguments: String) {
        println(arguments)
    }

    fun pwd(currentPath: String) {
        println(currentPath)
    }

    fun cd(arguments: String) {
        val target = when {
            arguments.isBlank() -> System.getenv("HOME") ?: "."
            arguments.startsWith("/") -> arguments
            arguments.startsWith("~") -> {
                val home = System.getenv("HOME") ?: ""
                arguments.replaceFirst("~", home)
            }
            else -> shell.currentPath.resolve(arguments).toString()
        }

        val newPath = Path(target)

        if (newPath.exists() && newPath.isDirectory()) {
            shell.currentPath = newPath.normalize().toAbsolutePath()
        } else {
            println("cd: $arguments: No such file or directory")
        }
    }

    fun type(arguments: String, recognizedCommands: Set<String>) {
        if (arguments.isEmpty()) {
            return
        }

        val filePath = getPath(arguments)

        if (recognizedCommands.contains(arguments)) {
            println("$arguments is a shell builtin")
        } else if (filePath != null) {
            if (System.getProperty("os.name").lowercase().contains("win")) {
                println("$arguments is $filePath\\$arguments")
            } else {
                println("$arguments is $filePath/$arguments")
            }
        } else {
            println("$arguments: not found")
        }
    }

    fun executeProgram(path: String, command: String, argument: String) {
        val argumentsList = argument.split(" ").filter { it.isNotEmpty() }
        val executable = File(path, command)
        val commandPath = executable.absolutePath

        val cmd = mutableListOf<String>().apply {
            add(commandPath)
            addAll(argumentsList)
        }

        val processBuilder = ProcessBuilder(cmd)
        processBuilder.redirectErrorStream(true)
        processBuilder.directory(shell.currentPath.toFile())

        try {
            val process = processBuilder.start()
            process.inputStream.bufferedReader().use { reader ->
                reader.lines().forEach { line -> println(line) }
            }
            process.waitFor()
        } catch (e: IOException) {
            println("Failed to execute command: ${e.message}")
        } catch (e: InterruptedException) {
            println("Process was interrupted: ${e.message}")
            Thread.currentThread().interrupt()
        }
    }

    fun getPath(arguments: String): String? {
        val pathEnv = System.getenv("PATH") ?: ""
        val pathSeparator = if (System.getProperty("os.name").lowercase().contains("win")) ";" else ":"

        val currentPath = shell.currentPath.toString()
        val paths = (pathEnv.split(pathSeparator) + currentPath).filter { it.isNotBlank() }.toSet()

        val path = paths.firstOrNull { dir ->
            val file = File(dir, arguments)
            file.exists() && file.isFile && file.canExecute()
        }

        return path
    }

}
