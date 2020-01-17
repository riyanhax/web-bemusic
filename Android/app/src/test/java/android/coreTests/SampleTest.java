package android.coreTests;

import android.content.Context;
import android.coreTests.utils.TestUtil;
import android.media.MediaMetadata;
import android.media.MediaMetadataEditor;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.MovieBox;
import com.digiapp.jilmusic.AppObj;
import com.digiapp.jilmusic.media.MediaTagHelper;

import org.jcodec.containers.mp4.MP4Util;
import org.jcodec.containers.mp4.boxes.MetaValue;
import org.jcodec.movtool.MP4Edit;
import org.jcodec.movtool.MetadataEditor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;
import ealvatag.audio.AudioFile;
import ealvatag.audio.AudioFileIO;
import ealvatag.audio.AudioHeader;
import ealvatag.audio.exceptions.CannotReadException;
import ealvatag.audio.exceptions.CannotWriteException;
import ealvatag.audio.exceptions.InvalidAudioFrameException;
import ealvatag.audio.mp4.Mp4AtomTree;
import ealvatag.audio.mp4.Mp4AudioFileReader;
import ealvatag.audio.mp4.atom.Mp4BoxHeader;
import ealvatag.audio.mp4.atom.Mp4StcoBox;
import ealvatag.tag.FieldDataInvalidException;
import ealvatag.tag.FieldKey;
import ealvatag.tag.NullTag;
import ealvatag.tag.Tag;
import ealvatag.tag.TagException;
import ealvatag.utils.tree.DefaultMutableTreeNode;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.MatcherAssert.assertThat;


public class SampleTest {

    @Test
    public void canChangeMP3Tags(){

        String newTitle = "MP3 Title";

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("test1.mp3").getFile());
        assertThat("Test file not exists",file.exists());

        AudioFile audioFile = null;
        try {
            audioFile = AudioFileIO.read(file);
        } catch (Exception e) {
            e.printStackTrace();
            assertThat(e.toString(),false);
            return;
        }

        final AudioHeader audioHeader = audioFile.getAudioHeader();
        final int channels = audioHeader.getChannelCount();
        final int bitRate = audioHeader.getBitRate();
        final String encodingType = audioHeader.getEncodingType();

        System.out.println("channels " + channels + " bitRate " + bitRate + " encodingType " + encodingType);
        Tag tag = audioFile.getTag().or(NullTag.INSTANCE);
        final String title = tag.getValue(FieldKey.TITLE).or("");
        if ("".equals(title)) {
            if (tag == NullTag.INSTANCE) {
                tag = audioFile.setNewDefaultTag();
            }
        }

        try {
            tag.setField(FieldKey.TITLE, newTitle);
            audioFile.save();
        } catch (Exception e) {
            e.printStackTrace();
            assertThat(e.toString(),false);
            return;
        }

        String currentTag = getTagFromAudioFile(FieldKey.TITLE,file);

        assertThat("Tag is wrong" + currentTag,currentTag.equalsIgnoreCase(newTitle));
        System.out.println("Current tag: " + currentTag);
    }

    @Test
    public void canChangeM4ATags() throws IOException, CannotReadException, CannotWriteException, TagException, InvalidAudioFrameException {
        ClassLoader classLoader = getClass().getClassLoader();
        final File file = new File(classLoader.getResource("19.m4a").getFile());
        assertThat("Test file not exists",file.exists());

        MediaTagHelper.setAudioTag(file.getAbsolutePath(),FieldKey.TITLE,"title");
        String newTag = MediaTagHelper.getAudioTag(file.getAbsolutePath(),FieldKey.TITLE);
        assertThat("Not equals",newTag.equalsIgnoreCase("title"));
    }

    @Test
    public void canChangeM4ATagsNoExtension() throws IOException, CannotReadException, CannotWriteException, TagException, InvalidAudioFrameException {
        ClassLoader classLoader = getClass().getClassLoader();
        final File file = new File(classLoader.getResource("example_m4a").getFile());
        assertThat("Test file not exists",file.exists());

        MediaTagHelper.setAudioTag(file.getAbsolutePath(),FieldKey.TITLE,"title");
        String newTag = MediaTagHelper.getAudioTag(file.getAbsolutePath(),FieldKey.TITLE);
        assertThat("Not equals",newTag.equalsIgnoreCase("title"));
    }

    @Test
    public void canChangeMP4TagsISO() throws IOException {
        String newTitle = "Sample Title";

        ClassLoader classLoader = getClass().getClassLoader();
        final File testFile = new File(classLoader.getResource("sample2.mp4").getFile());
        assertThat("Test file not exists",testFile.exists());

        IsoFile isoFile = new IsoFile(testFile.getAbsolutePath());
        MovieBox moov = isoFile.getMovieBox();
        for(Box b : moov.getBoxes()) {
            System.out.println(b);
        }

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(testFile.getAbsolutePath());
        String curTitle = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        System.out.println(curTitle);
    }

    @Test
    public void canChange3GPTags(){

    }

    @Test
    public void canChangeMP4TagsJCodec() throws IOException {

        ClassLoader classLoader = getClass().getClassLoader();
        final File testFile = new File(classLoader.getResource("sample2.mp4").getFile());
        assertThat("Test file not exists",testFile.exists());


        MetadataEditor mediaMeta = MetadataEditor.createFrom(testFile);
        Map<String, MetaValue> meta = mediaMeta.getKeyedMeta();

        meta.put("title", MetaValue.createString("New value")); // fourcc for '©ART'
        mediaMeta.save(false); // fast mode is off


        MetadataEditor metaTest = MetadataEditor.createFrom(testFile);
        Map<String, MetaValue> metaData = metaTest.getKeyedMeta();
        assertThat("Not saved",metaData.size()>0);

    }


    @Test
    public void canChangeMP4TagsEalvatag() throws TagException, CannotReadException, InvalidAudioFrameException, IOException, CannotWriteException {

        String newTitle = "Sample Title";

        ClassLoader classLoader = getClass().getClassLoader();
        final File testFile = new File(classLoader.getResource("sample2.mp4").getFile());
        assertThat("Test file not exists",testFile.exists());

        final Mp4AtomTree treeBefore = new Mp4AtomTree(new RandomAccessFile(testFile, "r"));
        final List<Mp4StcoBox> beforeStcos = treeBefore.getStcos();
        System.out.println("Chunk Offsets before (stco atoms): " + beforeStcos.size());

        Assert.assertEquals(2, beforeStcos.size());
        int freeSpace = 0;
        for (final DefaultMutableTreeNode node : treeBefore.getFreeNodes()) {
            freeSpace += ((Mp4BoxHeader)node.getUserObject()).getDataLength();
        }
        System.out.println("Available free space: " + freeSpace);

        // fill up free space
        final AudioFile audioFile = AudioFileIO.read(testFile);
        final char[] chars = new char[freeSpace * 2]; // twice the size of the total available free space
        Arrays.fill(chars, 'C');
        audioFile.getTag().or(NullTag.INSTANCE).setField(FieldKey.TITLE, new String(chars));
        audioFile.save();

        final Mp4AtomTree treeAfter = new Mp4AtomTree(new RandomAccessFile(testFile, "r"));
        final List<Mp4StcoBox> afterStcos = treeAfter.getStcos();
        System.out.println("Chunk Offsets after (stco atoms): " + afterStcos.size());
        Assert.assertEquals(beforeStcos.size(), afterStcos.size());

        // verify constant shift
        int shift = -1;
        for (int i = 0; i < beforeStcos.size(); i++) {
            final Mp4StcoBox before = beforeStcos.get(i);
            final Mp4StcoBox after = afterStcos.get(i);
            if (shift == -1) {
                shift = getOffsetShift(before, after);
            }
            Assert.assertFalse(0 == shift);
            Assert.assertEquals(shift, getOffsetShift(before, after));
        }

        String currentTag = getTagFromAudioFile(FieldKey.TITLE,testFile);

        assertThat("Tag is wrong" + currentTag,currentTag.equalsIgnoreCase(newTitle));
        System.out.println("Current tag: " + currentTag);

    }

    private static int getOffsetShift(final Mp4StcoBox before, final Mp4StcoBox after) {
        return before.getFirstOffSet() - after.getFirstOffSet();
    }

    private String getTagFromAudioFile(FieldKey key,File file){
        String tagResult = "";

        AudioFile audioFile = null;
        try {
            audioFile = AudioFileIO.read(file);
        } catch (Exception e) {
            e.printStackTrace();
            return tagResult;
        }
        Tag tag = audioFile.getTag().or(NullTag.INSTANCE);
        tagResult = tag.getValue(key).or("");


        return tagResult;
    }
}
