package com.digiapp.jilmusic.offlineView.views;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.R;
import com.digiapp.jilmusic.model.MvpViewHolder;
import com.digiapp.jilmusic.offlineView.presenters.MusicTrackViewPresenter;
import com.digiapp.jilmusic.utils.Core;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Optional;

public class MusicTrackViewHolder extends MvpViewHolder<MusicTrackViewPresenter> implements MediaItemView {

    public static final int STATE_INVALID = -1;
    public static final int STATE_NONE = 0;
    public static final int STATE_PLAYABLE = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_PLAYING = 3;

    private static ColorStateList sColorStatePlaying;
    private static ColorStateList sColorStateNotPlaying;

    @Nullable @BindView(R.id.chkSelected)
    CheckBox chkSelected;
    @Nullable @BindView(R.id.play_eq)
    SimpleDraweeView play_eq;
    @BindView(R.id.title)
    TextView title;
    @Nullable @BindView(R.id.description)
    TextView description;
    @Nullable @BindView(R.id.imgFavorite)
    ImageView imgFavorite;
    @Nullable @BindView(R.id.imgMenu)
    ImageView imgMenu;

    private Context mContext;
    private Handler mHandler = new Handler(Looper.myLooper());
    private ItemClickListener itemClickListener;

    public MusicTrackViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        chkSelected.setOnCheckedChangeListener((buttonView, isChecked) -> presenter.setSelected(isChecked));
        if(imgFavorite!=null) {
            imgFavorite.setOnClickListener(v -> presenter.onFavouritesButtonClicked());
        }
        imgMenu.setOnClickListener(v -> presenter.onMenuClicked());
        imgMenu.setFocusable(true);
        itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClicked(getAdapterPosition());
            }
        });

        itemView.setOnLongClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onLongItemClicked(getAdapterPosition());
            }
            return false;
        });

        if (sColorStateNotPlaying == null || sColorStatePlaying == null) {
            initializeColorStateLists(AppObj.getGlobalContext());
        }
    }

    private void initializeColorStateLists(Context ctx) {
        if(ctx!=null) {

            int currentNightMode =  ctx.getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;

            switch (currentNightMode) {
                case Configuration.UI_MODE_NIGHT_NO:
                    sColorStateNotPlaying = ColorStateList.valueOf(ctx.getResources().getColor(
                            R.color.media_item_icon_not_playing));
                case Configuration.UI_MODE_NIGHT_YES:
                    sColorStateNotPlaying = ColorStateList.valueOf(ctx.getResources().getColor(
                            R.color.white));
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    sColorStateNotPlaying = ColorStateList.valueOf(ctx.getResources().getColor(
                            R.color.media_item_icon_not_playing));
            }

            sColorStatePlaying = ColorStateList.valueOf(ctx.getResources().getColor(
                    R.color.media_item_icon_playing));
        }
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;

        if (sColorStateNotPlaying == null || sColorStatePlaying == null) {
            initializeColorStateLists(mContext);
        }
    }

    public Context getContext() {
        return mContext;
    }

    public ImageView getImgMenu() {
        return imgMenu;
    }

    @Override
    public void setChecked(boolean checked) {
        chkSelected.setChecked(checked);
    }

    @Override
    public void setCheckedVisible(boolean visible) {
        chkSelected.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setTitle(String value) {
        title.setText(value);
    }

    @Override
    public void setDescription(String value) {
        description.setText(value);
    }

    @Override
    public void setFavourite(boolean value) {
        if (value) {
            imgFavorite.setImageDrawable(AppObj.getGlobalContext().getResources().getDrawable(R.drawable.baseline_favorite_white_18));
        } else {
            imgFavorite.setImageDrawable(AppObj.getGlobalContext().getResources().getDrawable(R.drawable.baseline_favorite_border_white_18));
        }
    }

    @Override
    public void setFavouriteVisible(boolean visible) {
        if(imgFavorite!=null) {
            imgFavorite.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setImageURI(String path) {
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.fromFile(new File(path))).build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(imageRequest)
                .setAutoPlayAnimations(true)
                .build();
        play_eq.setController(controller);
    }

    @Override
    public void setItemPlaying(boolean playing) {

        initializeColorStateLists(getContext());

        if (playing) {
            title.setTextColor(sColorStatePlaying.getDefaultColor());
            description.setTextColor(sColorStatePlaying.getDefaultColor());
        } else {
            title.setTextColor(sColorStateNotPlaying.getDefaultColor());
            description.setTextColor(sColorStateNotPlaying.getDefaultColor());
        }
    }

    @Override
    public void confirmUserDelete() {
        MaterialDialog.Builder builder = Core.getDialogBuilder(mContext)
                .title(R.string.text_are_u_sure)
                .positiveText(R.string.text_yes)
                .negativeText(R.string.text_cancel)
                .onPositive((dialog, which) -> {
                    presenter.deleteModel();
                });
        builder.show();
    }

    @Override
    public void showMessage(String message) {
        mHandler.post(() -> {
            if (mContext instanceof Activity) {
                View parentLayout = ((Activity) mContext).findViewById(android.R.id.content);
                Snackbar.make(parentLayout, message, Snackbar.LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setMenuVisible(boolean visible) {
        if(imgMenu!=null) {
            imgMenu.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public interface ItemClickListener {
        void onItemClicked(int pos);

        void onLongItemClicked(int pos);
    }

}
