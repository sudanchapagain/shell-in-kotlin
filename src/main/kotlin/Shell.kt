import kotlin.io.path.Path
import kotlin.system.exitProcess

class Shell {
    private val builtins = Builtins(this)
    private val recognizedCommands = arrayOf("exit", "echo", "pwd", "cd", "type")

    @Suppress("HasPlatformType")
    var currentPath = Path("").toAbsolutePath()

    fun repl() {
        while (true) {
            print("$ ")
            val input = readln().trim()
            evaluate(input)
        }
    }

    private fun evaluate(input: String) {
        val (command, arguments) = splitInput(input)
        val filePath = builtins.getPath(command) // fetch path of command (for executable files, if it exists)
        if (recognizedCommands.contains(command)) {
            runBuiltin(command, arguments)
        } else if (filePath != null) {
            builtins.executeProgram(filePath, command, arguments)
        } else {
            println("${command}: command not found")
        }
    }

    private fun runBuiltin(command: String, arguments: String) {
        when (command) {
            "exit" -> exitProcess(0)
            "echo" -> builtins.echo(arguments)
            "pwd" -> builtins.pwd(currentPath.toString())
            "type" -> builtins.type(arguments, recognizedCommands)
            "cd" -> builtins.cd(arguments)
        }
    }

    private fun splitInput(input: String): Pair<String, String> {
        // split the input with <space> as delimiter & 2 max substrings. 1st for command name, 2nd
        // for arguments to be passed to commands.
        val parts = input.split(" ", limit = 2)
        val command = parts[0]
        // assign the second input substring to arguments if it exists.
        val arguments = if (parts.size > 1) parts[1] else ""
        return Pair(command, arguments)
    }

}