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
package librerias.utilidades;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Esta clase permite llevar un control de los mensajes que se imprimen por
 * pantalla para controlar la depuración en su desarrollo.
 *
 * @author juanpablotoledogavagnin
 */
public class Log {

    private static final String directorio = System.getProperty("user.home") + "/";
    private static final String ficheroNombre = "AirSendLog.csv";
    private static final String separador = "-----------------------------------";
    private static final String mensajeInicial = " Comienza ejecución de AirSend";
    private static final String mensajeFinal = " Finaliza ejecución de AirSend";
    private static final java.io.File archivo = new java.io.File(directorio + ficheroNombre);
    private static final boolean activarLogs = false; //Si es true el programa funcionará usando archivos Logs
    private static final boolean activarTerminal = true; //Si es true el programa imprimirá mensajes por pantalla
    private static final boolean registrarSondeo = true; //Si es true el programa registrará los mensajes del protocolo de sondeo

    /**
     * Este método inicializa un mensaje por pantalla y por el fichero de log
     */
    public static void inicializar() {
        Date fechaHora = new Date();
        SimpleDateFormat tiempoValor = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String textoFechaHora = tiempoValor.format(fechaHora);
        Log.insertarTexto(separador);
        Log.insertarTexto(textoFechaHora + mensajeInicial);
    }

    /**
     * Método destinado para registrar mensajes de error
     *
     * @param entrada
     */
    public static void error(String entrada) {
        Log.insertarTexto("ERROR: " + entrada);
    }

    /**
     * Método destinado para registrar mensajes informativos
     *
     * @param entrada
     */
    public static void info(String entrada) {
        Log.insertarTexto("INFO: " + entrada);
    }

    /**
     * Método destinado a registrar mensajes informativos relacionados con el
     * proceso de desarrollo con el fin de facilitar la identificación de
     * mensajes de depuración
     *
     * @param entrada
     */
    public static void debug(String entrada) {
        Log.insertarTexto("DEBUG: " + entrada);
    }

    /**
     * Método destinado a registrar mensajes asociados al protocolo de sondeo
     * (Emisor)
     *
     * @param entrada
     */
    public static void sondeoEmisor(String entrada) {
        if (Log.registrarSondeo) {
            Log.insertarTexto("SONDEO-EMISOR: " + entrada);
        }
    }

    /**
     * Método destinado a registrar mensajes asociados al protocolo de sondeo
     * (Receptor)
     *
     * @param entrada
     */
    public static void sondeoReceptor(String entrada) {
        if (Log.registrarSondeo) {
            Log.insertarTexto("SONDEO-RECEPTOR: " + entrada);
        }
    }

    /**
     * Este método despide la ejecución del programa notificandolo por pantalla
     * y registrándolo en un fichero
     */
    public static void finalizar() {
        Date fechaHora = new Date();
        SimpleDateFormat tiempoValor = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String textoFechaHora = tiempoValor.format(fechaHora);
        Log.insertarTexto(textoFechaHora + mensajeFinal);
        Log.insertarTexto(separador);
    }

    /**
     * Método para imprimir por pantalla y escribir un mensaje en el fichero
     * log, será invocado por métodos de su propia clase facilitando la
     * reutilización de código.
     *
     * @param entrada String
     */
    private static void insertarTexto(String entrada) {
        try {
            // Mensaje por Log
            if (Log.activarLogs) {
                FileWriter fileWriter = new FileWriter(archivo, true);
                BufferedWriter bufferFileWriter = new BufferedWriter(fileWriter);
                fileWriter.append(entrada + "\n");
                bufferFileWriter.close();
            }
            // Mensaje por terminal de comandos
            if (Log.activarTerminal) {
                if (entrada.startsWith("ERROR:")) {
                    android.util.Log.e("AirSend",entrada);
                } else {
                    android.util.Log.i("AirSend",entrada);
                }
            }
        } catch (Exception ex) {
            android.util.Log.e("AirSend",ex.getLocalizedMessage());
        }
    }

}
