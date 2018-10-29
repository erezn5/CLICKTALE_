package helpers;

import com.google.gson.Gson;

public class CageArchivePackage {

    String events;
    String streams;
    String webPage;
    String recording;
    String avro;
    String errorException;
    String rawData;
    String webPageHash;
 //   String audit;


    Gson g;
    CageArchivePackage cageArchivePackage;

    public CageArchivePackage(String resposne){
        g = new Gson();
        cageArchivePackage = g.fromJson(resposne,CageArchivePackage.class);
        cageArchivePackage.getCageArchivePackageObject();

    }

    public CageArchivePackage getCageArchivePackageObject() {
        return cageArchivePackage;
    }


    public CageArchivePackage getCageArchivePackage() {
        return cageArchivePackage;
    }

  //  public String getAudit() { return audit; }

    public String getWebPageHash(){
        return webPageHash;
    }

    public String getEvents() {
        return events;
    }

    public String getStreams() {
        return streams;
    }

    public String getWebPage() {
        return webPage;
    }

    public String getRecording() {
        return recording;
    }

    public String getAvro() {
        return avro;
    }

    public String getErrorException() {
        return errorException;
    }

    public String getRawData() {
        return rawData;
    }

    public Gson getG() {
        return g;
    }

}
