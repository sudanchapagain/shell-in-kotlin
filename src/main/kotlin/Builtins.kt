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
        val newPath = when {
            arguments.startsWith("/") -> Path(arguments)
            arguments.startsWith("~") -> {
                val home = System.getenv("HOME") ?: ""
                Path(arguments.replace("~", home))
            }

            else -> shell.currentPath.resolve(arguments)
        }

        if (newPath.exists() && newPath.isDirectory()) {
            shell.currentPath = newPath.normalize().toAbsolutePath()
        } else {
            println("cd: $arguments: No such file or directory")
        }
    }

    fun type(arguments: String, recognizedCommands: Array<String>) {
        if (arguments.isEmpty()) {
            return
        }

        val filePath = getPath(arguments)

        // check if it's shell builtin
        if (recognizedCommands.contains(arguments)) {
            println("$arguments is a shell builtin")
        } else if (filePath != null) {
            // print the path of executable argument
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
        // arguments string to a list of arguments with filters to remove un-necessary spaces.
        val argumentsList = argument.split(" ").filter { it.isNotEmpty() }
        val arguments = argumentsList.toTypedArray()
        // assign path variable the value of `path\command`
        val pathCommand =
            if (System.getProperty("os.name").lowercase().contains("win")) "$path\\$command" else "$path/$command"

        val processBuilder = ProcessBuilder(pathCommand, *arguments)
        processBuilder.redirectErrorStream(true)

        try {
            val process = processBuilder.start()
            // Read the output of the process
            process.inputStream.bufferedReader().use { reader ->
                reader.lines().forEach { line -> println(line) }
            }
            process.waitFor()
        } catch (e: IOException) {
            println("Failed to execute command: ${e.message}")
        } catch (e: InterruptedException) {
            println("Process was interrupted: ${e.message}")
            Thread.currentThread().interrupt() // Restore interrupted status
        }
    }

    fun getPath(arguments: String): String? {
        // fetch PATH environment variable which is a
        // string containing a list of directories separated by a specific
        // character (colon : on Unix-like systems and semicolon ; on Windows).
        val pathEnv = System.getenv("PATH")
        val pathSeparator = if (System.getProperty("os.name").lowercase().contains("win")) ";" else ":"
        // add current path to the PATH list.
        // pathEnv.split() makes substrings of all paths specified by the separator above.
        val currentPath = Path("").toAbsolutePath().toString()
        val paths = (pathEnv.split(pathSeparator) + currentPath).toSet()

        // firstOrNull function iterates over the collection, applies the lambda to each element,
        // and returns the first element that matches the condition or null.
        // Lambda expression makes a file object that take the directory and the file name
        // to represent target which is matched to test if it exists, is a file, and is an executable.
        // dir is a placeholder for the item in paths currently being processed.
        val path = paths.firstOrNull { dir ->
            val file = File(dir, arguments)
            file.exists() && file.isFile && file.canExecute()
        }

        return path
    }

}