package com.distribuidora.modelo;

import java.util.ArrayList;
import java.util.Locale;

public final class Cliente {
    private final String identificacion;
    private final String nombre;
    private final String contacto;
    private final ArrayList<Pedido> pedidos = new ArrayList<Pedido>();

    public Cliente(String identificacion, String nombre, String contacto) {
        if (identificacion == null || identificacion.trim().isEmpty()) {
            throw new IllegalArgumentException("La identificación es obligatoria.");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (contacto == null || contacto.trim().isEmpty()) {
            throw new IllegalArgumentException("El contacto es obligatorio.");
        }
        this.identificacion = identificacion.trim().toUpperCase(Locale.ROOT);
        this.nombre = nombre.trim();
        this.contacto = contacto.trim();
    }

    public String getIdentificacion() { return identificacion; }
    public String getNombre() { return nombre; }
    public String getContacto() { return contacto; }

    public void agregarPedido(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("El pedido no puede ser nulo.");
        }
        pedidos.add(pedido);
    }

    public ArrayList<Pedido> getPedidos() {
        return new ArrayList<Pedido>(pedidos);
    }
}
