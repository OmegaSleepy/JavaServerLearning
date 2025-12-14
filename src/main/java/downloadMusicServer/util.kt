package downloadMusicServer

private const val WATCH_KEY_WORD = "watch?v="
private const val PLAYLIST_KEY_WORD = "playlist?list=OLAK5uy_"

private const val SINGLE_CODE_LENGHT = 11
private const val PLAYLIST_CODE_LENGHT = 33

fun packageSingles(singles: List<String>):String {

    val builder = StringBuilder("-single{")
    for (single in singles) {
        builder.append(single).append(',')
    }
    builder.removeRange(builder.length - 1, builder.length - 1)
    builder.append("}")

    return builder.toString()
}

fun packageAlbums(albums: List<String>):String {
    val builder = StringBuilder("-albums{")
    for (album in albums) {
        builder.append(album).append(',')
    }
    builder.removeRange(builder.length - 1, builder.length - 1)
    builder.append("}")
    return builder.toString()
}

fun getSingleHash(fullUrl: String): String {
    val codeStartlist: Int = fullUrl.lastIndexOf(WATCH_KEY_WORD)

    return if (codeStartlist == -1) fullUrl else fullUrl
        .substring(codeStartlist + WATCH_KEY_WORD.length)
        .substring(0, SINGLE_CODE_LENGHT)
}

fun getAlbumHash(fullUrl: String): String {
    val codeStartlist: Int = fullUrl.lastIndexOf(PLAYLIST_KEY_WORD)

    return if (codeStartlist == -1) fullUrl else fullUrl
        .substring(codeStartlist + PLAYLIST_KEY_WORD.length)
        .substring(0, PLAYLIST_CODE_LENGHT)
}