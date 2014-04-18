package com.budius.chromecast.converter;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooserBuilder;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MainController implements ExecutionControl.ProgressListener, Settings.Interface {

    private Control[] controls;
    public Button btnInputFile;
    public Button btnInputFolder;
    public Button btnOutputFolder;
    public Button btnGo;
    public ListView list;
    public CheckBox checkDelete;
    public ComboBox comboQuality;
    public ComboBox comboSpeed;
    public TextField textOutputFolder;
    public TextField textInputFolder;
    public Label txtProgress;

    private Executor BACKGROUND = Executors.newSingleThreadExecutor();
    private File lastFolder = null;

    public void initialize() {
        System.out.println("onStart");
        comboQuality.getSelectionModel().select(Settings.QUALITY_HIGH);
        comboSpeed.getSelectionModel().select(Settings.SPEED_SLOW);
        checkDelete.setSelected(true);

        controls = new Control[]{
                btnInputFile, btnInputFolder, btnOutputFolder, btnGo,
                checkDelete, comboQuality, comboSpeed,
                textOutputFolder, textInputFolder};

        list.setItems(Log.debug);
        textInputFolder.setText("_");
        textOutputFolder.setText("_");

    }

    @Override
    public void onProgressUpdate(int processed, int total) {
        total_video_files.set(total);
        processed_video_files.set(processed);
        Platform.runLater(updateListenerRunnable);
    }

    @Override
    public void onComplete() {
        for (Control c : controls) {
            c.setDisable(false);
        }
    }


    private File pickFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a file:");

        File system = new File(System.getProperty("user.home"));
        File initial = lastFolder == null ? system : lastFolder;
        if (initial.exists())
            chooser.setInitialDirectory(initial);

        File chosen = chooser.showOpenDialog(JavaFxApplication.stage);
        if (chosen != null)
            lastFolder = chosen.getParentFile();

        return chosen;
    }

    private File pickFolder() {
        DirectoryChooserBuilder b = DirectoryChooserBuilder.create();
        b.title("Choose a folder");

        File system = new File(System.getProperty("user.home"));
        File initial = lastFolder == null ? system : lastFolder;
        if (initial.exists())
            b.initialDirectory(initial);

        File chosen = b.build().showDialog(JavaFxApplication.stage);
        if (chosen != null)
            lastFolder = chosen;

        return chosen;
    }

    private AtomicInteger processed_video_files = new AtomicInteger();
    private AtomicInteger total_video_files = new AtomicInteger();
    private Runnable updateListenerRunnable = new Runnable() {
        @Override
        public void run() {
            txtProgress.setText("Progress: " + processed_video_files.get() + "/" + total_video_files.get());
        }
    };

    //
    // clicks
    // =================================================================================================================
    public void click_btnInputFile(ActionEvent actionEvent) {
        System.out.println("click_btnInputFile");
        File f = pickFile();
        if (f != null) {
            textInputFolder.setText(f.getAbsolutePath());
            textOutputFolder.setText(f.getParentFile().getAbsolutePath());
        }
    }

    public void click_btnInputFolder(ActionEvent actionEvent) {
        System.out.println("click_btnInputFolder");
        File f = pickFolder();
        if (f != null) {
            textInputFolder.setText(f.getAbsolutePath());
            textOutputFolder.setText(f.getAbsolutePath());
        }
    }

    public void click_btnOutputFolder(ActionEvent actionEvent) {
        System.out.println("click_btnOutputFolder");
        File f = pickFolder();
        if (f != null)
            textOutputFolder.setText(f.getAbsolutePath());
    }

    public void click_btnGo(ActionEvent actionEvent) {
        System.out.println("click_btnGo");

        File input = new File(textInputFolder.getText());
        File output = new File(textOutputFolder.getText());

        if (input.exists() && output.exists() && output.isDirectory()) {
            Log.clear();
            Main.setSettings(this);

            // get settings
            deleteOnConversion = checkDelete.isSelected();
            quality = comboQuality.getSelectionModel().getSelectedIndex();
            speed = comboSpeed.getSelectionModel().getSelectedIndex();

            Log.d("=====================================================================================================");
            Log.d(deleteOnConversion ? "Delete on success." : "Do not delete on success");
            Log.d("Speed: " + Settings.ARRAY_SPEED[speed]);
            Log.d("Quality: " + Settings.ARRAY_QUALITY[quality]);
            Log.d("Settings:");

            for (Control c : controls) {
                c.setDisable(true);
            }

            ExecutionControl ec = new ExecutionControl(input, output, this);
            BACKGROUND.execute(ec);

        } else {
            Log.d("Invalid input/output folder");
        }


    }


    private boolean deleteOnConversion;
    private int quality;
    private int speed;

    @Override
    public boolean deleteOriginalFileOnSuccessfulConversion() {
        return deleteOnConversion;
    }

    @Override
    public int getQuality() {
        return quality;
    }

    @Override
    public int getSpeed() {
        return speed;
    }
}
