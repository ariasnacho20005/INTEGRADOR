package com.distribuidora.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ProductoElectronico extends Producto {
    public static final int RECARGO_POR_CADA_SEIS_MESES_PORCENTAJE = 1;
    private final int mesesGarantia;

    public ProductoElectronico(String codigo, String nombre, long precioBaseCentavos,
                               int stock, int mesesGarantia) {
        super(codigo, nombre, precioBaseCentavos, stock);
        if (mesesGarantia < 0 || mesesGarantia > 360) {
            throw new IllegalArgumentException("La garantía debe estar entre 0 y 360 meses.");
        }
        this.mesesGarantia = mesesGarantia;
        calcularPrecioFinalCentavos(); // Detecta aquí un posible desbordamiento, antes de registrar el producto.
    }

    @Override
    public String getTipo() { return "Electrónico"; }

    @Override
    protected long calcularPrecioFinalCentavos() {
        BigDecimal recargo = BigDecimal.valueOf(getPrecioBaseCentavos())
                .multiply(BigDecimal.valueOf(mesesGarantia))
                .multiply(BigDecimal.valueOf(RECARGO_POR_CADA_SEIS_MESES_PORCENTAJE))
                .divide(BigDecimal.valueOf(600), 0, RoundingMode.HALF_UP);
        return Math.addExact(getPrecioBaseCentavos(), recargo.longValueExact());
    }

    public int getMesesGarantia() { return mesesGarantia; }

    @Override
    public String getDetalle() { return "Garantía: " + mesesGarantia + " meses"; }
}
