package com.budius.chromecast.converter;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooserBuilder;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MainController implements ExecutionControl.ProgressListener {

    public Button btnGo;
    private Executor BACKGROUND = Executors.newSingleThreadExecutor();
    public ListView listRuntimeExec;
    public ListView listLog;
    public TextField textFolder;
    public Label txtProgress;
    private File file;
    private File currentDir;

    public void click_selectFolder(ActionEvent actionEvent) {
        File file = pickFolder();
        if (file == null) {
            btnGo.setDisable(true);
            textFolder.setText("");
        } else {
            btnGo.setDisable(false);
            this.file = file;
            textFolder.setText(file.getAbsolutePath());
        }
    }

    public void click_selectFile(ActionEvent actionEvent) {
        File file = pickFile();
        if (file == null) {
            btnGo.setDisable(true);
            textFolder.setText("");
        } else {
            btnGo.setDisable(false);
            this.file = file;
            textFolder.setText(file.getAbsolutePath());
        }
    }

    public void click_Go(ActionEvent actionEvent) {

        processed_video_files.set(0);

        Log.clear();

        listLog.setItems(Log.verbose);
        listRuntimeExec.setItems(Log.debug);

        if (file != null && file.exists()) {

            textFolder.setDisable(true);
            btnGo.setDisable(true);

            ExecutionControl ec = new ExecutionControl(file, null, this);
            BACKGROUND.execute(ec);
        }
    }

    private File pickFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a file:");
        chooser.setInitialDirectory(getCurrentDir());
        return setCurrentDir(chooser.showOpenDialog(JavaFxApplication.stage));
    }

    private File pickFolder() {
        DirectoryChooserBuilder b = DirectoryChooserBuilder.create();
        b.title("Choose a folder");
        b.initialDirectory(getCurrentDir());
        return setCurrentDir(b.build().showDialog(JavaFxApplication.stage));
    }

    private File getCurrentDir() {
        if (currentDir == null) {
            String dir = System.getProperty("user.home");
            File newCurrentDir = new File(dir);
            if (newCurrentDir.exists() && newCurrentDir.isDirectory()) {
                return newCurrentDir;
            } else {
                return null;
            }
        } else {
            return currentDir;
        }
    }

    private File setCurrentDir(File file) {
        if (file != null)
            if (file.isDirectory()) {
                currentDir = file;
            } else {
                currentDir = file.getParentFile();
            }
        return file;
    }

    @Override
    public void onProgressUpdate(int processed, int total) {
        processed_video_files.set(processed);
        total_video_files.set(total);
        Platform.runLater(updateListenerRunnable);
    }

    @Override
    public void onComplete() {
        textFolder.setDisable(false);
        btnGo.setDisable(false);
    }

    private AtomicInteger processed_video_files = new AtomicInteger();
    private AtomicInteger total_video_files = new AtomicInteger();
    private Runnable updateListenerRunnable = new Runnable() {
        @Override
        public void run() {
            txtProgress.setText("Progress: " + processed_video_files.get() + " of " + total_video_files.get());
        }
    };
}
