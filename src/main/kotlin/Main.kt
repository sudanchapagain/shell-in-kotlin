fun main() {
    val recognizedCommands = arrayOf("exit", "echo", "type")

    // REPL loop
    while(true){
        // prompt symbol
        print("$ ")

        val input = readln().trim()
        // split the input with <space> as delimiter &
        // 2 max substrings. 1st for command name, 2nd
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
    if (recognizedCommands.contains(arguments)){
        println("$arguments is a shell builtin")
    } else {
        println("$arguments: not found")
    }
}