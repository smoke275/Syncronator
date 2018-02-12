package persistence;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Folder {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("files")
    @Expose
    private List<File> files = null;
    @SerializedName("folders")
    @Expose
    private List<Folder> folders = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

}