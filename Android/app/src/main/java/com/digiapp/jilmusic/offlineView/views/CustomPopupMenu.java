package com.digiapp.jilmusic.offlineView.views;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.MenuRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.digiapp.jilmusic.R;

import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.beans.MusicTrack;
import com.digiapp.jilmusic.beans.SelectionList;
import com.digiapp.jilmusic.dao.MusicRoomDatabase;
import com.digiapp.jilmusic.dao.SelectionsDAO;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import static android.view.View.GONE;

public class CustomPopupMenu {

    View mAncorView;
    Context mContext;
    PopupWindow mPopupWindow;
    View popupView,topView;
    MusicTrack mMusicTrack;
    RecyclerView recyclerView;
    OnItemClickListener clickListener;
    View viewAddSelection;
    @MenuRes int mMainMenu;
    ImageView separator;

    public static final int ADD_NEW_SELECTION = 993;

    public interface OnItemClickListener {
        public void onItemClick(int position);

        public void onItemClick(MenuItem menuItem);
    }

    public static Menu newMenuInstance(Context context) {
        try {
            Class<?> menuBuilderClass = Class.forName("com.android.internal.view.menu.MenuBuilder");
            Constructor<?> constructor = menuBuilderClass.getDeclaredConstructor(Context.class);
            return (Menu) constructor.newInstance(context);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public CustomPopupMenu(View ancorView, MusicTrack musicTrack, Context context, @MenuRes int menuId) {
        mAncorView = ancorView;
        mContext = context;
        mMusicTrack = musicTrack;
        mMainMenu = menuId;

        mPopupWindow = new PopupWindow(AppObj.getGlobalContext());
        LayoutInflater inflater = (LayoutInflater)
                AppObj.getGlobalContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.item_popup_menu, null);
        topView = popupView.findViewById(R.id.topView);
        separator = popupView.findViewById(R.id.separator);

        if (musicTrack == null) {
            topView.setVisibility(GONE);
            separator.setVisibility(GONE);
        } else {
            topView.setVisibility(View.VISIBLE);

            SimpleDraweeView simpleDraweeView = popupView.findViewById(R.id.profilePic);
            if(musicTrack.localPicturePath!=null) {
                simpleDraweeView.setImageURI(Uri.fromFile(new File(musicTrack.localPicturePath)));
            }
            TextView title = popupView.findViewById(R.id.title);
            title.setText(musicTrack.name);

            TextView description = popupView.findViewById(R.id.description);
            description.setText(musicTrack.album_name);
        }

        viewAddSelection = popupView.findViewById(R.id.viewAddSelection);
        viewAddSelection.setVisibility(GONE);

        TextView txtBack = popupView.findViewById(R.id.txtBack);
        txtBack.setOnClickListener(v -> {
            viewAddSelection.setVisibility(GONE);
            onBackButtonPressed();
        });
        Button btnAddSelection = popupView.findViewById(R.id.btnAddSelection);
        btnAddSelection.setOnClickListener(v -> showNewSelection());

        recyclerView = (RecyclerView) popupView.findViewById(R.id.menuList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AppObj.getGlobalContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        initMenuAdapter();
    }

    public static CustomPopupMenu getCustomPopupMenu(View ancorView, @MenuRes int menuId, Context context){
        return new CustomPopupMenu(ancorView, null, context, menuId);
    }

    public static CustomPopupMenu getCustomPopupMenu(View ancorView, MusicTrack musicTrack, Context context){
        return new CustomPopupMenu(ancorView, musicTrack, context, R.menu.menu_popup);
    }

    public void setClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    private void initMenuAdapter() {

        Menu menu = CustomPopupMenu.newMenuInstance(AppObj.getGlobalContext());
        new MenuInflater(AppObj.getGlobalContext()).inflate(mMainMenu, menu);

        ArrayList<MenuItem> menuList = new ArrayList<>();
        for (int i = 0; i < menu.size(); i++) {

            MenuItem item = menu.getItem(i);
            if (item.getItemId() == R.id.menu_favorite) {
                if (mMusicTrack.favorite) {
                    item.setTitle(mContext.getString(R.string.remove_fav));
                } else {
                    item.setTitle(mContext.getString(R.string.add_favorite));
                }
            }
            menuList.add(item);
        }

        PopupMenuAdapter popupMenuAdapter = new PopupMenuAdapter(menuList);
        recyclerView.setAdapter(popupMenuAdapter);

        popupMenuAdapter.setClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (clickListener != null) {
                    clickListener.onItemClick(position);
                }
            }

            @Override
            public void onItemClick(MenuItem menuItem) {
                if (clickListener != null) {
                    clickListener.onItemClick(menuItem);
                }
            }
        });
    }

    private void showNewSelection() {
        // Core.getAddNewSelectionDialog(mContext).show();
        if (clickListener != null) {
            clickListener.onItemClick(ADD_NEW_SELECTION);
        }
    }

    private void onBackButtonPressed() {
        initMenuAdapter();
    }

    public boolean isSelectionVisible() {
        return viewAddSelection.getVisibility() == View.VISIBLE;
    }

    public void showSelectionMenu() {

        viewAddSelection.setVisibility(View.VISIBLE);

        ArrayList<MenuItem> menuList = new ArrayList<>();
        Menu menu = CustomPopupMenu.newMenuInstance(AppObj.getGlobalContext());

        SelectionsDAO selectionsDAO = MusicRoomDatabase.getDatabase(AppObj.getGlobalContext()).selectionsDAO();
        for (SelectionList selectionList : selectionsDAO.getAllSelections()) {
            // menu.add(selectionList.name);
            MenuItem menuItem = menu.add(Menu.NONE, (int) selectionList.id, Menu.NONE, selectionList.name);
            menuItem.setTitleCondensed(String.valueOf(selectionList.id));
            // menuItem.setNumericShortcut((char) selectionList.id);
            menuList.add(menuItem);
        }
        PopupMenuAdapter popupMenuAdapter = new PopupMenuAdapter(menuList);
        recyclerView.setAdapter(popupMenuAdapter);

        popupMenuAdapter.setClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (clickListener != null) {
                    clickListener.onItemClick(position);
                }
            }

            @Override
            public void onItemClick(MenuItem menuItem) {
                if (clickListener != null) {
                    clickListener.onItemClick(menuItem);
                }
            }
        });
    }

    public void show() {
        if (mPopupWindow != null) {
            mPopupWindow.setContentView(popupView);
            mPopupWindow.setWidth(700);
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setTouchable(true);
            mPopupWindow.showAsDropDown(mAncorView);
        }
    }

    public void dismiss() {
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
    }

    public static class PopupMenuAdapter extends RecyclerView.Adapter<PopupMenuAdapter.ViewHolder> {

        ArrayList<MenuItem> planetList;

        OnItemClickListener clickListener;

        public void setClickListener(OnItemClickListener clickListener) {
            this.clickListener = clickListener;
        }

        public PopupMenuAdapter(ArrayList<MenuItem> planetList) {
            this.planetList = planetList;
        }

        @Override
        public PopupMenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_row, parent, false);
            PopupMenuAdapter.ViewHolder viewHolder = new PopupMenuAdapter.ViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(PopupMenuAdapter.ViewHolder holder, int position) {

            MenuItem menuItem = planetList.get(position);
            holder.text.setText(menuItem.getTitle());

            if (menuItem.getItemId() == R.id.menu_add_selection) {
                holder.image.setVisibility(View.VISIBLE);
            } else {
                holder.image.setVisibility(View.GONE);
            }

            if (clickListener != null) {
                holder.text.setOnClickListener(v -> {
                    clickListener.onItemClick(position);
                    clickListener.onItemClick(planetList.get(position));
                });
            }
        }

        @Override
        public int getItemCount() {
            return planetList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            protected ImageView image;
            protected TextView text;

            public ViewHolder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.rowImage);
                text = (TextView) itemView.findViewById(R.id.rowTitle);
            }
        }
    }
}
