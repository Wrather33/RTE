package com.example.richtexteditor;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;

public class AppFile {
    private File file;
    private String document;
    private ArrayList<String> undo = new ArrayList<>();
    private ArrayList<String> redo = new ArrayList<>();

    public AppFile(File file) {
        this.file = file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppFile appFile = (AppFile) o;
        return Objects.equals(file, appFile.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, document);
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getDocument() {
        return document;
    }

    public String undo(){
        if(canUndo()) {
            String res = undo.remove(0);
            redo.add(0, res);
            return res;
        }
        return document;
    }
    public String redo(){
        if(canRedo()) {
            String res = redo.remove(0);
            undo.add(0, res);
            return res;
        }
        return document;
    }
    public boolean canUndo(){
        return undo.size() > 0;
    }
    public boolean canRedo(){
        return redo.size() > 0;
    }


    public void setDocument(String document, boolean push) {
        if(push){
            undo.add(0, document);
        }
        this.document = document;
    }
}
