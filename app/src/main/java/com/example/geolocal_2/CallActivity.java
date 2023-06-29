package com.example.geolocal_2;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class CallActivity extends AppCompatActivity {
    private static final int REQUEST_CALL_PERMISSION = 1;
    private static final int REQUEST_OVERLAY_PERMISSION = 2;
    private WindowManager windowManager;
    private View floatView;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName adminComponentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComponentName = new ComponentName(this, MyDeviceAdmin.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // Verificar si se tienen los permisos de ventana flotante
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
        } else {
            // Verificar si se tienen los permisos para realizar llamadas
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Solicitar permiso de llamada telefónica
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE},
                        REQUEST_CALL_PERMISSION);
            } else {
                // Mostrar ventana flotante negra
                showFloatingWindow();
                // Realizar la llamada y apagar la pantalla
                makeCallAndLockScreen();
            }
        }
    }

    private void showFloatingWindow() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = LayoutInflater.from(this);
        floatView = inflater.inflate(R.layout.floating_layout, null);
        windowManager.addView(floatView, params);
    }

    private void makeCallAndLockScreen() {
        String phoneNumber = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("phone_number", null);

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));

            try {
                startActivity(callIntent);
                Toast.makeText(this, "Llamada realizada correctamente", Toast.LENGTH_SHORT).show();
                lockScreen(); // Apagar la pantalla después de realizar la llamada
            } catch (Exception e) {
                Toast.makeText(this, "Error al realizar la llamada", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Ingrese un número de teléfono", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void lockScreen() {
        boolean isAdminActive = MyDeviceAdmin.isAdminActive(this);

        if (isAdminActive) {
            // Apagar la pantalla
            devicePolicyManager.lockNow();
        } else {
            // Si la aplicación no tiene permisos de administrador de dispositivo, solicitarlos al usuario
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Se requieren permisos de administrador para apagar la pantalla");
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso de llamada telefónica otorgado
                // Mostrar ventana flotante negra
                showFloatingWindow();
                // Realizar la llamada y apagar la pantalla
                makeCallAndLockScreen();
            } else {
                Toast.makeText(this, "No se tienen los permisos necesarios para realizar una llamada",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                // Permiso de ventana flotante otorgado
                // Verificar si se tienen los permisos para realizar llamadas
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Solicitar permiso de llamada telefónica
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},
                            REQUEST_CALL_PERMISSION);
                } else {
                    // Mostrar ventana flotante negra
                    showFloatingWindow();
                    // Realizar la llamada y apagar la pantalla
                    makeCallAndLockScreen();
                }
            } else {
                Toast.makeText(this, "No se tienen los permisos necesarios para mostrar una ventana flotante",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Eliminar la ventana flotante al salir de la actividad
        if (windowManager != null && floatView != null) {
            windowManager.removeView(floatView);
        }
    }
}