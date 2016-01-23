/*
 * Copyright 2015 Pablo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package librerias.nucleo.sondeo;

import librerias.utilidades.Log;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Clase encargada de emitir mensajes de sondeo por la red
 *
 * @author Pablo
 */
public class EmisorSondeo implements Runnable {

    private DatagramSocket socket;

    /**
     * Método que realiza el trabajo iterativo de enviar un mensaje de sondeo lo
     * realiza a través de todos los dispositivos de red disponibles
     */
    private void emitirSondeo() {
        try {
            while (true) {
                //Preparamos un socket en un puerto aleatorio para enviar el datagrama UDP
                MensajeSondeoJSON mensaje = new MensajeSondeoJSON();
                final Gson gson = new Gson();
                String mensajeJSON = (String) gson.toJson(mensaje);
                socket = new DatagramSocket();
                socket.setBroadcast(true);
                byte[] bufferMensaje = mensajeJSON.getBytes("UTF8");

                //Se propaga inicialmente el datagrama por 255.255.255.255
                try {
                    DatagramPacket sendPacket = new DatagramPacket(bufferMensaje, bufferMensaje.length, InetAddress.getByName(NucleoSondeo.dirBroadcast), NucleoSondeo.puertoBroadcast);
                    socket.send(sendPacket);
                } catch (Exception e) {
                    System.err.printf(e.getLocalizedMessage());
                    Log.error("Ocurrió un error al enviar el datagrama de sondeo: "+mensajeJSON);
                }
                Log.sondeoEmisor(">>> Sonda enviada: " + mensajeJSON);
                socket.close();
                Thread.sleep(NucleoSondeo.tiempoSleppLoopSondeo);
            }
        } catch (IOException | InterruptedException ex) {
            Log.sondeoEmisor("Servicio de SondeoEmisor Interrumpido");
        }
    }

    /**
     * Método de concurrencia de clase
     */
    @Override
    public void run() {
        this.emitirSondeo();
    }
}