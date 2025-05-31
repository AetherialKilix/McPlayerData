import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URI
import java.util.*

val uuidURI = URI("https://api.mojang.com/users/profiles/minecraft/")
val nameURI = URI("https://sessionserver.mojang.com/session/minecraft/profile/")

@Serializable
data class MojangProfile(
    /** UUID without dashes */
    val id: String,
    /** Player name */
    val name: String,
    /** Whether the UUID represents a valid Mojang account */
    val online: Boolean = true
)

@Serializable
data class MojangSession(
    val id: String,
    val name: String,
    val properties: List<MojangSessionProperty>,
    val profileActions: List<String>
)

@Serializable
data class MojangSessionProperty(
    val name: String,
    val value: String
)

fun getProfile(player: String, offline: Boolean = false): MojangProfile {
    if (offline) return return MojangProfile(
        clean(UUID.nameUUIDFromBytes("OfflinePlayer:$player".toByteArray(Charsets.UTF_8))),
        player,
        false
    )

    val json = uuidURI.resolve(player).toURL()
        .openStream()
        .readAllBytes()
        .toString(Charsets.UTF_8)
    return Json.decodeFromString<MojangProfile>(json)
}

fun clean(uuid: UUID) = clean(uuid.toString())
fun clean(uuid: String) = uuid.replace("-", "")

fun removeUUIDDashes(uuid: String) = uuid.replace("-", "")
fun addUUIDDashes(uuid: String) = uuid.toMutableList()
    .also {
        it.add(8, '-')
        it.add(13, '-')
        it.add(18, '-')
        it.add(23, '-')
    }.joinToString("")

fun toUUID(uuid: String) =
    if (hasDashes(uuid)) UUID.fromString(uuid)
    else UUID.fromString(addUUIDDashes(uuid))

fun hasDashes(str: String): Boolean {
    if (str.length != 36) return false
    if (str[8] != '-') return false
    if (str[13] != '-') return false
    if (str[18] != '-') return false
    if (str[23] != '-') return false
    return true
}

fun fetchSession(uuid: UUID): MojangSession {
    val clean = removeUUIDDashes(uuid.toString())
    val uri = nameURI.resolve(clean)
    val json = uri.toURL()
        .openStream()
        .readAllBytes()
        .toString(Charsets.UTF_8)
    return Json.decodeFromString<MojangSession>(json)
}

fun handleArgs(mode: String?, argument: String?): Unit? = when {
    mode == null -> null
    argument == null -> null
    argument == "help" -> null
    mode == "online" -> println(getProfile(argument))
    mode == "offline" -> println(getProfile(argument, offline = true))
    mode == "session" -> println(fetchSession(toUUID(argument)))
    mode == "find" -> findMatches(argument)
    else -> null
}

fun findMatches(player: String) {
    val online: MojangProfile = getProfile(player)
    val offline: MojangProfile = getProfile(player, offline = true)

    println("""
        Looking for profiles:
          $online
          $offline
    """.trimIndent())

    val matchSet = setOfNotNull(online.id, offline.id, addUUIDDashes(online.id), addUUIDDashes(offline.id))

    val matches = System.`in`
        .bufferedReader()
        .lineSequence()
        .filter { line ->
            matchSet.any { match -> line.contains(match, ignoreCase = true) }
        }
        .toList()

    if (matches.isEmpty()) return println("No matches")
    println("Matches (${matches.size}):")
    matches.forEach { println("  $it") }
}

fun main(args: Array<String>) {
    // handle args, and simply return
    if (handleArgs(args.getOrNull(0), args.getOrNull(1)) == null) return

    // if failed, print usage:
    println("""
        Usage: playerdata <action> <argument>
        Actions:
             online  [player name]: Fetch the MojangProfile for the given player name.
             offline [player name]: Generate an offline MojangProfile for the given player name.
             session [uuid]       : Fetch the MojangSession for the given UUID (with or without dashes).
             find    [player name]: Read from stdin and find the UUID for the given player name.
    """.trimIndent())

}
