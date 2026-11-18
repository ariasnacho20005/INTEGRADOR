package com.distribuidora.modelo;

import java.time.LocalDateTime;

public final class Pedido {
    private final long codigo;
    private final Cliente cliente;
    private final Producto producto;
    private final int cantidad;
    private final LocalDateTime fecha;
    private final long importeTotalCentavos;

    public Pedido(long codigo, Cliente cliente, Producto producto, int cantidad,
                  LocalDateTime fecha, long importeTotalCentavos) {
        if (cliente == null || producto == null || fecha == null) {
            throw new IllegalArgumentException("El pedido requiere cliente, producto y fecha.");
        }
        if (cantidad <= 0 || importeTotalCentavos <= 0) {
            throw new IllegalArgumentException("La cantidad y el importe deben ser mayores que cero.");
        }
        this.codigo = codigo;
        this.cliente = cliente;
        this.producto = producto;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.importeTotalCentavos = importeTotalCentavos;
    }

    public long getCodigo() { return codigo; }
    public Cliente getCliente() { return cliente; }
    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public LocalDateTime getFecha() { return fecha; }
    public long getImporteTotalCentavos() { return importeTotalCentavos; }
}
