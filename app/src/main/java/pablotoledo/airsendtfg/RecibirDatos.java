package pablotoledo.airsendtfg;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Random;

import librerias.nucleo.negociacion.EmisorNegociacion;
import librerias.nucleo.negociacion.MensajeNegociacionJSON;
import librerias.nucleo.negociacion.NucleoNegociacion;
import librerias.nucleo.negociacion.ReceptorNegociacion;
import librerias.nucleo.sondeo.MensajeSondeoJSON;
import librerias.nucleo.transferencia.ReceptorTransferencia;
import librerias.utilidades.Utilidades;

public class RecibirDatos extends AppCompatActivity {

    private MensajeNegociacionJSON mensajeRecibido;
    private ReceptorTransferencia receptorTransferencia;
    private Thread hiloTransferencia;
    private NotificationManager mNotificationManager;
    private Random generator = new Random();
    private int notifyID = generator.nextInt(24);;
    private NotificationCompat.Builder mNotifyBuilder;
    private ProgressBar mProgress;
    private boolean notificadaPropuesta;
    private TextView estado;
    private boolean salir = false;
    private Thread hiloNotificaciones;

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recibir_datos);
        this.mensajeRecibido = (MensajeNegociacionJSON) getIntent().getSerializableExtra("mensaje");
        this.cargarTextoInicial();
        this.estado = (TextView) findViewById(R.id.estadoText);
        this.mProgress = (ProgressBar) findViewById(R.id.progressBar2);
        this.mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void cargarTextoInicial(){
        TextView remitente = (TextView) findViewById(R.id.remitenteTexto);
        remitente.setText(remitente.getText() + " " + mensajeRecibido.getNombreEmisor());

        TextView ipDestino = (TextView) findViewById(R.id.IPDestinoTexto);
        ipDestino.setText(ipDestino.getText() + " " + mensajeRecibido.getIpEmisor());

        TextView tamano = (TextView) findViewById(R.id.TamañoTexto);
        tamano.setText(tamano.getText() + " " + mensajeRecibido.getTamano());

        TextView numeroArchivos = (TextView) findViewById(R.id.NumeroArchivosTexto);
        numeroArchivos.setText(numeroArchivos.getText() + " " + mensajeRecibido.getListaElementos().length);

        TextView estado = (TextView) findViewById(R.id.estadoText);
        estado.setText(estado.getText() + " " + "Pendiente de su decisión");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recibir_datos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void botonAceptar(View v){
        new Thread(new Runnable() {
            @Override
            public void run() {
                receptorTransferencia = new ReceptorTransferencia(mensajeRecibido);
                hiloTransferencia = new Thread(receptorTransferencia);
                hiloTransferencia.start();
                EmisorNegociacion.enviarMensajeAceptadoQ1(mensajeRecibido, receptorTransferencia.getPuerto());
                hiloBarraNotificaciones();
                hiloPantalla();
            }
        }).start();
    }

    public void botonCancelar(View v){
        super.onBackPressed();
    }

    public void hiloBarraNotificaciones() {
        this.hiloNotificaciones = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                notificarProgreso();
            }
        });
        hiloNotificaciones.start();
    }

    private void notificarProgreso() {
        try {
            while (true) {
                String estado = NucleoNegociacion.recuperarMensaje(this.mensajeRecibido.getIdentificadorMensaje()).getTipoMensaje();
                String titulo = "";
                String texto = "";

                if ((estado.equals(MensajeNegociacionJSON.tipoMensajes[0])&&(!this.notificadaPropuesta))) {
                    titulo = "Propuesta recibida";
                    texto = mensajeRecibido.getNombreEmisor()+" quiere enviarte algo";
                    mNotifyBuilder = new NotificationCompat.Builder(this)
                            .setContentTitle(titulo)
                            .setContentText(texto)
                            .setSmallIcon(R.drawable.icono128);
                    mNotificationManager.notify(
                            notifyID,
                            mNotifyBuilder.build());
                    this.notificadaPropuesta=true;
                }
                if (estado.equals(MensajeNegociacionJSON.tipoMensajes[1])) {
                    titulo = "Propuesta aceptada";
                    texto = "Esperando envío";
                    mNotifyBuilder = new NotificationCompat.Builder(this)
                            .setContentTitle(titulo)
                            .setContentText(texto)
                            .setSmallIcon(R.drawable.icono128);
                    mNotificationManager.notify(
                            notifyID,
                            mNotifyBuilder.build());
                }
                if (estado.equals(MensajeNegociacionJSON.tipoMensajes[2])) {
                    titulo = "Denegado";
                    texto = "Propuesta denegada";
                    mNotifyBuilder = new NotificationCompat.Builder(this)
                            .setContentTitle(titulo)
                            .setContentText(texto)
                            .setSmallIcon(R.drawable.icono128);
                    mNotificationManager.notify(
                            notifyID,
                            mNotifyBuilder.build());
                    break;
                }
                if (estado.equals(MensajeNegociacionJSON.tipoMensajes[3])) {
                    titulo = "Recepción en progreso";
                    texto = "Recepción en progreso de "+mensajeRecibido.getNombreEmisor();
                    mNotifyBuilder = new NotificationCompat.Builder(this)
                            .setContentTitle(titulo)
                            .setContentText(texto)
                            .setProgress((int) this.mensajeRecibido.getTamano() * 1024, this.receptorTransferencia.getProgreso(), false)
                            .setSmallIcon(R.drawable.icono128);
                    mNotificationManager.notify(
                            notifyID,
                            mNotifyBuilder.build());
                }
                if (estado.equals(MensajeNegociacionJSON.tipoMensajes[4])) {
                    titulo = "Envío completo";
                    texto = "Transferencia de "+mensajeRecibido.getNombreEmisor()+" completada";
                    mNotifyBuilder = new NotificationCompat.Builder(this)
                            .setContentTitle(titulo)
                            .setContentText(texto)
                            .setSmallIcon(R.drawable.icono128);
                    mNotificationManager.notify(
                            notifyID,
                            mNotifyBuilder.build());
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {

                }
            }
        } catch (NullPointerException e) {

        }
    }

    public void hiloPantalla(){
        new Thread() {
            @Override
            public void run() {
                try {
                    while (!salir) {
                        synchronized (this) {
                            wait(600);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String estadoMensaje = NucleoNegociacion.recuperarMensaje(mensajeRecibido.getIdentificadorMensaje()).getTipoMensaje();
                                    String titulo = "";
                                    String texto = "";
                                    estado = (TextView) findViewById(R.id.estadoText);
                                    if ((estadoMensaje.equals(MensajeNegociacionJSON.tipoMensajes[0])&&(!notificadaPropuesta))) {
                                        estado.setText("Estado: Propuesta enviada");
                                    }
                                    if (estadoMensaje.equals(MensajeNegociacionJSON.tipoMensajes[1])) {
                                        estado.setText("Estado: Propuesta aceptada");
                                    }
                                    if (estadoMensaje.equals(MensajeNegociacionJSON.tipoMensajes[2])) {
                                        estado.setText("Estado: Propuesta denegada");
                                        salir=true;
                                    }
                                    if (estadoMensaje.equals(MensajeNegociacionJSON.tipoMensajes[3])) {
                                        mProgress.setMax((int)mensajeRecibido.getTamano() * 1024);
                                        mProgress.setProgress(receptorTransferencia.getProgreso());
                                        estado.setText("Estado: Envío en progreso");
                                    }
                                    if (estadoMensaje.equals(MensajeNegociacionJSON.tipoMensajes[4])) {
                                        estado.setText("Estado: Transferencia completada");
                                        mProgress.setMax(1024);
                                        mProgress.setProgress(1024);
                                        hiloNotificaciones.interrupt();
                                        notificarProgreso();
                                        salir=true;
                                    }
                                }
                            });
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }



}
