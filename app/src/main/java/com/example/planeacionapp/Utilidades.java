package com.example.planeacionapp;

import android.content.DialogInterface;
import android.view.View;

import com.example.planeacionapp.Constantes;

import androidx.appcompat.app.AlertDialog;

/**
 * Clase Utilidades
 *
 * Clase que contiene métodos generales para la aplicación
 * @author Jaime Buelvas
 * @version: 1.0
 */

/**
 * Created by jramirez on 19/06/2015.
 */
public class Utilidades {

    /**
     * Visualiza un mensaje de advertencia debido a un error de la transacción
     * o a una validación de algún dato.
     *
     * @param mensaje Mensaje a mostrar
     * @param v       View
     */
    public void mostrarDialogoAdvertenciaMsg(String mensaje, View v) {
        new AlertDialog.Builder(v.getContext())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Advertencia")
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //finish();
                    }
                })
                /*.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Toast.makeText(getApplicationContext(),"Nothing Happens",Toast.LENGTH_LONG).show();
                    }
                })*/
                .show();
    }

    /**
     * Visualiza un mensaje de información de la transacción.
     *
     * @param mensaje Mensaje a mostrar
     * @param v       View
     */
    public void mostrarDialogoInfoMsg(String mensaje, View v) {
        new AlertDialog.Builder(v.getContext())
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("Información")
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .show();
    }
}
