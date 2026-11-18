package com.distribuidora.servicio;

import com.distribuidora.algoritmos.Algoritmos;
import com.distribuidora.modelo.Cliente;
import com.distribuidora.modelo.Pedido;
import com.distribuidora.modelo.Producto;
import com.distribuidora.modelo.ProductoAlimenticio;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

/** Administra las colecciones del negocio y aplica las reglas de venta. */
public final class Distribuidora {
    private final ArrayList<Producto> productos = new ArrayList<Producto>();
    private final ArrayList<Cliente> clientes = new ArrayList<Cliente>();
    private final ArrayList<Pedido> pedidos = new ArrayList<Pedido>();
    private long siguienteCodigoPedido = 1;

    public void registrarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo.");
        }
        if (buscarProducto(producto.getCodigo()) != null) {
            throw new IllegalArgumentException("Ya existe un producto con ese código.");
        }
        productos.add(producto);
    }

    public void registrarCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo.");
        }
        if (buscarCliente(cliente.getIdentificacion()) != null) {
            throw new IllegalArgumentException("Ya existe un cliente con esa identificación.");
        }
        clientes.add(cliente);
    }

    public Producto buscarProducto(String codigo) {
        return Algoritmos.busquedaLineal(productos, codigo);
    }

    public ArrayList<Producto> ordenarProductosPorPrecio() {
        return Algoritmos.mergeSortPorPrecio(productos);
    }

    public Pedido confirmarPedido(String identificacionCliente, String codigoProducto, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad solicitada debe ser mayor que cero.");
        }
        Cliente cliente = buscarCliente(identificacionCliente);
        if (cliente == null) {
            throw new IllegalArgumentException("No existe un cliente con esa identificación.");
        }
        Producto producto = buscarProducto(codigoProducto);
        if (producto == null) {
            throw new IllegalArgumentException("No existe un producto con ese código.");
        }
        if (producto instanceof ProductoAlimenticio
                && ((ProductoAlimenticio) producto).estaVencido(LocalDate.now())) {
            throw new IllegalArgumentException("No se puede vender un producto alimenticio vencido.");
        }
        if (cantidad > producto.getStock()) {
            throw new IllegalArgumentException("No hay stock suficiente para completar el pedido.");
        }
        long importe = Math.multiplyExact(producto.getPrecioFinalCentavos(), (long) cantidad);
        Pedido pedido = new Pedido(siguienteCodigoPedido, cliente, producto, cantidad,
                LocalDateTime.now(), importe);

        // Todas las validaciones ocurren antes de modificar las existencias.
        producto.reducirStock(cantidad);
        pedidos.add(pedido);
        cliente.agregarPedido(pedido);
        producto.agregarPedido(pedido);
        siguienteCodigoPedido++;
        return pedido;
    }

    private Cliente buscarCliente(String identificacion) {
        for (int i = 0; i < clientes.size(); i++) {
            Cliente cliente = clientes.get(i);
            if (cliente.getIdentificacion().equalsIgnoreCase(identificacion)) {
                return cliente;
            }
        }
        return null;
    }

    public ArrayList<Producto> getProductos() { return new ArrayList<Producto>(productos); }
    public ArrayList<Cliente> getClientes() { return new ArrayList<Cliente>(clientes); }
    public ArrayList<Pedido> getPedidos() { return new ArrayList<Pedido>(pedidos); }

    public long getVentasTotalesCentavos() {
        long total = 0;
        for (int i = 0; i < pedidos.size(); i++) {
            total = Math.addExact(total, pedidos.get(i).getImporteTotalCentavos());
        }
        return total;
    }
}
