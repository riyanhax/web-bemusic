package android.instrumental;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

// @RunWith(AndroidJUnit4.class)
@RunWith(Parameterized.class)
@MediumTest
public class ConvertorTests {

    @Mock
    Context mMockContext;

    @Before
    public void setUp() {
        mMockContext = InstrumentationRegistry.getContext();
    }

    @Test
    public void canConvertToMP3File() throws InterruptedException {


        //ClassLoader classLoader = getClass().getClassLoader();
        // final File file = new File(Uri.parse(classLoader.getResource("19.m4a").getFile()).getPath());
        // assertThat("Test file not exists",file.exists());
        try {
            mMockContext.getAssets().open("19.m4a");
        } catch (IOException e) {
            e.printStackTrace();
        }

        final CountDownLatch signal = new CountDownLatch(1);

        AndroidAudioConverter.load(mMockContext, new ILoadCallback() {
            @Override
            public void onSuccess() {
                signal.countDown();

                AndroidAudioConverter.with(mMockContext)
                        .setFile(null)
                        .setFormat(AudioFormat.MP3)
                        .setCallback(new IConvertCallback() {

                            @Override
                            public void onSuccess(File file) {

                            }

                            @Override
                            public void onFailure(Exception e) {

                            }
                        })
                        .convert();
            }

            @Override
            public void onFailure(Exception error) {
                signal.countDown();
            }
        });

        signal.await();// wait for callback
    }

}
