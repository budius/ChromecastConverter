package com.budius.chromecast.converter;

import java.io.File;
import java.util.Arrays;

/**
 * Executes FFMPEG into one specific file.
 * Deletes the old and rename the newly created to match the old (except by the extension)
 */
public class FFMpeg implements Runnable {

    private final SingleConversionSetting conversionSetting;
    private final FFMpegBuilder builder;
    private final File runtimeFolder;

    public FFMpeg(SingleConversionSetting scs) {
        conversionSetting = scs;
        builder = new FFMpegBuilder(scs);
        runtimeFolder = conversionSetting.getOriginalVideoFile().getParentFile();
    }

    @Override
    public void run() {

        int type = builder.getType();

        if (type == FFMpegBuilder.TYPE_NO_GOOD) {
            Log.d("Cannot understand file " + conversionSetting.getOriginalVideoFile().getAbsolutePath());
            Log.fileLog("Cannot understand file " + conversionSetting.getOriginalVideoFile().getAbsolutePath());
        } else if (type == FFMpegBuilder.TYPE_NO_CHANGE) {
            Log.d("No need to change " + conversionSetting.getOriginalVideoFile().getAbsolutePath());
            Log.fileLog("No need to change " + conversionSetting.getOriginalVideoFile().getAbsolutePath());
        } else if (convertVideo(type)) {
            generateSubtitle();
            File outputFile = conversionSetting.getTempVideoFile();

            if (!outputFile.exists() || outputFile.length() == 0) {
                if (outputFile.exists())
                    outputFile.delete();
                Log.d("Fail!");
                Log.fileLog("Failed to convert " + conversionSetting.getOriginalVideoFile().getAbsolutePath());
            } else {
                try {
                    if (conversionSetting.deleteOriginalFileOnSuccessfulConversion()) {
                        if (!conversionSetting.getOriginalVideoFile().delete()) {
                            Log.d("Fail to delete file");
                            Log.fileLog("Fail to delete file " + conversionSetting.getOriginalVideoFile().getAbsolutePath());
                        }
                    }
                    File newName = conversionSetting.getOutputVideoFile();
                    if (!outputFile.renameTo(newName)) {
                        Log.d("Fail to rename file " + outputFile.getAbsolutePath() + " to " + newName.getAbsolutePath());
                        Log.fileLog("Fail to rename file " + outputFile.getAbsolutePath() + " to " + newName.getAbsolutePath());
                    }

                } catch (Exception e) {
                    Log.d("Fail to delete-rename file");
                    Log.fileLog("Fail to delete-rename file " + conversionSetting.getOriginalVideoFile().getAbsolutePath());
                }
            }
            Log.d("Complete!");
        } else {
            Log.d("Fail!");
            Log.fileLog("Failed to convert " + conversionSetting.getOriginalVideoFile().getAbsolutePath());
        }
    }

    private boolean convertVideo(int type) {

        if (false) {
            return true;
        }

        long startTime = System.currentTimeMillis();
        String[] cmd;
        RuntimeExec rt;

        switch (type) {
            case FFMpegBuilder.TYPE_CONVERT_ONLY_AUDIO:
            case FFMpegBuilder.TYPE_JUST_CHANGE_CONTAINER:
            case FFMpegBuilder.TYPE_CRF:
                Log.d("Starting single pass for file: " + conversionSetting.getOriginalVideoFile().getAbsolutePath());
                cmd = builder.getSinglePass();
                if (cmd != null) {
                    logCmd(cmd);
                    rt = new RuntimeExec(cmd, runtimeFolder, RuntimeExec.VERBOSE);
                    if (rt.execute()) {
                        Log.d("Completed in " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds");
                        return true;
                    }
                }
                break;
            case FFMpegBuilder.TYPE_TWO_PASS:
                Log.d("Starting first pass for file: " + conversionSetting.getOriginalVideoFile().getAbsolutePath());
                cmd = builder.getFirstPass();
                if (cmd != null) {
                    logCmd(cmd);
                    rt = new RuntimeExec(cmd, runtimeFolder, RuntimeExec.VERBOSE);
                    if (rt.execute()) {
                        Log.d("First pass completed in " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds");
                        startTime = System.currentTimeMillis();
                        cmd = builder.getSecondPass();
                        if (cmd != null) {
                            logCmd(cmd);
                            rt = new RuntimeExec(cmd, runtimeFolder, RuntimeExec.VERBOSE);
                            if (rt.execute()) {
                                Log.d("Second pass completed in " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds");
                                return true;
                            }
                        }
                    }
                }
                break;
        }
        Log.e("Fail to convert " + conversionSetting.getOriginalVideoFile().getAbsolutePath());
        return false;
    }

    private void generateSubtitle() {
        int numberOfSubs = builder.getNumberOfSubtitles();
        if (numberOfSubs > 0) {
            RuntimeExec rt;
            for (int i = 0; i < numberOfSubs; i++) {
                Log.d("Generating subtitle " + i + " for: " + conversionSetting.getOriginalVideoFile().getAbsolutePath());
                String[] cmd = builder.getSubtitle(i);
                rt = new RuntimeExec(cmd, runtimeFolder, RuntimeExec.VERBOSE);
                rt.execute();
            }
        }
    }

    private void logCmd(String[] cmd) {
        Log.d("Executing " + Arrays.toString(cmd).replace(",", " ").trim());
    }

}
