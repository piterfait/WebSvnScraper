/*
 * Esta clase controla los enlaces que se van a recorrer empleando la clase por hilos del Recorredor de enlaces
 *
 *
 * @author Peter Fight
 * @version 0.1
 * @since 2020-02-02
 * */

import org.jetbrains.annotations.NotNull;

import javax.websocket.Session;
import java.io.*;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.concurrent.Executors.newFixedThreadPool;


public class Scraper {
    String _pathTemporal;
    String _pathRemove;
    Session _session;

    /**
     * Constructor
     * @param url
     * @param session
     */
    public Scraper(String url, Session session)
    {
        this._session = session;
        this._pathRemove = url;
        File directory = new File(String.valueOf("/TempWebSvnScraper"));
        if(!directory.exists()){
            directory.mkdir();
        }
        else{
            directory.delete();
        }
        checkFolderUrl(url);
    }

    /**
     * Inspecciona los enlaces dentro de una url (primera entrada)
     * @param url
     */
    private void checkFolderUrl(String url)
    {
        ArrayList<String> urlsFolder = new ArrayList<String>();
        ArrayList<String> urlsFiles = new ArrayList<String>();

        //Saco el listado de enlaces
        ArrayList<String> enlaces = getEnlacesFromUrl(url);

        Persistidor persistidor = new Persistidor(_pathRemove, _session, this);
        //Proceso los primeros enlaces
        ExecutorService executor = newFixedThreadPool(5);
        for (int i = 0; i < enlaces.size(); i++) {

            Runnable worker = new RecorredorDeEnlaces(enlaces.get(i), _session, persistidor, this);
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            //No ha terminado
        }
        //El proceso ha terminado
        persistidor.procesaLosEnlacesRestantes();
    }

    /**
     * Inspecciona los enlaces dentro de una url (entradas sucesivas)
     * @param direccion
     */
    public ArrayList<String> getEnlacesFromUrl(@NotNull String direccion)
    {
        ArrayList<String> resultado = new ArrayList<String>();
        try {
            System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

            String protocolo = direccion.split("://")[0];
            if(direccion.indexOf("://") == -1)
            {
                direccion = _pathRemove + direccion;
            }
            protocolo = protocolo.replace("../","");
            if(protocolo.indexOf("http") == -1){
                protocolo = "http";
            }

            String host = direccion.split("://")[1].split("/")[0];
            String params =  direccion.split("://")[1].replace(direccion.split("://")[1].split("/")[0],"");
            URL url = new URL(protocolo,host,80, params);
            InputStream is = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            try {
                String cadena;
                while ((cadena = br.readLine()) != null) {
                    Pattern p = Pattern.compile("href=\"(.*?)\"");
                    Matcher m = p.matcher(cadena);
                    String urlToAdd = null;
                    if (m.find() && cadena.indexOf("://") == -1) {
                        urlToAdd = m.group(1); // this variable should contain the link URL
                    }
                    //Descarto rutas relativas
                    if(urlToAdd != null && !urlToAdd.contains("..")){
                        resultado.add(urlToAdd);
                    }
                }
            } finally {
                br.close();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultado;
    }
}
