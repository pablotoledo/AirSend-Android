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

import librerias.nucleo.negociacion.EmisorNegociacion;
import librerias.nucleo.negociacion.MensajeNegociacionJSON;
import librerias.nucleo.negociacion.NucleoNegociacion;
import librerias.utilidades.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Pablo
 */
public class EmisorTransferencia implements Runnable {

    //Nivel de compresión (Valorar establecer un valor en función del sistema)
    private int nivelCompresion = 3;

    //Objetos que provienen de otras clases
    private MensajeNegociacionJSON mensaje;

    //Socket
    private Socket socketDatos;

    //Valor para JProgressBar
    private int progreso;

    private String CARPETAORIGEN;

    /**
     * Método constructor que precisa de ciertos objetos para trabajar
     * correctamente de la EnviarInterfaz que lo va a crear.
     *
     * @param mensaje Mensaje
     */
    public EmisorTransferencia(MensajeNegociacionJSON mensaje) {
        //this.cargarListado(mensaje.getListaElementos());
        this.CARPETAORIGEN = mensaje.getListaElementos()[0].getParent() + "/";
        this.mensaje = mensaje;
        
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
     * Método main a ejecutarse cuando se lance esta clase como hilo concurrente
     */
    @Override
    public void run() {
        /*
        Hasta que no se recibe un mensaje de ACEPTADO o DENEGADO esperamos de forma
        activa a que se de una respuesta
        */
        while (true) {
            try {
                MensajeNegociacionJSON mensajeGuardado = NucleoNegociacion.recuperarMensaje(mensaje.getIdentificadorMensaje());
                if (mensajeGuardado.getTipoMensaje().equals(NucleoNegociacion.tipoMensajes[3])) {
                    mensaje = mensajeGuardado;
                    if((mensaje.getListaElementos().length>1)||(mensaje.isDirectorioUnico()))
                        transmitirComprimido(this.mensaje.getListaElementos());
                    else
                        transmitirUnico(this.mensaje.getListaElementos());
                    break;
                }
                if (mensajeGuardado.getTipoMensaje().equals(NucleoNegociacion.tipoMensajes[2])) {
                    mensaje = mensajeGuardado;
                    break;
                }
                Thread.sleep(500);
            } catch (UnknownHostException ex) {
                //Cuando la dirección del receptor no se puede resolver cuando se va
                //a realizar la creación del socket
                Log.error("Fallo de envío: IP de destino no resuelta para el mensaje " + this.mensaje.getIdentificadorMensaje());
                Logger.getLogger(EmisorTransferencia.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                //Cuando se rompe la conexión de forma inesperada debe tenerse en cuenta
                Log.error("Fallo de envío: Fallo durante la transferencia en el mensaje " + this.mensaje.getIdentificadorMensaje());
                Logger.getLogger(EmisorTransferencia.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Log.error("Fallo o interrupción de la conexión: " + ex);
            }
        }
    }

    /**
     * Método privado que se encarga de enviar en un socket dado los archivos ya
     * previamente comprimidos
     *
     * @param listaEnviar File[] - Lista de ficheros a enviar del Mensaje
     * @throws IOException
     */
    private void transmitirComprimido(File[] listaEnviar) throws IOException {
        //Socket
        socketDatos = new Socket(InetAddress.getByName(mensaje.getIpDestinatario()), mensaje.getPuertoReceptorTransferencia());
        //Conectamos un ZipOutputStream al socket, lo que provoca que se escriban
        //los datos comprimidos en el equipo remoto
        ZipOutputStream zos = new ZipOutputStream(socketDatos.getOutputStream());
        //Establecemos el nivel de compresión que queremos
        zos.setLevel(this.nivelCompresion);
        //Ordenamos comprimir la lista recibida como parámentro de entrada
        this.comprimir(listaEnviar, zos);
        //Cerramos de forma protocolaria el socket y el flujo de compresión
        zos.close();
        socketDatos.close();
        //Avisamos que hemos terminado
        EmisorNegociacion.enviarMensajeTerminadoQ1(mensaje);
    }

    /**
     * Método privado que se encarga de enviar en un socket un único archivo
     *
     * @param listaEnviar File[] - Lista de ficheros a enviar del Mensaje
     * @throws IOException
     */
    private void transmitirUnico(File[] listaEnviar) throws IOException{
        //Socket
        socketDatos = new Socket(InetAddress.getByName(mensaje.getIpDestinatario()), mensaje.getPuertoReceptorTransferencia());
        //Conectamos un ZipOutputStream al socket, lo que provoca que se escriban
        //los datos comprimidos en el equipo remoto
        OutputStream os = socketDatos.getOutputStream();
        InputStream in = new FileInputStream(listaEnviar[0]);
        byte[] buf = new byte[1024];
        int len = 0;
        while ((len = in.read(buf)) != -1) {
            os.write(buf, 0, len);
            int valor = (int) len;
            this.progreso = (valor / 1024) + this.progreso;
        }
        //Cerramos de forma protocolaria el socket y el flujo de compresión
        os.close();
        socketDatos.close();
        //Avisamos que hemos terminado
        EmisorNegociacion.enviarMensajeTerminadoQ1(mensaje);
    }

    /**
     * Este método privado se encarga de recorrer la lista de ficheros y
     * recorrer esta de forma recursiva, diferenciando ficheros de directorios y
     * tratando a cada uno de la forma que se necesita para crear un fichero ZIP
     * correctamente
     *
     * @param entrada File[] - Lista del Mensaje
     * @param zos ZipOutputStream - Inicializado con respecto al socket
     */
    private void comprimir(File[] entrada, ZipOutputStream zos) {
        //Recorremos el array File[] de entrada
        for (File entrada1 : entrada) {
            //Comprobamos si es un directorio
            if (entrada1.isDirectory()) {
                //Si es un directorio
                try {
                    //Añadimos la carpeta al comprimido
                    nuevaCarpeta(zos, entrada1.getPath(), entrada1.getParent());
                } catch (Exception ex) {
                    Logger.getLogger(EmisorTransferencia.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                //Si no es un directorio se supone que es un fichero
                try {
                    //Añadimos el fichero al comprimido
                    nuevoFichero(zos, entrada1.getName(), entrada1);
                } catch (Exception ex) {
                    Logger.getLogger(EmisorTransferencia.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Método privado que permite añadir un fichero al flujo de datos para
     * comprimirlo
     *
     * @param zos ZipOutputStream - Flujo de datos
     * @param nombre String - Nombre del fichero a comprimir
     * @param archivo File - Archivo a comprimir
     * @throws Exception
     */
    private void nuevoFichero(ZipOutputStream zos, String nombre, File archivo) throws Exception {
        //Registramos el suceso
        //Log.info("A "+this.CARPETAORIGEN);
        //Log.info("B "+archivo.getPath()); 
        Log.info("Compresión: Añadiendo fichero " + nombre + "...");
        //Creamos una entrada de registro
        String entrada = archivo.getPath().substring(this.CARPETAORIGEN.length());
        //Log.info("Compresión e-> " + entrada + " previo!");
        entrada = entrada.replace('\\', '/');//Necesario para sistemas Windows :@
        ZipEntry ze = new ZipEntry(entrada);
        //Añadimos la entrada al flujo
        zos.putNextEntry(ze);
        //Creamos un flujo de lectura que será dirigido al flujo de compresión
        FileInputStream in = new FileInputStream(archivo);
        int len;
        byte buffer[] = new byte[1024];
        while ((len = in.read(buffer)) > 0) {
            zos.write(buffer, 0, len);
            int valor = (int) len;
            this.progreso = (valor / 1024) + this.progreso;
        }
        Log.info("Compresión e-> " + entrada + " OK!");
        //Cerramos el flujo de lectura
        in.close();
        //Informamos de que hemos terminado de enviar el fichero actual
        zos.closeEntry();
        
    }

    /**
     * Método privado que permite añadir un directorio al flujo de datos para
     * comprimirlo
     *
     * @param zos ZipOutputStream - Flujo de datos
     * @param rutaCarpeta String - Ruta de carpeta
     * @param ubicacionCarpeta String - Ubicación de la carpeta parent de la
     * carpeta
     * @throws Exception
     */
    private void nuevaCarpeta(ZipOutputStream zos, String rutaCarpeta, String ubicacionCarpeta) throws Exception {
        //Creamos un objeto File apuntando a la carpeta a comprimir
        File carpeta = new File(rutaCarpeta);
        if (carpeta.exists()) {
            //Si la ruta existe
            if (carpeta.isDirectory()) {
                //Si se trata de un directorio
                //Llamada recursiva para resolver esta situacion
                File f2[] = carpeta.listFiles();
                for (File f21 : f2) {
                    nuevaCarpeta(zos, f21.getAbsolutePath(), ubicacionCarpeta);
                }
            } else {
                //Si se trata de un fichero
                String nombreCarpeta = rutaCarpeta.substring(ubicacionCarpeta.length() + 1, rutaCarpeta.length());
                //Invocamos el método asociado a comprimir un fichero
                this.nuevoFichero(zos, nombreCarpeta, carpeta);
            }
        } else {
            System.out.println("No se encuentra el archivo o carpeta: " + ubicacionCarpeta);
        }

    }
}
