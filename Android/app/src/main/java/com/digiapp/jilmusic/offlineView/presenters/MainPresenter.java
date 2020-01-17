package com.digiapp.jilmusic.offlineView.presenters;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;

import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.MusicService;
import com.digiapp.jilmusic.R;
import com.digiapp.jilmusic.beans.MusicTrack;
import com.digiapp.jilmusic.beans.SelectionList;
import com.digiapp.jilmusic.dao.MusicRoomDatabase;
import com.digiapp.jilmusic.dao.MusicTrackDAO;
import com.digiapp.jilmusic.dao.SelectionsDAO;
import com.digiapp.jilmusic.model.BasePresenter;
import com.digiapp.jilmusic.model.MusicProvider;
import com.digiapp.jilmusic.model.RemoteJSONSource;
import com.digiapp.jilmusic.offlineView.views.MainView;
import com.digiapp.jilmusic.utils.QueueHelper;
import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.digiapp.jilmusic.utils.MediaIDHelper.MEDIA_ID_FAVORITE;
import static com.digiapp.jilmusic.utils.MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE;
import static com.digiapp.jilmusic.utils.MediaIDHelper.MEDIA_ID_MUSICS_BY_SELECTION;

public class MainPresenter extends BasePresenter<List<MusicTrack>, MainView> {
    private boolean isLoadingData = false;

    public enum TYPE {
        FAVOURITES,
        ALL,
        SELECTIONS
    }

    @Override
    protected void updateView() {
        // Business logic is in the presenter
        if (model.size() == 0) {
            view().showEmpty();
        } else {
            view().showData(model);
        }
    }

    @Override
    public void bindView(@NonNull MainView view) {
        super.bindView(view);

        // Let's not reload data if it's already here
        if (model == null && !isLoadingData) {
            view().showLoading();
            loadData();
        }
    }

    public void loadData() {
        isLoadingData = true;
        new LoadDataTask(TYPE.ALL).execute();
    }

    public void loadFavouritesData() {
        isLoadingData = true;
        new LoadDataTask(TYPE.FAVOURITES).execute();
    }

    public void loadSelectionsData(int parentId) {
        isLoadingData = true;
        LoadDataTask loadDataTask = new LoadDataTask(TYPE.SELECTIONS);
        loadDataTask.setParentId(parentId);
        loadDataTask.execute();
    }

    public void loadSelectionsData(){
        isLoadingData = true;
        new LoadDataTask(TYPE.SELECTIONS).execute();
    }

    // It's OK for this class not to be static and to keep a reference to the Presenter, as this
    // is retained during orientation changes and is lightweight (has no activity/view reference)
    private class LoadDataTask extends AsyncTask<Void, Void, Void> {

        MusicTrackDAO musicTrackDAO = MusicRoomDatabase.getDatabase(AppObj.getGlobalContext()).musicTrackDAO();
        SelectionsDAO selectionsDAO = MusicRoomDatabase.getDatabase(AppObj.getGlobalContext()).selectionsDAO();

        TYPE mType;
        int parentId =-1;

        public LoadDataTask(TYPE type) {
            mType = type;
        }

        public void setParentId(int parentId) {
            this.parentId = parentId;
        }

        @Override
        protected Void doInBackground(Void... params) {
            model = new ArrayList<MusicTrack>();

            if(mType==TYPE.SELECTIONS){

                if(parentId == -1) {
                    List<SelectionList> lists = selectionsDAO.getAllSelections();
                    for (SelectionList list : lists) {
                        MusicTrack musicTrack = new MusicTrack();
                        musicTrack.id = Ints.checkedCast(list.id);
                        musicTrack.name = list.name;

                        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                                .setMediaId(MEDIA_ID_MUSICS_BY_GENRE)
                                .setTitle("browse")
                                .setSubtitle("browse subtitle")
                                .setIconUri(Uri.parse("android.resource://" +
                                        "com.example.android.uamp/drawable/ic_by_genre"))
                                .build();

                        musicTrack.mediaItem = new MediaBrowserCompat.MediaItem(description,
                                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
                        model.add(musicTrack);
                    }
                }else{
                    List<SelectionList> lists = selectionsDAO.getByParent(parentId);
                    for (SelectionList list : lists) {
                        MusicTrack musicTrack = musicTrackDAO.getById(list.track_id);
                        if(musicTrack==null){
                            continue;
                        }

                        MusicTrack track = prepareMusicTrack(musicTrack, MEDIA_ID_MUSICS_BY_SELECTION + "/" + parentId);
                        if (track != null) {
                            model.add(track);
                        }
                    }
                }
            }else {
                List<MusicTrack> list = new ArrayList<>();
                if (mType == TYPE.ALL) {
                    list = musicTrackDAO.getAllMusicTracks();
                } else if (mType == TYPE.FAVOURITES) {
                    list = musicTrackDAO.getAllFavorite();
                }

                for (MusicTrack musicTrack : list) {
                    MusicTrack track = null;
                    if(mType == TYPE.FAVOURITES) {
                        track = prepareMusicTrack(musicTrack, MEDIA_ID_FAVORITE);
                    }else{
                        track = prepareMusicTrack(musicTrack,null);
                    }
                    if (track != null) {
                        model.add(track);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            isLoadingData = false;

            // updating media queue
            if(mType == TYPE.ALL){
                QueueHelper.updateQueue("ALL_GENRE",model);
            }

            updateView();
        }

        protected MusicTrack prepareMusicTrack(@NonNull MusicTrack musicTrack,@Nullable String hierarhy) {
            if (!com.digiapp.jilmusic.utils.FileUtils.isExists(musicTrack.localPath)) {
                return null;
            }
            try {
                musicTrack.item = RemoteJSONSource.buildFromFile(musicTrack.localPath, musicTrack);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }

            if(hierarhy!=null) {
                musicTrack.mediaItem = MusicProvider.createMediaItem(musicTrack.item, hierarhy);
            }else{
                musicTrack.mediaItem = MusicProvider.createMediaItem(musicTrack.item);
            }

            return musicTrack;
        }
    }
}