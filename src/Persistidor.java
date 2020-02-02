/*
* Esta clase se encarga de insertar en el disco duro las carpetas y ficheros que se van descubriendo
* durante el recorrido de los enlaces.
*
* @author Peter Fight
* @version 0.1
* @since 2020-02-02
* */


import javax.websocket.Session;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Persistidor {
    private static final int BUFFER_SIZE = 4096;
    private String _pathRemove;
    private Scraper _scraper;
    private Session _session;
    private ArrayList<String> urlsFolder;

    private ArrayList<String> urlsFolderPrevio;

    public synchronized ArrayList<String> getUrlsFolder(){
        if(this.urlsFolder == null)
        {
            this.urlsFolder = new ArrayList<String>();
        }
        //this.urlsFolder.removeAll(getUrlsFolderPrevio());
        return this.urlsFolder;
    }
    public synchronized void setUrlsFolder(ArrayList<String> urls){
        if(urlsFolder == null)
        {
            urlsFolder = new ArrayList<String>();
        }
        urlsFolder = urls;
    }


    public synchronized ArrayList<String> getUrlsFolderPrevio(){
        if(this.urlsFolderPrevio == null)
        {
            this.urlsFolderPrevio = new ArrayList<String>();
        }
        return urlsFolderPrevio;
    }
    public synchronized void setUrlsFolderPrevio(ArrayList<String> urls){
        if(urlsFolderPrevio == null)
        {
            urlsFolderPrevio = new ArrayList<String>();
        }
        urlsFolderPrevio = urls;
    }
    /**
     * Constructor
     * @param pathRemove El path de la url que tengo que eliminar al guardar en disco
     * @param session para enviar mensajito mediante socket
     * @param scraper referencia a la clase araña para poder lanzar nuevas consultas
     */
    public Persistidor(String pathRemove, Session session, Scraper scraper){
        _pathRemove = pathRemove;
        _session = session;
        _scraper = scraper;
    }


    /**
     * Al ser un enlace y no un directorio, descarga el archivo del enlace y lo guarda en la carpeta pertienente
     * @param enlace El enlace en cuestión
     */
    public boolean procesaEnlace(String enlace) throws IOException {

        if(enlace.indexOf("://") == -1)
        {
            enlace = _pathRemove + enlace;
        }
        String protocolo = enlace.split("://")[0];
        String host = enlace.split("://")[1].split("/")[0];
        String params =  enlace.split("://")[1].replace(enlace.split("://")[1].split("/")[0],"");
        URL url = new URL(protocolo,host,80, params);




        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();

            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = enlace.substring(enlace.lastIndexOf("/") + 1,
                        enlace.length());
            }

            URL website = url;
            String directorioGuardar = enlace.replace(_pathRemove,"");
            directorioGuardar = directorioGuardar.replace(fileName,"");
            String saveFilePath = "/TempWebSvnScraper/"+directorioGuardar + File.separator + fileName;
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(saveFilePath);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

        } else {
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();

        return true;
    }

    /**
     * AL ser un fichero, crea el directorio en el servidor / local
     * @param enlace referencia a la clase araña para poder lanzar nuevas consultas
     */
    public boolean procesaFichero(String enlace)
    {
        String directorioGuardar = enlace.replace(_pathRemove,"");
        File directory = new File(String.valueOf("/TempWebSvnScraper/"+directorioGuardar));
        if(!directory.exists()){
            directory.mkdir();
        }
        return true;
    }

    /**
     * Recorre los enlaces pendientes de recorrer y descubiertos en esta iteración
     */
    public void procesaLosEnlacesRestantes()
    {
        //Saco el listado de enlaces
        ArrayList<String> enlaces = getUrlsFolder();
        if(enlaces != null && enlaces.size() > 0) {
            Persistidor persistidor = new Persistidor(this._pathRemove, this._session, _scraper);
            persistidor.setUrlsFolderPrevio(this.urlsFolder);
            //Proceso los primeros enlaces
            ExecutorService executor = Executors.newFixedThreadPool(5);
            for (int i = 0; i < enlaces.size(); i++) {
                Runnable worker = new RecorredorDeEnlaces(enlaces.get(i), _session, persistidor, _scraper);
                executor.execute(worker);
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
                //No ha terminado
            }
            //El proceso ha terminado
            persistidor.procesaLosEnlacesRestantes();
        }
    }
}
