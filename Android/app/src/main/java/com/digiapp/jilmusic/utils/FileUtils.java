package com.digiapp.jilmusic.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    public static boolean isExists(String path){
        if(path==null){
            return false;
        }
        return new File(path).exists();
    }

    public static void saveSamplesFromAssets(Context context,String filename) throws IOException {

        File directory = MediaIDHelper.getMediaDirectory(context);

        AssetFileDescriptor afd = context.getAssets().openFd(filename);

        InputStream in = null;
        OutputStream out = null;
        try {
            in = afd.createInputStream();//assetManager.open(filename);
            File outFile = new File(directory, filename);;

            out = new FileOutputStream(outFile);
            copyFile(in, out);
        } catch(IOException e) {
            Log.e("tag", "Failed to copy asset file: " + filename, e);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}
