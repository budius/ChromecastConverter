package com.budius.chromecast.converter;

import javafx.application.Platform;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * It's a runnable that executes FFPROBE, FFMPEG and clear temp files on a single file or folder (recursively)
 */
public class ExecutionControl implements Runnable {

    private static final List<String> VIDEO_EXTENSION =
            Arrays.asList("mp4", "mkv", "avi", "mpeg", "mpg", "mpe", "mov", "qt", "asf", "flv", "wmv", "m1v", "m2v", "3gp");

    private final File file;
    private final ProgressListener listener;
    private int total_video_files;
    private AtomicInteger processed_video_files;

    public ExecutionControl(File file, ProgressListener listener) {
        this.file = file;
        this.listener = listener;
    }

    @Override
    public void run() {
        processed_video_files = new AtomicInteger();
        processed_video_files.set(0);
        if (file.isDirectory()) {
            Log.setFileLogPath(file);
            total_video_files = getVideoFileCount(file);
            Platform.runLater(updateListenerRunnable);
            executeFolder(file);
        } else {
            Log.setFileLogPath(file.getParentFile());
            total_video_files = 1;
            Platform.runLater(updateListenerRunnable);
            executeFile(file);
        }
        if (listener != null)
            listener.onComplete();
    }

    private void executeFolder(File folder) {
        for (File f : folder.listFiles(videoFilesFilter)) {
            if (f.isDirectory()) {
                executeFolder(f);
            } else {
                executeFile(f);
            }
        }
    }

    private void executeFile(File file) {
        Log.d("================================================================================");

        // get data
        FFProbe ffProbe = new FFProbe(file);
        if (ffProbe.getData() != null) {

            // execute ffmpeg
            FFMpeg ffmpeg = new FFMpeg(ffProbe.getFile(), ffProbe.getData());
            ffmpeg.run();

            // clean up ffmpeg temp files
            File folder = file.getParentFile();
            File[] tempFiles = folder.listFiles(tempFilesFilter);
            for (File f : tempFiles) {
                f.delete();
            }
            processed_video_files.incrementAndGet();
            Platform.runLater(updateListenerRunnable);
        } else {
            Log.e("Failed to get FFPROBE for " + file.getAbsolutePath());
            Log.fileLog("Failed to get FFPROBE for " + file.getAbsolutePath());
        }
    }

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

    public interface ProgressListener {
        public void onProgressUpdate(int processed, int total);

        public void onComplete();
    }

    private Runnable updateListenerRunnable = new Runnable() {
        @Override
        public void run() {
            if (listener != null)
                listener.onProgressUpdate(processed_video_files.get(), total_video_files);
        }
    };
}