package downloadMusicServer

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Path

@JvmField
val PYTHON_PATH: Path = Path.of("C:/Users/THEBEAST/AppData/Local/Programs/Python/Python310/python.exe")
@JvmField
val MUSIC_DIRECTORY: Path = Path.of("C:/Users/THEBEAST/music")

fun runPython(args: Array<String>) {

    val scriptPath = extractPythonScript()

    val command = ArrayList<String>()
    command.add(PYTHON_PATH.toString())
    command.add(scriptPath.toAbsolutePath().toString())
    command.addAll(args)

    val pb = ProcessBuilder(command)
        .directory(MUSIC_DIRECTORY.toFile())
        .redirectErrorStream(true)

    val process = pb.start()

    BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
        reader.forEachLine { println(it) }
    }

    val exitCode = process.waitFor()
    println("Python script finished with exit code $exitCode")
}
