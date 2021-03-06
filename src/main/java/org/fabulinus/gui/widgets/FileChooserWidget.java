package org.fabulinus.gui.widgets;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
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
        setOnTextFieldEnter();
    }

    private void setLayout(){
        setCenter(pathField);
        setRight(browseButton);
        setPadding(new Insets(5));
    }

    private void setOnBrowseButtonClick(){
        browseButton.setOnAction(action -> {
            File selectedFile = fileChooser.showOpenDialog(new Stage());
            if (selectedFile != null) {
                pathField.setText(selectedFile.toString());
                listener.onFileSelected(selectedFile);
            } else {
                listener.onError("No file selected.");
            }
        });
    }

    private void setOnTextFieldEnter(){
        pathField.setOnKeyPressed(event -> {
            if (KeyCode.ENTER.equals(event.getCode())){
                String path = pathField.getText();
                File file = new File(path);
                if (file.exists()){
                    listener.onFileSelected(file);
                } else {
                    listener.onError(String.format("File '%s' does not exist!", path));
                }
            }
        });
    }
}
