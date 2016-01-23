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

import java.util.ArrayList;

/**
 * Clase de abstracción para la el sondeo que aisla la lógica interna a las
 * capas superiores
 *
 * @author Pablo
 */
public class NucleoSondeo {

    public static int puertoBroadcast = 8585;
    public static String dirBroadcast = "255.255.255.255";
    public static int tiempoSleppLoopSondeo = 3000;

    private static Thread hiloEmisorSondeo;
    private static Thread hiloReceptorSondeo;
    private static EmisorSondeo emisorSondeo;
    private static ReceptorSondeo receptorSondeo;

    /**
     * Constructor
     */
    public NucleoSondeo() {
        this.emisorSondeo = new EmisorSondeo();
        this.receptorSondeo = new ReceptorSondeo();
        this.hiloEmisorSondeo = new Thread(this.emisorSondeo);
        this.hiloReceptorSondeo = new Thread(this.receptorSondeo);
        this.hiloEmisorSondeo.start();
        this.hiloReceptorSondeo.start();
    }
    
    /**
     * Permite deterner los hilos
     */
    public void pararNucleo(){
        hiloEmisorSondeo.interrupt();
        hiloReceptorSondeo.interrupt();
        receptorSondeo.liberarSockets();
    }

    /**
     * Permite arrancar los hilos
     */
    public void runNucleo(){
        this.emisorSondeo = new EmisorSondeo();
        this.receptorSondeo = new ReceptorSondeo();
        this.hiloEmisorSondeo = new Thread(this.emisorSondeo);
        this.hiloReceptorSondeo = new Thread(this.receptorSondeo);
        this.hiloEmisorSondeo.start();
        this.hiloReceptorSondeo.start();
    }
    
    /**
     * Permite objetener una lista de elementos recibidos y actualizada
     * @return Lista de elementos recibidos y actualizada
     */
    public static ArrayList<MensajeSondeoJSON> getListaElementos() {
        return receptorSondeo.getLista();
    }

}
