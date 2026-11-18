package com.distribuidora.algoritmos;

import com.distribuidora.modelo.Producto;
import java.util.ArrayList;

/** Búsquedas y ordenamientos implementados con ciclos y recursión propios. */
public final class Algoritmos {
    private Algoritmos() { }

    public static Producto busquedaLineal(ArrayList<Producto> productos, String codigo) {
        for (int i = 0; i < productos.size(); i++) {
            Producto actual = productos.get(i);
            if (actual.getCodigo().equalsIgnoreCase(codigo)) {
                return actual;
            }
        }
        return null;
    }

    /** Requiere que la colección ya esté ordenada por código ascendente. */
    public static Producto busquedaBinaria(ArrayList<Producto> ordenadosPorCodigo, String codigo) {
        int izquierda = 0;
        int derecha = ordenadosPorCodigo.size() - 1;
        while (izquierda <= derecha) {
            int medio = izquierda + (derecha - izquierda) / 2;
            Producto actual = ordenadosPorCodigo.get(medio);
            int comparacion = actual.getCodigo().compareToIgnoreCase(codigo);
            if (comparacion == 0) {
                return actual;
            }
            if (comparacion < 0) {
                izquierda = medio + 1;
            } else {
                derecha = medio - 1;
            }
        }
        return null;
    }

    /** Selection sort: precio final descendente y, en empate, código ascendente. */
    public static void ordenarSeleccionPorPrecio(ArrayList<Producto> productos) {
        for (int inicio = 0; inicio < productos.size() - 1; inicio++) {
            int mejor = inicio;
            for (int candidato = inicio + 1; candidato < productos.size(); candidato++) {
                if (compararPrecio(productos.get(candidato), productos.get(mejor)) < 0) {
                    mejor = candidato;
                }
            }
            if (mejor != inicio) {
                Producto temporal = productos.get(inicio);
                productos.set(inicio, productos.get(mejor));
                productos.set(mejor, temporal);
            }
        }
    }

    public static ArrayList<Producto> mergeSortPorPrecio(ArrayList<Producto> productos) {
        ArrayList<Producto> copia = new ArrayList<Producto>(productos);
        mergeSortPorPrecioEnSitio(copia);
        return copia;
    }

    public static void mergeSortPorPrecioEnSitio(ArrayList<Producto> productos) {
        Producto[] auxiliar = new Producto[productos.size()];
        mergePrecio(productos, auxiliar, 0, productos.size() - 1);
    }

    /** Prepara el arreglo usado como índice de la búsqueda binaria. */
    public static ArrayList<Producto> prepararIndicePorCodigo(ArrayList<Producto> productos) {
        ArrayList<Producto> copia = new ArrayList<Producto>(productos);
        mergeSortPorCodigoEnSitio(copia);
        return copia;
    }

    public static void mergeSortPorCodigoEnSitio(ArrayList<Producto> productos) {
        Producto[] auxiliar = new Producto[productos.size()];
        mergeCodigo(productos, auxiliar, 0, productos.size() - 1);
    }

    public static int compararPrecio(Producto a, Producto b) {
        int porPrecio = Long.compare(b.getPrecioFinalCentavos(), a.getPrecioFinalCentavos());
        if (porPrecio != 0) {
            return porPrecio;
        }
        return a.getCodigo().compareToIgnoreCase(b.getCodigo());
    }

    private static void mergePrecio(ArrayList<Producto> lista, Producto[] auxiliar,
                                    int izquierda, int derecha) {
        if (izquierda >= derecha) {
            return;
        }
        int medio = izquierda + (derecha - izquierda) / 2;
        mergePrecio(lista, auxiliar, izquierda, medio);
        mergePrecio(lista, auxiliar, medio + 1, derecha);
        int i = izquierda;
        int j = medio + 1;
        int k = izquierda;
        while (i <= medio && j <= derecha) {
            if (compararPrecio(lista.get(i), lista.get(j)) <= 0) {
                auxiliar[k++] = lista.get(i++);
            } else {
                auxiliar[k++] = lista.get(j++);
            }
        }
        while (i <= medio) { auxiliar[k++] = lista.get(i++); }
        while (j <= derecha) { auxiliar[k++] = lista.get(j++); }
        for (int posicion = izquierda; posicion <= derecha; posicion++) {
            lista.set(posicion, auxiliar[posicion]);
        }
    }

    private static void mergeCodigo(ArrayList<Producto> lista, Producto[] auxiliar,
                                    int izquierda, int derecha) {
        if (izquierda >= derecha) {
            return;
        }
        int medio = izquierda + (derecha - izquierda) / 2;
        mergeCodigo(lista, auxiliar, izquierda, medio);
        mergeCodigo(lista, auxiliar, medio + 1, derecha);
        int i = izquierda;
        int j = medio + 1;
        int k = izquierda;
        while (i <= medio && j <= derecha) {
            if (lista.get(i).getCodigo().compareToIgnoreCase(lista.get(j).getCodigo()) <= 0) {
                auxiliar[k++] = lista.get(i++);
            } else {
                auxiliar[k++] = lista.get(j++);
            }
        }
        while (i <= medio) { auxiliar[k++] = lista.get(i++); }
        while (j <= derecha) { auxiliar[k++] = lista.get(j++); }
        for (int posicion = izquierda; posicion <= derecha; posicion++) {
            lista.set(posicion, auxiliar[posicion]);
        }
    }
}
