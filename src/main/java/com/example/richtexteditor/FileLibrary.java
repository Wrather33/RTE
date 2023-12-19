package com.example.richtexteditor;

import java.io.File;
import java.util.ArrayList;

public class FileLibrary {
    private ArrayList<AppFile> files = new ArrayList<>();
    private static FileLibrary fileLibrary = null;
    public static FileLibrary getFileLibrary(){
        if(fileLibrary == null){
            return new FileLibrary();
        }
        else {
            return fileLibrary;
        }
    }

    public void setFiles(ArrayList<AppFile> files) {
        this.files = files;
    }

    public ArrayList<AppFile> getFiles() {
        return files;
    }
    public boolean AddFile(File file){
        if(getFiles().stream()
                .filter(c -> c.getFile().equals(file))
                .findFirst()
                .orElse(null) == null){
            return getFiles().add(new AppFile(file));
        }
        else {
            return false;
        }
    }
    public void RemoveFile(File file){
        for (int i = 0; i < getFiles().size(); i++) {
            if (getFiles().get(i).getFile().equals(file)){
                getFiles().remove(i);
                break;
            }
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
