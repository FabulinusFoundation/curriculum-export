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
import org.fabulinus.gui.widgets.FileChooserWidget;
import org.fabulinus.gui.widgets.FileSelectionListener;
import org.fabulinus.importing.ImportListener;
import org.fabulinus.importing.Importer;
import org.fabulinus.model.Content;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by Timon on 06.07.2015.
 */
public class CurriculumExporter extends Application implements ImportListener, FileSelectionListener {
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

        Scene scene = new Scene(borderPane, 800, 600);
        progressBar.prefWidthProperty().bind(scene.widthProperty());
        primaryStage.setScene(scene);
        safeSetIcon(primaryStage);
        primaryStage.setTitle("Curriculum Exporter");
        primaryStage.show();
    }

    private void safeSetIcon(Stage stage){
        try {
            InputStream is = getClass().getResourceAsStream("/icon.gif");
            stage.getIcons().add(new Image(is));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        if (!selectedContent.isEmpty()) {
            showExportDialog();
        }
        super.stop();
    }

    private void showExportDialog(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        safeSetIcon((Stage) alert.getDialogPane().getScene().getWindow());
        alert.setTitle("Save export");
        alert.setHeaderText("Would you like to save the export?");

        ButtonType buttonYes = new ButtonType("Yes");
        ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonYes, buttonNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonYes){
            export();
        }
    }

    private void export(){
        try {
            String fileName = "export-" + new Date().getTime();
            File file = new File(System.getProperty("user.home") + File.separator + fileName);
            FileWriter writer = new FileWriter(file, true);
            BufferedWriter out = new BufferedWriter(writer);
            for (Content content : selectedContent) {
                out.append(content.description());
                out.newLine();
            }
            out.flush();
            out.close();
        } catch (IOException e){
            onError("Could not export file! (" + e.getMessage() + ")");
        }
    }

    @Override
    public void onFileSelected(File file) {
        new Importer(file, this).start();
    }

    @Override
    public void onProgress(double progress) {
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
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
