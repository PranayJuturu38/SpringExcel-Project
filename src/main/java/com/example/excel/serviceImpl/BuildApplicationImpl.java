package com.example.excel.serviceImpl;

import com.example.excel.service.BuildApplication;



import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class BuildApplicationImpl implements BuildApplication {

    private static final long MEGABYTE = 1024L * 1024L;
    @Autowired
    private HttpServletRequest request;


    @Override
    public Path buildVue(MultipartFile zipFile) throws Exception {
        String extension = FilenameUtils.getExtension(zipFile.getOriginalFilename());
        String fileName = FilenameUtils.getBaseName(zipFile.getOriginalFilename());
        String os = System.getProperty("os.name").toLowerCase();
        String dir = System.getProperty("user.dir");

        try {
            if (!extension.equals("zip")) {
                throw new Exception("File type not supported");
            }

            File convertedFile = convert(zipFile);


            String unzippedPath = unzipFile(convertedFile);

            String fullUnZippedPath = unzippedPath  + fileName;

            commandPrompt(os,fullUnZippedPath,"rm -rf node_modules/");
            commandPrompt(os,fullUnZippedPath,"npm install");

            commandPrompt(os,fullUnZippedPath, "npm run build");

           Path sourcePath = Paths.get(  fullUnZippedPath + "/dist");
           Path destinationPath = Paths.get(dir +"/"+ fileName +"dist");
           return moveFolder(sourcePath, destinationPath);

        } catch (Exception e) {
            throw new Exception("Couldn't build the application :" + e.getMessage());
        }
    }


    public static File convert(MultipartFile file) throws Exception {
        try {

            File convFile = new File(file.getOriginalFilename());
            convFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(file.getBytes());

            fos.close();
            return convFile;

        } catch (Exception e) {
            throw new Exception("File not converted :" + e.getMessage());
        }
    }

    String unzipFile(File zipFile) throws IOException {

        String pathToZipFile = zipFile.getCanonicalPath();

        String destPath = pathToZipFile.replace(zipFile.getName(),"");
        File destDir = new File(destPath);

        byte[] buffer = new byte[1024];


        ZipInputStream zis = new ZipInputStream(new FileInputStream(pathToZipFile));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {

            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();

        return destPath;

    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
    void commandPrompt(String osName,String filePath, String command) throws IOException {

        ProcessBuilder builder = new ProcessBuilder();

            if(osName.contains("win")){
                builder = new ProcessBuilder( "cmd.exe", "/c","cd "  +filePath+"/"+ "&&" + command );
            }

            else if(osName.contains("lin")){

                builder = new ProcessBuilder( "bash", "-c", "cd "  +filePath+"/"+ "&&" + command);

            }
            else if(osName.contains("mac")){
                builder = new ProcessBuilder("sh","-c","cd "  +filePath+"/"+ "&&" + command);
            }


        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = null;
        while (true) {
            line = r.readLine();
            if (line == null) {
                break;
            }

            System.out.println(line);
        }
        p.destroy();
    }


    Path moveFolder(Path source, Path destination) throws Exception {

        try {

            if (Files.exists(destination)) {
                FileSystemUtils.deleteRecursively(destination);
            }
            Files.move(source, destination);

            return destination;
        } catch (Exception e) {
            throw new Exception("Couldn't move the file:" + e.getMessage());
        }
    }


}
