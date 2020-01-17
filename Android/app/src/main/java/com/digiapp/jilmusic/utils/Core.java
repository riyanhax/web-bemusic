package com.digiapp.jilmusic.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mukesh.tinydb.TinyDB;
import com.digiapp.jilmusic.R;

import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.api.ApiClient;
import com.digiapp.jilmusic.api.ApiInterface;
import com.digiapp.jilmusic.api.beans.UserInfo;
import com.digiapp.jilmusic.beans.SelectionList;
import com.digiapp.jilmusic.dao.MusicRoomDatabase;
import com.digiapp.jilmusic.dao.SelectionsDAO;

import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Core {

    public static String PARAM_USER = "PARAM_USER";
    public static String PARAM_LOGGED = "PARAM_LOGGED";
    public static String PARAM_USERACTIVE = "PARAM_USERACTIVE";
    public static String PARAM_USER_ENDS = "PARAM_USER_ENDS";

    public static final int CALLBACK_SIMPLE_STRING = 1;
    public static final int CALLBACK_REFRESH = 2;
    public static final int CALLBACK_DELETE= 5;
    public static final int TAB_FAVORITE = 38;
    public static final int TAB_MUSIC = 32;
    public static final int TAB_SELECTION = 90;

    public static String getMusicId(String path) {
        return path.substring(path.lastIndexOf("/") + 1, path.length()).replaceAll(".temp", "")
                .replaceAll(".mp4","")
                .replaceAll(".mp3","")
                .replaceAll(".m4a","")
                .replaceAll(".3gp","")
                .replaceAll(".webm","");
    }

    public static Bitmap getBitmapFromPath(String path) {
        try {
            Bitmap bitmap = null;
            File f = new File(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MaterialDialog.Builder getDialogBuilder(Context context){
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.backgroundColor(context.getResources().getColor(R.color.color_back));
        return builder;
    }

    public static boolean isUserLogged(Context context) {
        TinyDB tinyDB = new TinyDB(context);
        return tinyDB.getBoolean(PARAM_LOGGED);
    }

    public static void setUserInfo(Context context, UserInfo userInfo) {
        TinyDB tinyDB = new TinyDB(context);
        tinyDB.putObject(PARAM_USER, userInfo);
    }

    public static UserInfo getCurrentUser() {
        TinyDB tinyDB = new TinyDB(AppObj.getGlobalContext());
        return (UserInfo) tinyDB.getObject(PARAM_USER, UserInfo.class);
    }

    public static void setUserLogged(Context context, boolean logged) {
        TinyDB tinyDB = new TinyDB(context);
        tinyDB.putBoolean(PARAM_LOGGED, logged);

        if (logged == false) {
            setUserActive(context, 0, "");
            setUserInfo(context, null);
        }
    }

    public long getDateFromString(String value) {
        long res = 0;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = format.parse(value);
            res = date.getTime();

            System.out.println(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return res;
    }

    public static void setUserActive(Context context, int active, String ends) {
        TinyDB tinyDB = new TinyDB(context);
        tinyDB.putBoolean(PARAM_USERACTIVE, active == 1 ? true : false);
        tinyDB.putString(PARAM_USER_ENDS, ends);
    }

    public static boolean isUserActive(Context context) {
        TinyDB tinyDB = new TinyDB(context);
        return tinyDB.getBoolean(PARAM_USERACTIVE);
    }

    /*public static MaterialDialog.Builder getSelectionDialog(Activity activity, Handler.Callback callback) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity);

        SelectionsDAO selectionsDAO = MusicRoomDatabase.getDatabase(activity).selectionsDAO();
        List<SelectionList> list = selectionsDAO.getAllSelections();

        builder.items(list)
                .title("Choose selection: ")
                .positiveText("Add new selection")
                .negativeText("Cancel")
                .itemsCallback((dialog, itemView, position, text) -> {
                    Message message = new Message();
                    message.obj = list.get(position);
                    message.what = position;
                    callback.handleMessage(message);
                })
                .onPositive((dialog, which) -> {
                    getAddNewSelectionDialog(activity).show();
                    dialog.dismiss();
                });

        return builder;
    }
*/
    public static void showSnackBar(Activity activity, String message) {
        View parentLayout = activity.findViewById(android.R.id.content);
        Snackbar.make(parentLayout, message, Snackbar.LENGTH_LONG).show();
    }

   /* public static MaterialDialog.Builder getAddNewSelectionDialog(Context activity) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity);
        builder.backgroundColor(activity.getResources().getColor(R.color.color_dialog));
        builder.customView(R.layout.dialog_new_selection,true);
        return builder;
    }*/

    public static MaterialDialog.Builder getFullSelectionDialog(Activity activity, Handler.Callback callback) {
        MaterialDialog.Builder builder = Core.getDialogBuilder(activity);

        SelectionsDAO selectionsDAO = MusicRoomDatabase.getDatabase(activity).selectionsDAO();
        List<SelectionList> list = selectionsDAO.getAllSelections();

        builder.items(list)
                .title(R.string.choose_sel)
                .positiveText(R.string.add_new_sel)
                .negativeText(R.string.text_cancle)
                .itemsCallback((dialog, itemView, position, text) -> {
                    Message message = new Message();
                    message.obj = list.get(position);
                    message.what = position;
                    callback.handleMessage(message);
                })
                .onPositive((dialog, which) -> {
                    getSelectionDialog(activity,msg -> {
                        if (msg.what == CALLBACK_SIMPLE_STRING
                                && msg.obj != null) {
                            showSnackBar(activity, msg.obj.toString());

                            getFullSelectionDialog(activity,callback).show(); // recreate dialog
                            // dialog.show();
                        }
                        return false;
                    }).show();

                    dialog.dismiss();
                });

        return builder;
    }

    public static MaterialDialog getSelectionDialog(Context activity,SelectionList selection,Handler.Callback callback) {

        MaterialDialog.Builder builder = Core.getDialogBuilder(activity);
        builder.customView(R.layout.dialog_new_selection,true);
        builder.cancelable(false);
        builder.backgroundColor(activity.getResources().getColor(R.color.color_dialog));

        MaterialDialog materialDialog = builder.build();

        View customView = materialDialog.getCustomView();
        EditText etName = customView.findViewById(R.id.etName);
        Button btnClose = customView.findViewById(R.id.btnClose);
        Button btnCreate = customView.findViewById(R.id.btnCreate);

        etName.setText(selection.name);

        btnClose.setOnClickListener(v -> materialDialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String newText = etName.getText().toString();
            if(newText.isEmpty()){
                etName.setError(activity.getString(R.string.text_name_required));
                return;
            }

            Handler handler = new Handler();
            handler.post(() -> {

                SelectionsDAO selectionsDAO = MusicRoomDatabase.getDatabase(activity).selectionsDAO();
                selection.name = newText;
                selectionsDAO.insertSelectionList(selection);

                // response back to view
                Message message = new Message();
                message.obj = activity.getString(R.string.sel_updated);
                message.what = CALLBACK_SIMPLE_STRING;
                callback.handleMessage(message);

            });
            materialDialog.dismiss();
        });

        return materialDialog;
    }

    public static MaterialDialog getSelectionDialog(Context activity,Handler.Callback callback) {

        MaterialDialog.Builder builder = Core.getDialogBuilder(activity);
        builder.customView(R.layout.dialog_new_selection,true);
        builder.cancelable(false);
        builder.backgroundColor(activity.getResources().getColor(R.color.color_dialog));

        MaterialDialog materialDialog = builder.build();

        View customView = materialDialog.getCustomView();
        EditText etName = customView.findViewById(R.id.etName);
        Button btnClose = customView.findViewById(R.id.btnClose);
        Button btnCreate = customView.findViewById(R.id.btnCreate);

        btnClose.setOnClickListener(v -> materialDialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String newText = etName.getText().toString();
            if(newText.isEmpty()){
                etName.setError(activity.getString(R.string.text_name_required));
                return;
            }

            Handler handler = new Handler();
            handler.post(() -> {
                SelectionsDAO selectionsDAO = MusicRoomDatabase.getDatabase(activity).selectionsDAO();
                SelectionList selectionList = new SelectionList();
                selectionList.name = newText;
                selectionsDAO.insertSelectionList(selectionList);

                // response back to view
                if(callback!=null) {
                    Message message = new Message();
                    message.obj = activity.getString(R.string.new_sel_added);
                    message.what = CALLBACK_SIMPLE_STRING;
                    callback.handleMessage(message);
                }
            });
            materialDialog.dismiss();
        });

        return materialDialog;
    }

    public static void checkSubscription() {

        UserInfo userInfo = null;
        try {
            userInfo = getCurrentUser();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        Log.d("debug", "checking subscription");

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        apiInterface.subscription(userInfo.id).enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.code() != 200) {
                    Log.e("debug", "response.code()!=200");
                    return;
                }

                try {
                    Core.setUserActive(AppObj.getGlobalContext(), response.body().is_active, response.body().ends_with);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                Log.e("debug", t.toString());
            }
        });
    }
}
