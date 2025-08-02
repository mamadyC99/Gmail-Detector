package com.simple.gmaildetector;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * ESTA ES LA PANTALLA QUE VE EL USUARIO
 * 
 * BASADO EN: https://github.com/Chagall/notification-listener-service-example
 * 
 * ¿QUÉ HACE ESTA PANTALLA?
 * 1. Verificar si los permisos están activados
 * 2. Mostrar instrucciones claras al usuario
 * 3. Abrir la configuración de Android para activar permisos
 * 4. Mostrar el estado del servicio (funcionando o no)
 */
public class MainActivity extends Activity {

    // Nombre para logs
    private static final String TAG = "MainActivity";
    
    // Constantes de Android (copiadas de la documentación oficial)
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    
    // Referencias a elementos de la pantalla
    private TextView textoEstado;
    private Button botonConfigurar;

    /**
     * FUNCIÓN QUE SE EJECUTA CUANDO SE ABRE LA APP
     * Es como __init__ en Python
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Decirle a Android qué diseño usar (definido en activity_main.xml)
        setContentView(R.layout.activity_main);
        
        Log.i(TAG, "🚀 Pantalla principal abierta");
        
        // Conectar elementos de la pantalla con variables
        inicializarElementos();
        
        // Configurar qué pasa cuando se tocan los botones
        configurarBotones();
        
        // Verificar estado actual
        actualizarEstado();
    }
    
    /**
     * SE EJECUTA CADA VEZ QUE VOLVEMOS A LA APP
     * Por ejemplo, después de configurar permisos
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "📱 Usuario volvió a la app - verificando estado...");
        actualizarEstado();
    }

    /**
     * FUNCIÓN: CONECTAR ELEMENTOS DE LA PANTALLA
     * findViewById es como document.getElementById en JavaScript
     */
    private void inicializarElementos() {
        Log.d(TAG, "🔗 Conectando elementos de la pantalla...");
        
        // Encontrar el texto donde mostraremos el estado
        textoEstado = findViewById(R.id.texto_estado);
        
        // Encontrar el botón de configuración
        botonConfigurar = findViewById(R.id.boton_configurar);
        
        Log.d(TAG, "✅ Elementos conectados correctamente");
    }

    /**
     * FUNCIÓN: CONFIGURAR QUÉ PASA CUANDO SE TOCAN BOTONES
     */
    private void configurarBotones() {
        
        // Cuando el usuario toca el botón de configurar
        botonConfigurar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "👆 Usuario tocó botón de configuración");
                abrirConfiguracionNotificaciones();
            }
        });
    }

    /**
     * FUNCIÓN: VERIFICAR SI TENEMOS PERMISOS Y ACTUALIZAR PANTALLA
     */
    private void actualizarEstado() {
        Log.d(TAG, "🔍 Verificando estado de permisos...");
        
        // Preguntar a Android si tenemos permisos
        boolean tenemosPermisos = verificarPermisosNotificaciones();
        
        if (tenemosPermisos) {
            mostrarEstadoActivo();
        } else {
            mostrarEstadoInactivo();
        }
    }

    /**
     * FUNCIÓN: VERIFICAR SI TENEMOS PERMISOS DE NOTIFICACIONES
     * 
     * CÓMO FUNCIONA:
     * 1. Android guarda una lista de apps con permisos en una configuración
     * 2. Leemos esa configuración
     * 3. Buscamos si nuestra app está en la lista
     * 
     * @return true = tenemos permisos, false = no tenemos permisos
     */
    private boolean verificarPermisosNotificaciones() {
        
        // Obtener nombre de nuestro paquete (como com.simple.gmaildetector)
        String nuestroPaquete = getPackageName();
        Log.d(TAG, "📦 Nuestro paquete: " + nuestroPaquete);
        
        // Leer configuración de Android con la lista de apps autorizadas
        String listaAppsAutorizadas = Settings.Secure.getString(
            getContentResolver(), 
            ENABLED_NOTIFICATION_LISTENERS
        );
        
        Log.d(TAG, "📋 Apps con permisos: " + listaAppsAutorizadas);
        
        // Si la lista está vacía, no tenemos permisos
        if (TextUtils.isEmpty(listaAppsAutorizadas)) {
            Log.d(TAG, "❌ Lista vacía - no tenemos permisos");
            return false;
        }
        
        // La lista es como "com.app1:com.app2:com.app3"
        // Separar por ":" y buscar nuestro paquete
        String[] nombresApps = listaAppsAutorizadas.split(":");
        
        for (String nombreApp : nombresApps) {
            // Cada nombre puede ser como "com.simple.gmaildetector/com.simple.gmaildetector.NotificationListener"
            ComponentName componente = ComponentName.unflattenFromString(nombreApp);
            
            if (componente != null) {
                String paqueteEncontrado = componente.getPackageName();
                Log.d(TAG, "🔍 Comparando: " + paqueteEncontrado + " con " + nuestroPaquete);
                
                if (nuestroPaquete.equals(paqueteEncontrado)) {
                    Log.i(TAG, "✅ ¡Encontrado! Tenemos permisos");
                    return true;
                }
            }
        }
        
        Log.d(TAG, "❌ No encontrado en la lista - no tenemos permisos");
        return false;
    }

    /**
     * FUNCIÓN: MOSTRAR QUE EL SERVICIO ESTÁ ACTIVO
     */
    private void mostrarEstadoActivo() {
        Log.i(TAG, "✅ Mostrando estado ACTIVO");
        
        String mensaje = "✅ GMAIL DETECTOR ACTIVO\n\n"
            + "📧 Escuchando notificaciones de Gmail\n"
            + "🌐 Enviando datos a: http://188.26.192.227:8000\n"
            + "📱 ID dispositivo: MOBILE_001\n\n"
            + "🎯 Todo funcionando correctamente.\n"
            + "Cuando llegue un email a Gmail,\n"
            + "se enviará automáticamente al servidor.\n\n"
            + "💡 Para ver actividad: adb logcat | grep GmailDetector";
        
        textoEstado.setText(mensaje);
        textoEstado.setTextColor(0xFF4CAF50); // Verde
        botonConfigurar.setText("🔧 Reconfigurar");
    }

    /**
     * FUNCIÓN: MOSTRAR QUE NECESITAMOS CONFIGURAR PERMISOS
     */
    private void mostrarEstadoInactivo() {
        Log.w(TAG, "❌ Mostrando estado INACTIVO");
        
        String mensaje = "❌ CONFIGURACIÓN REQUERIDA\n\n"
            + "Para que Gmail Detector funcione:\n\n"
            + "1️⃣ Toca el botón de abajo\n"
            + "2️⃣ Busca 'Gmail Detector' en la lista\n"
            + "3️⃣ Activa el interruptor\n"
            + "4️⃣ Vuelve aquí\n\n"
            + "⚠️ Sin estos permisos, la app\n"
            + "no puede leer notificaciones de Gmail.\n\n"
            + "🔒 Es seguro - solo lee Gmail,\n"
            + "no puede leer otros datos.";
        
        textoEstado.setText(mensaje);
        textoEstado.setTextColor(0xFFFF5722); // Rojo
        botonConfigurar.setText("🔧 Configurar Permisos");
    }

    /**
     * FUNCIÓN: ABRIR CONFIGURACIÓN DE ANDROID PARA ACTIVAR PERMISOS
     */
    private void abrirConfiguracionNotificaciones() {
        try {
            Log.i(TAG, "🔧 Abriendo configuración de acceso a notificaciones...");
            
            // Crear Intent para abrir configuración específica
            Intent intent = new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
            
            Log.d(TAG, "✅ Configuración abierta");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error abriendo configuración: " + e.getMessage());
            
            // Plan B: abrir configuración general
            try {
                Intent intentGeneral = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intentGeneral);
                Log.d(TAG, "🔄 Configuración general abierta como respaldo");
            } catch (Exception e2) {
                Log.e(TAG, "💥 Error total: " + e2.getMessage());
            }
        }
    }
}