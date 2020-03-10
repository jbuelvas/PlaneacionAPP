package com.example.planeacionapp;

import android.view.View;

import com.example.planeacionapp.Constantes;

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
        QustomDialogBuilder qustomDialogBuilder = new QustomDialogBuilder(v.getContext()).
                setTitle("Advertencia").
                setTitleBold(true).
                setTitleColor(Constantes.COLOR_TITULO_ALERTAS).
                setDividerColor(Constantes.COLOR_TITULO_ALERTAS).
                setMessage(mensaje).
                //setIcon(v.getContext().getResources().getDrawable(R.drawable.error96)).
                setPositiveButton(true);

        qustomDialogBuilder.show();
    }
}
