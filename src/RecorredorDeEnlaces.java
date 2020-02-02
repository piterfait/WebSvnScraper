/*
 * Esta clase se encarga de recorrer todos los enlaces mediante hilos
 *
 *  @author Peter Fight
 * @version 0.1
 * @since 2020-02-02
 * */


import javax.websocket.Session;
import java.io.Console;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class RecorredorDeEnlaces implements Runnable {
    private String _enlace;
    private Session _session;
    private Persistidor _persistidor;
    private Scraper _scraper;

    public Persistidor get_persistidor() {
        return _persistidor;
    }

    public void procesoTerminado(){
        _persistidor.procesaLosEnlacesRestantes();
    }

    /**
     * Constructor
     * @param s
     * @param session para enviar mensajitos
     * @param persistidor persiste en el servidor / local los enlaces que se van descubriendo
     * @param scraper para lanzar nuevas iteraciones con los enlaces que se van descubriendo
     */
    public RecorredorDeEnlaces(String s, Session session, Persistidor persistidor, Scraper scraper){
        this._enlace =s;
        this._session = session;
        this._persistidor = persistidor;
        this._scraper = scraper;
    }
    @Override
    public void run() {

        //new Socket().onMessage("Iniciando el procesado de: "+enlace,session);
        processCommand();
        //new Socket().onMessage("Terminado el procesado de: "+enlace,session);

    }
    private void processCommand() {
        try {
            if(!_enlace.endsWith("/")){
                get_persistidor().procesaEnlace(_enlace);
            }
            else if(_enlace.endsWith("/")){
                if(_enlace == null){
                    throw new Exception("enlace es nulo");
                }
                if(get_persistidor() == null)
                {
                    throw new Exception("persistidor null");
                }

                get_persistidor().procesaFichero(_enlace);
                ArrayList<String> nuevosEnlacesPorProcesar = _scraper.getEnlacesFromUrl(_enlace);
                //Almaceno en el persistidor todos los nuevos directorios que aparezcan
                // para procesarlos posterioremente
                ArrayList<String> urlsFolders = new ArrayList<String>();
//                urlsFolders = (ArrayList<String>)nuevosEnlacesPorProcesar
//                        .stream()
//                        .filter((x -> x.endsWith("/")))
//                        .collect(Collectors.toCollection(() -> new ArrayList<String>()))
//                        ;
                urlsFolders = nuevosEnlacesPorProcesar;
                for(int i = 0; i < urlsFolders.size(); i++)
                {
                    urlsFolders.set(i, _enlace + urlsFolders.get(i).replace("../",""));
                }
                if(get_persistidor().getUrlsFolder().size() > 0) {
                    urlsFolders.addAll(get_persistidor().getUrlsFolder());
                }
                get_persistidor().setUrlsFolder(urlsFolders);
            }
            try {
                synchronized (_session) {
                    if (_session.isOpen()) {
                        _session.getBasicRemote().sendText(_enlace);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public String toString(){
        return this._enlace;
    }
}