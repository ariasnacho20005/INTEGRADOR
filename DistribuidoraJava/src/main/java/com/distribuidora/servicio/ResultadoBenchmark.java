package com.distribuidora.servicio;

public final class ResultadoBenchmark {
    private final int cantidad;
    private final double generarDatosNs;
    private final double copiarIndiceNs;
    private final double ordenarIndiceCodigoNs;
    private final double copiarSeleccionNs;
    private final double copiarMergeNs;
    private final double linealExisteNs;
    private final double binariaExisteNs;
    private final double linealAusenteNs;
    private final double binariaAusenteNs;
    private final double seleccionNs;
    private final double mergeSortNs;

    public ResultadoBenchmark(int cantidad, double generarDatosNs, double copiarIndiceNs,
            double ordenarIndiceCodigoNs, double copiarSeleccionNs, double copiarMergeNs,
            double linealExisteNs, double binariaExisteNs, double linealAusenteNs,
            double binariaAusenteNs, double seleccionNs, double mergeSortNs) {
        this.cantidad = cantidad;
        this.generarDatosNs = generarDatosNs;
        this.copiarIndiceNs = copiarIndiceNs;
        this.ordenarIndiceCodigoNs = ordenarIndiceCodigoNs;
        this.copiarSeleccionNs = copiarSeleccionNs;
        this.copiarMergeNs = copiarMergeNs;
        this.linealExisteNs = linealExisteNs;
        this.binariaExisteNs = binariaExisteNs;
        this.linealAusenteNs = linealAusenteNs;
        this.binariaAusenteNs = binariaAusenteNs;
        this.seleccionNs = seleccionNs;
        this.mergeSortNs = mergeSortNs;
    }

    public int getCantidad() { return cantidad; }
    public double getGenerarDatosNs() { return generarDatosNs; }
    public double getCopiarIndiceNs() { return copiarIndiceNs; }
    public double getOrdenarIndiceCodigoNs() { return ordenarIndiceCodigoNs; }
    public double getCopiarSeleccionNs() { return copiarSeleccionNs; }
    public double getCopiarMergeNs() { return copiarMergeNs; }
    public double getLinealExisteNs() { return linealExisteNs; }
    public double getBinariaExisteNs() { return binariaExisteNs; }
    public double getLinealAusenteNs() { return linealAusenteNs; }
    public double getBinariaAusenteNs() { return binariaAusenteNs; }
    public double getSeleccionNs() { return seleccionNs; }
    public double getMergeSortNs() { return mergeSortNs; }
}
