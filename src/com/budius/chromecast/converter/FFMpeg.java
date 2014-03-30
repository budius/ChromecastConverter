package com.budius.chromecast.converter;

import com.budius.chromecast.converter.ffprobe_model.Probe;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Arrays;

/**
 * Executes FFMPEG into one specific file.
 * Deletes the old and rename the newly created to match the old (except by the extension)
 */
public class FFMpeg implements Runnable {

    private final File folder;
    private final File inputFile;
    private final FFMpegBuilder builder;

    public FFMpeg(File f, Probe p) {
        inputFile = f;
        folder = inputFile.getParentFile();
        builder = new FFMpegBuilder(inputFile, p);
    }

    @Override
    public void run() {

        int type = builder.getType();

        if (type == FFMpegBuilder.TYPE_NO_GOOD) {
            Log.d("Cannot understand file " + inputFile.getAbsolutePath());
            Log.fileLog("Cannot understand file " + inputFile.getAbsolutePath());
        } else if (type == FFMpegBuilder.TYPE_NO_CHANGE) {
            Log.d("No need to change " + inputFile.getAbsolutePath());
            Log.fileLog("No need to change " + inputFile.getAbsolutePath());
        } else if (convertVideo(type)) {
            generateSubtitle();
            File outputFile = builder.getGeneratedFile();

            if (!outputFile.exists() || outputFile.length() == 0) {
                if (outputFile.exists())
                    outputFile.delete();
                Log.d("Fail!");
                Log.fileLog("Failed to convert " + inputFile.getAbsolutePath());
            } else {
                try {
                    if (inputFile.delete()) {
                        File newName = getMp4Filename();
                        if (!outputFile.renameTo(newName)) {
                            Log.d("Fail to rename file " + outputFile.getAbsolutePath() + " to " + newName.getAbsolutePath());
                            Log.fileLog("Fail to rename file " + outputFile.getAbsolutePath() + " to " + newName.getAbsolutePath());
                        }
                    } else {
                        Log.d("Fail to delete file");
                        Log.fileLog("Fail to delete file " + inputFile.getAbsolutePath());
                    }
                } catch (Exception e) {
                    Log.d("Fail to delete-rename file");
                    Log.fileLog("Fail to delete-rename file " + inputFile.getAbsolutePath());
                }
            }
            Log.d("Complete!");
        } else {
            Log.d("Fail!");
            Log.fileLog("Failed to convert " + inputFile.getAbsolutePath());
        }
    }

    private boolean convertVideo(int type) {

        long startTime = System.currentTimeMillis();
        String[] cmd;
        RuntimeExec rt;

        switch (type) {
            case FFMpegBuilder.TYPE_CONVERT_ONLY_AUDIO:
            case FFMpegBuilder.TYPE_JUST_CHANGE_CONTAINER:
                Log.d("Starting single pass for file: " + inputFile.getAbsolutePath());
                cmd = builder.getSinglePass();
                if (cmd != null) {
                    logCmd(cmd);
                    rt = new RuntimeExec(cmd, folder, RuntimeExec.VERBOSE);
                    if (rt.execute()) {
                        Log.d("Completed in " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds");
                        return true;
                    }
                }
                break;
            case FFMpegBuilder.TYPE_TWO_PASS:
                Log.d("Starting first pass for file: " + inputFile.getAbsolutePath());
                cmd = builder.getFirstPass();
                if (cmd != null) {
                    logCmd(cmd);
                    rt = new RuntimeExec(cmd, folder, RuntimeExec.VERBOSE);
                    if (rt.execute()) {
                        Log.d("First pass completed in " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds");
                        startTime = System.currentTimeMillis();
                        cmd = builder.getSecondPass();
                        if (cmd != null) {
                            logCmd(cmd);
                            rt = new RuntimeExec(cmd, folder, RuntimeExec.VERBOSE);
                            if (rt.execute()) {
                                Log.d("Second pass completed in " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds");
                                return true;
                            }
                        }
                    }
                }
                break;
        }
        Log.e("Fail to convert " + inputFile.getAbsolutePath());
        return false;
    }

    private void generateSubtitle() {
        int numberOfSubs = builder.getNumberOfSubtitles();
        if (numberOfSubs > 0) {
            RuntimeExec rt;
            Log.d("Generating subtitles for: " + inputFile.getAbsolutePath());
            for (int i = 0; i < numberOfSubs; i++) {
                String[] cmd = builder.getSubtitle(i);
                rt = new RuntimeExec(cmd, folder, RuntimeExec.VERBOSE);
                rt.execute();
            }
        }
    }

    private void logCmd(String[] cmd) {
        Log.d("Executing " + Arrays.toString(cmd).replace(",", " ".trim()));
    }

    private File getMp4Filename() {
        String extension = ".mp4";
        File f = null;
        int unique = 0;
        while (f == null || f.exists()) {
            String name = inputFile.getName();
            String fileName = FilenameUtils.removeExtension(name) + extension;
            f = new File(folder, fileName);
            unique++;
            extension = "_" + Integer.toString(unique) + ".mp4";
        }
        return f;
    }
}
