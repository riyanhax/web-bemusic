package com.digiapp.jilmusic.dashboardView.models;

import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.R;

import at.huber.youtubeExtractor.YtFile;

public class YouTubeLink extends Object {

    public YtFile ytFile;

    public YouTubeLink(YtFile ytFile) {
        this.ytFile = ytFile;
    }

    @Override
    public String toString() {
        if (ytFile == null) {
            return "";
        }

        try {
            int height = ytFile.getFormat().getHeight();

            if(height==144){
                return AppObj.getGlobalContext().getString(R.string.video_low);
            }

            if(height==240){
                return AppObj.getGlobalContext().getString(R.string.video_medium);
            }


            if(height>=480){
                return AppObj.getGlobalContext().getString(R.string.video_hight);
            }

            if(ytFile.getFormat().getExt().equalsIgnoreCase("m4a")){
                //noinspection HardCodedStringLiteral
                return AppObj.getGlobalContext().getString(R.string.audio_only); //NON-NLS
            }

            return "Video" + " - " + height;
        } catch (Exception ex) {
            return "ex";
        }
    }
}
