fun main() {
    val recognizedCommands = arrayOf("help", "exit", "ls", "cd", "pwd", "mkdir", "rmdir", "touch", "rm", "cat", "echo", "clear")

    while(true){
      print("$ ")
      val command = readln()

      if (command == "exit") {
        return
      }

      if (recognizedCommands.contains(command)) {
        return
      } else {
        println("${command}: command not found")
      }
    }
}
