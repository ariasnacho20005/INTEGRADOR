package com.distribuidora.servicio;

import com.distribuidora.algoritmos.Algoritmos;
import com.distribuidora.modelo.Producto;
import com.distribuidora.modelo.ProductoAlimenticio;
import com.distribuidora.modelo.ProductoElectronico;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Random;

/** Ejecuta pruebas en memoria y cronometra solo las operaciones indicadas. */
public final class BenchmarkService {
    private static final int[] TAMANOS = {100, 1000, 10000};
    private static final int REPETICIONES = 5;
    private static final int CONSULTAS_POR_REPETICION = 1000;
    private static volatile long sumidero;

    public ArrayList<ResultadoBenchmark> ejecutarPruebas() {
        ArrayList<ResultadoBenchmark> resultados = new ArrayList<ResultadoBenchmark>();
        for (int i = 0; i < TAMANOS.length; i++) {
            resultados.add(ejecutarParaCantidad(TAMANOS[i]));
        }
        return resultados;
    }

    private ResultadoBenchmark ejecutarParaCantidad(int cantidad) {
        // Inicializa clases y caminos del benchmark antes de cronometrar las repeticiones.
        ArrayList<Producto> calentamiento = generarDatos(Math.min(cantidad, 500), 8675309L);
        prepararCalentamiento(calentamiento);

        ArrayList<Producto> original = null;
        long tiempoGeneracion = 0;
        for (int repeticion = 0; repeticion < REPETICIONES; repeticion++) {
            long inicio = System.nanoTime();
            ArrayList<Producto> generado = generarDatos(cantidad, 20261117L + repeticion);
            tiempoGeneracion += System.nanoTime() - inicio;
            if (repeticion == 0) {
                original = generado;
            }
        }

        long tiempoCopiaIndice = 0;
        long tiempoOrdenCodigo = 0;
        ArrayList<Producto> indiceCodigo = null;
        for (int repeticion = 0; repeticion < REPETICIONES; repeticion++) {
            long inicioCopia = System.nanoTime();
            ArrayList<Producto> copia = new ArrayList<Producto>(original);
            tiempoCopiaIndice += System.nanoTime() - inicioCopia;

            long inicioOrden = System.nanoTime();
            // Merge sort manual por código; su costo se informa aparte del tiempo de búsqueda.
            Algoritmos.mergeSortPorCodigoEnSitio(copia);
            tiempoOrdenCodigo += System.nanoTime() - inicioOrden;
            if (repeticion == REPETICIONES - 1) {
                indiceCodigo = copia;
            }
        }

        String existente = original.get(cantidad / 2).getCodigo();
        String ausente = String.format("P-%08d", cantidad);
        calentarBusquedas(original, indiceCodigo, existente, ausente);
        if (Algoritmos.busquedaLineal(original, existente) == null
                || Algoritmos.busquedaBinaria(indiceCodigo, existente) == null
                || Algoritmos.busquedaLineal(original, ausente) != null
                || Algoritmos.busquedaBinaria(indiceCodigo, ausente) != null) {
            throw new IllegalStateException("La búsqueda no encontró los resultados esperados.");
        }
        double linealExiste = medirBusqueda(original, existente, false);
        double binariaExiste = medirBusqueda(indiceCodigo, existente, true);
        double linealAusente = medirBusqueda(original, ausente, false);
        double binariaAusente = medirBusqueda(indiceCodigo, ausente, true);

        long tiempoCopiaSeleccion = 0;
        long tiempoCopiaMerge = 0;
        long tiempoSeleccion = 0;
        long tiempoMerge = 0;
        ArrayList<Producto> ordenSeleccion = null;
        ArrayList<Producto> ordenMerge = null;

        for (int repeticion = 0; repeticion < REPETICIONES; repeticion++) {
            long inicioCopiaSeleccion = System.nanoTime();
            ArrayList<Producto> copiaSeleccion = new ArrayList<Producto>(original);
            tiempoCopiaSeleccion += System.nanoTime() - inicioCopiaSeleccion;
            long inicioSeleccion = System.nanoTime();
            Algoritmos.ordenarSeleccionPorPrecio(copiaSeleccion);
            tiempoSeleccion += System.nanoTime() - inicioSeleccion;
            if (repeticion == REPETICIONES - 1) {
                ordenSeleccion = copiaSeleccion;
            }

            long inicioCopiaMerge = System.nanoTime();
            ArrayList<Producto> copiaMerge = new ArrayList<Producto>(original);
            tiempoCopiaMerge += System.nanoTime() - inicioCopiaMerge;
            long inicioMerge = System.nanoTime();
            Algoritmos.mergeSortPorPrecioEnSitio(copiaMerge);
            tiempoMerge += System.nanoTime() - inicioMerge;
            if (repeticion == REPETICIONES - 1) {
                ordenMerge = copiaMerge;
            }
        }

        verificarMismoOrden(ordenSeleccion, ordenMerge);
        return new ResultadoBenchmark(
                cantidad,
                tiempoGeneracion / (double) REPETICIONES,
                tiempoCopiaIndice / (double) REPETICIONES,
                tiempoOrdenCodigo / (double) REPETICIONES,
                tiempoCopiaSeleccion / (double) REPETICIONES,
                tiempoCopiaMerge / (double) REPETICIONES,
                linealExiste,
                binariaExiste,
                linealAusente,
                binariaAusente,
                tiempoSeleccion / (double) REPETICIONES,
                tiempoMerge / (double) REPETICIONES
        );
    }

    private ArrayList<Producto> generarDatos(int cantidad, long semilla) {
        Random random = new Random(semilla + cantidad);
        ArrayList<Producto> datos = new ArrayList<Producto>(cantidad);
        for (int i = 0; i < cantidad; i++) {
            String codigo = String.format("P-%08d", i);
            long precio = 10000L + random.nextInt(5_000_000);
            int stock = 1 + random.nextInt(100);
            if (i % 2 == 0) {
                datos.add(new ProductoAlimenticio(codigo, "Alimento de prueba " + i,
                        precio, stock, LocalDate.now().plusDays(30 + (i % 300))));
            } else {
                int garantia = 6 * (i % 13);
                datos.add(new ProductoElectronico(codigo, "Electrónico de prueba " + i,
                        precio, stock, garantia));
            }
        }
        return datos;
    }

    private void prepararCalentamiento(ArrayList<Producto> datos) {
        for (int i = 0; i < datos.size(); i++) {
            datos.get(i).getPrecioFinalCentavos();
        }
        for (int repeticion = 0; repeticion < 120; repeticion++) {
            ArrayList<Producto> porCodigo = new ArrayList<Producto>(datos);
            Algoritmos.mergeSortPorCodigoEnSitio(porCodigo);
            ArrayList<Producto> porSeleccion = new ArrayList<Producto>(datos);
            Algoritmos.ordenarSeleccionPorPrecio(porSeleccion);
            ArrayList<Producto> porMerge = new ArrayList<Producto>(datos);
            Algoritmos.mergeSortPorPrecioEnSitio(porMerge);
            verificarMismoOrden(porSeleccion, porMerge);
        }
    }

    private void calentarBusquedas(ArrayList<Producto> original, ArrayList<Producto> indice,
                                   String existente, String ausente) {
        for (int i = 0; i < 1500; i++) {
            sumidero += Algoritmos.busquedaLineal(original, existente).getPrecioFinalCentavos();
            sumidero += Algoritmos.busquedaLineal(original, ausente) == null ? 1 : 0;
            sumidero += Algoritmos.busquedaBinaria(indice, existente).getPrecioFinalCentavos();
            sumidero += Algoritmos.busquedaBinaria(indice, ausente) == null ? 1 : 0;
        }
    }

    private double medirBusqueda(ArrayList<Producto> datos, String codigo, boolean binaria) {
        long totalNs = 0;
        long verificacion = 0;
        for (int repeticion = 0; repeticion < REPETICIONES; repeticion++) {
            long inicio = System.nanoTime();
            for (int consulta = 0; consulta < CONSULTAS_POR_REPETICION; consulta++) {
                Producto encontrado = binaria
                        ? Algoritmos.busquedaBinaria(datos, codigo)
                        : Algoritmos.busquedaLineal(datos, codigo);
                verificacion += encontrado == null ? 1 : encontrado.getPrecioFinalCentavos();
            }
            totalNs += System.nanoTime() - inicio;
        }
        sumidero = verificacion;
        return totalNs / (double) (REPETICIONES * CONSULTAS_POR_REPETICION);
    }

    private void verificarMismoOrden(ArrayList<Producto> a, ArrayList<Producto> b) {
        if (a.size() != b.size()) {
            throw new IllegalStateException("Los ordenamientos devolvieron distinta cantidad de productos.");
        }
        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).getCodigo().equals(b.get(i).getCodigo())) {
                throw new IllegalStateException("Los algoritmos de ordenamiento produjeron órdenes diferentes.");
            }
        }
    }
}
