package com.budius.chromecast.converter;

import com.budius.chromecast.converter.ffprobe_model.Probe;
import com.budius.chromecast.converter.ffprobe_model.Stream;
import com.budius.chromecast.converter.ffprobe_model.Tags;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Captures data from FFPROBE and creates appropriates FFMPEG commands
 */
public class FFMpegBuilder {

    // TODO: maybe check what FFMPEG modules are installed and switch to native AAC case libfdk_aac is not present

    private static final long K = 1024;
    private static final long MAX_AUDIO_BIT_RATE_PER_CHANNEL = 128 * K;
    private static final long DEFAULT_AUDIO_BIT_RATE_PER_CHANNEL = 64 * K;
    private static final long MAX_VIDEO_BIT_RATE = 1555 * K; // equals to fit 2 hours movie in 1.4 GB
    private static final String MP4 = "mp4";

    public static final int TYPE_NO_GOOD = 1;
    public static final int TYPE_CONVERT_ONLY_AUDIO = 2;
    public static final int TYPE_TWO_PASS = 3;
    public static final int TYPE_NO_CHANGE = 4;
    public static final int TYPE_JUST_CHANGE_CONTAINER = 5;

    // FFProbe values ========================================================================
    private static final String CODEC_TYPE_VIDEO = "video";
    private static final String CODEC_TYPE_AUDIO = "audio";
    private static final String CODEC_TYPE_SUBTITLE = "subtitle";

    // Chromecast Supported Media ============================================================
    private static final String VIDEO_CODEC = "h264";
    private static final String VIDEO_PROFILE = "High";

    private static final String AUDIO_CODEC_1 = "aac";
    private static final String AUDIO_CODEC_2 = "mp3";

    private static final String SUB_CODEC = "subrip"; // TODO: any others?

    // the original video file name
    private final File inputfile;

    // the generated video file name (that will be later renamed)
    private File outputfile;

    // the data about the original video
    private final Probe probe;

    private int type;
    private int numberOfSubtitles;
    private ArrayList<String[]> subtitles = new ArrayList<String[]>();
    private String[] firstPass;
    private String[] secondPass;
    private String[] singlePass;

    public FFMpegBuilder(File f, Probe p) {
        inputfile = f;
        probe = p;

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
            case TYPE_CONVERT_ONLY_AUDIO:
            case TYPE_JUST_CHANGE_CONTAINER:
                singlePass = internalGetSinglePass();
                return;
            case TYPE_TWO_PASS:
                firstPass = internalGetFirstPass();
                secondPass = internalGetSecondPass();
                return;
            // TODO: I don't believe any of th commands generation is failing,
            // TODO: but double check it with more data and in case fails,
            // TODO: generate a standard, good quality command
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

    public File getGeneratedFile() {
        return outputfile;
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
                String ext = FilenameUtils.getExtension(inputfile.getAbsolutePath());
                if (MP4.equals(ext) && probe.getFormat().getFormat_name().contains(MP4)) {
                    return TYPE_NO_CHANGE;
                }

                return TYPE_JUST_CHANGE_CONTAINER;

            }

            return TYPE_CONVERT_ONLY_AUDIO;
        }

        return TYPE_TWO_PASS;
    }

    private int internalGetNumberOfSubtitles() {
        int numberOfSubtitles = 0;
        for (Stream s : probe.getStreams()) {
            if (CODEC_TYPE_SUBTITLE.equals(s.getCodec_type())) {
                if (SUB_CODEC.equals(s.getCodec_name())) {
                    numberOfSubtitles++;
                }
            }
        }
        return numberOfSubtitles;
    }

    //
    // ffmpeg command generation
    // =================================================================================================================
    private String[] internalGetFirstPass() {

        if (internalGetType() != TYPE_TWO_PASS)
            return null;

        outputfile = new File(getFilename("_temp.mp4", true));

        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add("ffmpeg");
        cmd.add("-i");
        cmd.add(inputfile.getAbsolutePath());
        String videoBitrate = getVideoBitrate();

        if (videoBitrate == null)
            return null;

        addVideoConversion(cmd, videoBitrate);

        cmd.add("-an");
        cmd.add("-pass");
        cmd.add("1");
        cmd.add(outputfile.getAbsolutePath());

        return getArray(cmd);
    }

    private String[] internalGetSecondPass() {

        if (internalGetType() != TYPE_TWO_PASS)
            return null;

        if (outputfile == null)
            return null;

        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add("ffmpeg");
        cmd.add("-y");
        cmd.add("-i");
        cmd.add(inputfile.getAbsolutePath());
        String videoBitrate = getVideoBitrate();

        if (videoBitrate == null)
            return null;

        addVideoConversion(cmd, videoBitrate);
        addAudioConversion(cmd);

        cmd.add("-movflags");
        cmd.add("+faststart");
        cmd.add("-pass");
        cmd.add("2");
        cmd.add(outputfile.getAbsolutePath());

        return getArray(cmd);
    }

    private String[] internalGetSinglePass() {

        int type = internalGetType();
        switch (type) {
            case TYPE_NO_GOOD:
                Log.v("");
            case TYPE_NO_CHANGE:
            case TYPE_TWO_PASS:
                return null;
        }

        outputfile = new File(getFilename("_temp.mp4", true));

        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add("ffmpeg");
        cmd.add("-i");
        cmd.add(inputfile.getAbsolutePath());
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
        cmd.add(outputfile.getAbsolutePath());

        return getArray(cmd);
    }

    private String[] internalGetSubtitle(int position) {

        Stream subtitle = null;

        // select the correct stream
        int subIndex = 0;
        int ffprobeSubIndex = 0;
        for (Stream s : probe.getStreams()) {
            if (CODEC_TYPE_SUBTITLE.equals(s.getCodec_type())) {
                if (SUB_CODEC.equals(s.getCodec_name())) {
                    if (subIndex == position) {
                        subtitle = s;
                    }
                    subIndex++;
                }
                if (subtitle == null)
                    ffprobeSubIndex++;
            }
        }

        if (subtitle == null)
            return null;

        // get the filename for the
        String language = null;
        Tags t = subtitle.getTags();
        if (t != null) {
            language = t.getLanguage();
        }

        String postfix;
        if (language == null) {
            postfix = ".srt";
        } else {
            postfix = "_" + language + ".srt";
        }

        String fileName = getFilename(postfix, true);

        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add("ffmpeg");
        cmd.add("-i");
        cmd.add(inputfile.getAbsolutePath());
        cmd.add("-vn");
        cmd.add("-an");
        cmd.add("-codec:s:" + ffprobeSubIndex);
        cmd.add("srt");
        cmd.add(fileName);

        return getArray(cmd);
    }

    //
    // internal helpers
    // =================================================================================================================
    private String getFilename(String extension, boolean mustBeUnique) {
        File f = null;
        int unique = 0;
        while (f == null || f.exists()) {
            File folder = inputfile.getParentFile();
            String name = inputfile.getName();
            String fileName = FilenameUtils.removeExtension(name) + extension;
            f = new File(folder, fileName);
            if (!mustBeUnique)
                return f.getAbsolutePath();
            unique++;
            extension = "_" + Integer.toString(unique) + extension;
        }
        return f.getAbsolutePath();
    }

    private String[] getArray(List<String> list) {
        String[] strings = new String[list.size()];
        strings = list.toArray(strings);
        return strings;
    }

    private Stream getStream(String name) {
        for (int i = 0; i < probe.getStreams().size(); i++)
            if (name.equals(probe.getStreams().get(i).getCodec_type()))
                return probe.getStreams().get(i);
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
                // aac doesn't need to be bigger than that
                long abr = Long.parseLong(audioBitRate);
                if (abr > MAX_AUDIO_BIT_RATE_PER_CHANNEL * audioStream.getChannels()) {
                    bitRateAdded = true;
                    cmd.add("-b:a");
                    cmd.add(Long.toString(MAX_AUDIO_BIT_RATE_PER_CHANNEL * audioStream.getChannels()));
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
        I found cases where mpeg1 streams return the uncompressed bitrate, rendering absurdly high bit rates
        Those cases we try again, by using the file bitrate, if that still too big, we just use the MAX val
        */

        long br = getVideoBitrateBasedOnVideoStreamBitRate();
        if (br <= 0 || br > MAX_VIDEO_BIT_RATE) {
            br = getVideoBitrateBasedOnFileBitrate();
        }

        if (br <= 0 || br > MAX_VIDEO_BIT_RATE) {
            br = MAX_VIDEO_BIT_RATE;
        }
        return Long.toString(br);
    }

    private long getVideoBitrateBasedOnFileBitrate() {
        // file bitrate
        String fileBitrate = probe.getFormat().getBit_rate();
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
            abr = DEFAULT_AUDIO_BIT_RATE_PER_CHANNEL * audioStream.getChannels();
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
