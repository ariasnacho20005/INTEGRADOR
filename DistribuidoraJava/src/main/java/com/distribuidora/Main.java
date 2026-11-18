package com.distribuidora;

import com.distribuidora.modelo.Cliente;
import com.distribuidora.modelo.Pedido;
import com.distribuidora.modelo.Producto;
import com.distribuidora.modelo.ProductoAlimenticio;
import com.distribuidora.modelo.ProductoElectronico;
import com.distribuidora.servicio.BenchmarkService;
import com.distribuidora.servicio.Distribuidora;
import com.distribuidora.servicio.ResultadoBenchmark;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

/** Menú de consola y punto de entrada del sistema. */
public final class Main {
    private static final Scanner ENTRADA = new Scanner(System.in);
    private static final NumberFormat FORMATO_DINERO = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
    private static final Distribuidora DISTRIBUIDORA = new Distribuidora();

    private Main() { }

    public static void main(String[] args) {
        if (args.length > 0 && "--benchmark".equalsIgnoreCase(args[0])) {
            mostrarResultadosBenchmark(new BenchmarkService().ejecutarPruebas());
            return;
        }

        int opcion = -1;
        while (opcion != 0) {
            mostrarMenu();
            opcion = leerEntero("Elige una opción: ", 0, 8);
            try {
                switch (opcion) {
                    case 1: registrarProducto(); break;
                    case 2: registrarCliente(); break;
                    case 3: consultarProductos(); break;
                    case 4: localizarProducto(); break;
                    case 5: mostrarProductosOrdenados(); break;
                    case 6: crearPedido(); break;
                    case 7: mostrarEstadisticas(); break;
                    case 8: ejecutarBenchmark(); break;
                    case 0: System.out.println("\nHasta luego."); break;
                    default: System.out.println("Opción no reconocida.");
                }
            } catch (IllegalArgumentException | ArithmeticException error) {
                System.out.println("\nNo se pudo completar la operación: " + error.getMessage());
            } catch (RuntimeException error) {
                System.out.println("\nOcurrió un error controlado: " + error.getMessage());
            }
            if (opcion != 0) {
                System.out.println("\nPresiona Enter para volver al menú.");
                ENTRADA.nextLine();
            }
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n==================================================");
        System.out.println("       SISTEMA DE INVENTARIO - DISTRIBUIDORA");
        System.out.println("==================================================");
        System.out.println("1. Registrar producto");
        System.out.println("2. Registrar cliente");
        System.out.println("3. Consultar productos");
        System.out.println("4. Localizar producto por código");
        System.out.println("5. Ordenar productos por precio final");
        System.out.println("6. Registrar y confirmar pedido");
        System.out.println("7. Ver estadísticas comerciales");
        System.out.println("8. Ejecutar pruebas de rendimiento");
        System.out.println("0. Salir");
    }

    private static void registrarProducto() {
        System.out.println("\n-- Registrar producto --");
        String codigo = leerTexto("Código: ").toUpperCase(Locale.ROOT);
        String nombre = leerTexto("Nombre: ");
        long precioBase = leerPrecio("Precio base (ej. 1250,50): ");
        int stock = leerEntero("Stock inicial (0 o más): ", 0, Integer.MAX_VALUE);
        System.out.println("1. Alimenticio    2. Electrónico");
        int tipo = leerEntero("Tipo: ", 1, 2);

        Producto producto;
        if (tipo == 1) {
            LocalDate vencimiento = leerFecha("Vencimiento (AAAA-MM-DD): ");
            producto = new ProductoAlimenticio(codigo, nombre, precioBase, stock, vencimiento);
        } else {
            int meses = leerEntero("Meses de garantía (0-360): ", 0, 360);
            producto = new ProductoElectronico(codigo, nombre, precioBase, stock, meses);
        }
        DISTRIBUIDORA.registrarProducto(producto);
        System.out.println("Producto " + producto.getCodigo() + " registrado. Precio final: "
                + dinero(producto.getPrecioFinalCentavos()));
    }

    private static void registrarCliente() {
        System.out.println("\n-- Registrar cliente --");
        String identificacion = leerTexto("Identificación: ").toUpperCase(Locale.ROOT);
        String nombre = leerTexto("Nombre: ");
        String contacto = leerTexto("Contacto (teléfono o correo): ");
        DISTRIBUIDORA.registrarCliente(new Cliente(identificacion, nombre, contacto));
        System.out.println("Cliente registrado correctamente.");
    }

    private static void consultarProductos() {
        System.out.println("\n-- Productos registrados --");
        ArrayList<Producto> productos = DISTRIBUIDORA.getProductos();
        if (productos.isEmpty()) {
            System.out.println("No hay productos cargados.");
            return;
        }
        imprimirProductos(productos);
    }

    private static void localizarProducto() {
        System.out.println("\n-- Localizar producto --");
        String codigo = leerTexto("Código a buscar: ");
        Producto producto = DISTRIBUIDORA.buscarProducto(codigo);
        if (producto == null) {
            System.out.println("No se encontró el código " + codigo + ".");
            return;
        }
        System.out.println("Código:       " + producto.getCodigo());
        System.out.println("Nombre:       " + producto.getNombre());
        System.out.println("Tipo:         " + producto.getTipo());
        System.out.println("Precio base:  " + dinero(producto.getPrecioBaseCentavos()));
        System.out.println("Precio final: " + dinero(producto.getPrecioFinalCentavos()));
        System.out.println("Stock:        " + producto.getStock());
        System.out.println("Detalle:      " + producto.getDetalle());
    }

    private static void mostrarProductosOrdenados() {
        System.out.println("\n-- Precio final descendente; empates por código ascendente --");
        ArrayList<Producto> ordenados = DISTRIBUIDORA.ordenarProductosPorPrecio();
        if (ordenados.isEmpty()) {
            System.out.println("No hay productos para ordenar.");
            return;
        }
        imprimirProductos(ordenados);
    }

    private static void crearPedido() {
        System.out.println("\n-- Confirmar pedido --");
        String identificacion = leerTexto("Identificación del cliente: ");
        String codigo = leerTexto("Código del producto: ");
        int cantidad = leerEntero("Cantidad solicitada: ", 1, Integer.MAX_VALUE);
        Pedido pedido = DISTRIBUIDORA.confirmarPedido(identificacion, codigo, cantidad);
        System.out.println("Pedido #" + pedido.getCodigo() + " confirmado.");
        System.out.println("Cliente: " + pedido.getCliente().getNombre());
        System.out.println("Producto: " + pedido.getProducto().getNombre());
        System.out.println("Importe: " + dinero(pedido.getImporteTotalCentavos()));
        System.out.println("Stock restante: " + pedido.getProducto().getStock());
    }

    private static void mostrarEstadisticas() {
        ArrayList<Producto> productos = DISTRIBUIDORA.getProductos();
        int alimentos = 0;
        int electronicos = 0;
        int unidades = 0;
        for (int i = 0; i < productos.size(); i++) {
            Producto producto = productos.get(i);
            unidades = Math.addExact(unidades, producto.getStock());
            if (producto instanceof ProductoAlimenticio) { alimentos++; }
            if (producto instanceof ProductoElectronico) { electronicos++; }
        }
        System.out.println("\n-- Estadísticas comerciales --");
        System.out.println("Productos:          " + productos.size());
        System.out.println("  Alimenticios:     " + alimentos);
        System.out.println("  Electrónicos:     " + electronicos);
        System.out.println("Unidades en stock:  " + unidades);
        System.out.println("Clientes:           " + DISTRIBUIDORA.getClientes().size());
        System.out.println("Pedidos:            " + DISTRIBUIDORA.getPedidos().size());
        System.out.println("Ventas acumuladas:  " + dinero(DISTRIBUIDORA.getVentasTotalesCentavos()));
    }

    private static void ejecutarBenchmark() {
        System.out.println("\nGenerando conjuntos en memoria y ejecutando cinco repeticiones por prueba...");
        mostrarResultadosBenchmark(new BenchmarkService().ejecutarPruebas());
    }

    private static void mostrarResultadosBenchmark(ArrayList<ResultadoBenchmark> resultados) {
        System.out.println("\n=== PREPARACIÓN DE DATOS (promedio de 5 repeticiones, ns) ===");
        System.out.printf("%8s %18s %18s %20s %18s %18s%n",
                "Cantidad", "Generar datos", "Copiar índice", "Ordenar índice", "Copiar selección", "Copiar merge");
        for (int i = 0; i < resultados.size(); i++) {
            ResultadoBenchmark r = resultados.get(i);
            System.out.printf(Locale.US, "%8d %18.2f %18.2f %20.2f %18.2f %18.2f%n",
                    r.getCantidad(), r.getGenerarDatosNs(), r.getCopiarIndiceNs(),
                    r.getOrdenarIndiceCodigoNs(), r.getCopiarSeleccionNs(), r.getCopiarMergeNs());
        }

        imprimirTablaBusqueda(resultados, true);
        imprimirTablaBusqueda(resultados, false);

        System.out.println("\n=== ORDENAMIENTO (promedio de 5 repeticiones, ns) ===");
        System.out.printf("%8s %24s %24s%n", "Cantidad", "Selección (simple)", "Merge Sort");
        for (int i = 0; i < resultados.size(); i++) {
            ResultadoBenchmark r = resultados.get(i);
            System.out.printf(Locale.US, "%8d %24.2f %24.2f%n",
                    r.getCantidad(), r.getSeleccionNs(), r.getMergeSortNs());
        }
        System.out.println("La copia y la preparación del índice binario se informan aparte, fuera de los tiempos de algoritmo.");
    }

    private static void imprimirTablaBusqueda(ArrayList<ResultadoBenchmark> resultados, boolean existente) {
        System.out.println(existente
                ? "\n=== BÚSQUEDA: código existente (promedio ns por consulta) ==="
                : "\n=== BÚSQUEDA: código inexistente (promedio ns por consulta) ===");
        System.out.printf("%8s %24s %24s%n", "Cantidad", "Lineal", "Binaria");
        for (int i = 0; i < resultados.size(); i++) {
            ResultadoBenchmark r = resultados.get(i);
            double lineal = existente ? r.getLinealExisteNs() : r.getLinealAusenteNs();
            double binaria = existente ? r.getBinariaExisteNs() : r.getBinariaAusenteNs();
            System.out.printf(Locale.US, "%8d %24.2f %24.2f%n", r.getCantidad(), lineal, binaria);
        }
    }

    private static void imprimirProductos(ArrayList<Producto> productos) {
        System.out.printf("%-12s %-28s %-16s %14s %14s %8s  %s%n",
                "CÓDIGO", "NOMBRE", "TIPO", "BASE", "FINAL", "STOCK", "DETALLE");
        for (int i = 0; i < productos.size(); i++) {
            Producto p = productos.get(i);
            System.out.printf("%-12s %-28s %-16s %14s %14s %8d  %s%n",
                    p.getCodigo(), acortar(p.getNombre(), 28), p.getTipo(),
                    dinero(p.getPrecioBaseCentavos()), dinero(p.getPrecioFinalCentavos()),
                    p.getStock(), p.getDetalle());
        }
    }

    private static String acortar(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 3) + "...";
    }

    private static String dinero(long centavos) {
        return FORMATO_DINERO.format(BigDecimal.valueOf(centavos, 2));
    }

    private static String leerTexto(String etiqueta) {
        while (true) {
            System.out.print(etiqueta);
            String valor = ENTRADA.nextLine().trim();
            if (!valor.isEmpty()) { return valor; }
            System.out.println("El valor no puede quedar vacío.");
        }
    }

    private static int leerEntero(String etiqueta, int minimo, int maximo) {
        while (true) {
            System.out.print(etiqueta);
            String texto = ENTRADA.nextLine().trim();
            try {
                int valor = Integer.parseInt(texto);
                if (valor >= minimo && valor <= maximo) { return valor; }
            } catch (NumberFormatException ignored) {
                // Se vuelve a solicitar sin cerrar el menú.
            }
            System.out.println("Ingresa un número entero entre " + minimo + " y " + maximo + ".");
        }
    }

    private static long leerPrecio(String etiqueta) {
        while (true) {
            System.out.print(etiqueta);
            String texto = ENTRADA.nextLine().trim();
            try {
                String normalizado = normalizarDecimal(texto);
                BigDecimal monto = new BigDecimal(normalizado);
                if (monto.scale() > 2 || monto.signum() <= 0) { throw new NumberFormatException(); }
                return monto.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact();
            } catch (NumberFormatException | ArithmeticException error) {
                System.out.println("Precio inválido. Usa un valor mayor que cero con hasta dos decimales.");
            }
        }
    }

    private static String normalizarDecimal(String texto) {
        if (texto.indexOf(',') >= 0 && texto.indexOf('.') >= 0) {
            if (texto.lastIndexOf(',') > texto.lastIndexOf('.')) {
                return texto.replace(".", "").replace(',', '.');
            }
            return texto.replace(",", "");
        }
        return texto.replace(',', '.');
    }

    private static LocalDate leerFecha(String etiqueta) {
        while (true) {
            System.out.print(etiqueta);
            String texto = ENTRADA.nextLine().trim();
            try {
                return LocalDate.parse(texto);
            } catch (RuntimeException error) {
                System.out.println("Fecha inválida. Usa el formato AAAA-MM-DD.");
            }
        }
    }
}
