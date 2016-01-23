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

import librerias.utilidades.Persistencia;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esta clase estructurará el mensaje a enviar para el broadcast que permita
 * anunciar dentro de la red nuestra presencia
 * @author Pablo
 */
public class MensajeSondeoJSON implements Serializable {

    private String sistemaOperativo; //Innecesario ¿?
    private String nombreEquipo; //Nombre del equipo
    private String nombreUsuario; //Nombre del usuario actual del sistema
    private String direccionIP; //IP Local, generalmente se asigna en destino
    private String tiempo; //Timestamp para permitir descartes de mensajes por antigüedad
    private String iconoUsuario; //Nombre del icono del que hace uso el usuario
    private String idEmisor; //Id local del usuario asignada por AirSend

    /**
     * Constructor de clase, se asignan automáticamente todos los valores posibles
     */
    public MensajeSondeoJSON() {
        this.sistemaOperativo = System.getProperty("os.name");;
        this.nombreEquipo = "Android";
        this.nombreUsuario = Persistencia.getNombreUsuario();
        this.direccionIP = "";
        this.iconoUsuario = Persistencia.getGatoUsuario();
        this.tiempo = new Timestamp(System.currentTimeMillis()).toString();
        this.idEmisor = Persistencia.getIdUsuario();
    }

    //Getters para la clase GSON
    /**
     * Id única del usuario, asignada por AirSend
     * @return (String) ID de AirSend
     */
    public String getIdEmisor() {
        return idEmisor;
    }

    /**
     * Icono de usuario
     * @return (String) Icono
     */
    public String getIconoUsuario() {
        return iconoUsuario;
    }

    /**
     * Sistema operativo del emisor del mensaje de sondeo
     * @return (String) Icono de usuario
     */
    public String getSistemaOperativo() {
        return sistemaOperativo;
    }

    /**
     * Nombre del sistema o equipo
     * @return (String) Nombre del equipo
     */
    public String getNombreEquipo() {
        return nombreEquipo;
    }

    /**
     * Nombre del usuario del sistema
     * @return (String) UserName
     */
    public String getNombreUsuario() {
        return nombreUsuario;
    }

    /**
     * Dirección IP, generalmente asignado una vez alcanza un equipo de destino
     * @return (String) Dirección IP
     */
    public String getDireccionIP() {
        return direccionIP;
    }

    /**
     * Timestamp de origen
     * @return (String) TimeStamp
     */
    public String getTiempo() {
        return tiempo;
    }

    /**
     * Asigna la dirección IP de origen del mensaje
     * @param direccionIP (String) IP
     */
    public void setDireccionIP(String direccionIP) {
        this.direccionIP = direccionIP;
    }

    /**
     * Comparador de clase bajo el criterio de idEmisor
     * @param entrada
     * @return 
     */
    public boolean igual(MensajeSondeoJSON entrada) {
        return entrada.getIdEmisor().equals(this.idEmisor);
    }

}
