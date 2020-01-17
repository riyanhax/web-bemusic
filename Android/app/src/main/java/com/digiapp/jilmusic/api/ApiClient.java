package com.digiapp.jilmusic.api;


import com.digiapp.jilmusic.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.utils.NetworkHelper;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Dispatcher;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by artembogomaz on 11/19/2016.
 */

public class ApiClient {

    public static final String BASE_URL = BuildConfig.SERVER_URL;

    private static Retrofit retrofit = null;
    private static Dispatcher dispatcher = null;

    public static <T> T createRetrofitService(final Class<T> clazz) {

        dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(150);

        File httpCacheDirectory = new File(AppObj.getGlobalContext().getCacheDir(), "responses");
        int cacheSize = 20 * 1024 * 1024; // 20 MiB
        Cache cache = new Cache(httpCacheDirectory, cacheSize);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor(interceptor)
                .connectTimeout(10, TimeUnit.SECONDS)
                .dispatcher(dispatcher)
                .addNetworkInterceptor(interceptor)
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    if (NetworkHelper.isOnline(AppObj.getGlobalContext())) {
                        request = request.newBuilder()
                                .cacheControl(new CacheControl.Builder()
                                        .maxAge(0, TimeUnit.SECONDS)
                                        .maxStale(365, TimeUnit.DAYS).build())
                                .build();
                    }

                    try {
                        Response originalResponse = chain.proceed(request);
                        if (NetworkHelper.isOnline(AppObj.getGlobalContext())) {
                            int maxAge = 60 * 60;
                            return originalResponse.newBuilder()
                                    //.header("Cache-Control", "public, max-age=" + maxAge)
                                    .build();
                        } else {
                            int maxStale = 60 * 60 * 24 * 28;
                            return originalResponse.newBuilder()
                                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                                    .build();
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                        return new Response.Builder().body(ResponseBody.create(MediaType.parse("json"),"")).code(777).protocol(Protocol.HTTP_1_1).request(request).message("null value").build();
                    }
                })
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .registerTypeAdapter(Boolean.class, booleanAsIntAdapter)
                .registerTypeAdapter(boolean.class, booleanAsIntAdapter)
                .create();

        final Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        T service = restAdapter.create(clazz);

        return service;
    }

    private static final TypeAdapter<Boolean> booleanAsIntAdapter = new TypeAdapter<Boolean>() {
        @Override
        public void write(JsonWriter out, Boolean value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value);
            }
        }

        @Override
        public Boolean read(JsonReader in) throws IOException {
            JsonToken peek = in.peek();
            switch (peek) {
                case BOOLEAN:
                    return in.nextBoolean();
                case NULL:
                    in.nextNull();
                    return null;
                case NUMBER:
                    return in.nextInt() != 0;
                case STRING:
                    return Boolean.parseBoolean(in.nextString());
                default:
                    throw new IllegalStateException("Expected BOOLEAN or NUMBER but was " + peek);
            }
        }
    };

    public static Dispatcher getDispatcher() {
        return dispatcher;
    }

    public static Retrofit getClient() throws IllegalStateException {
        if (retrofit == null) {

            File httpCacheDirectory = new File(AppObj.getGlobalContext().getCacheDir(), "responses");
            int cacheSize = 20 * 1024 * 1024; // 20 MiB
            Cache cache = new Cache(httpCacheDirectory, cacheSize);

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            dispatcher = new Dispatcher();
            dispatcher.setMaxRequests(150);

            OkHttpClient client = new OkHttpClient.Builder()
                    .cache(cache)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .dispatcher(dispatcher)
                    .addNetworkInterceptor(interceptor)
                    .build();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .registerTypeAdapter(Boolean.class, booleanAsIntAdapter)
                    .registerTypeAdapter(boolean.class, booleanAsIntAdapter)
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}
