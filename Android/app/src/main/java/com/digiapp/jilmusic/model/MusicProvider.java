/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.digiapp.jilmusic.model;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.digiapp.jilmusic.R;

import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.beans.MusicTrack;
import com.digiapp.jilmusic.beans.SelectionList;
import com.digiapp.jilmusic.dao.MusicRoomDatabase;
import com.digiapp.jilmusic.dao.MusicTrackDAO;
import com.digiapp.jilmusic.dao.SelectionsDAO;
import com.digiapp.jilmusic.utils.LogHelper;
import com.digiapp.jilmusic.utils.MediaIDHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.digiapp.jilmusic.utils.MediaIDHelper.MEDIA_ID_FAVORITE;
import static com.digiapp.jilmusic.utils.MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE;
import static com.digiapp.jilmusic.utils.MediaIDHelper.MEDIA_ID_MUSICS_BY_SELECTION;
import static com.digiapp.jilmusic.utils.MediaIDHelper.MEDIA_ID_ROOT;
import static com.digiapp.jilmusic.utils.MediaIDHelper.createMediaID;

/**
 * Simple data provider for music tracks. The actual metadata source is delegated to a
 * MusicProviderSource defined by a constructor argument of this class.
 */
public class MusicProvider {

    private static final String TAG = LogHelper.makeLogTag(MusicProvider.class);

    private MusicProviderSource mSource;

    // Categorized caches for music track data:
    public ConcurrentMap<String, List<MusicTrack>> mMusicListByGenre;
    private ConcurrentMap<SelectionList, List<MusicTrack>> mMusicListBySelection;

    private final ConcurrentMap<Integer, MusicTrack> mMusicListById;
    private final ConcurrentMap<Integer, MusicTrack> mFavoriteMusicListById;

    private final Set<String> mFavoriteTracks; // not used

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    private volatile State mCurrentState = State.NON_INITIALIZED;

    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }

    public MusicProvider() {
        this(new RemoteJSONSource());
    }

    public MusicProvider(MusicProviderSource source) {
        mSource = source;
        mMusicListByGenre = new ConcurrentHashMap<>();
        mMusicListById = new ConcurrentHashMap<>();
        mFavoriteMusicListById = new ConcurrentHashMap<>();
        mFavoriteTracks = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    }

    /**
     * Get an iterator over the list of genres
     *
     * @return genres
     */
    public Iterable<String> getGenres() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByGenre.keySet();
    }

    /**
     * Get an iterator over the list of Selections
     *
     * @return Selections
     */
    public Iterable<SelectionList> getSelections() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }

        if (mMusicListBySelection == null) {
            return Collections.emptyList();
        }
        return mMusicListBySelection.keySet();
    }

    /**
     * Get an iterator over a shuffled collection of all songs
     */
    public Iterable<MusicTrack> getShuffledMusic() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }

        List<MusicTrack> shuffled = new ArrayList<>(mMusicListById.size());

        for (MusicTrack mutableMetadata : mMusicListById.values()) {
            shuffled.add(mutableMetadata);
        }
        // Collections.shuffle(shuffled);

        return shuffled;
    }

    /**
     * Get an iterator over a shuffled collection of all songs
     */
    public Iterable<MusicTrack> getShuffledFavoriteMusic() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }

        List<MusicTrack> shuffled = new ArrayList<>(mFavoriteMusicListById.size());

        for (MusicTrack mutableMetadata : mFavoriteMusicListById.values()) {
            shuffled.add(mutableMetadata);
        }
        // Collections.shuffle(shuffled); FIXME - remove this

        return shuffled;
    }

    /**
     * Get music tracks of the given genre
     */
    public List<MusicTrack> getMusicsByGenre(String genre) {

        if (mCurrentState != State.INITIALIZED || !mMusicListByGenre.containsKey(genre)) {
            return Collections.emptyList();
        }

        return mMusicListByGenre.get(genre);
    }

    /**
     * Get music tracks of the given genre
     */
    public List<MusicTrack> getMusicsBySelection(SelectionList selectionList) {

        if (mCurrentState != State.INITIALIZED || !mMusicListBySelection.containsKey(selectionList)) {
            return Collections.emptyList();
        }

        return mMusicListBySelection.get(selectionList);
    }

    /**
     * Get music tracks of the given genre
     */
    public List<MediaMetadataCompat> getMusicsByGenrePrepared(String genre) {

        if (mCurrentState != State.INITIALIZED || !mMusicListByGenre.containsKey(genre)) {
            return Collections.emptyList();
        }
        List<MusicTrack> tmpList = getMusicsByGenre(genre);

        Collections.sort(tmpList, new Comparator<MusicTrack>() {
            @Override
            public int compare(MusicTrack one, MusicTrack another) {
                int returnVal = 0;

                if (one.id < another.id) {
                    returnVal = -1;
                } else if (one.id > another.id) {
                    returnVal = 1;
                } else if (one.id == another.id) {
                    returnVal = 0;
                }
                return returnVal;
            }
        });

        ArrayList<MediaMetadataCompat> result = new ArrayList<>();
        for (MusicTrack musicTrack : tmpList) {
            result.add(musicTrack.item);
        }

        return result;
    }

    public List<MediaMetadataCompat> getMusicsFavoriteForQueue(String id) {

        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }

        // here we need to generate queue based on selected position
        ArrayList<MediaMetadataCompat> result = new ArrayList<>();
        result.add(mFavoriteMusicListById.get(Integer.parseInt(id)).item);

        ArrayList<MediaMetadataCompat> prevItems = new ArrayList<>();
        boolean addToNext = false;
        for (MusicTrack mutableMetadata : mFavoriteMusicListById.values()) {
            if (mutableMetadata.id == Integer.parseInt(id)) {
                addToNext = true;
            } else {
                if (addToNext) {
                    result.add(mutableMetadata.item);
                } else {
                    prevItems.add(mutableMetadata.item);
                }
            }
        }

        result.addAll(prevItems);
        /*for (MusicTrack mutableMetadata : mFavoriteMusicListById.values()) {
            result.add(mutableMetadata.item);
        }*/

        return result;
    }

    /*public List<MediaMetadataCompat> getMusicsFavorite(String category) {

        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }

        ArrayList<MediaMetadataCompat> result = new ArrayList<>();
        for (MusicTrack mutableMetadata : mFavoriteMusicListById.values()) {
            result.add(mutableMetadata.item);
        }

        return result;
    }

    public List<MediaMetadataCompat> getMusicsBySelectionPrepared(String categoryValue) {

        if (categoryValue.split("/").length != 2) {
            return new ArrayList<>();
        }

        long selId = Long.parseLong(categoryValue.split("/")[0]);

        SelectionsDAO selectionsDAO = MusicRoomDatabase.getDatabase(AppObj.getGlobalContext()).selectionsDAO();
        SelectionList selectionList = selectionsDAO.getById(selId);

        if (mCurrentState != State.INITIALIZED || !mMusicListBySelection.containsKey(selectionList)) {
            return Collections.emptyList();
        }

        List<MusicTrack> tmpList = getMusicsBySelection(selectionList);
        ArrayList<MediaMetadataCompat> result = new ArrayList<>();
        for (MusicTrack musicTrack : tmpList) {
            result.add(musicTrack.item);
        }

        return result;
    }*/

    public List<MediaMetadataCompat> getMusicsBySelectionPrepared(long selId) {

        SelectionsDAO selectionsDAO = MusicRoomDatabase.getDatabase(AppObj.getGlobalContext()).selectionsDAO();
        SelectionList selectionList = selectionsDAO.getById(selId);

        if (mMusicListBySelection == null
                || selectionList == null) {
            return new ArrayList<>();
        }

        if (mCurrentState != State.INITIALIZED || !mMusicListBySelection.containsKey(selectionList)) {
            return Collections.emptyList();
        }

        List<MusicTrack> tmpList = getMusicsBySelection(selectionList);
        ArrayList<MediaMetadataCompat> result = new ArrayList<>();
        for (MusicTrack musicTrack : tmpList) {
            result.add(musicTrack.item);
        }

        return result;
    }

    /**
     * Very basic implementation of a search that filter music tracks with title containing
     * the given query.
     */
    public List<MediaMetadataCompat> searchMusicBySongTitle(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_TITLE, query);
    }

    /**
     * Very basic implementation of a search that filter music tracks with album containing
     * the given query.
     */
    public List<MediaMetadataCompat> searchMusicByAlbum(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_ALBUM, query);
    }

    /**
     * Very basic implementation of a search that filter music tracks with artist containing
     * the given query.
     */
    public List<MediaMetadataCompat> searchMusicByArtist(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_ARTIST, query);
    }

    /**
     * Very basic implementation of a search that filter music tracks with a genre containing
     * the given query.
     */
    public List<MediaMetadataCompat> searchMusicByGenre(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_GENRE, query);
    }

    private List<MediaMetadataCompat> searchMusic(String metadataField, String query) {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        ArrayList<MediaMetadataCompat> result = new ArrayList<>();
        query = query.toLowerCase(Locale.US);

        // TODO fix this
        /*for (MutableMediaMetadata track : mMusicListById.values()) {
            if (track.metadata.getString(metadataField).toLowerCase(Locale.US)
                    .contains(query)) {
                result.add(track.metadata);
            }
        }
        */

        return result;
    }


    /**
     * Return the MediaMetadataCompat for the given musicID.
     *
     * @param musicId The unique, non-hierarchical music ID.
     */
    public MusicTrack getMusic(int musicId) {
        return mMusicListById.containsKey(musicId) ? mMusicListById.get(musicId) : null;
    }

    public MusicTrack getMusic(String musicId) {
        return mMusicListById.containsKey(Integer.parseInt(musicId)) ? mMusicListById.get(Integer.parseInt(musicId)) : null;
    }

    public synchronized void updateMusicArt(String musicId, Bitmap albumArt, Bitmap icon) {

        // TODO fix this

        /*MediaMetadataCompat metadata = getMusic(musicId);
        metadata = new MediaMetadataCompat.Builder(metadata)

                // set high resolution bitmap in METADATA_KEY_ALBUM_ART. This is used, for
                // example, on the lockscreen background when the media session is active.
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)

                // set small version of the album art in the DISPLAY_ICON. This is used on
                // the MediaDescription and thus it should be small to be serialized if
                // necessary
                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, icon)

                .build();

        MutableMediaMetadata mutableMetadata = mMusicListById.get(musicId);
        if (mutableMetadata == null) {
            throw new IllegalStateException("Unexpected error: Inconsistent data structures in " +
                    "MusicProvider");
        }

        mutableMetadata.metadata = metadata;*/
    }

    public void setFavorite(String musicId, boolean favorite) {
        if (favorite) {
            mFavoriteTracks.add(musicId);
        } else {
            mFavoriteTracks.remove(musicId);
        }
    }

    public boolean isInitialized() {
        return mCurrentState == State.INITIALIZED;
    }

    public boolean isFavorite(String musicId) {
        return mFavoriteTracks.contains(musicId);
    }

    /**
     * Get the list of music tracks from a server and caches the track information
     * for future reference, keying tracks by musicId and grouping by genre.
     */
    public void retrieveMediaAsync(final Callback callback) {
        LogHelper.d(TAG, "retrieveMediaAsync called");
        /*if (mCurrentState == State.INITIALIZED) {
            if (callback != null) {
                // Nothing to do, execute callback immediately
                callback.onMusicCatalogReady(true);
            }
            return;
        }*/

        // Asynchronously load the music catalog in a separate thread
        new AsyncTask<Void, Void, State>() {
            @Override
            protected State doInBackground(Void... params) {
                retrieveMedia();
                return mCurrentState;
            }

            @Override
            protected void onPostExecute(State current) {
                if (callback != null) {
                    try {
                        callback.onMusicCatalogReady(current == State.INITIALIZED);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }.execute();
    }

    private synchronized void buildListBySelection() {

        ConcurrentMap<SelectionList, List<MusicTrack>> newMusicListBySelection = new ConcurrentHashMap<>();

        SelectionsDAO selectionsDAO = MusicRoomDatabase.getDatabase(AppObj.getGlobalContext()).selectionsDAO();
        List<SelectionList> selectionLists = selectionsDAO.getAllSelections();

        for (SelectionList sel : selectionLists) {

            ArrayList<MusicTrack> musicTracks = new ArrayList<>();

            List<SelectionList> childList = selectionsDAO.getByParent(sel.id);
            MusicTrackDAO musicTrackDAO = MusicRoomDatabase.getDatabase(AppObj.getGlobalContext()).musicTrackDAO();
            for (SelectionList child : childList) {
                try {
                    MusicTrack musicTrack = musicTrackDAO.getById(child.track_id);

                    if (musicTrack != null) {
                        musicTrack.item = RemoteJSONSource.buildFromFile(musicTrack.localPath, musicTrack);
                        musicTracks.add(musicTrack);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    continue;
                }
            }

            newMusicListBySelection.put(sel, musicTracks);
        }

        mMusicListBySelection = newMusicListBySelection;
    }

    private synchronized void buildListsByGenre() {
        ConcurrentMap<String, List<MusicTrack>> newMusicListByGenre = new ConcurrentHashMap<>();

        for (MusicTrack m : mMusicListById.values()) {

            MediaMetadataCompat mediaData = ((MediaMetadataCompat) m.item);
            if (mediaData == null) {
                continue;
            }
            String genre = mediaData.getString(MediaMetadataCompat.METADATA_KEY_GENRE);
            if (genre == null) {
                genre = "";
            }

            List<MusicTrack> list = newMusicListByGenre.get(genre);
            if (list == null) {
                list = new ArrayList<>();
                newMusicListByGenre.put(genre, list);
            }

            list.add(m);
        }
        mMusicListByGenre = newMusicListByGenre;
    }

    private synchronized void retrieveMedia() {
        try {
            // if (mCurrentState == State.NON_INITIALIZED) {
            mCurrentState = State.INITIALIZING;

            if (mFavoriteMusicListById != null) {
                mFavoriteMusicListById.clear();
            }

            if (mMusicListById != null) {
                mMusicListById.clear();
            }

            MusicTrackDAO musicTrackDAO = MusicRoomDatabase.getDatabase(AppObj.getGlobalContext()).musicTrackDAO();
            List<MusicTrack> list = musicTrackDAO.getAllMusicTracks();
            for (MusicTrack musicTrack : list) {

                if (!com.digiapp.jilmusic.utils.FileUtils.isExists(musicTrack.localPath)) {
                    continue;
                }

                try {
                    musicTrack.item = RemoteJSONSource.buildFromFile(musicTrack.localPath, musicTrack);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    continue;
                }

                try {
                    mMusicListById.put(musicTrack.id, musicTrack);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    continue;
                }

                // adding favorite music as well
                if (musicTrack.favorite) {
                    try {
                        mFavoriteMusicListById.put(musicTrack.id, musicTrack);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        continue;
                    }
                }
            }

            buildListsByGenre();
            buildListBySelection();

            mCurrentState = State.INITIALIZED;
            //  }
        } finally {
            if (mCurrentState != State.INITIALIZED) {
                // Something bad happened, so we reset state to NON_INITIALIZED to allow
                // retries (eg if the network connection is temporary unavailable)
                mCurrentState = State.NON_INITIALIZED;
            }
        }
    }

    /*public List<MusicTrack> getChildren(String mediaId, Resources resources) {
        List<MusicTrack> mediaItems = new ArrayList<>();

       *//* if (!MediaIDHelper.isBrowseable(mediaId)) {
            return mediaItems;
        }*//*

        if (MEDIA_ID_ROOT.equals(mediaId)) {
            // mediaItems.add(createBrowsableMediaItemForRoot(resources));
            for (MusicTrack metadata : getShuffledMusic()) {
                mediaItems.add(metadata);
            }

        }
        *//*
        else if (MEDIA_ID_MUSICS_BY_GENRE.equals(mediaId)) {
            for (String genre : getGenres()) {
                // mediaItems.add(createBrowsableMediaItemForGenre(genre, resources));
            }

            // FIXME Not possible in current app, but better to fix

        } else if (mediaId.startsWith(MEDIA_ID_MUSICS_BY_GENRE)) {
            String genre = MediaIDHelper.getHierarchy(mediaId)[1];
            for (MediaMetadataCompat metadata : getMusicsByGenre(genre)) {
                mediaItems.add(createMediaItem(metadata));
            }*//*

         else {
            LogHelper.w(TAG, "Skipping unmatched mediaId: ", mediaId);
        }
        return mediaItems;
    }*/

    public List<MediaBrowserCompat.MediaItem> getChildrenPrepared(String mediaId, Resources resources) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        if (!MediaIDHelper.isBrowseable(mediaId)) {
            return mediaItems;
        }

        if (MEDIA_ID_ROOT.equals(mediaId)) {
            for (MusicTrack metadata : getShuffledMusic()) {
                mediaItems.add(createMediaItem(metadata.item));
            }
        } else if (MEDIA_ID_FAVORITE.equals(mediaId)) { // getting favorite here
            for (MusicTrack metadata : getShuffledFavoriteMusic()) {
                mediaItems.add(createMediaItem(metadata.item, MEDIA_ID_FAVORITE));
            }
        } else if (mediaId.equalsIgnoreCase(MEDIA_ID_MUSICS_BY_SELECTION)) { // means root of all selections

            Iterator<SelectionList> iterator = getSelections().iterator();
            while (iterator.hasNext()) {
                SelectionList selectionList = iterator.next();
                mediaItems.add(createBrowsableMediaItemForSelection(selectionList, resources));
            }

        } else if (mediaId.startsWith(MEDIA_ID_MUSICS_BY_SELECTION)) {// means child
            if (MediaIDHelper.getHierarchy(mediaId).length > 1) {

                String selectionId = MediaIDHelper.getHierarchy(mediaId)[1];
                for (MediaMetadataCompat metadata : getMusicsBySelectionPrepared(Long.parseLong(selectionId))) {
                    mediaItems.add(createMediaItem(metadata, MEDIA_ID_MUSICS_BY_SELECTION + "/" + selectionId));
                }

            }

        } else {
            LogHelper.w(TAG, "Skipping unmatched mediaId: ", mediaId);
        }
        return mediaItems;
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForRoot(Resources resources) {
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MEDIA_ID_MUSICS_BY_GENRE)
                .setTitle(resources.getString(R.string.browse_genres))
                .setSubtitle(resources.getString(R.string.browse_genre_subtitle))
                .setIconUri(Uri.parse("android.resource://" +
                        "com.example.android.uamp/drawable/ic_by_genre"))
                .build();
        return new MediaBrowserCompat.MediaItem(description,
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForSelection(SelectionList selectionList,
                                                                              Resources resources) {
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(createMediaID(null, MEDIA_ID_MUSICS_BY_SELECTION, String.valueOf(selectionList.id)))
                .setTitle(selectionList.name)
                .setSubtitle("")
                .build();
        return new MediaBrowserCompat.MediaItem(description,
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForGenre(String genre,
                                                                          Resources resources) {
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(createMediaID(null, MEDIA_ID_MUSICS_BY_GENRE, genre))
                .setTitle(genre)
                .setSubtitle(resources.getString(
                        R.string.browse_musics_by_genre_subtitle, genre))
                .build();
        return new MediaBrowserCompat.MediaItem(description,
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    public static MediaBrowserCompat.MediaItem createMediaItem(MediaMetadataCompat metadata) {
        // Since mediaMetadata fields are immutable, we need to create a copy, so we
        // can set a hierarchy-aware mediaID. We will need to know the media hierarchy
        // when we get a onPlayFromMusicID call, so we can create the proper queue based
        // on where the music was selected from (by artist, by genre, random, etc)
        String genre = metadata.getString(MediaMetadataCompat.METADATA_KEY_GENRE) == null ? "genre" : metadata.getString(MediaMetadataCompat.METADATA_KEY_GENRE);
        String hierarchyAwareMediaID = createMediaID(
                metadata.getDescription().getMediaId(), MEDIA_ID_MUSICS_BY_GENRE, genre);
        MediaMetadataCompat copy = new MediaMetadataCompat.Builder(metadata)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, hierarchyAwareMediaID)
                .build();
        return new MediaBrowserCompat.MediaItem(copy.getDescription(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);

    }

    public static MediaBrowserCompat.MediaItem createMediaItem(MediaMetadataCompat metadata, String hierarchy) {
        // Since mediaMetadata fields are immutable, we need to create a copy, so we
        // can set a hierarchy-aware mediaID. We will need to know the media hierarchy
        // when we get a onPlayFromMusicID call, so we can create the proper queue based
        // on where the music was selected from (by artist, by genre, random, etc)
        String hierarchyAwareMediaID = createMediaID(
                metadata.getDescription().getMediaId(), hierarchy);

        MediaMetadataCompat copy = new MediaMetadataCompat.Builder(metadata)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, hierarchyAwareMediaID)
                .build();
        return new MediaBrowserCompat.MediaItem(copy.getDescription(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);

    }

}
