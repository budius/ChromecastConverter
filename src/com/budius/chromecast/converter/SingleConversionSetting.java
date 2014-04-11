package com.budius.chromecast.converter;

import com.budius.chromecast.converter.ffprobe_model.Probe;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Locale;

/**
 * Created by budius on 06.04.14.
 */
public class SingleConversionSetting implements Settings.Interface {

    private static final String VIDEO_EXT = ".mp4";
    private static final String SUBT_EXT = ".srt";

    private final File originalVideoFile;
    private final File outputFolder;
    private final Probe ffProbe;

    private final File tempVideoFile;
    private final File outputVideoFile;

    public SingleConversionSetting(File originalVideoFile, File outputFolder, Probe ffProbe) {
        this.originalVideoFile = originalVideoFile;
        this.outputFolder = outputFolder;
        this.ffProbe = ffProbe;

        tempVideoFile = getFilename("_temp_" + Long.toString(System.currentTimeMillis()) + VIDEO_EXT);
        outputVideoFile = getFilename(VIDEO_EXT);

    }

    public File getOriginalVideoFile() {
        return originalVideoFile;
    }

    public File getOutputFolder() {
        return outputFolder;
    }

    public Probe getFfProbe() {
        return ffProbe;
    }

    public File getTempVideoFile() {
        return tempVideoFile;
    }

    public File getOutputVideoFile() {
        return outputVideoFile;
    }

    public File getSubtitleFileName(String language_code) {
        return getFilename("_" + language_code.toUpperCase(Locale.ENGLISH) + SUBT_EXT);
    }

    private File getFilename(String extension) {
        File f = null;
        int unique = 0;
        while (f == null || f.exists()) {

            if (f != null) { // not first run anymore
                unique++;
                extension = "_" + Integer.toString(unique) + extension;
            }

            String name = originalVideoFile.getName();
            String fileName = FilenameUtils.removeExtension(name) + extension;
            f = new File(outputFolder, fileName);
        }
        return f;
    }

    @Override
    public boolean deleteOriginalFileOnSuccessfulConversion() {
        return Main.getSettings().deleteOriginalFileOnSuccessfulConversion();
    }

    @Override
    public int getQuality() {
        return Main.getSettings().getQuality();
    }
}
