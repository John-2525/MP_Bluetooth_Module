package Classes;

import java.io.Serializable;

public class Firebase_Database_Image_Video_Audio_Upload implements Serializable {

    private String FileName;
    private String FileDownloadUri;

    public Firebase_Database_Image_Video_Audio_Upload() {

    }

    public Firebase_Database_Image_Video_Audio_Upload(String Name, String Uri) {
        this.FileName = Name;
        this.FileDownloadUri = Uri;
    }

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String name) {
        this.FileName = name;
    }

    public String getFileDownloadUri() {
        return FileDownloadUri;
    }

    public void setFileDownloadUri(String uri) {
        this.FileDownloadUri = uri;
    }

}
