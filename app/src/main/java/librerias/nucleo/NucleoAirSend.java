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
package librerias.nucleo;

import android.app.Activity;
import android.content.Context;

import librerias.nucleo.negociacion.NucleoNegociacion;
import librerias.nucleo.sondeo.MensajeSondeoJSON;
import librerias.nucleo.sondeo.NucleoSondeo;
import librerias.utilidades.Persistencia;
import pablotoledo.airsendtfg.MenuPrincipal;

import java.util.ArrayList;

/**
 * Clase que encargada de gestionar los mecanismos de sondeo y negociacion
 * @author Pablo
 */
public class NucleoAirSend {
    
    private static NucleoSondeo nucleoSondeo;
    private static NucleoNegociacion nucleoNegociacion;
    public static Context contexto;
    public static MenuPrincipal actividad;
    
    /**
     * Carga los núcleos
     */
    public static void cargarNucleos(Context context,MenuPrincipal entrada){
        nucleoSondeo = new NucleoSondeo();
        contexto = context;
        nucleoNegociacion = new NucleoNegociacion();
        actividad = entrada;
        Persistencia.cargarPersistencia(context);
    }
    
    /**
     * Permite recuperar la lista de dispositivos
     * @return lista de dispositivos
     */
    public synchronized static ArrayList<MensajeSondeoJSON> getListaDispositivos(){
        return nucleoSondeo.getListaElementos();
    }
    
    /**
     * Realiza una interrupción de los hilos
     */
    public static void pararNucleos(){
        nucleoSondeo.pararNucleo();
        nucleoNegociacion.pararNucleo();
    }
    
    /**
     * Arranca los nucleos detenidos
     */
    public static void runNucleos(){
        nucleoSondeo.runNucleo();
        nucleoNegociacion.runNucleo();
    }
    
}
