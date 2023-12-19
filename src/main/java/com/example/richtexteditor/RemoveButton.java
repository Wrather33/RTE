package com.example.richtexteditor;

import javafx.scene.control.Button;

import java.io.File;

public class RemoveButton extends Button {
    private File file;

    public RemoveButton(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
