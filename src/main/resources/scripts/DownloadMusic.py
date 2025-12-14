# noinspection PyInterpreter
import os
import re
import sys
import yt_dlp
import sqlite3
from datetime import datetime

# --- config ---
MP3_BITRATE = "192"  # kbps
# ----------------

# Regex to remove illegal filename characters (Windows-safe)
INVALID_FILENAME_RE = re.compile(r'[<>:"/\\|?*\x00-\x1F]')

DB_PATH = r"C:\Users\THEBEAST\Downloads\Server\test.sqlite"

def init_db():
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    cur.execute("""
                CREATE TABLE IF NOT EXISTS songs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    video_id TEXT UNIQUE,
                    title TEXT,
                    artist TEXT,
                    album TEXT,
                    filepath TEXT,
                    source_url TEXT,
                    downloaded_at TEXT
                )
                """)
    conn.commit()
    conn.close()

def save_song(info, final_path):
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()

    cur.execute("""
                INSERT OR IGNORE INTO songs
        (video_id, title, artist, album, filepath, source_url, downloaded_at)
        VALUES (?, ?, ?, ?, ?, ?, ?)
                """, (
                    info.get("id"),
                    info.get("title"),
                    info.get("uploader"),
                    info.get("playlist_title") or "Single",
                    final_path,
                    info.get("webpage_url"),
                    datetime.utcnow().isoformat()
                ))

    conn.commit()
    conn.close()

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

# --- unified playlist/album download function ---
def download_playlist(url):
    """
    Download a playlist or album given its ID or full URL.
    Stores files in folder by playlist_title and converts to MP3.
    """
    if not url.startswith("http"):
        url = "https://www.youtube.com/playlist?list=" + url

    ydl_opts = {
        "format": "bestaudio/best",
        "outtmpl": "%(playlist_title)s/%(title)s.%(ext)s",
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
        "addmetadata": True,
    }

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        ydl.add_progress_hook(db_hook)
        ydl.download([url])

def download_single(video_url: str):
    """
    Download a single video as MP3, set Artist=uploader and Album="Single".
    Saves to: Single/<sanitized uploader name>/<title>.mp3
    If uploader is missing, uses Single/Unknown/.
    """
    video_url = (video_url)

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
        ydl.add_progress_hook(db_hook)

        if info:
            info["playlist_title"] = album_name
            info["uploader"] = info.get("uploader") or uploader
            ydl.process_info(info)
        else:
            ydl.download([video_url])

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

# --- parse_run_args fixed to return playlists ---
def parse_run_args(argv):
    """
    Parse run arguments of the form:
      -albums{id1,id2,...}
      -single{id1,id2,...}
      -playlists{id1,id2,...}
    Returns three lists: (albums_list, singles_list, playlists_list).
    """
    joined = " ".join(argv or [])
    albums = []
    singles = []
    playlists = []

    for name, dest in (("playlists", playlists), ("albums", albums), ("single", singles)):
        matches = re.findall(fr"-{name}\{{([^}}]*)\}}", joined, flags=re.IGNORECASE)
        for m in matches:
            parts = [p.strip() for p in re.split(r"[,\s]+", m) if p.strip()]
            dest.extend(parts)

    return albums, singles, playlists

def db_hook(info_dict):
    if info_dict.get("status") == "finished":
        info = info_dict.get("info_dict")
        filename = os.path.splitext(info_dict.get("_filename") or info_dict.get("filename"))[0] + ".mp3"

        if info and filename:
            save_song(info, filename)
            print(f"[DB] Saved: {info.get('title')}")


# --- main ---
if __name__ == "__main__":
    print("Initializing Database...")
    init_db()
    print("Downloading Begun")

    # parse command-line arguments
    albums_arg, singles_arg, playlists_arg = parse_run_args(sys.argv[1:])

    albums_url = albums_arg
    singles_url = singles_arg
    playlists_url = playlists_arg

    # download albums as playlists
    if albums_url:
        download(albums_url, download_playlist)
    if playlists_url:
        download(playlists_url, download_playlist)
    if singles_url:
        download(singles_url, download_single)