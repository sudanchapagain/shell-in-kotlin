package np.com.sudanchapagain

import kotlin.io.path.Path
import kotlin.system.exitProcess

class Shell {
    private val builtins = Builtins(this)
    private val recognizedCommands = setOf("exit", "echo", "pwd", "cd", "type")

    var currentPath = Path("").toAbsolutePath()

    fun repl() {
        while (true) {
            print("$ ")
            val input = try {
                readln().trim()
            } catch (e: Exception) {
                println()
                break
            }
            if (input.isEmpty()) continue
            evaluate(input)
        }
    }

    private fun evaluate(input: String) {
        val (command, arguments) = splitInput(input)
        if (command.isEmpty()) return
        val filePath = builtins.getPath(command)
        if (recognizedCommands.contains(command)) {
            runBuiltin(command, arguments)
        } else if (filePath != null) {
            builtins.executeProgram(filePath, command, arguments)
        } else {
            println("$command: command not found")
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
        if (input.isBlank()) return Pair("", "")
        val parts = input.split(" ", limit = 2)
        val command = parts[0]
        val arguments = if (parts.size > 1) parts[1] else ""
        return Pair(command, arguments)
    }

}
