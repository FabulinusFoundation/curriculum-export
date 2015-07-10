package org.fabulinus.gui.widgets;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Created by Timon on 09.07.2015.
 */
public class FileChooserWidget extends BorderPane {
    private final TextField pathField;
    private final Button browseButton;
    private final FileChooser fileChooser;
    private final FileSelectionListener listener;

    public FileChooserWidget(FileSelectionListener selectionListener) {
        super();
        String path = System.getProperty("user.home");
        pathField = new TextField(path);
        browseButton = new Button("Browse...");
        fileChooser = new FileChooser();
        listener = selectionListener;
        fileChooser.setInitialDirectory(new File(path));
        setLayout();
        setOnBrowseButtonClick();
    }

    private void setLayout(){
        setCenter(pathField);
        setRight(browseButton);
        setPadding(new Insets(5));
        pathField.setEditable(false);
    }

    private void setOnBrowseButtonClick(){
        browseButton.setOnAction(action -> {
            File selectedFile = fileChooser.showOpenDialog(new Stage());
            if (selectedFile != null) {
                pathField.setText(selectedFile.toString());
                listener.onFileSelected(selectedFile);
            }
        });
    }
}
