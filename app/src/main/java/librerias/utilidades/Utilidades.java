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

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.text.DecimalFormat;

/**
 *
 * @author juanpablotoledogavagnin
 */
public class Utilidades {

    private static ServerSocket singleton;
    /**
     * Permite el calculo del peso de un array de File y devuelve la fecha en
     * formato String #.## MB
     *
     * @param lista
     * @return String formato #.## MB
     */
    public static String calcularTamano(File[] lista) {
        double peso = 0;
        for (int i = 0; i < lista.length; i++) {
            peso = peso + extraccionRecursiva(lista[i]);
        }
        peso = peso / (1024 * 1024);
        DecimalFormat df = new DecimalFormat("#.###");
        return df.format(peso);
    }

    /**
     * Permite el calculo del peso de un array de File y devuelve la fecha en
     * formato double
     *
     * @param lista
     * @return String formato #.## MB
     */
    public static double calcularTamanoD(File[] lista) {
        double peso = 0;
        for (File lista1 : lista) {
            peso = peso + extraccionRecursiva(lista1);
        }
        peso = peso / (1024 * 1024);
        return peso;
    }

    /**
     * Método auxiliar para el cálculo recursivo de los File que son directorios
     *
     * @param entrada
     * @return
     */
    private static long extraccionRecursiva(File entrada) {
        long length = 0;
        if (entrada.isDirectory()) {
            for (File file : entrada.listFiles()) {
                if (file.isFile()) {
                    length += file.length();
                } else {
                    length += extraccionRecursiva(file);
                }
            }
        } else {
            length = entrada.length();
        }

        return length;
    }
    
    /**
     * Este método garantiza que AirSend mantenga solo una instancia en ejecución
     * @return booleano
     */
    public static boolean singleton(){
        try {
            singleton = new ServerSocket(8827);
            return true;
        } catch (IOException ex) {
            Log.error("Existe otra instacia en ejecución");
            System.exit(0);
            return false;
        }
    }

    /**
     * Este método permite comprobar si existe una conexión wifi activa
     * @param context
     * @return
     */
    public static boolean existeConexionWIFI(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (wifiNetwork != null && wifiNetwork.isConnected())
        {
            return true;
        }
        return false;
    }

    public static String obtenerDireccionDesdeUri(Activity activity, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = activity.managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}
