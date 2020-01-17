package com.digiapp.jilmusic.components;

import android.os.Build;
import android.webkit.WebResourceResponse;

import com.digiapp.jilmusic.AppObj;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkWebResourceResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebviewResourceHelper {
    private static WebviewResourceHelper instance;
    private List<LocalAssetMapModel> localAssetMapModelList;
    private List<String> overridableExtensions = new ArrayList<>(Arrays.asList("js", "css", "png", "jpg", "woff", "ttf", "eot", "ico", "mp4"));

    private WebviewResourceHelper(){

    }

    public static WebviewResourceHelper getInstance(){
        if(instance == null){
            instance = new WebviewResourceHelper();
        }
        return instance;
    }

    public String getLocalAssetPath(String url){
        if(StringUtils.isEmpty(url)){
            return "";
        }
        if(localAssetMapModelList == null){
            localAssetMapModelList = getLocalAssetList();
        }
        if(CollectionUtils.isNotEmpty(localAssetMapModelList)){
            for(LocalAssetMapModel localAssetMapModel : localAssetMapModelList){
                if(localAssetMapModel.url.equals(url)){
                    return localAssetMapModel.asset_url;
                }
            }
        }
        return "";
    }

    public String getLocalFilePath(String url){
        String localFilePath = "";
        String fileNameForUrl = getLocalFileNameForUrl(url);
        if(StringUtils.isNotEmpty(fileNameForUrl) && fileExists(fileNameForUrl)){
            localFilePath = getFileFullPath(fileNameForUrl);
        }
        return localFilePath;
    }

    public String getLocalFileNameForUrl(String url){
        String localFileName = "";
        String[] parts = url.split("/");
        if(parts.length > 0){
            localFileName = parts[parts.length-1];
        }
        return localFileName;
    }

    private boolean fileExists(String fileName){
        String path = AppObj.getGlobalContext()
                .getFilesDir() + "/cart/" + fileName;
        return new File(path).exists();
    }

    private String getFileFullPath(String relativePath){
        return AppObj.getGlobalContext().getFilesDir() + "/cart/" + relativePath;
    }

    private List<LocalAssetMapModel> getLocalAssetList(){
        List<LocalAssetMapModel> localAssetMapModelList = new ArrayList<>();
        String pageData = null;
        /*try {
            pageData = ResourceAccessHelper.getJsonData(AppObj.getGlobalContext(), "web-assets/map.json");
        } catch (IOException e) {
        }
        if(pageData !=null){
            Type listType = new TypeToken<ArrayList<LocalAssetMapModel>>() {
            }.getType();
            localAssetMapModelList = new Gson().fromJson(pageData,listType);
        }

        pageData = null;
        try {
            pageData = ResourceAccessHelper.getJsonData(AppObj.getGlobalContext(), "web-assets/fonts-map.json");
        } catch (IOException e) {
        }
        if(pageData !=null){
            Type listType = new TypeToken<ArrayList<LocalAssetMapModel>>() {
            }.getType();
            List<LocalAssetMapModel> fontsMap = new Gson().fromJson(pageData,listType);
            localAssetMapModelList.addAll(fontsMap);
        }*/
        return localAssetMapModelList;
    }

    public List<String> getOverridableExtensions(){
        return overridableExtensions;
    }

    public String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    public static String getMimeType(String fileExtension){
        String mimeType = "";
        switch (fileExtension){
            case "css" :
                mimeType = "text/css";
                break;
            case "js" :
                mimeType = "text/javascript";
                break;
            case "png" :
                mimeType = "image/png";
                break;
            case "jpg" :
            case "/image/":
                mimeType = "image/jpeg";
                break;
            case "ico" :
                mimeType = "image/x-icon";
                break;
            case "mp4":
                mimeType = "video/mp4";
                break;
            case "https://www.youtube.com/":
                mimeType = "video/mp4";
            case "woff" :
            case "ttf" :
            case "eot" :
                mimeType = "application/x-font-opentype";
                break;
        }
        return mimeType;
    }

    public static WebResourceResponse getWebResourceResponseFromAsset(String assetPath, String mimeType, String encoding) throws IOException{
        InputStream inputStream =  AppObj.getGlobalContext().getAssets().open(assetPath);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int statusCode = 200;
            String reasonPhase = "OK";
            Map<String, String> responseHeaders = new HashMap<String, String>();
            responseHeaders.put("Access-Control-Allow-Origin", "*");
            return new WebResourceResponse(mimeType, encoding, statusCode, reasonPhase, responseHeaders, inputStream);
        }
        return new WebResourceResponse(mimeType, encoding, inputStream);
    }

    public static XWalkWebResourceResponse getXWalkWebResourceResponseFromAsset(String assetPath, String mimeType, String encoding, XWalkResourceClient client) throws IOException{
        InputStream inputStream =  AppObj.getGlobalContext().getAssets().open(assetPath);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int statusCode = 200;
            String reasonPhase = "OK";
            Map<String, String> responseHeaders = new HashMap<String, String>();
            responseHeaders.put("Access-Control-Allow-Origin", "*");
            return client.createXWalkWebResourceResponse(mimeType, encoding, inputStream,statusCode, reasonPhase, responseHeaders);
        }
        return client.createXWalkWebResourceResponse(mimeType, encoding, inputStream);
    }

    public static WebResourceResponse getWebResourceResponseFromFile(String filePath, String mimeType, String encoding) throws FileNotFoundException {
        File file = new File(filePath);
        FileInputStream fileInputStream = new FileInputStream(file);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int statusCode = 200;
            String reasonPhase = "OK";
            Map<String, String> responseHeaders = new HashMap<String, String>();
            responseHeaders.put("Access-Control-Allow-Origin","*");
            return new WebResourceResponse(mimeType, encoding, statusCode, reasonPhase, responseHeaders, fileInputStream);
        }
        return new WebResourceResponse(mimeType, encoding, fileInputStream);
    }

    public static XWalkWebResourceResponse getXWalkWebResourceResponseFromFile(String filePath, String mimeType, String encoding,XWalkResourceClient client) throws FileNotFoundException {
        File file = new File(filePath);
        FileInputStream fileInputStream = new FileInputStream(file);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int statusCode = 200;
            String reasonPhase = "OK";
            Map<String, String> responseHeaders = new HashMap<String, String>();
            responseHeaders.put("Access-Control-Allow-Origin","*");
            return client.createXWalkWebResourceResponse(mimeType, encoding, fileInputStream, statusCode, reasonPhase, responseHeaders);
        }
        return client.createXWalkWebResourceResponse(mimeType, encoding, fileInputStream);
    }

    private class LocalAssetMapModel{
        String url;
        String asset_url;
    }
}
