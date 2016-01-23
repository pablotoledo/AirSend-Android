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
package librerias.nucleo.transferencia;

import librerias.nucleo.negociacion.MensajeNegociacionJSON;
import librerias.utilidades.Log;
import librerias.utilidades.Persistencia;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Pablo
 */
public class ReceptorTransferencia implements Runnable {

    //Valores ServerSocket y puerto
    private int puerto;
    private ServerSocket socketRecepcion;
    
    //Mensaje de negociación
    private MensajeNegociacionJSON mensaje;
    
    //Ruta y nombre de fichero
    private String rutaFichero = Persistencia.getRutaDescarga();
    private String nombreFichero;
    
    //Valor para JProgressBar
    private int progreso;
    
    /**
     * Método constructor
     *
     * @param mensaje Mensaje - mensaje a recibir
     */
    public ReceptorTransferencia(MensajeNegociacionJSON mensaje) {
        this.mensaje = mensaje;
        this.asignarAtributosSocket();
        this.nombreFichero = mensaje.getNombreEmisor()+".zip";
        Log.info("Asignado futuro hilo de recepción de datos");
    }

    /**
     * Método que devuelve el progreso en número de bytes de la transferencia
     * actual
     * @return (int) Bytes
     */
    public int getProgreso() {
        return progreso;
    }

    /**
     * Método que permite inicializar la ruta donde recibir los archivos
     *
     * @param rutaFichero String - ruta del JFileChooser
     */
    public void setRutaFichero(String rutaFichero) {
        this.rutaFichero = rutaFichero;
    }

    /**
     * Método que asigna el puerto de recepción e inicializa el ServerSocket
     */
    private void asignarAtributosSocket() {
        //Se busca un puerto libre aleatorio en el rango de 8000 y 8400
        int puertoRecibirDatos = (int) (Math.random() * (8400 - 8000) + 8000);
        boolean bandera = true;
        while (bandera) {
            try {
                ServerSocket server = new ServerSocket(puertoRecibirDatos);
                Log.info("Puerto de recepción de datos asignado a " + puertoRecibirDatos);
                this.puerto = puertoRecibirDatos;
                this.socketRecepcion = server;
                mensaje.setPuertoReceptorTransferencia(puertoRecibirDatos);
                bandera = false;
            } catch (IOException x) {
                puertoRecibirDatos = (int) (Math.random() * (8400 - 8000) + 8000);
                Log.error("Reasignado puerto de recepción de datos");
            }
        }
    }

    /**
     * Método que permite conocer el puerto asignado del objeto actual
     *
     * @return int - puerto del ServerSocket
     */
    public int getPuerto() {
        return puerto;
    }

    /**
     * Método main concurrente
     */
    @Override
    public void run() {
        Socket transferencia = null;
        int buffer = 3000;
        InputStream entrada;
        String timeStamp = new SimpleDateFormat(" hhmm - ddmmyyyy").format(new Date());
        String fichero ="";
        String rutaFicheroUnico = this.mensaje.getListaElementos()[0].getName();
        String tipoFicheroUnico=(rutaFicheroUnico.contains("."))?rutaFicheroUnico.substring(this.mensaje.getListaElementos()[0].getName().lastIndexOf(".")):"null";
        if((this.mensaje.isDirectorioUnico())||(this.mensaje.getListaElementos().length>1))
            fichero = this.rutaFichero + this.nombreFichero +timeStamp+ ".zip";
        else
            fichero = (!tipoFicheroUnico.equals("null"))?this.rutaFichero+ timeStamp+tipoFicheroUnico:this.rutaFichero+ timeStamp+".zip";
        try {
            Log.info("Transmisión: A la espera concurrente para recibir datos");
            transferencia = socketRecepcion.accept(); // Espera conexiones de clientes. Crea socket "transferencia"
            Log.info("Transmisión: Se conecta con el emisor");
            entrada = transferencia.getInputStream();
            if (transferencia != null && entrada != null) {
                File f = new File(fichero);
                OutputStream sin = new FileOutputStream(f);
                int n = entrada.available(); //Num de bytes que pueden ser leidos sin bloqueo
                byte buf[] = new byte[buffer];
                Log.info("Transmisión: Recibiendo flujo de datos");
                while ((n = entrada.read(buf)) >= 0) {
                    this.progreso = (n/1024)+this.progreso;
                    sin.write(buf, 0, n);
                }
                sin.close();
            }   // sin.close ();
            Thread.sleep(5000);
            entrada.close();
            transferencia.close();
            Log.info("Transmisión: Se cierra la conexión con el emisor");
            socketRecepcion.close();

            Log.info("Transmisión: Se cierra y libera el puerto " + this.puerto);
        } catch (IOException e) {
            Log.error("Fallo en la conexión: " + e);
        } catch (InterruptedException ex) {
            Log.error("Fallo o interrupción de la conexión: " + ex);
        }

    }
}
