fun main() {
    val recognizedCommands = arrayOf("help", "exit", "ls", "cd", "pwd", "mkdir", "rmdir", "touch", "rm", "cat", "echo", "clear")
    print("$ ")
    val command = readln()
    if (recognizedCommands.contains(command)) {
        return
    } else {
      println("${command}: command not found")
    }
}
