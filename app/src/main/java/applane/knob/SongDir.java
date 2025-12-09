package applane.knob;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.security.SecureRandom;
import java.util.ArrayList;

public class SongDir
{
    private static Cursor cursor;
    private static int songId = -1;
    public static synchronized boolean init(Context ctx)
    {
        songId = -1;
        songList.clear();
        cursor = null;

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0 AND " +
                MediaStore.Audio.Media.DATA + " LIKE ?";

        ContentResolver contentResolver = ctx.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        cursor = contentResolver.query(uri, projection, selection,
                new String[]{ sp.cardPrefix(ctx)  +"%" }, null);
        if (cursor != null)
        {
            initSongList();
            nextSong(ctx);
        }
        return hasSongs();
    }

    public static synchronized boolean initCard(Context ctx, String cardPrefix)
    {
        sp.cardPrefix(ctx, cardPrefix);
        return init(ctx);
    }

    public static synchronized boolean hasSongs()
    {
        return (cursor != null && cursor.getCount() > 0);
    }

    public static synchronized void nextSong(Context ctx)
    {
        if (!hasSongs()) return;
        songId = nextSongId();
    }

    public static synchronized Song current(Context ctx)
    {
        if (!hasSongs()) return null;

        if (songId >=0 && cursor.moveToPosition(songId))
        {
            Song s = new Song();
            s.file = getColumn(MediaStore.Audio.Media.DATA);
            s.title = getColumn(MediaStore.Audio.Media.TITLE);
            s.artist = getColumn(MediaStore.Audio.Media.ARTIST);
            return s;
        }
        return null;
    }

    private static String getColumn(String id)
    {
        int index = cursor.getColumnIndex(id);
        if (index < 0) return "";
        return cursor.getString(index);
    }

    private static String getAlbumFile(Context ctx, String albumId)
    {
        Cursor albums = ctx.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID+ "=?",
                new String[] {String.valueOf(albumId)},
                null);

        if (albums != null && albums.moveToFirst()) {
            int index = albums.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
            if (index >= 0)
            {
                String file = albums.getString(index);
                if (file != null) return file;
            }
        }
        return "";
    }

    // SONG QUEUE
    private static ArrayList<Integer> songList = new ArrayList<>();
    private static SecureRandom random = new SecureRandom();

    private static void initSongList()
    {
        if (!hasSongs()) return;
        songList.clear();
        songList.ensureCapacity(totalSongCount());
        for (int i = 0; i < totalSongCount(); i++)
            songList.add(i, i);
    }

    private static int nextSongId()
    {
        if (!hasSongs()) return -1;
        if (songList.size() == 0)
            initSongList();
        return songList.remove(random.nextInt(songList.size()));
    }

    private static int totalSongCount() { return cursor.getCount(); }
}
