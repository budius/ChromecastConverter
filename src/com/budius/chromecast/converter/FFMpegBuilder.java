package com.budius.chromecast.converter;

import com.budius.chromecast.converter.ffprobe_model.Stream;
import com.budius.chromecast.converter.ffprobe_model.Tags;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.budius.chromecast.converter.FFProbe.*;

/**
 * Captures data from FFPROBE and creates appropriates FFMPEG commands
 */
public class FFMpegBuilder {

    // TODO: maybe check what FFMPEG modules are installed and switch to native AAC case libfdk_aac is not present

    private static final String MP4 = "mp4";

    public static final int TYPE_NO_GOOD = 1;
    public static final int TYPE_CONVERT_ONLY_AUDIO = 2;
    public static final int TYPE_TWO_PASS = 3;
    public static final int TYPE_NO_CHANGE = 4;
    public static final int TYPE_JUST_CHANGE_CONTAINER = 5;
    public static final int TYPE_CRF = 6; // uses single pass Constant Rate Factor

    // Chromecast Supported Media ============================================================
    private static final String VIDEO_CODEC = "h264";
    private static final String VIDEO_PROFILE = "High";

    private static final String AUDIO_CODEC_1 = "aac";
    private static final String AUDIO_CODEC_2 = "mp3";

    private static final String INVALID_SUBTITLE = "dvd_subtitle";

    private int type;
    private int numberOfSubtitles;
    private ArrayList<String[]> subtitles = new ArrayList<String[]>();
    private String[] firstPass;
    private String[] secondPass;
    private String[] singlePass;


    private final SingleConversionSetting conversionSetting;

    public FFMpegBuilder(SingleConversionSetting scs) {
        conversionSetting = scs;

        // Here we will calculate all the data this object needs.
        // Calculating it ahead of conversion allows us to have fallback commands
        // in case of any error during generation.
        type = internalGetType();
        numberOfSubtitles = internalGetNumberOfSubtitles();
        for (int i = 0; i < numberOfSubtitles; i++) {
            subtitles.add(internalGetSubtitle(i));
        }

        switch (type) {
            case TYPE_NO_GOOD:
            case TYPE_NO_CHANGE:
                return;
            case TYPE_CRF:
                singlePass = internalGetCrfSinglePass();
                return;
            case TYPE_CONVERT_ONLY_AUDIO:
            case TYPE_JUST_CHANGE_CONTAINER:
                singlePass = internalGetSinglePass();
                return;
            case TYPE_TWO_PASS:
                firstPass = internalGetFirstPass();
                secondPass = internalGetSecondPass();

                // getVideoBitrate() might fail, on those cases we revert back to CRF
                if (firstPass == null || secondPass == null) {
                    type = TYPE_CRF;
                    singlePass = internalGetCrfSinglePass();
                }

                return;
        }

    }

    //
    // public getters
    // =================================================================================================================
    public int getType() {
        return type;
    }

    public int getNumberOfSubtitles() {
        return numberOfSubtitles;
    }

    public String[] getFirstPass() {
        return firstPass;
    }

    public String[] getSecondPass() {
        return secondPass;
    }

    public String[] getSinglePass() {
        return singlePass;
    }

    public String[] getSubtitle(int position) {
        if (position >= subtitles.size())
            return null;
        else
            return subtitles.get(position);
    }

    //
    // general information about this file
    // =================================================================================================================
    private int internalGetType() {

        Stream videoStream = getStream(CODEC_TYPE_VIDEO);
        if (videoStream == null)
            return TYPE_NO_GOOD;

        Stream audioStream = getStream(CODEC_TYPE_AUDIO);
        if (audioStream == null)
            return TYPE_NO_GOOD;

        // good video codec and profile
        if (VIDEO_CODEC.equals(videoStream.getCodec_name())
                && VIDEO_PROFILE.equals(videoStream.getProfile())) {

            // good audio codec
            if (AUDIO_CODEC_1.equals(audioStream.getCodec_name())
                    || AUDIO_CODEC_2.equals(audioStream.getCodec_name())) {

                // good container
                String ext = FilenameUtils.getExtension(conversionSetting.getOriginalVideoFile().getAbsolutePath());
                if (MP4.equals(ext) && conversionSetting.getFfProbe().getFormat().getFormat_name().contains(MP4)) {
                    return TYPE_NO_CHANGE;
                }

                return TYPE_JUST_CHANGE_CONTAINER;

            }

            return TYPE_CONVERT_ONLY_AUDIO;
        }

        // same file size uses 2 pass, other qualities uses CRF
        return conversionSetting.getQuality() == Settings.QUALITY_SAME_FILE_SIZE ? TYPE_TWO_PASS : TYPE_CRF;
    }

    private int internalGetNumberOfSubtitles() {
        int numberOfSubtitles = 0;
        for (Stream s : conversionSetting.getFfProbe().getStreams()) {
            if (CODEC_TYPE_SUBTITLE.equals(s.getCodec_type()) &&
                    !INVALID_SUBTITLE.equals(s.getCodec_name())) {
                numberOfSubtitles++;
            }
        }
        return numberOfSubtitles;
    }

    //
    // ffmpeg command generation
    // =================================================================================================================
    private String[] internalGetFirstPass() {

        if (type != TYPE_TWO_PASS)
            return null;

        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add("ffmpeg");
        cmd.add("-i");
        cmd.add(conversionSetting.getOriginalVideoFile().getAbsolutePath());
        String videoBitrate = getVideoBitrate();

        if (videoBitrate == null)
            return null;

        addVideoConversion(cmd, videoBitrate);

        cmd.add("-an");
        cmd.add("-pass");
        cmd.add("1");
        cmd.add(conversionSetting.getTempVideoFile().getAbsolutePath());

        return getArray(cmd);
    }

    private String[] internalGetSecondPass() {

        if (type != TYPE_TWO_PASS)
            return null;

        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add("ffmpeg");
        cmd.add("-y");
        cmd.add("-i");
        cmd.add(conversionSetting.getOriginalVideoFile().getAbsolutePath());
        String videoBitrate = getVideoBitrate();

        if (videoBitrate == null)
            return null;

        addVideoConversion(cmd, videoBitrate);
        addAudioConversion(cmd);

        cmd.add("-movflags");
        cmd.add("+faststart");
        cmd.add("-pass");
        cmd.add("2");
        cmd.add(conversionSetting.getTempVideoFile().getAbsolutePath());

        return getArray(cmd);
    }

    private String[] internalGetSinglePass() {

        switch (type) {
            case TYPE_NO_GOOD:
                Log.v("");
            case TYPE_NO_CHANGE:
            case TYPE_TWO_PASS:
                return null;
        }

        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add("ffmpeg");
        cmd.add("-i");
        cmd.add(conversionSetting.getOriginalVideoFile().getAbsolutePath());
        cmd.add("-vcodec");
        cmd.add("copy");
        if (type == TYPE_CONVERT_ONLY_AUDIO) {
            addAudioConversion(cmd);
        } else {
            cmd.add("-acodec");
            cmd.add("copy");
        }

        cmd.add("-movflags");
        cmd.add("+faststart");
        cmd.add(conversionSetting.getTempVideoFile().getAbsolutePath());

        return getArray(cmd);
    }

    private String[] internalGetCrfSinglePass() {

        if (type != TYPE_CRF)
            return null;

        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add("ffmpeg");
        cmd.add("-i");
        cmd.add(conversionSetting.getOriginalVideoFile().getAbsolutePath());
        cmd.add("-c:v");
        cmd.add("libx264");
        cmd.add("-profile:v");
        cmd.add("high");
        cmd.add("-level");
        cmd.add("5");
        cmd.add("-preset");
        cmd.add(Settings.ARRAY_SPEED[conversionSetting.getSpeed()]);
        cmd.add("-crf");
        switch (conversionSetting.getQuality()) {
            case Settings.QUALITY_SUPER:
                cmd.add("15");
                break;
            case Settings.QUALITY_NORMAL:
                cmd.add("23");
                break;
            case Settings.QUALITY_HIGH:
            default:
                cmd.add("18");
                break;
        }

        String videoBitRate = getVideoBitrate();
        if (videoBitRate != null) {
            cmd.add("-maxrate");
            cmd.add(videoBitRate);
            cmd.add("-bufsize");
            cmd.add("5M");
        }

        addAudioConversion(cmd);
        cmd.add("-movflags");
        cmd.add("+faststart");
        cmd.add(conversionSetting.getTempVideoFile().getAbsolutePath());

        return getArray(cmd);
    }

    private String[] internalGetSubtitle(int position) {

        Stream subtitle = null;

        int subtitleIndex = 0;

        for (int streamIndex = 0; streamIndex < conversionSetting.getFfProbe().getStreams().size(); streamIndex++) {
            Stream s = conversionSetting.getFfProbe().getStreams().get(streamIndex);
            if (CODEC_TYPE_SUBTITLE.equals(s.getCodec_type()) &&
                    !INVALID_SUBTITLE.equals(s.getCodec_name())) {

                if (position == subtitleIndex) {
                    subtitle = s;
                }

                subtitleIndex++; // increment for all valid subtitles
            }
        }


        if (subtitle == null)
            return null;

        // get the language code for the filename
        String language = null;
        Tags t = subtitle.getTags();
        if (t != null) {
            language = t.getLanguage();
        }

        if (language == null)
            language = "DEFAULT";

        File fileName = conversionSetting.getSubtitleFileName(language);

        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add("ffmpeg");
        cmd.add("-i");
        cmd.add(conversionSetting.getOriginalVideoFile().getAbsolutePath());
        cmd.add("-vn");
        cmd.add("-an");
        cmd.add("-map");
        cmd.add("0:s:" + position);
        cmd.add(fileName.getAbsolutePath());

        return getArray(cmd);
    }

    //
    // internal helpers
    // =================================================================================================================

    private String[] getArray(List<String> list) {
        String[] strings = new String[list.size()];
        strings = list.toArray(strings);
        return strings;
    }

    private Stream getStream(String name) {
        for (int i = 0; i < conversionSetting.getFfProbe().getStreams().size(); i++)
            if (name.equals(conversionSetting.getFfProbe().getStreams().get(i).getCodec_type()))
                return conversionSetting.getFfProbe().getStreams().get(i);
        return null;
    }

    private void addVideoConversion(List<String> cmd, String videoBitrate) {
        cmd.add("-c:v");
        cmd.add("libx264");
        cmd.add("-profile:v");
        cmd.add("high");
        cmd.add("-level");
        cmd.add("5");
        cmd.add("-preset");
        cmd.add("slow");
        cmd.add("-b:v");
        cmd.add(videoBitrate);
    }

    private void addAudioConversion(List<String> cmd) {
        cmd.add("-c:a");
        cmd.add("libfdk_aac");

        /*
        High Efficiency profile is giving trouble for some encodings.
        e.g.: pcm_s16le, 8000 Hz, 1 channels, s16, 128 kb/s (video from a mini camera I have)
         */
        //cmd.add("-profile:a");
        //cmd.add("aac_he");
        cmd.add("-cutoff");
        cmd.add("18000");

        boolean bitRateAdded = false;
        Stream audioStream = getStream(CODEC_TYPE_AUDIO);
        String audioBitRate = audioStream.getBit_rate();

        if (audioBitRate == null || audioBitRate.trim().length() == 0) {
            // if it doesn't have, just let FFMPEG do its default thing
            bitRateAdded = true;
        } else {
            try {
                long abr = Long.parseLong(audioBitRate);
                if (abr > Settings.getMaxPerChannelAudioBitRate(conversionSetting) * audioStream.getChannels()) {
                    bitRateAdded = true;
                    cmd.add("-b:a");
                    cmd.add(Long.toString(Settings.getMaxPerChannelAudioBitRate(conversionSetting) * audioStream.getChannels()));
                }
            } catch (Exception e) { /* number format */ }
        }

        if (!bitRateAdded) {
            cmd.add("-b:a");
            cmd.add(audioBitRate);
        }
    }

    private String getVideoBitrate() {

        /*
        I found cases where mpeg1 streams return the uncompressed bitrate, rendering absurdly high bit rates.
        So we're getting the smaller from the two
        */

        long br_stream = getVideoBitrateBasedOnVideoStreamBitRate();
        long br_file = getVideoBitrateBasedOnFileBitrate();
        long br = br_file > br_stream ? br_stream : br_file;

        if (br <= 0)
            return null;
        else
            return Long.toString(br);
    }

    private long getVideoBitrateBasedOnFileBitrate() {
        // file bitrate
        String fileBitrate = conversionSetting.getFfProbe().getFormat().getBit_rate();
        long fbr;
        if (fileBitrate == null || fileBitrate.trim().length() == 0) {
            return 0l;
        } else {
            try {
                fbr = Long.parseLong(fileBitrate);
            } catch (Exception e) {
                // number format exception
                return 0l;
            }
        }

        // audio bitrate
        long abr;
        Stream audioStream = getStream(CODEC_TYPE_AUDIO);
        String audioBitrate = audioStream.getBit_rate();
        try {
            abr = Long.parseLong(audioBitrate);
        } catch (Exception e) {
            // number format exception
            abr = Settings.getDefaultPerChannelAudioBitRate() * audioStream.getChannels();
        }

        return fbr - abr;
    }

    private long getVideoBitrateBasedOnVideoStreamBitRate() {
        Stream videoStream = getStream(CODEC_TYPE_VIDEO);
        try {
            return Long.parseLong(videoStream.getBit_rate());
        } catch (Exception e) {
            // number format exception
            return 0l;
        }
    }

}
