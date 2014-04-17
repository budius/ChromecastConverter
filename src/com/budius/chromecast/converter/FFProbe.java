package com.budius.chromecast.converter;

import com.budius.chromecast.converter.ffprobe_model.Probe;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;

/**
 * Executes FFPROBE in a single file returning all data related to it
 */
public class FFProbe {

    public static final String CODEC_TYPE_VIDEO = "video";
    public static final String CODEC_TYPE_AUDIO = "audio";
    public static final String CODEC_TYPE_SUBTITLE = "subtitle";

    private final String[] CMD_JSON =
            {"ffprobe", "-v", "quiet", "-print_format", "json", "-show_format", "-show_streams", ""};

    private final String[] CMD_LOG =
            {"ffprobe", ""};

    private Probe data;
    private File file;

    public FFProbe(File file) {
        if (file.exists())
            this.file = file;
        else
            throw new RuntimeException("file must be a valid path");
    }

    public Probe getData() {
        if (data == null) {
            data = parse();
        }
        return data;
    }

    public File getFile() {
        return file;
    }

    private Probe parse() {

        Log.d("ffprobe parsing file: " + file.getAbsolutePath());

        String filePath = file.getAbsolutePath();

        // just for logging ===============================
        CMD_LOG[CMD_LOG.length - 1] = filePath;
        RuntimeExec rt = new RuntimeExec(CMD_LOG, null, RuntimeExec.VERBOSE);
        rt.execute();

        // real data ===============================
        CMD_JSON[CMD_JSON.length - 1] = filePath;
        rt = new RuntimeExec(CMD_JSON, null, RuntimeExec.STRING_RESPONSE);

        if (rt.execute()) {
            String response = rt.getResponse();
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(response, Probe.class);
        }

        return null;
    }
}
