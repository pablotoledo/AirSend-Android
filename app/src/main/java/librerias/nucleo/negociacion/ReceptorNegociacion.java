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
package librerias.nucleo.negociacion;


import android.app.Activity;
import android.content.Intent;

import librerias.nucleo.NucleoAirSend;
import librerias.nucleo.transferencia.ReceptorTransferencia;
import librerias.utilidades.Log;
import librerias.utilidades.Persistencia;
import pablotoledo.airsendtfg.MenuPrincipal;
import pablotoledo.airsendtfg.RecibirDatos;

import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pablo
 */
public class ReceptorNegociacion extends Activity implements Runnable{

    private ServerSocket server_socket;

    /**
     * Método principal del hijo de ejecución
     */
    public void run() {
        Socket socket = null;
        try {
            server_socket = new ServerSocket(NucleoNegociacion.puertoNucleoNegociacion);
            while (true) {
                Log.info("Esperando negociación ....");
                socket = server_socket.accept();
                Log.info("Aceptada solicitud de negociacion " + socket.getInetAddress());
                // Se recibe petición

                InputStream inputStream = socket.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] content = new byte[2048];
                int bytesRead = -1;
                while ((bytesRead = inputStream.read(content)) != -1) {
                    baos.write(content, 0, bytesRead);
                }

                String mensajito = baos.toString();
                String mensajeString = mensajito.substring(mensajito.indexOf("{"), mensajito.lastIndexOf("}") + 1);
                MensajeNegociacionJSON mensaje = new Gson().fromJson(mensajeString, MensajeNegociacionJSON.class);
                procesarMensaje(mensaje, socket.getInetAddress().getHostAddress());
                socket.close();
            }
        } catch (Exception ioe) {
            Log.info("ReceptorNegociacion socket cerrado");
        }
    }

    /**
     * Libera los sockets para evitar solapamientos si se reinicia la clase actual
     */
    public void liberarSockets() {
        try {
            this.server_socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ReceptorNegociacion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Procesa el mensaje recibido derivandolo al método que lo tratará según su tipología
     * @param mensaje
     * @param dirIPMensaje 
     */
    private void procesarMensaje(MensajeNegociacionJSON mensaje, String dirIPMensaje) {
        //Se comprueba que no este nulo
        if (mensaje != null) {
            //lanzar análisis de casos
            if (mensaje.getTipoMensaje().equals(MensajeNegociacionJSON.tipoMensajes[0])) {
                //Caso de PROPUESTA
                mensaje.setIpEmisor(dirIPMensaje);
                this.procesarMensajePROPUESTA(mensaje);
            } else if (mensaje.getTipoMensaje().equals(MensajeNegociacionJSON.tipoMensajes[1])) {
                //Caso de ACEPTADO
                this.procesarMensajeACEPTADO(mensaje);
            } else if (mensaje.getTipoMensaje().equals(MensajeNegociacionJSON.tipoMensajes[2])) {
                //Caso de DENEGADO
                this.procesarMensajeDENEGADO(mensaje);
            } else if (mensaje.getTipoMensaje().equals(MensajeNegociacionJSON.tipoMensajes[3])) {
                //Caso de COMIENZO
                this.procesarMensajeCOMIENZO(mensaje);
            } else if (mensaje.getTipoMensaje().equals(MensajeNegociacionJSON.tipoMensajes[4])) {
                //Caso de TERMINADO
                this.procesarMensajeTERMINADO(mensaje);
            } else {
                Log.debug("Tipo de Mensaje no reconocido: " + mensaje.getTipoMensaje());
            }
        }
    }

    /**
     * Método que aisla el código referente a los mensajes de tipo PROPUESTA,
     * este método codifica mecanismos necesarios para activar la interfaz de la
     * parte receptora
     *
     * @param entrada Mensaje de tipo PROPUESTA
     */
    private void procesarMensajePROPUESTA(MensajeNegociacionJSON entrada) {
        //Comprobamos si se trata de un origen de confianza
        if (!Persistencia.getListaDispositivosConfianza().containsKey(entrada.getIdentificadorEmisor())) {
            //Agregamos a la lista de recibidos del nucleo de negociación
            //Como el receptor es quien recibe el mensaje, creamos una interfaz
            //new RecibirVentana(entrada).setVisible(true);
            NucleoNegociacion.listaPropuesta.put(entrada.getIdentificadorEmisor(), entrada);
            Log.info("Mensaje de propuesta " + entrada.getIdentificadorEmisor() + " procesado");
            Intent intent = new Intent(NucleoAirSend.actividad, RecibirDatos.class);
            intent.putExtra("mensaje",entrada);
            NucleoAirSend.actividad.startActivity(intent);
        } else {
            //Si se trata de un origen de confianza tratamos este evento de forma individual
            this.recibirDeEmisorDeConfianza(entrada);
        }
    }

    /**
     * Este método se encarga de recibir un mensaje desde un dispositivo de
     * confianza
     */
    private void recibirDeEmisorDeConfianza(MensajeNegociacionJSON entrada) {
        ReceptorTransferencia receptor = new ReceptorTransferencia(entrada);
        receptor.setRutaFichero(Persistencia.getRutaDescarga());
        Thread hiloReceptor = new Thread(receptor);
        hiloReceptor.start();
        EmisorNegociacion.enviarMensajeAceptadoQ1(entrada, receptor.getPuerto());
        Log.info("Mensaje de propuesta de dispositivo de confianza " + entrada.getIdentificadorEmisor() + " procesado");
    }

    /**
     * Método que aisla el código referente a los mensajes de tipo ACEPTADO
     *
     * @param entrada Mensaje de tipo ACEPTADO
     */
    private void procesarMensajeACEPTADO(MensajeNegociacionJSON entrada) {
        //Como este mensaje lo recibe el emisor, debera comprobar la 
        //preexistencia de un mensaje de PROPUESTA
        if (NucleoNegociacion.listaPropuesta.containsKey(entrada.getIdentificadorMensaje())) {
            //Asignamos el valor de COMIENZO al mensaje almacenado
            entrada.setTipoMensaje(MensajeNegociacionJSON.tipoMensajes[3]);
            //Actualizamos los valores en nuestras listas de mensajes
            NucleoNegociacion.listaPropuesta.remove(entrada.getIdentificadorMensaje());
            NucleoNegociacion.listaComienzo.put(entrada.getIdentificadorMensaje(), entrada);
            //Enviamos mensaje de Comienzo
            EmisorNegociacion.enviarMensaje(entrada.getIpDestinatario(), entrada);
            Log.info("Mensaje Aceptado recibido con ID: " + entrada.getIdentificadorMensaje());
        }
    }

    /**
     * Método que aisla el código referente a los mensajes de tipo DENEGADO
     *
     * @param entrada Mensaje de tipo DENEGADO
     */
    private void procesarMensajeDENEGADO(MensajeNegociacionJSON entrada) {
        //Como este mensaje lo recibe el emisor, hay que comprobar la
        //preexistencia de un mensaje de Propuesta
        if (NucleoNegociacion.listaPropuesta.containsKey(entrada.getIdentificadorMensaje())) {
            //Procesamos la situacion creando los cambios necesarios en las listas
            NucleoNegociacion.listaPropuesta.remove(entrada.getIdentificadorMensaje());
            NucleoNegociacion.listaDenegado.put(entrada.getIdentificadorMensaje(), entrada);
            Log.info("Mensaje Denegado recibido con ID: " + entrada.getIdentificadorMensaje());
        }
    }

    /**
     * Método que aisla el código referente a los mensajes de tipo COMIENZO
     *
     * @param entrada Mensaje de tipo COMIENZO
     */
    private void procesarMensajeCOMIENZO(MensajeNegociacionJSON entrada) {
        //Como este mensaje lo recibe el receptor, hay que comprobar la
        //preexistencia
        if (NucleoNegociacion.listaAceptado.containsKey(entrada.getIdentificadorMensaje())) {
            //Actualizamos situación de listas
            NucleoNegociacion.listaAceptado.remove(entrada.getIdentificadorMensaje());
            NucleoNegociacion.listaComienzo.put(entrada.getIdentificadorMensaje(), entrada);
            Log.info("Mensaje Comienzo recibido con ID: " + entrada.getIdentificadorMensaje());
        }
    }

    private void procesarMensajeTERMINADO(MensajeNegociacionJSON entrada) {
        //Como este mensaje lo recibe el receptor, hay que comprobar la
        //preexistencia
        if (NucleoNegociacion.listaComienzo.containsKey(entrada.getIdentificadorMensaje())) {
            //Actualizamos situación de listas
            NucleoNegociacion.listaComienzo.remove(entrada.getIdentificadorMensaje());
            NucleoNegociacion.listaTerminado.put(entrada.getIdentificadorMensaje(), entrada);
            Log.info("Mensaje Terminado recibido con ID: " + entrada.getIdentificadorMensaje());
        }
    }

}
