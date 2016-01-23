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

import librerias.nucleo.sondeo.MensajeSondeoJSON;
import librerias.utilidades.Log;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 *
 * @author Pablo
 */
public class EmisorNegociacion {

    /**
     * Permite generar y enviar un mensaje de propuesta de envio
     *
     * @param destino IP de destino
     * @param archivos Listado de ficheros a enviar
     * @return
     */
    public static MensajeNegociacionJSON generarMensajeEmisorQ1(MensajeSondeoJSON destino, File[] archivos) {
        MensajeNegociacionJSON mensaje = new MensajeNegociacionJSON(archivos, destino.getDireccionIP(), destino.getIdEmisor());
        NucleoNegociacion.listaPropuesta.put(mensaje.getIdentificadorMensaje(), mensaje);
        enviarMensaje(destino.getDireccionIP(), mensaje);
        return mensaje;
    }

    /**
     * Permite enviar un mensaje para aceptar una propuesta de envío
     *
     * @param entrada MensajeNegociacionJSON
     * @param puerto Necesaria una asignación previa
     */
    public static void enviarMensajeAceptadoQ1(MensajeNegociacionJSON entrada, int puerto) {
        //Comprobamos que sea un mensaje de propuesta
        if (entrada.getTipoMensaje().equals(MensajeNegociacionJSON.getTipoMensajes()[0])) {
            //Asignamos el valor de tipo al mensaje de entrada
            entrada.setTipoMensaje(MensajeNegociacionJSON.getTipoMensajes()[1]);
            //Eliminamos el mensaje en la lista de listaPropuesta
            NucleoNegociacion.listaPropuesta.remove(entrada.getIdentificadorMensaje());
            //Asignamos puerto
            entrada.setPuertoReceptorTransferencia(puerto);
            //Asignamos a listaCominzo
            NucleoNegociacion.listaAceptado.put(entrada.getIdentificadorMensaje(), entrada);
            //Enviamos aceptación al emisor
            enviarMensaje(entrada.getIpEmisor(), entrada);
            Log.info("Mensaje " + entrada.getIdentificadorMensaje() + " modificado a ACEPTADO y enviado");
        } else {
            Log.error(entrada.getIdentificadorMensaje() + " no es un mensaje de propuesta");
        }
    }

    /**
     * Permite generar y enviar un mensaje para denegar una propuesta de envío
     *
     * @param entrada Mensaje
     */
    public static void enviarMensajeDenegadoQ1(MensajeNegociacionJSON entrada) {
        //Comprobamos que sea un mensaje de propuesta
        if (entrada.getTipoMensaje().equals(MensajeNegociacionJSON.getTipoMensajes()[0])) {
            //Asignamos el valor de tipo al mensaje de entrada
            entrada.setTipoMensaje(MensajeNegociacionJSON.getTipoMensajes()[2]);
            //Eliminamos el mensaje en la lista de listaPropuesta
            NucleoNegociacion.listaPropuesta.remove(entrada.getIdentificadorMensaje());
            //Enviamos denegacion al emisor
            enviarMensaje(entrada.getIpEmisor(), entrada);
            Log.info("Mensaje " + entrada.getIdentificadorMensaje() + " modificado a DENEGADO y enviado");
            //Al denegarse el mensaje se elimina de listaPropuestaRecibida
            //NucleoNegociacion.eliminarLPropuestaRecibida(entrada);
        } else {
            Log.error(entrada.getIdentificadorMensaje() + " no es un mensaje de propuesta");
        }
    }

    /**
     * Permite generar y enviar un mensaje avisar de que comienza la
     * transferencia
     *
     * @param entrada Mensaje
     */
    public static void enviarMensajeComienzoQ2(MensajeNegociacionJSON entrada) {
        //Comprobamos que sea un mensaje previamente aceptado
        if (entrada.getTipoMensaje().equals(MensajeNegociacionJSON.tipoMensajes[1])) {
            //Asignamos el valor de tipo al mensaje de entrada
            entrada.setTipoMensaje(MensajeNegociacionJSON.tipoMensajes[3]);
            //Actualizamos el mensaje en la lista de listaPropuestaEnviada
            NucleoNegociacion.listaAceptado.remove(entrada.getIdentificadorMensaje());
            NucleoNegociacion.listaComienzo.put(entrada.getIdentificadorMensaje(), entrada);
            //Enviamos mensaje al receptor
            enviarMensaje(entrada.getIpDestinatario(), entrada);
            Log.info("Mensaje " + entrada.getIdentificadorMensaje() + " modificado a COMIENZO y enviado");
        } else {
            Log.error(entrada.getIdentificadorMensaje() + " no se trata de una solicitud previamente aceptada");
        }
    }

    /**
     * Permite enviar un mensaje al finalizar la transferencia
     * @param entrada 
     */
    public static void enviarMensajeTerminadoQ1(MensajeNegociacionJSON entrada) {
        //Asignamos el valor de tipo al mensaje de entrada
        entrada.setTipoMensaje(MensajeNegociacionJSON.tipoMensajes[4]);
        //Actualizamos el mensaje en la lista de listaPropuestaEnviada
        NucleoNegociacion.listaComienzo.remove(entrada.getIdentificadorMensaje());
        NucleoNegociacion.listaTerminado.put(entrada.getIdentificadorMensaje(), entrada);
        //Enviamos mensaje al receptor
        enviarMensaje(entrada.getIpDestinatario(), entrada);
        Log.info("Mensaje " + entrada.getIdentificadorMensaje() + " modificado a TERMINADO y enviado");
    }

    /**
     * Método auxiliar que permite enviar un objeto mensaje a la dirección
     * determinada
     *
     * @param direccion String - IP de destino
     * @param mensaje Mensaje - objeto a enviar
     */
    protected static void enviarMensaje(String direccion, MensajeNegociacionJSON mensaje) {
        try {
            // Se envia mensaje proponiendo el envío de datos
            Socket socket = new Socket(InetAddress.getByName(direccion), NucleoNegociacion.puertoNucleoNegociacion);
            // Enviamos mensaje
            ObjectOutputStream oo = new ObjectOutputStream(socket.getOutputStream());
            byte[] bufferMensaje = new Gson().toJson(mensaje).getBytes("UTF-8");
            //Lo enviamos como bytes en UTF8
            oo.write(bufferMensaje);
            oo.flush();
            socket.close();
            Log.info("Mensaje de propuesta enviado a " + direccion);
        } catch (IOException ioe) {
            Log.error("Problema al enviar mensaje: generarMensajeEmisorQ1 en NucleoNegociación");
        }
    }

}
