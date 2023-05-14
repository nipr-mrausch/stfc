import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class SystemString(val id: Long, val key: String, val text: String)

@Serializable
data class System(
    val id: Long,
    val est_warp: Int,
    val is_deep_space: Boolean,
    val level: Int,
    val coords_x: Long,
    val coords_y: Long,
    val has_player_containers: Boolean
)

@Serializable
data class Hub(val id: Long, val hubId: Long)

@Serializable
data class NewHub(val id: Long, var children: List<Long>)

private val json = Json {
    ignoreUnknownKeys = true
}

fun main(args: Array<String>) {

    // parsing data back
    val names = json.decodeFromString<List<SystemString>>(
        SystemString::class.java.classLoader.getResource("systems_en.json").readText()
    )

    val systems = json.decodeFromString<List<System>>(SystemString::class.java.classLoader.getResource("systems.json").readText())

    val playerSystems = systems.filter { it.has_player_containers }

    val nameMap = names.filter { it.key == "name" }.associate { it.id to it.text }

//    playerSystems.associate { nameMap[it.id]!! to it }.toSortedMap{a,b -> a.text.compareTo(b.text)}.forEach { n, s ->
//        println("${n?.text}, ${s.est_warp}, ${s.level}, ${if (s.is_deep_space) "Y" else "N"}")
//    }

    val hubMap =
        json.decodeFromString<List<NewHub>>(SystemString::class.java.classLoader.getResource("hubs.json").readText())
            .toMutableList()

    fun unmappedSystems(level: Int) =
        playerSystems.filter { it.level == level }
            .filter { s -> !hubMap.any { it.id == s.id || it.children.contains(s.id) } }
            .filter { !(nameMap[it.id]?.contains("Alpha")?:false) }
            .map { nameMap[it.id] to it.id }

    val levels = (1..49).toMutableList()
    levels.forEach {
        println("Level $it Systems Unmapped")
        unmappedSystems(it).forEach(::println)
    }

    val systemMap = systems.associateBy { it.id }

    while(true) {
        println("System Name")
       println(readName(nameMap))


    }

}

fun defineHubs(nameMap: Map<Long, String>, hubMap: MutableList<NewHub>) {
    while (true) {
        println("Closest Hub")
        val h = readName(nameMap)

        if (hubMap.any { it.id == h })
            println("Duplicate")

        val s = mutableListOf(h)
        while (true) {
            println("System")
            val sys = readName(nameMap)
            if (sys == 0L)
                break
            else
                s.add(sys)
        }
        println("$h->$s")

        hubMap.add(NewHub(h, s))

        File("C:\\Users\\PowerSpec\\IdeaProjects\\STFC\\src\\main\\resources\\hubs.json").writeText(
            Json.encodeToString(
                hubMap
            )
        )
    }
}

fun readName(nameMap: Map<Long, String>): Long {
    val s = readln()
    return if (s == "end")
        0L else if (!nameMap.containsValue(s)) {
        println("Not found: $s")
        readName(nameMap)
    } else nameMap.filter { it.value == s }.firstNotNullOf { it.key }
}