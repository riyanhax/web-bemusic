package com.digiapp.jilmusic.beans;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.digiapp.jilmusic.dao.Converter;

@Entity(tableName = "music_track")
public class MusicTrack implements Parcelable {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "album_name")
    public String album_name;

    @ColumnInfo(name = "number")
    public int number;

    @ColumnInfo(name = "duration")
    public long duration;

    @TypeConverters(Converter.class)
    @ColumnInfo(name = "artists")
    public String[] artists;

    @ColumnInfo(name = "youtube_id")
    public String youtube_id;

    @ColumnInfo(name = "spotify_popularity")
    public String spotify_popularity;

    @ColumnInfo(name = "album_id")
    public int album_id;

    @ColumnInfo(name = "url")
    public String url;

    @ColumnInfo(name = "plays")
    public int plays;

    @TypeConverters(Converter.class)
    @ColumnInfo(name = "album")
    public AlbumTrack album;

    @ColumnInfo(name = "favorite")
    public boolean favorite;

    @ColumnInfo(name = "localPath")
    public String localPath;

    @ColumnInfo(name = "localPicturePath")
    public String localPicturePath;

    @ColumnInfo(name = "isVideo")
    public boolean isVideo;

    @Ignore
    public MediaMetadataCompat item;

    @Ignore
    public MediaBrowserCompat.MediaItem mediaItem;

    @Ignore
    public boolean isSelected = false;

    public MusicTrack(){
    }

    protected MusicTrack(Parcel in) {
        id = in.readInt();
        name = in.readString();
        album_name = in.readString();
        number = in.readInt();
        duration = in.readLong();
        artists = in.createStringArray();
        youtube_id = in.readString();
        spotify_popularity = in.readString();
        album_id = in.readInt();
        url = in.readString();
        plays = in.readInt();
        favorite = in.readByte() != 0;
        localPath = in.readString();
        localPicturePath = in.readString();
        isVideo = in.readByte() != 0;
        item = in.readParcelable(MediaMetadataCompat.class.getClassLoader());
        mediaItem = in.readParcelable(MediaBrowserCompat.MediaItem.class.getClassLoader());
    }

    public static final Creator<MusicTrack> CREATOR = new Creator<MusicTrack>() {
        @Override
        public MusicTrack createFromParcel(Parcel in) {
            return new MusicTrack(in);
        }

        @Override
        public MusicTrack[] newArray(int size) {
            return new MusicTrack[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(album_name);
        dest.writeInt(number);
        dest.writeLong(duration);
        dest.writeStringArray(artists);
        dest.writeString(youtube_id);
        dest.writeString(spotify_popularity);
        dest.writeInt(album_id);
        dest.writeString(url);
        dest.writeInt(plays);
        dest.writeByte((byte) (favorite ? 1 : 0));
        dest.writeString(localPath);
        dest.writeString(localPicturePath);
        dest.writeByte((byte) (isVideo ? 1 : 0));
        dest.writeParcelable(item, flags);
        dest.writeParcelable(mediaItem,flags);
    }

    public boolean isAudio(){
        return localPath!=null && (localPath.endsWith("m4a") || localPath.endsWith("3gp"));
    }
}
