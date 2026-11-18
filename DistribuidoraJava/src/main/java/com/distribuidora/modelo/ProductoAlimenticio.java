package com.distribuidora.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public final class ProductoAlimenticio extends Producto {
    public static final int DESCUENTO_PORCENTAJE = 5;
    private final LocalDate fechaVencimiento;

    public ProductoAlimenticio(String codigo, String nombre, long precioBaseCentavos,
                               int stock, LocalDate fechaVencimiento) {
        super(codigo, nombre, precioBaseCentavos, stock);
        if (fechaVencimiento == null) {
            throw new IllegalArgumentException("La fecha de vencimiento es obligatoria.");
        }
        this.fechaVencimiento = fechaVencimiento;
    }

    @Override
    public String getTipo() { return "Alimenticio"; }

    @Override
    protected long calcularPrecioFinalCentavos() {
        BigDecimal precio = BigDecimal.valueOf(getPrecioBaseCentavos());
        return precio.multiply(BigDecimal.valueOf(100 - DESCUENTO_PORCENTAJE))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }

    public boolean estaVencido(LocalDate fechaActual) {
        return fechaVencimiento.isBefore(fechaActual);
    }

    @Override
    public String getDetalle() { return "Vence: " + fechaVencimiento; }
}
