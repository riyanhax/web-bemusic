package com.digiapp.jilmusic.onlinePlayerView.models;

import android.util.Log;

import com.digiapp.jilmusic.components.WebviewResourceHelper;
import com.digiapp.jilmusic.utils.DiskCacheHelper;

import org.apache.commons.lang3.StringUtils;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkWebResourceRequest;
import org.xwalk.core.XWalkWebResourceResponse;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ResourceClient extends XWalkResourceClient {
    public ResourceClient(XWalkView view) {
        super(view);
    }

    @Override
    public XWalkWebResourceResponse shouldInterceptLoadRequest(XWalkView view, XWalkWebResourceRequest request) {
        String resourceUrl = request.getUrl().toString();

        // switch off stats for youtube
        if(resourceUrl.startsWith("https://www.youtube.com/api/stats")
                || resourceUrl.startsWith("https://www.youtube.com/youtubei/v1")
                || resourceUrl.startsWith("https://www.youtube.com/ptracking")
                || resourceUrl.startsWith("https://www.youtube.com/pagead"))
        {
            return null;
        }

        // for youtube video we also need to ignore
        if(resourceUrl.startsWith("https://www.youtube.com/watch?v=")
                || resourceUrl.indexOf("googlevideo.com")>=0){
            return super.shouldInterceptLoadRequest(view, request);
        }

        String fileExtension = WebviewResourceHelper.getInstance().getFileExt(resourceUrl);
        if (WebviewResourceHelper.getInstance().getOverridableExtensions().contains(fileExtension)) {
            String encoding = "UTF-8";
            String assetName = WebviewResourceHelper.getInstance().getLocalAssetPath(resourceUrl);
            if (StringUtils.isNotEmpty(assetName)) {
                String mimeType = WebviewResourceHelper.getInstance().getMimeType(fileExtension);
                if (StringUtils.isNotEmpty(mimeType)) {
                    try {
                        Log.e("---debug---", assetName);
                        return WebviewResourceHelper.getXWalkWebResourceResponseFromAsset(assetName, mimeType, encoding,this);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return super.shouldInterceptLoadRequest(view, request);
                    }
                }
            }
            String localFilePath = WebviewResourceHelper.getInstance().getLocalFilePath(resourceUrl);
            if (StringUtils.isNotEmpty(localFilePath)) {
                String mimeType = WebviewResourceHelper.getInstance().getMimeType(fileExtension);
                if (StringUtils.isNotEmpty(mimeType)) {
                    try {
                        Log.e("---debug---", localFilePath);
                        return WebviewResourceHelper.getXWalkWebResourceResponseFromFile(localFilePath, mimeType, encoding,this);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        return super.shouldInterceptLoadRequest(view, request);
                    }
                }
            }
        }
        if (fileExtension.endsWith("jpg")
                || fileExtension.endsWith("mp4")
                || fileExtension.endsWith("mp3")
                || fileExtension.endsWith("woff")
                || fileExtension.endsWith("ttf")
                || fileExtension.endsWith("ico")){
            try {
                InputStream inputStream = DiskCacheHelper.readFromCacheSync(resourceUrl);
                if (inputStream != null) {
                    return createXWalkWebResourceResponse(WebviewResourceHelper.getMimeType(fileExtension.replaceAll(".", "")),"UTF-8",inputStream);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return super.shouldInterceptLoadRequest(view, request);
            }
        }

        else if(resourceUrl.contains("googlevideo.com")){
            InputStream inputStream = DiskCacheHelper.readFromCacheSync(resourceUrl);
            if (inputStream != null) {

                return createXWalkWebResourceResponse(WebviewResourceHelper.getMimeType("https://www.youtube.com/"),"UTF-8",inputStream);
            }
        }
        else if(resourceUrl.contains("/image/")){
            InputStream inputStream = DiskCacheHelper.readFromCacheSync(resourceUrl);
            if (inputStream != null) {
                return createXWalkWebResourceResponse(WebviewResourceHelper.getMimeType("/image/"),"UTF-8",inputStream);
            }
        }
        else{
            Log.wtf("cache---","resourceUrl " + resourceUrl + "  fileExtension " + fileExtension);
        }
        return super.shouldInterceptLoadRequest(view, request);
    }
}
