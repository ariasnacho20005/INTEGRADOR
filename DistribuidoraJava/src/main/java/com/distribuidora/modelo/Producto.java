package com.distribuidora.modelo;

import java.util.ArrayList;
import java.util.Locale;

/** Datos y comportamientos comunes a todos los productos. */
public abstract class Producto {
    private final String codigo;
    private final String nombre;
    private final long precioBaseCentavos;
    private int stock;
    private final ArrayList<Pedido> pedidos = new ArrayList<Pedido>();
    private Long precioFinalCache;

    protected Producto(String codigo, String nombre, long precioBaseCentavos, int stock) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código es obligatorio.");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (precioBaseCentavos <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        this.codigo = codigo.trim().toUpperCase(Locale.ROOT);
        this.nombre = nombre.trim();
        this.precioBaseCentavos = precioBaseCentavos;
        this.stock = stock;
    }

    public abstract String getTipo();

    /** Cada subclase aporta su propia regla de precio final. */
    protected abstract long calcularPrecioFinalCentavos();

    public final long getPrecioFinalCentavos() {
        if (precioFinalCache == null) {
            precioFinalCache = calcularPrecioFinalCentavos();
        }
        return precioFinalCache.longValue();
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public long getPrecioBaseCentavos() { return precioBaseCentavos; }
    public int getStock() { return stock; }

    public void reducirStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
        if (cantidad > stock) {
            throw new IllegalArgumentException("No hay stock suficiente.");
        }
        stock -= cantidad;
    }

    public void agregarPedido(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("El pedido no puede ser nulo.");
        }
        pedidos.add(pedido);
    }

    public ArrayList<Pedido> getPedidos() {
        return new ArrayList<Pedido>(pedidos);
    }

    public abstract String getDetalle();
}
