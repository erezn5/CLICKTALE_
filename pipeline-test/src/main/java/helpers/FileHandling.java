package helpers;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class FileHandling {
   // ExternalRecordJsonParser cageArchivePackage;
    String m_directoryName="";
    File file = null;
    StringBuilder sb = new StringBuilder();
    private List<String> fileList;
    private static final String ZIPPED_FOLDER = "C:\\temp\\ArchivedFilesFromPusher\\test\\bundle_";
    private static final String SOURCE_FOLDER = "C:\\temp\\ArchivedFilesFromPusher\\test\\"; // SourceFolder path
    List<String> filesListInDir;
    public FileHandling(String i_directoryName){
        m_directoryName = i_directoryName;
    }

    public FileHandling(){

        fileList = new ArrayList< String >();
        }

    public void createFolder(String folderName){

        file = new File(folderName);
        boolean b = false;

        if(!file.exists()){
            b = file.mkdirs();
            System.out.println("Directory Successfully created");
        }else{
            System.out.println("Failed to Create Directory... File already exist!");
        }
    }

    public void createFolder(){
        file = new File(m_directoryName);
        boolean b = false;

        if(!file.exists()){
            b = file.mkdirs();
            System.out.println("Directory Successfully created");
        }else{
            System.out.println("Failed to Create Directory... File already exist!");
        }

    }

    public void writeToExistedFile(String fileName, String content){
        BufferedWriter bufferedWriter = null;
        FileWriter fileWriter = null;

        try{
            String data = content;
            File file = new File(fileName);

            if(!file.exists()){
                file.createNewFile();
            }

            fileWriter = new FileWriter(file.getAbsoluteFile(),true);
            bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(data);
            bufferedWriter.newLine();
      //      System.out.println("Done!");
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            try{
                if(bufferedWriter != null)
                    bufferedWriter.close();
                if(fileWriter != null)
                    fileWriter.close();
            } catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }

    public void writeToFile(String fileName,String content){

        try (PrintWriter writer = new PrintWriter(new File(fileName))) {
            writer.write(content);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public String readXMLFromFile(String filePath) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(filePath));


        try {
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }

//            System.out.println(sb);
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            br.close();
        }
        return sb.toString();
    }


    public void  deleteFiles(){

    }
    public String[] filesListFromFolder(String folderPath){
      //  cageArchivePackage.getCageArchivePackageObject();
        String[] fileList = new String[4];
        int i=0;
        File file = new File(folderPath);
        File[] files = file.listFiles();
        for(File f: files) {
            System.out.println(f.getName());
            fileList[i]=f.toString();
            i++;
        }

        return fileList;
    }

    public String getFileContentFromPath(String path) throws URISyntaxException, IOException {

        File file = new File(path);

        BufferedReader br = new BufferedReader(new FileReader(file));
        StringBuffer fileContents = new StringBuffer();
        String line = br.readLine();
        while (line != null) {
            fileContents.append(line);
            line = br.readLine();
        }

        br.close();

        return fileContents.toString();

    }
    public BufferedReader readFileFromPath(String path) throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader(path));

        return reader;

    }

    public void copyFromSourceToDst(String src, String dst) throws IOException {

        File source = new File(src);
        File destination = new File(dst);

        FileUtils.copyFileToDirectory(source,destination);

    }


    public String calcMD5AndReturnMD5Date(String sid) throws NoSuchAlgorithmException {
        //sid = "1649875094831110";
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(sid.getBytes());

        byte byteData[] = md.digest();

        StringBuffer sb = new StringBuffer();

        for(int i =0 ; i< byteData.length; i++){
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100,16).substring(1));
        }

        System.out.println("Digest (in hex format):: " + sb.toString());
        String str = sb.toString();
        str = str.substring(0,Math.min(str.length(),4));
        str = str.toUpperCase();
        String pattern = "yyMMdd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String date = simpleDateFormat.format(new Date());

        return str + "-" + date;
    }
//--------------------------------------------------------------------------------------------------------------------------------------------------------
    public void zip(String inputFolder, String targetZippedFolder) throws IOException {


        FileOutputStream fileOutputStream = null;

        fileOutputStream = new FileOutputStream(targetZippedFolder);
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

        File inputFile = new File(inputFolder);

        if(inputFile.isFile())
            zipFile(inputFile,"",zipOutputStream);
        else if(inputFile.isDirectory())
            zipFolderVer(zipOutputStream,inputFile,"");

        zipOutputStream.close();
    }

    public void unzipFile(String zipFilePath,String destDir){

    File dir = new File(destDir);
    FileInputStream fis;
    byte[] buffer = new byte[1024];
    // Create output directory if it doesn't exist
    if(!dir.exists()) dir.mkdirs();

    try {
        fis = new FileInputStream(zipFilePath);
        ZipInputStream zis = new ZipInputStream(fis);
        ZipEntry ze = zis.getNextEntry();
        while(ze != null){
            String fileName = "pipe_" + ze.getName();
            File newFile = new File(destDir + File.separator + fileName);
            System.out.println("Unzipping to "+newFile.getAbsolutePath());
            //create directories for sub directories in zip
            new File(newFile.getParent()).mkdirs();
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            //close this ZipEntry
            zis.closeEntry();
            ze = zis.getNextEntry();
        }
        //close last ZipEntry
        zis.closeEntry();
        zis.close();
        fis.close();
    } catch (IOException e) {
        e.printStackTrace();
    }

}
    public void zipFolderVer(ZipOutputStream zipOutputStream, File inputFolder, String parentName) throws IOException {
        String myname = parentName + inputFolder.getName()+"\\";

        ZipEntry folderZipEntry = new ZipEntry(myname);
        zipOutputStream.putNextEntry(folderZipEntry);

        File[] contents = inputFolder.listFiles();

        for(File f:contents){
            if(f.isFile())
                zipFile(f,myname,zipOutputStream);
            else if(f.isDirectory())
                zipFolderVer(zipOutputStream,f,myname);
        }

        zipOutputStream.closeEntry();
    }

    public void zipFile(File inputFile, String parentName, ZipOutputStream zipOutputStream) throws IOException{

        // A ZipEntry represents a file entry in the zip archive
        // We name the ZipEntry after the original file's name
        ZipEntry zipEntry = new ZipEntry(parentName+inputFile.getName());
        zipOutputStream.putNextEntry(zipEntry);

        FileInputStream fileInputStream = new FileInputStream(inputFile);
        byte[] buf = new byte[1024];
        int byteRead;

        // Read the input file by chucks of 1024 bytes
        // and write the read bytes to the zip stream
        while((byteRead = fileInputStream.read(buf)) > 0 ) {
            zipOutputStream.write(buf,0,byteRead);

        }
        // close ZipEntry to store the stream to the file
        zipOutputStream.closeEntry();


        System.out.println("Regular file :" + parentName+inputFile.getName() +" is zipped to archive :"+ZIPPED_FOLDER);


    }

//--------------------------------------------------------------------------------------------------------------------------------------------------------

    public long countNumberOfSubFolders(String subFoldersPath) throws IOException {

        long count = Files.find(
                Paths.get(subFoldersPath),
                1,  // how deep do we want to descend
                (path, attributes) -> attributes.isDirectory()
        ).count() - 1; // '-1' because '/subFolderPath' is also counted in

        return count;
    }
}
