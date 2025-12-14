# noinspection PyInterpreter
import os
import re
import sys
import yt_dlp

# --- config ---
MP3_BITRATE = "192"  # kbps
# ----------------

# Regex to remove illegal filename characters (Windows-safe)
INVALID_FILENAME_RE = re.compile(r'[<>:"/\\|?*\x00-\x1F]')

def sanitize_name(name: str, max_len: int = 200) -> str:
    """Sanitize a string so it can be used as a filename/dir on Windows and Unix."""
    if not name:
        return ""
    name = name.strip()
    # Replace invalid chars with underscore
    name = INVALID_FILENAME_RE.sub("_", name)
    # Collapse multiple spaces/underscores
    name = re.sub(r"[ \t]+", " ", name)
    name = re.sub(r"[_]{2,}", "_", name)
    # Trim length
    if len(name) > max_len:
        name = name[:max_len].rstrip()
    return name or "untitled"

def make_ydl_opts_for_outdir(outdir: str):
    """Return yt-dlp options dict targetting outdir and embedding metadata."""
    outdir = outdir.rstrip("/\\")
    outtmpl = os.path.join(outdir, "%(title)s.%(ext)s")

    return {
        "format": "bestaudio/best",
        "outtmpl": outtmpl,
        "noplaylist": False,
        "ignoreerrors": True,
        "quiet": False,
        "postprocessors": [
            {
                "key": "FFmpegExtractAudio",
                "preferredcodec": "mp3",
                "preferredquality": MP3_BITRATE,
            },
            {
                "key": "FFmpegMetadata",
                "add_metadata": True,
            },
        ],
        "embedmetadata": True,
        # optionally embed thumbnails: uncomment the next two lines
        # "writethumbnail": True,
        # "embedthumbnail": True,
    }

def download_playlist(playlist_url):

    playlist_url = "https://www.youtube.com/playlist?list=OLAK5uy_" + (playlist_url)

    ydl_opts = {
        "format": "bestaudio/best",

        # ✅ Files only use title as name, stored in folder by playlist
        "outtmpl": "%(playlist_title)s/%(title)s.%(ext)s",

        # Convert + write metadata properly
        "postprocessors": [
            {
                "key": "FFmpegExtractAudio",
                "preferredcodec": "mp3",
                "preferredquality": "192",
            },
            {
                "key": "FFmpegMetadata",  # <-- this handles title, artist, album, track
                "add_metadata": True,
            },
        ],

        "embedmetadata": True,
        "addmetadata": True,
        "noplaylist": False,
        "ignoreerrors": True,
        "quiet": False,
    }

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        ydl.download([playlist_url])

def download_single(video_url: str):
    """
    Download a single video as MP3, set Artist=uploader and Album="Single".
    Saves to: Single/<sanitized uploader name>/<title>.mp3
    If uploader is missing, uses Single/Unknown/.
    """
    video_url = "https://youtube.com/watch?v=" + (video_url)

    # Extract info first (no download) to get uploader -> allows us to build folder
    ydl_extract_opts = {"quiet": True, "skip_download": True}
    with yt_dlp.YoutubeDL(ydl_extract_opts) as ydl:
        try:
            info = ydl.extract_info(video_url, download=False)
        except Exception as e:
            print(f"[!] Error extracting info: {e}")
            # fallback: use unknown uploader
            info = {}

    uploader = info.get("uploader") or info.get("uploader_id") or "Unknown"
    uploader_safe = sanitize_name(uploader) or "Unknown"
    album_name = "Single"
    album_safe = sanitize_name(album_name)

    outdir = os.path.join(album_safe, uploader_safe)
    os.makedirs(outdir, exist_ok=True)

    # Build ydl options for this outdir
    ydl_opts = make_ydl_opts_for_outdir(outdir)

    # We rely on FFmpegMetadata + add_metadata to populate artist/album/track from info fields.
    # But since we're invoking download() now, ensure info contains keys; we can pass a selector via
    # "addheader": None etc. However, yt-dlp will populate metadata from the extracted info automatically.

    # The trick: run the download with a "URL list" that only contains our video_url.
    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        # We can optionally pass a hook to print progress
        def hook(d):
            if d.get("status") == "downloading":
                print(f"Downloading: {d.get('filename', '...')}")
            elif d.get("status") == "finished":
                print(f"Finished: {d.get('filename', '...')}")
        ydl.add_progress_hook(hook)

        # Download -- yt-dlp will extract metadata and FFmpegMetadata will embed it.
        # To make sure album becomes "Single", we can set "playlist_title" and/or "album" in the info dict
        # by using the --metadata-from-title facility is not necessary; instead we store the info in a small
        # temporary info dict override via ydl.download() path: easiest is to pre-download via info extraction
        # then call ydl.process_info to download a prepared info dict.
        try:
            if info:
                # ensure we override album/uploader in the info dict used for processing
                info["playlist_title"] = album_name  # album
                # yt-dlp expects 'uploader' to be present (it often is); set if missing
                info["uploader"] = info.get("uploader") or uploader
                # Now process the info directly (this downloads the item respecting outtmpl)
                ydl.process_info(info)
            else:
                # Fallback: just call download with the URL
                ydl.download([video_url])
        except Exception as e:
            print(f"[!] Download failed: {e}")

def download(urls, method):
    for url in urls:
        method(url)

# python
def parse_run_args(argv):
    """
    Parse run arguments of the form:
      -albums{id1,id2,...}
      -single{id1,id2,...}
    Returns two lists: (albums_list, singles_list).
    """
    joined = " ".join(argv or [])
    albums = []
    singles = []

    for name, dest in (("albums", albums), ("single", singles)):
        # find all occurrences like -albums{...} or -single{...}
        matches = re.findall(fr"-{name}\{{([^}}]*)\}}", joined, flags=re.IGNORECASE)
        for m in matches:
            parts = [p.strip() for p in re.split(r"[,\s]+", m) if p.strip()]
            dest.extend(parts)

    return albums, singles

if __name__ == "__main__":

    print("Downloading Begun")

    # Default album URLs (keeps previous example defaults)
    default_albums = []
    default_singles = []  # no default single IDs

    # Parse command-line arguments provided in run configuration
    albums_arg, singles_arg = parse_run_args(sys.argv[1:])

    urls = albums_arg or default_albums
    singles_url = singles_arg or default_singles

    download(urls, download_playlist)

    if singles_url:
        download(singles_url, download_single)

