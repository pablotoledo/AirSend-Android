package pablotoledo.airsendtfg;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Random;

import librerias.nucleo.negociacion.EmisorNegociacion;
import librerias.nucleo.negociacion.MensajeNegociacionJSON;
import librerias.nucleo.negociacion.NucleoNegociacion;
import librerias.nucleo.sondeo.MensajeSondeoJSON;
import librerias.nucleo.transferencia.EmisorTransferencia;
import librerias.utilidades.Utilidades;

public class EnviarDatos extends AppCompatActivity {

    private MensajeSondeoJSON mensaje;
    private String rutaFichero;
    private Thread hiloTransferencia;
    private EmisorTransferencia tranferencia;
    private MensajeNegociacionJSON mensajeNegociacion;
    private File[] archivos;
    private NotificationManager mNotificationManager;
    private Random generator = new Random();
    private int notifyID = generator.nextInt(12);;
    private NotificationCompat.Builder mNotifyBuilder;
    private ProgressBar mProgress;
    private Thread hiloNotificaciones;
    private  boolean notificadaPropuesta = false;
    private TextView estado;
    private boolean enviado = false;
    private boolean salir = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enviar_datos);
        this.mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        this.rutaFichero = getIntent().getStringExtra("fichero");
        this.mensaje = (MensajeSondeoJSON) getIntent().getSerializableExtra("mensaje");
        Toast.makeText(this, mensaje.getIdEmisor(), Toast.LENGTH_LONG).show();
        this.cargarTextoInicial();
        this.mProgress = (ProgressBar) findViewById(R.id.progressBar);
        this.estado = (TextView) findViewById(R.id.estadotext);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_enviar_datos, menu);
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

    public void cargarTextoInicial() {
        TextView remitente = (TextView) findViewById(R.id.remitenteTexto);
        remitente.setText(remitente.getText() + " " + mensaje.getNombreUsuario());

        TextView ipDestino = (TextView) findViewById(R.id.IPDestinoTexto);
        ipDestino.setText(ipDestino.getText() + " " + mensaje.getDireccionIP());

        TextView tamano = (TextView) findViewById(R.id.TamañoTexto);
        File[] lista = new File[1];
        lista[0] = new File(this.rutaFichero);
        this.archivos = lista;
        tamano.setText(tamano.getText() + " " + Utilidades.calcularTamano(lista));

        TextView numeroArchivos = (TextView) findViewById(R.id.NumeroArchivosTexto);
        numeroArchivos.setText(numeroArchivos.getText() + " " + lista.length);
    }

    public void botonEnviar(View v) {
        if(!this.enviado) {
            this.enviado=true;
            new Thread() {
                @Override
                public void run() {
                    mensajeNegociacion = EmisorNegociacion.generarMensajeEmisorQ1(mensaje, archivos);
                    tranferencia = new EmisorTransferencia(mensajeNegociacion);
                    hiloTransferencia = new Thread(tranferencia);
                    hiloTransferencia.start();
                    hiloBarraNotificaciones();
                    hiloPantalla();
                }
            }.start();
        }

    }

    public void hiloBarraNotificaciones() {
        this.hiloNotificaciones= new Thread(new Runnable() {
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
        this.hiloNotificaciones.start();
    }

    private void notificarProgreso() {
        try {
            while (true) {
                String estado = NucleoNegociacion.recuperarMensaje(this.mensajeNegociacion.getIdentificadorMensaje()).getTipoMensaje();
                String titulo = "";
                String texto = "";

                if ((estado == MensajeNegociacionJSON.tipoMensajes[0])&&(!this.notificadaPropuesta)) {
                    titulo = "Enviando propuesta";
                    texto = "Pendiente de decisión";
                    mNotifyBuilder = new NotificationCompat.Builder(this)
                            .setContentTitle(titulo)
                            .setContentText(texto)
                            .setSmallIcon(R.drawable.icono128);
                    mNotificationManager.notify(
                            notifyID,
                            mNotifyBuilder.build());
                    this.notificadaPropuesta=true;
                }
                if (estado == MensajeNegociacionJSON.tipoMensajes[1]) {
                    titulo = "Envio aceptado";
                    texto = "El destinatario aceptó la propuesta";
                    mNotifyBuilder = new NotificationCompat.Builder(this)
                            .setContentTitle(titulo)
                            .setContentText(texto)
                            .setSmallIcon(R.drawable.icono128);
                    mNotificationManager.notify(
                            notifyID,
                            mNotifyBuilder.build());
                }
                if (estado == MensajeNegociacionJSON.tipoMensajes[2]) {
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
                if (estado == MensajeNegociacionJSON.tipoMensajes[3]) {
                    titulo = "Envío en progreso";
                    texto = "Envío en progreso";
                    mNotifyBuilder = new NotificationCompat.Builder(this)
                            .setContentTitle(titulo)
                            .setContentText(texto)
                            .setProgress((int) this.mensajeNegociacion.getTamano() * 1024, this.tranferencia.getProgreso(), false)
                            .setSmallIcon(R.drawable.icono128);
                    mNotificationManager.notify(
                            notifyID,
                            mNotifyBuilder.build());
                }
                if (estado == MensajeNegociacionJSON.tipoMensajes[4]) {
                    titulo = "Envío completo";
                    texto = "Transferencia completada";
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
                                    String estadoMensaje = NucleoNegociacion.recuperarMensaje(mensajeNegociacion.getIdentificadorMensaje()).getTipoMensaje();
                                    String titulo = "";
                                    String texto = "";
                                    if ((estadoMensaje == MensajeNegociacionJSON.tipoMensajes[0])&&(!notificadaPropuesta)) {
                                        estado.setText("Estado: Propuesta enviada");
                                    }
                                    if (estadoMensaje == MensajeNegociacionJSON.tipoMensajes[1]) {
                                        estado.setText("Estado: Propuesta aceptada");
                                    }
                                    if (estadoMensaje == MensajeNegociacionJSON.tipoMensajes[2]) {
                                        estado.setText("Estado: Propuesta denegada");
                                        salir=true;
                                    }
                                    if (estadoMensaje == MensajeNegociacionJSON.tipoMensajes[3]) {
                                        mProgress.setMax((int) mensajeNegociacion.getTamano() * 1024);
                                        mProgress.setProgress(tranferencia.getProgreso());
                                        estado.setText("Estado: Envío en progreso");
                                    }
                                    if (estadoMensaje == MensajeNegociacionJSON.tipoMensajes[4]) {
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


    public void botonCancelar(View v) {
        super.onBackPressed();
    }
}
