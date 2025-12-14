package downloadMusicServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AlbumZipper {

    /**
     * Zips multiple files into a single zip named after the album.
     *
     * @param albumName The album name (used for the zip file)
     * @param files     List of Paths to include in the zip
     * @param outputDir Directory where the zip will be created
     * @throws IOException
     */
    public static void zipAlbum (String albumName, List<Path> files, Path outputDir) throws IOException {
        if (files == null || files.isEmpty()) {
            System.out.println("No files to zip for album: " + albumName);
            return;
        }

        // Ensure output directory exists
        Files.createDirectories(outputDir);

        // Sanitize album name for file system
        String safeAlbumName = albumName.replaceAll("[<>:\"/\\\\|?*]", "_");

        Path zipPath = outputDir.resolve(safeAlbumName + ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            for (Path file : files) {
                if (!Files.exists(file)) {
                    System.out.println("Skipping missing file: " + file);
                    continue;
                }

                // Use only the filename inside the zip, not full path
                ZipEntry entry = new ZipEntry(file.getFileName().toString());
                zos.putNextEntry(entry);

                // Copy file content into the zip
                Files.copy(file, zos);

                zos.closeEntry();
            }
        }

        System.out.println("Created zip: " + zipPath.toAbsolutePath());
    }

    // Example usage:
    public static void main (String[] args) throws IOException {
        List<Path> files = List.of(
                Paths.get("Single/Artist1/Song1.mp3"),
                Paths.get("Single/Artist1/Song2.mp3")
        );

        zipAlbum("My Album", files, Paths.get("output"));
    }
}