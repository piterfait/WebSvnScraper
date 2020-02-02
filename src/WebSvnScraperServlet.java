/*
 * Este servlet devuelve en zip todos los directorios y archivos extraídos
 *
 *
 * @author Peter Fight
 * @version 0.1
 * @since 2020-02-02
 * */

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@javax.servlet.annotation.WebServlet(name = "WebSvnScraperServlet")
public class WebSvnScraperServlet extends javax.servlet.http.HttpServlet {
    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {

    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        File dir = new File("/TempWebSvnScraper");
        String zipDirName = "WebSvnScrapper.zip";

        ZipFiles zipFiles = new ZipFiles();
        zipFiles.zipDirectory(dir, zipDirName);

        File archivoZip = new File(String.valueOf(zipDirName));

        OutputStream out = response.getOutputStream();
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=WebSvnScrapper.zip");
        FileInputStream in = new FileInputStream(archivoZip);
        byte[] buffer = new byte[100000000];
        int length;
        while ((length = in.read(buffer)) > 0){
            out.write(buffer, 0, length);
        }
        in.close();
        out.flush();
    }
}
