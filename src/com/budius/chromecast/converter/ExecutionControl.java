package com.budius.chromecast.converter;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * It's a runnable that executes FFPROBE, FFMPEG and clear temp files on a single file or folder (recursively)
 */
public class ExecutionControl implements Runnable {

    private static final List<String> VIDEO_EXTENSION =
            Arrays.asList("mp4", "mkv", "avi", "mpeg", "mpg", "mpe", "mov", "qt", "asf", "flv", "wmv", "m1v", "m2v", "3gp");

    private final File input, output;
    private final ProgressListener listener;
    private int total_video_files;
    private int processed_video_files;
    private final File inputParentFolder;

    public ExecutionControl(File input, File output, ProgressListener listener) {
        this.input = input;
        this.listener = listener;
        inputParentFolder = input.isDirectory() ? input : input.getParentFile();

        if (output == null) {
            this.output = inputParentFolder;
        } else {
            this.output = output;
        }

        Log.setFileLogPath(output);

    }

    @Override
    public void run() {
        processed_video_files = 0;
        if (input.isDirectory()) {
            total_video_files = getVideoFileCount(input);
            internalProgressListener.onProgressUpdate(processed_video_files, total_video_files);
            executeFolder(input);
        } else {
            total_video_files = 1;
            internalProgressListener.onProgressUpdate(processed_video_files, total_video_files);
            executeFile(input);
        }
        internalProgressListener.onComplete();
    }

    //
    // the real work gets done here
    // =========================================================================================================
    private void executeFolder(File folder) {

        List<File> files = Arrays.asList(folder.listFiles(videoFilesFilter));
        Collections.sort(files, alphabeticalOrderFiles);

        for (File f : files) {

            if (isCancel())
                return;

            if (f.isDirectory()) {
                executeFolder(f);
            } else {
                executeFile(f);
            }
        }
    }

    private void executeFile(File file) {

        if (isCancel())
            return;

        Log.d("================================================================================");

        // get data
        FFProbe ffProbe = new FFProbe(file);
        if (ffProbe.getData() != null) {

            // get output folder relative to this file and the parent folder
            File thisFileInputFolder = file.getParentFile();
            File thisFileOutputFolder = new File(output, thisFileInputFolder.getAbsolutePath().replace(inputParentFolder.getAbsolutePath(), ""));


            // generate single conversion settings
            SingleConversionSetting scs = new SingleConversionSetting(file, thisFileOutputFolder, ffProbe.getData());

            // execute ffmpeg
            FFMpeg ffmpeg = new FFMpeg(scs);
            ffmpeg.run();

            // clean up ffmpeg temp files
            File folder = file.getParentFile();
            File[] tempFiles = folder.listFiles(tempFilesFilter);
            for (File f : tempFiles) {
                f.delete();
            }
        } else {
            Log.e("Failed to get FFPROBE for " + file.getAbsolutePath());
            Log.fileLog("Failed to get FFPROBE for " + file.getAbsolutePath());
        }

        processed_video_files++;
        internalProgressListener.onProgressUpdate(processed_video_files, total_video_files);

    }

    //
    // Filters
    // =========================================================================================================
    private FilenameFilter tempFilesFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.startsWith("ffmpeg") &&
                    (name.endsWith("log") || name.endsWith("mbtree")) &&
                    name.contains("2pass");
        }
    };

    private FileFilter videoFilesFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            String ext = FilenameUtils.getExtension(pathname.getAbsolutePath()).toLowerCase();
            return pathname.isDirectory() || VIDEO_EXTENSION.contains(ext);
        }
    };

    private Comparator<File> alphabeticalOrderFiles = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    //
    // Helpers
    // =========================================================================================================
    private int getVideoFileCount(File folder) {
        int count = 0;
        for (File f : folder.listFiles(videoFilesFilter)) {
            if (f.isDirectory()) {
                count += getVideoFileCount(f);
            } else {
                count++;
            }
        }
        return count;
    }

    private boolean isCancel() {
        return Thread.currentThread().isInterrupted();
    }

    //
    // Interface
    // =========================================================================================================
    public interface ProgressListener {
        public void onProgressUpdate(int processed, int total);

        public void onComplete();
    }

    private ProgressListener internalProgressListener = new ProgressListener() {

        @Override
        public void onProgressUpdate(int processed, int total) {
            if (listener != null)
                listener.onProgressUpdate(processed, total);
        }

        @Override
        public void onComplete() {
            if (listener != null)
                listener.onComplete();
        }
    };
}