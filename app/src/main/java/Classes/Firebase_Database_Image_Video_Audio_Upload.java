package Classes;

import java.io.Serializable;

/**
 * Extending Serializable is necessary in order to ensure that data
 * can be passed through intent to the next activity
 */
public class Firebase_Database_Image_Video_Audio_Upload implements Serializable {

    private String FileName;
    private String FileDownloadUri;

    /**
     * Necessary as .getValue() in Image_Fragment.java and Video_Fragment.java
     * has a constraint that it requires the class to must have a default constructor
     * that takes no arguments
     */
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
