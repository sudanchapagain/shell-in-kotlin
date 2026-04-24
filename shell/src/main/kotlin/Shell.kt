package np.com.sudanchapagain.utils

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path
import kotlin.system.exitProcess

class Shell {
    private val builtins = Builtins(this)
    private val recognizedCommands = setOf("exit", "echo", "pwd", "cd", "type")

    var currentPath: Path = Path("").toAbsolutePath()
    private val historyDir = computeDataDir()
    private val historyFile = historyDir.resolve(".tiny_shell_history")

    init {
        try {
            Files.createDirectories(historyDir)
            if (!Files.exists(historyFile)) {
                Files.createFile(historyFile)
            }
        } catch (e: IOException) {
        }
    }

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
            val expanded = expandWindowsEnv(input)
            appendHistory(expanded)
            evaluate(expanded)
        }
    }

    private fun expandWindowsEnv(text: String): String {
        val os = System.getProperty("os.name").lowercase()
        if (!os.contains("win")) return text

        val regex = Regex("%([^%]+)%")
        val home = System.getenv("HOME") ?: "."

        return regex.replace(text) { match ->
            val name = match.groupValues[1]
            val env = System.getenv(name) ?: System.getenv(name.uppercase())
            if (env != null) return@replace env

            when (name.uppercase()) {
                "APPDATA" -> System.getenv("APPDATA") ?: (System.getenv("XDG_CONFIG_HOME") ?: "$home/.config")
                "LOCALAPPDATA" -> System.getenv("LOCALAPPDATA") ?: (System.getenv("XDG_DATA_HOME") ?: "$home/.local/share")
                "TEMP", "TMP" -> System.getenv("TEMP") ?: System.getProperty("java.io.tmpdir") ?: "/tmp"
                "SYSTEMROOT" -> System.getenv("SYSTEMROOT") ?: "C:\\Windows"
                "ALLUSERSPROFILE" -> System.getenv("ALLUSERSPROFILE") ?: "C:\\ProgramData"
                "USERPROFILE" -> System.getenv("USERPROFILE") ?: home
                else -> ""
            }
        }
    }

    private fun appendHistory(entry: String) {
        try {
            Files.writeString(
                historyFile, entry + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND
            )
        } catch (e: IOException) {
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

    private fun computeDataDir(): Path {
        val os = System.getProperty("os.name").lowercase()
        val home = System.getenv("HOME") ?: "."

        return when {
            os.contains("win") -> {
                val appdata = System.getenv("APPDATA") ?: System.getenv("LOCALAPPDATA") ?: home
                Path(appdata, "tiny-shell").toAbsolutePath()
            }

            os.contains("mac") || os.contains("darwin") -> {
                Path(home, "Library", "Application Support", "tiny-shell").toAbsolutePath()
            }

            else -> {
                val xdg = System.getenv("XDG_DATA_HOME") ?: "$home/.local/share"
                Path(xdg, "tiny-shell").toAbsolutePath()
            }
        }
    }
}
