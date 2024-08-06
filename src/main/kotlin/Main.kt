import java.io.File

fun main() {
    val recognizedCommands = arrayOf("exit", "echo", "type")

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

        if (recognizedCommands.contains(command)) {
            when (command) {
                "exit" -> return
                "echo" -> echo(arguments)
                "type" -> type(arguments, recognizedCommands)
            }
        } else {
            println("${command}: command not found")
        }
    }
}

private fun echo(arguments: String) {
    println(arguments)
}

private fun type(arguments: String, recognizedCommands: Array<String>) {
    if (arguments.isEmpty()){ return }

    // fetch PATH environment variable which is a
    // string containing a list of directories separated by a specific
    // character (colon : on Unix-like systems and semicolon ; on Windows).
    val pathEnv = System.getenv("PATH")
    val pathSeparator = if (System.getProperty("os.name").lowercase().contains("win")) ";" else ":"
    val paths = pathEnv.split(pathSeparator) // makes substrings of all paths specified by the separator above.

    // firstOrNull function iterates over the collection, applies the lambda to each element,
    // and returns the first element that matches the condition or null.
    // Lambda expression makes a file object that take the directory and the file name
    // to represent target which is matched to test if it exists, is a file, and is an executable.
    // dir is a placeholder for the item in paths currently being processed.
    val path = paths.firstOrNull { dir ->
        val file = File(dir, arguments)
        file.exists() && file.isFile && file.canExecute()
    }

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