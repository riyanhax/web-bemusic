package com.digiapp.jilmusic.utils;

import android.net.Uri;
import android.util.Log;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.cache.disk.FileCache;
import com.facebook.common.memory.PooledByteBuffer;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DiskCacheHelper {

    static final Executor executor = Executors.newFixedThreadPool(30);

    private static void putInCache(String url) {
        ImagePipeline imagePipeline = Fresco.getImagePipeline();

        DataSource<Void> dataSource = imagePipeline.prefetchToDiskCache(ImageRequest.fromUri(Uri.parse(url)), false);
        dataSource.subscribe(new BaseDataSubscriber<Void>() {
            @Override
            protected void onNewResultImpl(DataSource<Void> dataSource) {
                if (dataSource.isFinished()) {
                    Log.wtf("cache---","ADDED TO CACHE " + url);
                }
            }

            @Override
            protected void onFailureImpl(DataSource<Void> dataSource) {
                Log.wtf("cache---","ERROR ADDING TO CACHE " + url);
            }
        }, executor);
    }

    public static InputStream readFromCacheSync(String imageUrl) {

        Log.wtf("cache---",imageUrl);
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        boolean inDiskCache = imagePipeline.isInDiskCacheSync(ImageRequest.fromUri(Uri.parse(imageUrl)));
        if (!inDiskCache) {
            putInCache(imageUrl);
            return null;
        }

        CacheKey cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(ImageRequest.fromUri(imageUrl), null);
        try {
            Log.wtf("cache---",imageUrl + " --GOT FROM CACHE-- ");
            return readFromDiskCache(cacheKey);
        } catch (Exception e) {
            Log.wtf("cache---",imageUrl + " ERROR " + e.getMessage());
            return null;
        }
    }

    private static InputStream readFromDiskCache(final CacheKey key) throws IOException {
        try {
            FileCache fileCache = ImagePipelineFactory.getInstance().getMainFileCache();
            final BinaryResource diskCacheResource = fileCache.getResource(key);
            if (diskCacheResource == null) {
                return null;
            }
            PooledByteBuffer byteBuffer;
            final InputStream is = diskCacheResource.openStream();
            return is;
        } catch (IOException ioe) {
            return null;
        }
    }
}
