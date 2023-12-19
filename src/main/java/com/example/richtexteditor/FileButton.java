package com.example.richtexteditor;

import javafx.scene.Node;
import javafx.scene.control.Button;

import java.io.File;

public class FileButton extends Button {
    private File file;
    private String fileName;

    public FileButton(File file, String fileName) {
        this.file = file;
        this.fileName = fileName;
    }

    public FileButton(String text, File file, String fileName) {
        super(text);
        this.file = file;
        this.fileName = fileName;
    }

    public FileButton(String text, Node graphic, File file, String fileName) {
        super(text, graphic);
        this.file = file;
        this.fileName = fileName;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
