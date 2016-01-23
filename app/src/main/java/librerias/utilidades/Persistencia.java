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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import librerias.nucleo.negociacion.MensajeNegociacionJSON;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;


/**
 *
 * @author Pablo
 */
public class Persistencia implements Serializable{
    private static boolean licencia;
    private static String nombreUsuario;
    private static String gatoUsuario;
    private static String idUsuario = java.util.UUID.randomUUID().toString();
    private static String rutaDescarga = Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
    private static Map<String, MensajeNegociacionJSON> listaDispositivosConfianza = new HashMap<String, MensajeNegociacionJSON>();
    
    private boolean licenciaP;
    private String nombreUsuarioP = "";
    private String gatoUsuarioP;
    private String idUsuarioP;
    private String rutaDescargaP;
    private Map<String, MensajeNegociacionJSON> listaDispositivosConfianzaP;

    public Persistencia(){
        this.idUsuarioP = java.util.UUID.randomUUID().toString();
        this.rutaDescargaP = System.getProperty("user.home")+"/";
        this.listaDispositivosConfianzaP =  new HashMap<String, MensajeNegociacionJSON>();
    }

    public static void guardarPersistencia(Context context) {
        Persistencia objeto = new Persistencia();
        objeto.setLicenciaP(licencia);
        objeto.setNombreUsuarioP(nombreUsuario);
        objeto.setGatoUsuarioP(gatoUsuario);
        objeto.setIdUsuarioP(idUsuario);
        objeto.setRutaDescargaP(rutaDescarga);
        objeto.setListaDispositivosConfianzaP(listaDispositivosConfianza);
        String objetoEnJSON = new Gson().toJson(objeto);
        SharedPreferences sharedPref =  context.getSharedPreferences("PreferencesUser", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("Persistencia", objetoEnJSON);
        editor.commit();
    }
    
    public static void cargarPersistencia(Context context){
        SharedPreferences preferencesUser = context.getSharedPreferences("PreferencesUser", Context.MODE_PRIVATE);
        String jsonUserSaved = preferencesUser.getString("Persistencia",new Gson().toJson(new Persistencia()));
        Persistencia objeto = new Gson().fromJson(jsonUserSaved, Persistencia.class);
        licencia = objeto.isLicenciaP();
        nombreUsuario = objeto.getNombreUsuarioP();
        gatoUsuario = objeto.getGatoUsuarioP();
        idUsuario = objeto.getIdUsuarioP();
        listaDispositivosConfianza = objeto.getListaDispositivosConfianzaP();
        Log.info("Cargando persistencia");
        guardarPersistencia(context);
    }
    
    // Setters
    public static void setLicencia(boolean licencia) {
        Persistencia.licencia = licencia;
    }

    public static void setNombreUsuario(String nombreUsuario) {
        if(nombreUsuario.length()>10)
            Persistencia.nombreUsuario = nombreUsuario.substring(0, 9);
        else
            Persistencia.nombreUsuario = nombreUsuario;
    }

    public static void setGatoUsuario(String gatoUsuario) {
        Persistencia.gatoUsuario = gatoUsuario;
    }

    public static void setIdUsuario(String idUsuario) {
        Persistencia.idUsuario = idUsuario;
    }
    
    public static void setRutaDescarga(String rutaDescarga){
        Persistencia.rutaDescarga = rutaDescarga;
    }

    //Getters
    public static boolean isLicencia() {
        return licencia;
    }

    public static String getNombreUsuario() {
        return nombreUsuario;
    }

    public static String getGatoUsuario() {
        return gatoUsuario;
    }

    public static String getIdUsuario() {
        return idUsuario;
    }
    
    public static String getRutaDescarga(){
        return rutaDescarga;
    }

    public boolean isLicenciaP() {
        return licenciaP;
    }

    public String getNombreUsuarioP() {
        return nombreUsuarioP;
    }

    public String getGatoUsuarioP() {
        return gatoUsuarioP;
    }

    public String getIdUsuarioP() {
        return idUsuarioP;
    }

    public String getRutaDescargaP() {
        return rutaDescargaP;
    }

    public void setLicenciaP(boolean licenciaP) {
        this.licenciaP = licenciaP;
    }

    public void setNombreUsuarioP(String nombreUsuarioP) {
        this.nombreUsuarioP = nombreUsuarioP;
    }

    public void setGatoUsuarioP(String gatoUsuarioP) {
        this.gatoUsuarioP = gatoUsuarioP;
    }

    public void setIdUsuarioP(String idUsuarioP) {
        this.idUsuarioP = idUsuarioP;
    }

    public void setRutaDescargaP(String rutaDescargaP) {
        this.rutaDescargaP = rutaDescargaP;
    }

    public static Map<String, MensajeNegociacionJSON> getListaDispositivosConfianza() {
        return listaDispositivosConfianza;
    }

    public Map<String, MensajeNegociacionJSON> getListaDispositivosConfianzaP() {
        return listaDispositivosConfianzaP;
    }

    public static void setListaDispositivosConfianza(Map<String, MensajeNegociacionJSON> listaDispositivosConfianza) {
        Persistencia.listaDispositivosConfianza = listaDispositivosConfianza;
    }

    public void setListaDispositivosConfianzaP(Map<String, MensajeNegociacionJSON> listaDispositivosConfianzaP) {
        this.listaDispositivosConfianzaP = listaDispositivosConfianzaP;
    }
    
    
    
}
