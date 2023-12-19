package com.example.richtexteditor;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jsoup.helper.W3CDom;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import java.io.*;
import org.jsoup.*;
import org.w3c.dom.Document;

import java.net.URL;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


public class RichTextEditor extends Application {
    AtomicBoolean push = new AtomicBoolean(true);
    AtomicBoolean created = new AtomicBoolean(false);
    static AtomicReference<File> chooseFile = new AtomicReference<>();
    @Override
    public void start(Stage stage) throws IOException {
        FileLibrary fileLibrary = FileLibrary.getFileLibrary();
        WebView webView = new WebView();
        Button undo = new Button("undo");
        Button redo = new Button("redo");
        Button rename = new Button("Rename");
        rename.setDisable(true);
        HBox ControlBox = new HBox(undo, redo);
        undo.setDisable(true);
        redo.setDisable(true);
        ControlBox.setAlignment(Pos.CENTER);
        TextArea textArea = new TextArea();
        WebEngine webEngine = webView.getEngine();
        VBox vBox = new VBox();
        HBox buttons = new HBox();
        buttons.setAlignment(Pos.CENTER);
        HBox files = new HBox();
        Button create = new Button("Create");
        Button open = new Button("Open");
        Button save = new Button("Save");
        Button saveAs = new Button("Save as");
        save.setDisable(true);
        saveAs.setDisable(true);
        FileChooser fileChooser = new FileChooser();
       HBox panel = new HBox();
       HBox fileNames = new HBox();
       fileNames.setAlignment(Pos.CENTER);
       ScrollPane scrollPane = new ScrollPane(fileNames);
        scrollPane.setFitToHeight(true);
        fileNames.setFillHeight(true);
       panel.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(buttons, ControlBox, files, scrollPane, panel);
        panel.setAlignment(Pos.CENTER);
        files.setAlignment(Pos.CENTER);
        TextArea redact = new TextArea();
        Button copy = new Button("Copy");
        Button paste = new Button("Paste");
        Button cut = new Button("Cut");
        Button delete = new Button("Delete");
        Button replace = new Button("Replace");
        Button select = new Button("Select All");
        copy.setDisable(true);
        paste.setDisable(true);
        cut.setDisable(true);
        delete.setDisable(true);
        replace.setDisable(true);
        select.setDisable(true);
        MenuButton edit = new MenuButton("Edit");
        MenuItem Mcopy = new MenuItem();
        Mcopy.setGraphic(copy);
        MenuItem Mpaste = new MenuItem();
        Mpaste.setGraphic(paste);
        MenuItem Mcut = new MenuItem();
        Mcut.setGraphic(cut);
        MenuItem Mdelete = new MenuItem();
        Mdelete.setGraphic(delete);
        MenuItem Mreplace = new MenuItem();
        Mreplace.setGraphic(replace);
        MenuItem Mselect = new MenuItem();
        Mselect.setGraphic(select);
        edit.getItems().addAll(Mcopy, Mpaste, Mcut, Mdelete, Mreplace, Mselect);
        MenuButton m = new MenuButton("File");
        MenuItem menuItem = new MenuItem();
        menuItem.setGraphic(open);
        MenuItem menuItem1 = new MenuItem();
        menuItem1.setGraphic(create);
        MenuItem menuItem2 = new MenuItem();
        menuItem2.setGraphic(save);
        MenuItem menuItem3 = new MenuItem();
        MenuItem menuItem4 = new MenuItem();
        menuItem4.setGraphic(rename);
        menuItem3.setGraphic(saveAs);
        m.setAlignment(Pos.CENTER);
        m.getItems().addAll(menuItem, menuItem1, menuItem2, menuItem3, menuItem4);
        edit.setOnMouseEntered(event -> {
            edit.show();
        });

        edit.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue){
                edit.hide();
            }
        });
        m.setOnMouseEntered(event -> {
            m.show();
        });

        m.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue){
                m.hide();
            }
        });
        HBox buttonsContainer = new HBox(m, edit);
        buttonsContainer.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(buttonsContainer);
        redact.textProperty().addListener((observable, oldValue, newValue) -> {
            if(webEngine.getDocument()!= null){
                String doc = Jsoup.parse(redact.getText()).outerHtml();
                webEngine.executeScript("document.querySelector('html').innerHTML ="+"`"+doc+"`");
                if(webEngine.getDocument().getElementsByTagName("script").getLength() > 0){
                    String script = webEngine.getDocument().getElementsByTagName("script").item(0).getTextContent();
                    webEngine.executeScript(script);
                }
                AppFile appFile = fileLibrary.getFiles().stream()
                        .filter(c -> c.getFile().equals(chooseFile.get()))
                        .findFirst()
                        .orElse(null);
                if(appFile != null){
                    appFile.setDocument(redact.getText(), push.get());
                    push.set(true);
                }
                assert appFile != null;
                undo.setDisable(!appFile.canUndo());
                redo.setDisable(!appFile.canRedo());
            }
            copy.setOnAction(event -> {
                if(!redact.getSelectedText().isEmpty()) {
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    clipboard.clear();
                    ClipboardContent content = new ClipboardContent();
                    content.putString(redact.getSelectedText());
                    clipboard.setContent(content);
                }
            });
            cut.setOnAction(event -> {
                if(!redact.getSelectedText().isEmpty()) {
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    clipboard.clear();
                    ClipboardContent content = new ClipboardContent();
                    content.putString(redact.getSelectedText());
                    clipboard.setContent(content);
                    redact.replaceSelection("");
                }
            });
            paste.setOnAction(event -> {
                int position = redact.getCaretPosition();
                String result = redact.getText(position, redact.getText().length());
                Clipboard clipboard = Clipboard.getSystemClipboard();
                if(clipboard.hasString()){
                    result = redact.getText(0, position)+clipboard.getString()+result;
                    redact.setText(result);
                    redact.positionCaret(position);
                }
            });
            delete.setOnAction(event -> {
                if(!redact.getSelectedText().isEmpty()) {
                    redact.replaceSelection("");
                }
            });
            select.setOnAction(event -> {
                redact.selectAll();
            });
            replace.setOnAction(event -> {
                if(!redact.getSelectedText().isEmpty()) {
                    TextInputDialog textInputDialog = new TextInputDialog(redact.getSelectedText());
                    textInputDialog.setHeaderText("rename text");
                    Optional<String> text = textInputDialog.showAndWait();
                    String result = textInputDialog.getEditor().getText();
                    if(text.isPresent() && !textInputDialog.getEditor().getText().isEmpty() &&
                            !result.equals(redact.getSelectedText())){
                        redact.replaceSelection(text.get());
                    }}
            });
            undo.setOnAction(event -> {
                if(chooseFile.get() != null) {
                    AppFile appFile = fileLibrary.getFiles().stream()
                            .filter(c -> c.getFile().equals(chooseFile.get()))
                            .findFirst()
                            .orElse(null);
                    if (webEngine.getDocument() != null && appFile != null){
                        push.set(false);
                        redact.setText(appFile.undo());
                        }
                }
            });
            redo.setOnAction(event -> {
                if(chooseFile.get() != null) {
                    AppFile appFile = fileLibrary.getFiles().stream()
                            .filter(c -> c.getFile().equals(chooseFile.get()))
                            .findFirst()
                            .orElse(null);
                    if (webEngine.getDocument() != null && appFile != null){
                        push.set(false);
                        redact.setText(appFile.redo());
                    }
                }
            });

        });
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            AppFile appFile = fileLibrary.getFiles().stream()
                    .filter(c -> c.getFile().equals(chooseFile.get()))
                    .findFirst()
                    .orElse(null);
            if(appFile != null){
                assert appFile != null;
                appFile.setDocument(textArea.getText(), push.get());
                push.set(true);
                undo.setDisable(!appFile.canUndo());
                redo.setDisable(!appFile.canRedo());
                try {
                    if (Files.probeContentType(appFile.getFile().toPath()).equals("text/css")){
                        String styles = appFile.getDocument();
                        W3CDom w3CDom = new W3CDom();
                        for (int i = 0; i < fileLibrary.getFiles().size(); i++) {
                            Document document = w3CDom.fromJsoup(Jsoup.parse(fileLibrary.getFiles().get(i).getDocument()));
                            if(document != null && document.getElementsByTagName("link").getLength() > 0 &&
                                    document.getElementsByTagName("link").item(0).getAttributes().getNamedItem("href") != null){
                                String cssFile = document.getElementsByTagName("link").item(0).getAttributes().getNamedItem("href").getNodeValue();
                                Path htmlPath = Paths.get(fileLibrary.getFiles().get(i).getFile().toURI());
                                Path cssPath = Path.of(cssFile);
                                    Path htmlFile = Path.of(htmlPath.toFile().getParent());
                                    Path path = Path.of(htmlFile.resolve(cssPath).toUri());
                                    File res1 = new File(path.toUri());
                                    Path res2 = Path.of(appFile.getFile().getAbsolutePath());

                                    if(res1.getCanonicalPath().equals(res2.toString())){
                                        if(webEngine.getDocument()!= null) {
                                            org.jsoup.nodes.Document newdoc = Jsoup.parse(fileLibrary.getFiles().get(i).getDocument());
                                            if(newdoc.getElementsByTag("style").size() > 0){
                                                newdoc.getElementsByTag("style").get(0).text(styles);
                                            }
                                            else {
                                                newdoc.getElementsByTag("head").append("<style type='text/css'>"+
                                                        styles+"</style>");
                                            }
                                            String doc = newdoc.outerHtml();
                                            fileLibrary.getFiles().get(i).setDocument(doc, false);
                                            webEngine.executeScript("document.querySelector('html').innerHTML =" + "`" + doc + "`");
                                            if (webEngine.getDocument().getElementsByTagName("script").getLength() > 0) {
                                                String script = webEngine.getDocument().getElementsByTagName("script").item(0).getTextContent();
                                                webEngine.executeScript(script);
                                            }
                                        }
                                    }
                            }
                        }

                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                copy.setOnAction(event -> {
                    if(!textArea.getSelectedText().isEmpty()) {
                        Clipboard clipboard = Clipboard.getSystemClipboard();
                        clipboard.clear();
                        ClipboardContent content = new ClipboardContent();
                        content.putString(textArea.getSelectedText());
                        clipboard.setContent(content);
                    }
                });
                cut.setOnAction(event -> {
                    if(!textArea.getSelectedText().isEmpty()) {
                        Clipboard clipboard = Clipboard.getSystemClipboard();
                        clipboard.clear();
                        ClipboardContent content = new ClipboardContent();
                        content.putString(textArea.getSelectedText());
                        clipboard.setContent(content);
                        textArea.replaceSelection("");
                    }
                });
                paste.setOnAction(event -> {
                    int position = textArea.getCaretPosition();
                    String result = textArea.getText(position, textArea.getText().length());
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    if(clipboard.hasString()){
                        result = textArea.getText(0, position)+clipboard.getString()+result;
                        textArea.setText(result);
                        textArea.positionCaret(position);
                    }
                });
                delete.setOnAction(event -> {
                    if(!textArea.getSelectedText().isEmpty()) {
                        textArea.replaceSelection("");
                    }
                });
                select.setOnAction(event -> {
                    textArea.selectAll();
                });
                replace.setOnAction(event -> {
                    if(!textArea.getSelectedText().isEmpty()) {
                        TextInputDialog textInputDialog = new TextInputDialog(textArea.getSelectedText());
                        textInputDialog.setHeaderText("rename text");
                        Optional<String> text = textInputDialog.showAndWait();
                        String result = textInputDialog.getEditor().getText();
                        if(text.isPresent() && !textInputDialog.getEditor().getText().isEmpty() &&
                                !result.equals(textArea.getSelectedText())){
                        textArea.replaceSelection(text.get());
                    }}
                });
                undo.setOnAction(event -> {
                    if(chooseFile.get() != null) {
                        AppFile appf = fileLibrary.getFiles().stream()
                                .filter(c -> c.getFile().equals(chooseFile.get()))
                                .findFirst()
                                .orElse(null);
                        if (appf != null){
                            push.set(false);
                            textArea.setText(appf.undo());
                        }
                    }
                });
                redo.setOnAction(event -> {
                    if(chooseFile.get() != null) {
                        AppFile appf = fileLibrary.getFiles().stream()
                                .filter(c -> c.getFile().equals(chooseFile.get()))
                                .findFirst()
                                .orElse(null);
                        if (appf != null){
                            push.set(false);
                            textArea.setText(appf.redo());
                        }
                    }
        });}});
        open.setOnAction(event -> {
            fileChooser.setTitle("Open Resource File");
            File file;
            if (created.get()){
                file = new File(chooseFile.get().toURI());
            }
            else {
                file = fileChooser.showOpenDialog(stage);
            }
            if(file != null) {
                try {
                    AtomicReference<URL> url = new AtomicReference<URL>();
                    if (Files.probeContentType(file.toPath()) != null) {
                        if (fileLibrary.AddFile(file) || created.get()) {
                            chooseFile.set(file);
                            if (Files.probeContentType(file.toPath()).equals("text/html")) {
                                AppFile appFile = fileLibrary.getFiles().stream()
                                        .filter(c -> c.getFile().equals(chooseFile.get()))
                                        .findFirst()
                                        .orElse(null);
                                url.set(new URL(String.format("file:\\\\\\%s", file)));
                                webEngine.load(url.get().toExternalForm());
                                webEngine.setJavaScriptEnabled(true);
                                panel.getChildren().clear();
                                redact.setText(Files.readString(file.toPath()).toString());
                                appFile.setDocument(Jsoup.parse(redact.getText()).outerHtml(), true);
                                panel.getChildren().add(webView);
                                panel.getChildren().add(redact);

                            } else if (Files.probeContentType(file.toPath()).equals("text/plain")||
                            Files.probeContentType(file.toPath()).equals("text/css")) {
                                panel.getChildren().clear();
                                CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
                                decoder.onMalformedInput(CodingErrorAction.IGNORE);
                                InputStreamReader inputStreamReader = new InputStreamReader(
                                        new FileInputStream(file), decoder
                                );
                                BufferedReader bufferedReader = new BufferedReader( inputStreamReader);
                                StringBuilder data = new StringBuilder();
                                while (bufferedReader.ready()){
                                    data.append(bufferedReader.readLine());
                                    data.append("\n");
                                }
                                bufferedReader.close();
                                AppFile appFile = fileLibrary.getFiles().stream()
                                        .filter(c -> c.getFile().equals(chooseFile.get()))
                                        .findFirst()
                                        .orElse(null);
                                textArea.setText(data.toString());
                                panel.getChildren().add(textArea);
                            } else {
                                throw new IOException();
                            }
                            copy.setDisable(false);
                            paste.setDisable(false);
                            cut.setDisable(false);
                            delete.setDisable(false);
                            replace.setDisable(false);
                            select.setDisable(false);
                            rename.setDisable(false);
                            undo.setDisable(true);
                            redo.setDisable(true);
                            save.setDisable(false);
                            saveAs.setDisable(false);
                            FileButton name = new FileButton(file, file.getName());
                            RemoveButton remove = new RemoveButton(file);
                            remove.setText("x");
                            name.setText(name.getFileName());
                            HBox storageHbox = new HBox();
                            storageHbox.getChildren().addAll(name, remove);
                            fileNames.getChildren().add(storageHbox);
                            remove.setOnAction(event1 -> {
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setContentText("Save File?");
                                        Optional<ButtonType> result = alert.showAndWait();
                                        if (result.get() == ButtonType.OK) {
                                            AppFile appFile = fileLibrary.getFiles().stream()
                                                    .filter(c -> c.getFile().equals(remove.getFile()))
                                                    .findFirst()
                                                    .orElse(null);
                                            try {
                                                save(appFile);
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                fileLibrary.RemoveFile(remove.getFile());
                                fileNames.getChildren().remove(storageHbox);
                                if(fileLibrary.getFiles().isEmpty()){
                                    copy.setDisable(true);
                                    paste.setDisable(true);
                                    cut.setDisable(true);
                                    delete.setDisable(true);
                                    replace.setDisable(true);
                                    select.setDisable(true);
                                    save.setDisable(true);
                                    saveAs.setDisable(true);
                                    rename.setDisable(true);
                                }
                                panel.getChildren().clear();
                                undo.setDisable(true);
                                redo.setDisable(true);
                            });
                            name.setOnAction(event1 -> {
                                try {
                                    chooseFile.set(name.getFile());
                                    push.set(true);
                                        if (Files.probeContentType(chooseFile.get().toPath()).equals("text/html")) {
                                            AppFile appFile = fileLibrary.getFiles().stream()
                                                    .filter(c -> c.getFile().equals(chooseFile.get()))
                                                    .findFirst()
                                                    .orElse(null);
                                            redact.setText(appFile.getDocument());
                                            if(appFile != null && appFile.getDocument() != null){
                                                webEngine.executeScript("document.querySelector('html').innerHTML ="+"`"+appFile.getDocument()+"`");
                                                if(webEngine.getDocument()!= null){
                                                    if(webEngine.getDocument().getElementsByTagName("script").getLength() > 0){
                                                        String script = webEngine.getDocument().getElementsByTagName("script").item(0).getTextContent();
                                                        webEngine.executeScript(script);
                                                    }
                                                }
                                            }
                                            else {
                                                url.set(new URL(String.format("file:\\\\\\%s", chooseFile.get())));
                                                webEngine.load(url.get().toExternalForm());
                                                redact.setText(Files.readString(file.toPath()).toString());
                                            }
                                            webEngine.setJavaScriptEnabled(true);
                                            panel.getChildren().clear();
                                            panel.getChildren().add(webView);
                                            panel.getChildren().add(redact);

                                        } else if (Files.probeContentType(chooseFile.get().toPath()).equals("text/plain") ||
                                                Files.probeContentType(chooseFile.get().toPath()).equals("text/css")) {
                                            panel.getChildren().clear();
                                            AppFile appFile = fileLibrary.getFiles().stream()
                                                    .filter(c -> c.getFile().equals(chooseFile.get()))
                                                    .findFirst()
                                                    .orElse(null);

                                            if(appFile != null && appFile.getDocument() != null) {
                                                textArea.setText(appFile.getDocument());
                                            }
                                            else {
                                                CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
                                                decoder.onMalformedInput(CodingErrorAction.IGNORE);
                                                InputStreamReader inputStreamReader = new InputStreamReader(
                                                        new FileInputStream(chooseFile.get()), decoder
                                                );
                                                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                                                StringBuilder data = new StringBuilder();
                                                while (bufferedReader.ready()) {
                                                    data.append(bufferedReader.readLine());
                                                    data.append("\n");
                                                }
                                                bufferedReader.close();
                                                textArea.setText(data.toString());
                                            }
                                            rename.setDisable(false);
                                            remove.setFile(name.getFile());
                                            panel.getChildren().add(textArea);
                                        } else {
                                            throw new IOException();
                                        }
                                    undo.setDisable(true);
                                    redo.setDisable(true);
                                    } catch(IOException e){
                                        throw new RuntimeException(e);
                                    }
                            });
                            rename.setOnAction(ev -> {
                                if(!fileLibrary.getFiles().isEmpty() && chooseFile.get() != null) {
                                    AppFile appFile = fileLibrary.getFiles().stream()
                                            .filter(c -> c.getFile().equals(chooseFile.get()))
                                            .findFirst()
                                            .orElse(null);
                                    File fl = appFile.getFile();
                                    String fname = fl.getName().substring(0, fl.getName().lastIndexOf("."));
                                    String type = fl.getName().substring(fl.getName().lastIndexOf("."));
                                    TextInputDialog textInputDialog = new TextInputDialog(fname);
                                    textInputDialog.setHeaderText("rename file");
                                    Optional<String> filename = textInputDialog.showAndWait();
                                    String result = textInputDialog.getEditor().getText();
                                    if(filename.isPresent() && !textInputDialog.getEditor().getText().isEmpty() &&
                                            !result.equals(fname)){
                                        String newName = result+type;
                                        Path path = Path.of(fl.getParent());
                                        File out = new File(path+"\\"+newName);
                                        try {
                                            Path p = Files.move(fl.toPath(), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                            appFile.setFile(out);
                                            chooseFile.set(out);
                                            for (int i = 0; i < fileNames.getChildren().size(); i++) {
                                                ObservableList<Node> button = ( (HBox) fileNames.getChildren().get(i) ).getChildren();
                                                FileButton fileButton = (FileButton) button.get(0);
                                                RemoveButton removeButton = (RemoveButton) button.get(1);
                                                if(fileButton.getFile().equals(fl)){
                                                    fileButton.setFile(out);
                                                    fileButton.setFileName(out.getName());
                                                    fileButton.setText(fileButton.getFileName());
                                                    removeButton.setFile(out);
                                                }}

                                            } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }

                                    }

                            });


                        } else {
                            throw new IOException();
                        }
                    } else {
                        throw new IOException();
                    }

                }
             catch (IOException exception){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Wrong File");
                        alert.show();
                    }}
            created.set(false);
        });

        save.setOnAction(event -> {
            if(!fileLibrary.getFiles().isEmpty() && chooseFile.get() != null){
                AppFile appFile = fileLibrary.getFiles().stream()
                        .filter(c -> c.getFile().equals(chooseFile.get()))
                        .findFirst()
                        .orElse(null);
                try {
                    save(appFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        create.setOnAction(event -> {
            Label label = new Label("Enter file name");
            Label choose = new Label("Choose file type");
            ArrayList<String> langs = new ArrayList<>();
            Button accept = new Button("Accept");
            Button cancel = new Button("Cancel");
            HBox confirm = new HBox(accept, cancel);
            confirm.setAlignment(Pos.CENTER);
            langs.add("html");
            langs.add("css");
            langs.add("txt");
            ObservableList<String> observableList = FXCollections.observableList(langs);
            ListView<String> listView = new ListView<String>(observableList);
            TextField textField = new TextField();
            VBox input = new VBox(label, textField, choose, listView, confirm);
            panel.getChildren().clear();
            panel.getChildren().addAll(input);
            cancel.setOnAction(event1 -> {
                panel.getChildren().clear();
            });
            accept.setOnAction(event1 -> {
                String fileName = textField.getText();
                if(listView.getSelectionModel().getSelectedItem() != null && !fileName.isEmpty()){
                    String type = listView.getSelectionModel().getSelectedItem();
                    String fileres = String.format("%s.%s", fileName, type);
                    File file = new File(fileres);
                    String htmlcode = "<html>\n" +
                            " <head>\n" +
                            "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
                            "  <title></title>\n" +
                            " </head>\n" +
                            " <body>\n" +
                            " </body>\n" +
                            "</html>";
                    if(fileLibrary.AddFile(file)){
                        AppFile appFile = fileLibrary.getFiles().stream()
                                .filter(c -> c.getFile().equals(file))
                                .findFirst()
                                .orElse(null);
                        chooseFile.set(appFile.getFile());
                        try {
                            if(Files.probeContentType(chooseFile.get().toPath()).equals("text/html")) {
                                appFile.setDocument(htmlcode, true);
                            }
                            else {
                                appFile.setDocument("", true);
                            }
                            saveAs.setDisable(false);
                            save.setDisable(false);
                            saveAs.fire();
                            chooseFile.set(appFile.getFile());
                            created.set(true);
                            open.fire();

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Wrong data");
                    alert.show();
                }

            });


        });
        saveAs.setOnAction(event -> {
            if(!fileLibrary.getFiles().isEmpty() && chooseFile.get() != null){
                AppFile appFile = fileLibrary.getFiles().stream()
                        .filter(c -> c.getFile().equals(chooseFile.get()))
                        .findFirst()
                        .orElse(null);
                try {
                    saveAs(appFile, stage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Scene scene = new Scene(vBox, 320, 240);
        stage.setTitle("App");
        stage.setScene(scene);
        stage.show();

    }
    public static void save(AppFile appFile) throws IOException {
        if(appFile != null) {
            BufferedWriter out = new BufferedWriter(new FileWriter(appFile.getFile()));
            try {
                out.write(appFile.getDocument());
            } catch (IOException e) {
            } finally {
                out.close();
            }
        }
    }
    public static void saveAs(AppFile appFile, Stage stage) throws IOException {
        if(appFile != null) {
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(appFile.getFile()));
                out.write(appFile.getDocument());
                out.close();
                DirectoryChooser directoryChooser = new DirectoryChooser();
                Path filetrg = directoryChooser.showDialog(stage).toPath();
                Path filesrc = appFile.getFile().toPath();
                Path path = Files.move(filesrc, Path.of(filetrg + "\\" + filesrc.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                appFile.setFile(path.toFile());
                chooseFile.set(appFile.getFile());
            } catch (IOException | NullPointerException e) {
                Path filesrc = appFile.getFile().toPath();
                Path path = Files.move(filesrc, filesrc, StandardCopyOption.REPLACE_EXISTING);
                appFile.setFile(path.toFile());
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}