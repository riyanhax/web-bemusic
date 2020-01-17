package com.digiapp.jilmusic.media;


public class MediaTagHelper {

    // REMOVED DUE USELESS
    /*public static void syncTags(MusicTrack musicTrack) throws IOException,
            CannotWriteException,
            TagException,
            InvalidAudioFrameException,
            CannotReadException {

        if (musicTrack.isVideo) {
            setVideoTag(musicTrack.localPath, FieldKey.TITLE.toString(), musicTrack.localPath);
            setVideoTag(musicTrack.localPath, FieldKey.ALBUM.toString(), musicTrack.localPath);
        } else {
            setAudioTag(musicTrack.localPath, FieldKey.TITLE, musicTrack.name);
            setAudioTag(musicTrack.localPath, FieldKey.ARTIST, musicTrack.album_name);
        }

    }

    private static void setVideoTag(String filePath, String key, String value) throws IOException {

        File file = new File(filePath);
        MetadataEditor mediaMeta = MetadataEditor.createFrom(file);
        Map<String, MetaValue> meta = mediaMeta.getKeyedMeta();

        meta.put(key, MetaValue.createString(value));
        mediaMeta.save(false);
    }

    public static String getAudioTag(String filePath, FieldKey key) throws IOException, CannotReadException, TagException, InvalidAudioFrameException, CannotWriteException {

        String result = "";

        File file = new File(filePath);
        if(!file.exists()){
            throw new FileNotFoundException("not found");
        }

        if (filePath.endsWith("m4a")) {
            Mp4AudioFileReader reader = new Mp4AudioFileReader();
            final AudioFile m4a = reader.read(file, "m4a", false);

            Tag tag = m4a.getTag().or(NullTag.INSTANCE);
            if (tag == NullTag.INSTANCE) {
                tag = m4a.setNewDefaultTag();
            }

            result = tag.getValue(key).get();

        } else {
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag().or(NullTag.INSTANCE);
            if (tag == NullTag.INSTANCE) {
                tag = audioFile.setNewDefaultTag();
            }
            result = tag.getValue(key).get();
        }

        return result;
    }

    public static void setAudioTag(String filePath, FieldKey key, String value) throws TagException,
            CannotReadException,
            InvalidAudioFrameException,
            IOException,
            CannotWriteException{

        File file = new File(filePath);
        if(!file.exists()){
            throw new FileNotFoundException("not found");
        }

        if (filePath.endsWith("m4a")) {
            Mp4AudioFileReader reader = new Mp4AudioFileReader();
            final AudioFile m4a = reader.read(file, "m4a", false);

            Tag tag = m4a.getTag().or(NullTag.INSTANCE);
            if (tag == NullTag.INSTANCE) {
                tag = m4a.setNewDefaultTag();
            }

            tag.setField(key, value);
            m4a.save();

        } else if(filePath.endsWith("mp3")){

            AudioFile audioFile = null;
            try {
                audioFile = AudioFileIO.read(file);
            } catch (Exception e) {
                e.printStackTrace();
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
                tag.setField(key, value);
                audioFile.save();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

        } else {
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag().or(NullTag.INSTANCE);
            if (tag == NullTag.INSTANCE) {
                tag = audioFile.setNewDefaultTag();
            }

            tag.setField(key, value);
            audioFile.save();
        }
    }*/

}
