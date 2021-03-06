package org.fabulinus.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.fabulinus.exporting.Exporter;
import org.fabulinus.gui.widgets.FileChooserWidget;
import org.fabulinus.gui.widgets.FileSelectionListener;
import org.fabulinus.importing.ImportListener;
import org.fabulinus.importing.Importer;
import org.fabulinus.model.Content;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;

/**
 * Created by Timon on 06.07.2015.
 */
public class CurriculumExporter extends Application implements ImportListener, FileSelectionListener {
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getPercentInstance();
    private final FileChooserWidget fileChooserWidget;
    private final TreeView<Content> contentView;
    private final ProgressBar progressBar;
    private final Label statusLabel;
    private final List<Content> selectedContent;

    public static void main(String[] args) {
        Application.launch();
    }

    public CurriculumExporter() {
        fileChooserWidget = new FileChooserWidget(this);
        contentView = new TreeView<>();
        progressBar = new ProgressBar(0);
        statusLabel = new Label("Ready to import");
        selectedContent = new ArrayList<>();
        setLayout();
    }

    private void setLayout(){
        Insets insets = new Insets(5);
        contentView.setPadding(insets);
        statusLabel.setPadding(insets);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane borderPane = new BorderPane(contentView);
        borderPane.setTop(fileChooserWidget);
        borderPane.setBottom(new VBox(progressBar, statusLabel));

        Scene scene = new Scene(borderPane, 600, 400);
        progressBar.prefWidthProperty().bind(scene.widthProperty());
        primaryStage.setScene(scene);
        trySetIcon(primaryStage);
        primaryStage.setTitle("Curriculum Exporter");
        primaryStage.setOnCloseRequest(handler -> {
            if (!selectedContent.isEmpty()) {
                showExportDialog();
            }
        });
        primaryStage.show();
    }

    private void trySetIcon(Stage stage){
        try {
            InputStream is = getClass().getResourceAsStream("/icon.gif");
            stage.getIcons().add(new Image(is));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void showExportDialog(){
        String fileName = "export-" + new Date().getTime();
        String path = System.getProperty("user.home") + File.separator + fileName;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        trySetIcon((Stage) alert.getDialogPane().getScene().getWindow());
        alert.setTitle("Save export");
        alert.setHeaderText("Would you like to save this curriculum?");
        alert.setContentText(String.format("Path: '%s'", path));

        ButtonType buttonYes = new ButtonType("Yes");
        ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonYes, buttonNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonYes){
            export(path);
        }
    }

    private void export(String path){
        Exporter exporter = new Exporter(new File(path), selectedContent);
        try {
            exporter.start();
        } catch (Exception e){
            onError("Could not export file! (" + e.getMessage() + ")");
        }
    }

    @Override
    public void onFileSelected(File file) {
        new Importer(file, this).start();
        statusLabel.setText("Importing...");
    }

    @Override
    public void onProgress(double progress) {
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
            if (progress != -1) {
                statusLabel.setText(NUMBER_FORMAT.format(progress));
            }
        });
    }

    @Override
    public void onImportDone(Content content) {
        Platform.runLater(() -> {
            statusLabel.setText("Import done!");
            CheckBoxTreeItem<Content> root = new CheckBoxTreeItem<>(content);
            root.setExpanded(true);
            fillContentView(root, content.getChildren());
            contentView.setCellFactory(CheckBoxTreeCell.<Content>forTreeView());
            contentView.setRoot(root);
        });
    }

    private CheckBoxTreeItem<Content> fillContentView(CheckBoxTreeItem<Content> root, Collection<Content> children){
        for (Content child : children) {
            CheckBoxTreeItem<Content> item = new CheckBoxTreeItem<>(child);
            fillContentView(item, child.getChildren());
            root.getChildren().add(item);
        }

        root.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                if (root.getChildren().isEmpty()) {
                    selectedContent.add(root.getValue());
                }
            } else {
                selectedContent.remove(root.getValue());
            }
            statusLabel.setText(String.format("%d items selected.", selectedContent.size()));
        });
        return root;
    }

    @Override
    public void onError(String error) {
        Platform.runLater(() -> {
            statusLabel.setText("Error: " + error);
        });
    }
}
