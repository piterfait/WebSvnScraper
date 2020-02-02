<%--
  Created by Peter Fight.
  User: pitefait (test git commit) subido con commit
  Date: 01/02/2020
  Time: 13:00
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>WebSvnScraper</title>
    <%--    <script src="/WebSvnScraper_war/Contenido/socket.io.min.js"></script>--%>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css">
</head>
<body>

<div style="position: fixed; z-index: -99; width: 100%; height: 100%">
    <img style="width:100%;height:100%" src="https://images.unsplash.com/photo-1527572756213-1cda99a355c9?ixlib=rb-1.2.1&auto=format&fit=crop&w=1950&q=80"/>
</div>
<div class="container  p-100 " style="color:white">
    <div class="jumbotron text-center" style="background:rgba(255,0,0,0.1)">
        <h1>WebSvn Scraper</h1>
        <h7>(Desarrollado por Peter Fight)</h7>
<%--    </div>--%>
<%--    <div class="row">--%>
        <br><br><hr>
        <p>Araña para descargar proyectos de Web Svn sin User ni Pass (restricciones impuestas por el Sistema).</p>
        <p>Por cierto, <b>me lavo las manos del mal uso que le puedas dar.</b></p>
    </div>
    <div class="row text-center">
        <iframe style="width:50%;margin-left:25%;margin-bottom:2em;" width="560" height="100" src="https://www.youtube.com/embed/KMU0tzLwhbE" frameborder="0" allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
    </div>
    <div class="row">
        <%--<p>Invocar servlet -> <a href="WebSvnScraperServlet">here</a></p>--%>
        <label id="lblMensaje" style="color:lightgreen">Dime la url de WebSvn que quieres "arañar"...</label>
        <input class="form-control" type="text" placeholder="La url afortunada es..." id="url"/>
        <button onclick="iniciaProcesado()" id="btnSubmit" class="btn btn-outline-success">Iniciar proceso</button>
    </div>
    <hr>
    <div class="row">
        <h2>&#8505; Trazas </h2>
        <ul id="mensajes"></ul>
    </div>
</div>
<script type="text/javascript">
    var puerto = location.port;
    var clienteSocket = null;
    function iniciaProcesado(){
        // var xhr = new XMLHttpRequest();
        // xhr.onreadystatechange = function() {};
        // var url = document.getElementById("url").value;
        // xhr.open('GET', "/WebSvnScraper_war/WebSvnScraperServlet?url=" + encodeURIComponent(url));
        // xhr.send();
        var url = document.getElementById("url").value;
        clienteSocket.send('@@@URL@@@'+encodeURIComponent(url))


        document.getElementById("url").style.visibility = 'hidden';
        document.getElementById("btnSubmit").style.visibility = 'hidden';
        document.getElementById("lblMensaje").innerHTML = "Procesando la petición... Por favor, espere a que el proceso termine.";
        document.getElementById("lblMensaje").style.color = "lightgreen";
    }

    function addTrazas(msg) {
        var html = document.getElementById("mensajes").innerHTML;
        html = "<li style='color:lightgreen'>" + msg + "</li>";
        //document.getElementById("mensajes").innerHTML = html;
        document.getElementById("mensajes").innerHTML = html;
    }

    class WebSocketClient {
        constructor(protocol, hostname, port, endpoint) {
            this.webSocket = null;
            this.protocol = protocol;
            this.hostname = hostname;
            this.port = port;
            this.endpoint = endpoint;
        }

        connect() {
            try {
                var socketUrl = this.protocol + "://127.0.0.1:" + this.port + this.endpoint;
                this.webSocket = new WebSocket(socketUrl);
                this.webSocket.onopen = function (event) {
                    addTrazas("Websocket conectado correctamente.")
                }
                this.webSocket.onmessage = function (event) {
                    var msg = event.data;
                    addTrazas(msg);
                    console.log('onmessage::' + JSON.stringify(msg, null, 4));
                }
                this.webSocket.onclose = function (event) {
                    addTrazas("Conexión con el servidor cerrada");
                    console.log('onclose::' + JSON.stringify(event, null, 4));
                }
                this.webSocket.onerror = function (event) {
                    console.log('onerror::' + JSON.stringify(event, null, 4));
                }
            } catch (exception) {
                console.error(exception);
            }
        }

        getStatus() {
            return this.webSocket.readyState;
        }

        send(message) {
            if (this.webSocket.readyState == WebSocket.OPEN) {
                this.webSocket.send(message);
            } else {
                console.error('webSocket cerrado. readyState=' + this.webSocket.readyState);
            }
        }

        disconnect() {
            if (this.webSocket.readyState == WebSocket.OPEN) {
                this.webSocket.close();
            } else {
                console.error('webSocket cerrado. readyState=' + this.webSocket.readyState);
            }
        }
    }

    window.onload = function () {
        clienteSocket = new WebSocketClient('ws', '127.0.0.1', puerto, '/WebSvnScraper_war/WebSvnProgressSocket?push=TIME');
        clienteSocket.connect();
    };
</script>
</body>
</html>
