/*
 * Esta clase envía mensajitos al cliente mediante sockets
 *
 * @author Peter Fight
 * @version 0.1
 * @since 2020-02-02
 * */

import org.jetbrains.annotations.NotNull;



import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@ServerEndpoint("/WebSvnProgressSocket")
public class Socket {

    @OnOpen
    public void onOpen(@NotNull Session session) {
        System.out.println("onOpen::" + session.getId());
    }
    @OnClose
    public void onClose(Session session) {
        System.out.println("onClose::" +  session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) throws UnsupportedEncodingException {
        if(message.startsWith("@@@URL@@@")){
            String url = message.replace("@@@URL@@@","");
            String urlDecoded = URLDecoder.decode(url, StandardCharsets.UTF_8.toString());
            Scraper scraper = new Scraper(urlDecoded, session);
            scraper.getEnlacesFromUrl(urlDecoded);
        }
        System.out.println("onMessage::From=" + session.getId() + " Message=" + message);

        //Cuando llega aquí el proceso ha terminado
        try {
            session.getBasicRemote().sendText("Proceso terminado correctamente. Puede descargar el archivo pulsando <a target='_blank' href='/WebSvnScraper_war/WebSvnScraperServlet'><b>aquí</b></a>!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnError
    public void onError(Throwable t) {
        System.out.println("onError::" + t.getMessage());
    }

    public void enviaTraza(String mensaje, Session session) throws IOException, EncodeException {
        System.out.println("Mensaje from server: " + mensaje);
        try {
            session.getBasicRemote().sendText(mensaje);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
