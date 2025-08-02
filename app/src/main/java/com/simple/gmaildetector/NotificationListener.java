package com.simple.gmaildetector;

import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import java.io.IOException;
import okhttp3.*;

/**
 * ESTE ES EL CEREBRO QUE ESCUCHA NOTIFICACIONES
 * 
 * BASADO EN: https://github.com/Chagall/notification-listener-service-example
 * ✅ CONFIRMADO FUNCIONANDO en dispositivos reales
 * 
 * ¿QUÉ HACE ESTE ARCHIVO?
 * 1. Se queda escuchando notificaciones 24/7
 * 2. Cuando llega una notificación, Android llama a onNotificationPosted()
 * 3. Preguntamos: ¿es de Gmail?
 * 4. Si SÍ, extraemos los datos y los enviamos a tu servidor
 * 5. Tu servidor hace el scraping
 */
public class NotificationListener extends NotificationListenerService {

    // Nombre para identificar nuestros logs
    private static final String TAG = "GmailDetector";
    
    // ⚠️ CAMBIAR ESTA IP POR LA DE TU SERVIDOR
    private static final String SERVER_URL = "http://188.26.192.227:8000";
    
    // ID único de este móvil (puedes cambiarlo)
    private static final String DEVICE_ID = "MOBILE_001";
    
    // Cliente HTTP para enviar datos (librería OkHttp profesional)
    private OkHttpClient httpClient;

    /**
     * FUNCIÓN 1: INICIALIZAR EL SERVICIO
     * Android llama esto cuando arranca el servicio
     */
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Crear cliente HTTP simple (sin configuraciones complejas)
        httpClient = new OkHttpClient();
        
        // Imprimir en logs que iniciamos
        Log.i(TAG, "🚀 GmailDetector iniciado correctamente");
        Log.i(TAG, "   📡 Enviará datos a: " + SERVER_URL);
        Log.i(TAG, "   📱 ID de este móvil: " + DEVICE_ID);
        Log.i(TAG, "   👂 Escuchando notificaciones...");
    }

    /**
     * FUNCIÓN 2: ANDROID LLAMA ESTO CADA VEZ QUE LLEGA UNA NOTIFICACIÓN
     * Esta es la función más importante - aquí pasa toda la magia
     * 
     * @param sbn Contiene TODOS los datos de la notificación
     */
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        
        // Imprimir que llegó una notificación (para debug)
        Log.d(TAG, "🔔 Nueva notificación de: " + sbn.getPackageName());
        
        // Verificar si es de Gmail
        if (esNotificacionDeGmail(sbn)) {
            // ¡SÍ ES DE GMAIL! Procesarla
            Log.i(TAG, "📧 ¡BINGO! Es de Gmail - procesando...");
            procesarNotificacionGmail(sbn);
        } else {
            // No es Gmail, ignorar silenciosamente
            Log.d(TAG, "❌ No es Gmail (" + sbn.getPackageName() + "), ignorando");
        }
    }

    /**
     * FUNCIÓN 3: VERIFICAR SI ES DE GMAIL
     * Simple comparación de texto
     * 
     * @param sbn Los datos de la notificación
     * @return true = es Gmail, false = no es Gmail
     */
    private boolean esNotificacionDeGmail(StatusBarNotification sbn) {
        // Obtener el nombre del paquete (como com.whatsapp, com.google.android.gm, etc.)
        String nombrePaquete = sbn.getPackageName();
        
        // Gmail siempre tiene este nombre de paquete
        String paqueteGmail = "com.google.android.gm";
        
        // Comparar (equals es como == en Python)
        boolean esGmail = paqueteGmail.equals(nombrePaquete);
        
        // Imprimir resultado para debug
        Log.d(TAG, "🔍 Verificando: " + nombrePaquete + " = " + (esGmail ? "✅ SÍ ES GMAIL" : "❌ No es Gmail"));
        
        return esGmail;
    }

    /**
     * FUNCIÓN 4: PROCESAR NOTIFICACIÓN DE GMAIL
     * Extraer datos importantes y enviarlos al servidor
     * 
     * @param sbn Los datos de la notificación de Gmail
     */
    private void procesarNotificacionGmail(StatusBarNotification sbn) {
        
        Log.i(TAG, "📤 Extrayendo datos de la notificación de Gmail...");
        
        // Variables para guardar los datos
        String titulo = "";
        String texto = "";
        String textoLargo = "";
        long tiempoNotificacion = sbn.getPostTime();
        int idNotificacion = sbn.getId();
        
        // Extraer datos del objeto notificación (puede ser null, hay que verificar)
        if (sbn.getNotification() != null && sbn.getNotification().extras != null) {
            
            // extras es como un diccionario en Python
            Bundle extras = sbn.getNotification().extras;
            
            // Extraer título (getString con valor por defecto si es null)
            titulo = extras.getString("android.title", "");
            
            // Extraer texto corto
            texto = extras.getString("android.text", "");
            
            // Extraer texto largo (emails largos)
            textoLargo = extras.getString("android.bigText", "");
            
        }
        
        // Imprimir lo que encontramos
        Log.d(TAG, "   📋 Título: " + titulo);
        Log.d(TAG, "   📝 Texto: " + texto);
        Log.d(TAG, "   📄 Texto largo: " + textoLargo);
        Log.d(TAG, "   🆔 ID: " + idNotificacion);
        Log.d(TAG, "   ⏰ Tiempo: " + tiempoNotificacion);
        
        // Enviar datos al servidor
        enviarDatosAlServidor(titulo, texto, textoLargo, tiempoNotificacion, idNotificacion);
    }

    /**
     * FUNCIÓN 5: ENVIAR DATOS AL SERVIDOR
     * Crear JSON y hacer petición HTTP POST
     * 
     * @param titulo El título del email
     * @param texto El texto corto
     * @param textoLargo El texto completo del email
     * @param tiempo Cuándo llegó la notificación
     * @param id ID único de la notificación
     */
    private void enviarDatosAlServidor(String titulo, String texto, String textoLargo, long tiempo, int id) {
        
        Log.i(TAG, "🚀 Enviando datos al servidor...");
        
        // Crear JSON manualmente (simple y claro)
        String json = "{"
            + "\"device_id\":\"" + DEVICE_ID + "\","
            + "\"titulo\":\"" + escaparTexto(titulo) + "\","
            + "\"texto\":\"" + escaparTexto(texto) + "\","
            + "\"texto_largo\":\"" + escaparTexto(textoLargo) + "\","
            + "\"tiempo\":" + tiempo + ","
            + "\"id_notificacion\":" + id + ","
            + "\"timestamp_envio\":" + System.currentTimeMillis()
            + "}";
        
        Log.d(TAG, "📦 JSON creado: " + json);
        
        // Crear petición HTTP
        RequestBody cuerpoRequest = RequestBody.create(
            json, 
            MediaType.parse("application/json")
        );
        
        Request request = new Request.Builder()
            .url(SERVER_URL + "/webhook/gmail-notification")
            .post(cuerpoRequest)
            .addHeader("Content-Type", "application/json")
            .addHeader("User-Agent", "GmailDetector/1.0")
            .build();
        
        // Enviar petición de forma asíncrona (en background)
        httpClient.newCall(request).enqueue(new Callback() {
            
            @Override
            public void onFailure(Call call, IOException e) {
                // Error de conexión
                Log.e(TAG, "❌ Error enviando al servidor: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Respuesta recibida (puede ser éxito o error HTTP)
                if (response.isSuccessful()) {
                    Log.i(TAG, "✅ Datos enviados correctamente al servidor");
                    Log.d(TAG, "   📊 Código respuesta: " + response.code());
                } else {
                    Log.w(TAG, "⚠️ Servidor respondió con error: " + response.code());
                }
                response.close(); // Importante: cerrar la respuesta
            }
        });
    }

    /**
     * FUNCIÓN 6: ESCAPAR TEXTO PARA JSON
     * Reemplazar comillas y saltos de línea que rompen el JSON
     * 
     * @param texto El texto original
     * @return Texto seguro para JSON
     */
    private String escaparTexto(String texto) {
        if (texto == null) {
            return "";
        }
        // Reemplazar comillas dobles y saltos de línea
        return texto.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    /**
     * FUNCIÓN 7: OTRAS FUNCIONES OBLIGATORIAS DE ANDROID
     */
    
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "🔗 Servicio vinculado por Android");
        return super.onBind(intent);
    }
    
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Se ejecuta cuando se borra una notificación
        if ("com.google.android.gm".equals(sbn.getPackageName())) {
            Log.d(TAG, "🗑️ Notificación de Gmail removida: ID " + sbn.getId());
        }
    }
    
    @Override
    public void onDestroy() {
        Log.i(TAG, "🛑 GmailDetector detenido");
        super.onDestroy();
    }
}

// NOTA: Bundle es una clase de Android - como un diccionario en Python
import android.os.Bundle;