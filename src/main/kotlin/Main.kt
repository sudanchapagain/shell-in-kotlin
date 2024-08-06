import java.io.File
import java.io.IOException
import java.nio.file.Paths

fun main() {
    val recognizedCommands = arrayOf("exit", "echo", "pwd", "type")

    // REPL loop
    while(true){
        // prompt symbol
        print("$ ")

        val input = readln().trim()
        // split the input with <space> as delimiter & 2 max substrings. 1st for command name, 2nd
        // for arguments to be passed to commands.
        val parts = input.split(" ", limit = 2)
        val command = parts[0]
        // assign the second input substring to arguments if it exists.
        val arguments = if (parts.size > 1) parts[1] else ""
        // fetch path of command (for executable files, if it exists)
        val path = getPath(command)

        // Execute shell builtins
        if (recognizedCommands.contains(command)) {
            when (command) {
                "exit" -> return
                "echo" -> echo(arguments)
                "pwd" -> pwd()
                "type" -> type(arguments, recognizedCommands)
            }

        // Execute executable files
        } else if (path != null) {
           executeProgram(path, command, arguments)
        } else {
            println("${command}: command not found")
        }
    }
}

private fun echo(arguments: String) {
    println(arguments)
}

private fun pwd() {
    println(Paths.get("").toAbsolutePath())
    // println(File(".").absolutePath)
    // File object representing the current directory (".") is created and its absolute path is retrieved.
}

private fun type(arguments: String, recognizedCommands: Array<String>) {
    if (arguments.isEmpty()){ return }

    val path = getPath(arguments)

    // check if it's shell builtin
    if (recognizedCommands.contains(arguments)){
        println("$arguments is a shell builtin")
    } else if (path != null) {
        // print the path of executable argument
        if (System.getProperty("os.name").lowercase().contains("win")){
            println("$arguments is located at $path\\$arguments")
        } else {
            println("$arguments is not located at $path/$arguments")
        }
    } else {
        println("$arguments: not found")
    }
}

private fun getPath(arguments: String): String? {
    // fetch PATH environment variable which is a
    // string containing a list of directories separated by a specific
    // character (colon : on Unix-like systems and semicolon ; on Windows).
    val pathEnv = System.getenv("PATH")
    val pathSeparator = if (System.getProperty("os.name").lowercase().contains("win")) ";" else ":"

    // add current path to the PATH list.
    // pathEnv.split() makes substrings of all paths specified by the separator above.
    val currentPath = Paths.get("").toAbsolutePath().toString()
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

private fun executeProgram(path: String, command: String, argument: String){
    // arguments string to a list of arguments with filters to remove un-necessary spaces.
    val argumentsList = argument.split(" ").filter { it.isNotEmpty() }
    val arguments = argumentsList.toTypedArray()
    // assign path variable the value of `path\command`
    val pathCommand = path + "\\" + command

    val processBuilder = ProcessBuilder(pathCommand, *arguments)
    processBuilder.redirectErrorStream(true)

    try {
        val process = processBuilder.start()

        // Read the output of the process
        process.inputStream.bufferedReader().use { reader ->
            reader.lines().forEach { line -> println(line) }
        }

        // Wait for the process to complete
        val exitCode = process.waitFor()
        println("Process exited with code $exitCode")
    } catch (e: IOException) {
        println("Failed to execute command: ${e.message}")
    } catch (e: InterruptedException) {
        println("Process was interrupted: ${e.message}")
        Thread.currentThread().interrupt() // Restore interrupted status
    }
}
