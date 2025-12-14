package downloadMusicServer

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

fun packagePlaylists(playlists: List<String>):String {
    val builder = StringBuilder("-playlists{")
    for (album in playlists) {
        builder.append(album).append(',')
    }
    builder.removeRange(builder.length - 1, builder.length - 1)
    builder.append("}")
    return builder.toString()
}