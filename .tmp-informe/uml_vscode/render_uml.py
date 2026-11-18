from html import escape
from pathlib import Path
from math import hypot

from PIL import Image, ImageDraw, ImageFont


OUT = Path(r"C:\xampp\htdocs\integrador\DistribuidoraJava\docs")
PNG = OUT / "diagrama-uml-vscode.png"
W, H = 2620, 1650
CROP = (50, 140, 2580, 1470)
CROP_W = CROP[2] - CROP[0]
CROP_H = CROP[3] - CROP[1]

BG = "#F4F8FB"
INK = "#17324D"
LINE = "#587184"
MUTED = "#5F7483"
CARD = "#FFFFFF"
BORDER = "#A8BAC6"
ACCENTS = {
    "app": "#245C78",
    "model": "#2D7185",
    "service": "#416A96",
    "algorithms": "#527C64",
}

FONT_DIR = Path(r"C:\Windows\Fonts")
REGULAR_PATH = FONT_DIR / "segoeui.ttf"
BOLD_PATH = FONT_DIR / "segoeuib.ttf"
ITALIC_PATH = FONT_DIR / "segoeuii.ttf"


def font(size, bold=False, italic=False):
    path = BOLD_PATH if bold else ITALIC_PATH if italic else REGULAR_PATH
    try:
        return ImageFont.truetype(str(path), size)
    except OSError:
        return ImageFont.load_default()


img = Image.new("RGB", (W, H), BG)
draw = ImageDraw.Draw(img)
svg = [
    f'<svg xmlns="http://www.w3.org/2000/svg" width="{CROP_W}" height="{CROP_H}" viewBox="{CROP[0]} {CROP[1]} {CROP_W} {CROP_H}">',
    f'<rect width="{W}" height="{H}" fill="{BG}"/>',
    '<style>text{font-family:Segoe UI,Arial,sans-serif}.mono{font-family:Consolas,monospace}</style>',
]


def svg_text(x, y, text, size, color=INK, bold=False, italic=False, mono=False):
    klass = ' class="mono"' if mono else ""
    weight = ' font-weight="700"' if bold else ""
    style = ' font-style="italic"' if italic else ""
    svg.append(
        f'<text x="{x}" y="{y}" dominant-baseline="hanging" font-size="{size}" '
        f'fill="{color}"{weight}{style}{klass}>{escape(text)}</text>'
    )
    pil_font = font(size, bold=bold, italic=italic)
    draw.text((x, y), text, font=pil_font, fill=color, anchor="lt")


def rect(x, y, w, h, fill, stroke=None, sw=1):
    draw.rectangle((x, y, x + w, y + h), fill=fill, outline=stroke, width=sw if stroke else 1)
    edge = f' stroke="{stroke}" stroke-width="{sw}"' if stroke else ""
    svg.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="{fill}"{edge}/>')


def polyline(points, color=LINE, width=4, dashed=False, arrow=None, diamond=False):
    dash = 13
    if not dashed:
        draw.line(points, fill=color, width=width, joint="curve")
        svg.append(
            f'<polyline points="{" ".join(f"{x},{y}" for x, y in points)}" '
            f'fill="none" stroke="{color}" stroke-width="{width}" stroke-linejoin="round"/>'
        )
    else:
        for (x1, y1), (x2, y2) in zip(points, points[1:]):
            length = hypot(x2 - x1, y2 - y1)
            if length == 0:
                continue
            ux, uy = (x2 - x1) / length, (y2 - y1) / length
            step = 0
            while step < length:
                end = min(step + dash, length)
                if step < end:
                    a = (x1 + ux * step, y1 + uy * step)
                    b = (x1 + ux * end, y1 + uy * end)
                    draw.line((a, b), fill=color, width=width)
                step += dash * 1.8
        svg.append(
            f'<polyline points="{" ".join(f"{x},{y}" for x, y in points)}" '
            f'fill="none" stroke="{color}" stroke-width="{width}" stroke-dasharray="13 10" stroke-linejoin="round"/>'
        )
    if diamond:
        x0, y0 = points[0]
        x1, y1 = points[1]
        length = max(1, hypot(x1 - x0, y1 - y0))
        ux, uy = (x1 - x0) / length, (y1 - y0) / length
        px, py = -uy, ux
        dlen, half = 25, 13
        vertices = [
            (x0, y0),
            (x0 + ux * dlen / 2 + px * half, y0 + uy * dlen / 2 + py * half),
            (x0 + ux * dlen, y0 + uy * dlen),
            (x0 + ux * dlen / 2 - px * half, y0 + uy * dlen / 2 - py * half),
        ]
        draw.polygon(vertices, fill=BG, outline=color)
        draw.line(vertices + [vertices[0]], fill=color, width=3)
        svg.append(
            f'<polygon points="{" ".join(f"{x:.1f},{y:.1f}" for x, y in vertices)}" '
            f'fill="{BG}" stroke="{color}" stroke-width="3"/>'
        )
    if arrow:
        (x1, y1), (x2, y2) = points[-2], points[-1]
        length = max(1, hypot(x2 - x1, y2 - y1))
        ux, uy = (x2 - x1) / length, (y2 - y1) / length
        px, py = -uy, ux
        head, half = (27, 17) if arrow == "inheritance" else (19, 11)
        base = (x2 - ux * head, y2 - uy * head)
        left = (base[0] + px * half, base[1] + py * half)
        right = (base[0] - px * half, base[1] - py * half)
        if arrow == "inheritance":
            draw.polygon([(x2, y2), left, right], fill=BG, outline=color)
            draw.line([left, (x2, y2), right], fill=color, width=3)
            draw.line([left, right], fill=color, width=3)
            svg.append(
                f'<polygon points="{x2},{y2} {left[0]:.1f},{left[1]:.1f} {right[0]:.1f},{right[1]:.1f}" '
                f'fill="{BG}" stroke="{color}" stroke-width="3"/>'
            )
        else:
            draw.line([left, (x2, y2), right], fill=color, width=3)
            svg.append(
                f'<polyline points="{left[0]:.1f},{left[1]:.1f} {x2},{y2} {right[0]:.1f},{right[1]:.1f}" '
                f'fill="none" stroke="{color}" stroke-width="3"/>'
            )


def label(x, y, text):
    f = font(17, bold=True)
    box = draw.textbbox((x, y), text, font=f, anchor="lt")
    pad = 6
    coords = (box[0] - pad, box[1] - 3, box[2] + pad, box[3] + 3)
    draw.rounded_rectangle(coords, radius=5, fill=BG)
    svg.append(
        f'<rect x="{coords[0]}" y="{coords[1]}" width="{coords[2]-coords[0]}" '
        f'height="{coords[3]-coords[1]}" rx="5" fill="{BG}"/>'
    )
    svg_text(x, y, text, 17, color=LINE, bold=True)


def draw_box(x, y, w, h, package, name, attrs, methods, accent, stereotype=None):
    # restrained offset shadow
    draw.rectangle((x + 8, y + 8, x + w + 8, y + h + 8), fill="#DDE6EC")
    rect(x, y, w, h, CARD, BORDER, 2)
    rect(x, y, w, 74, accent)
    svg_text(x + 18, y + 9, package, 15, color="#D9E8EF")
    svg_text(x + 18, y + 33, name, 25, color="#FFFFFF", bold=True)
    if stereotype:
        svg_text(x + w - 152, y + 39, f"«{stereotype}»", 15, color="#E0EEF3", italic=True)
    body_font = font(18)
    line_h = 27
    cursor_y = y + 88
    for row in attrs:
        svg_text(x + 18, cursor_y, row, 18, color=INK)
        cursor_y += line_h
    divider_y = cursor_y + 4
    draw.line((x, divider_y, x + w, divider_y), fill=BORDER, width=2)
    svg.append(f'<line x1="{x}" y1="{divider_y}" x2="{x+w}" y2="{divider_y}" stroke="{BORDER}" stroke-width="2"/>')
    cursor_y = divider_y + 14
    for row in methods:
        svg_text(x + 18, cursor_y, row, 18, color=INK)
        cursor_y += line_h
    needed = cursor_y + 8
    if needed > y + h:
        raise ValueError(f"Text overflows {name}: needs through y={needed}, box ends at {y+h}")


# Relationships are drawn first so class boxes sit over the line ends.
polyline([(530, 680), (600, 680)], dashed=True, arrow="dependency")
label(540, 648, "opera")
polyline([(530, 850), (560, 850), (560, 1200), (600, 1200)], dashed=True, arrow="dependency")
label(532, 982, "benchmark")

# Distribuidora keeps lists of clients, products and orders.
polyline([(1040, 350), (1160, 350), (1160, 300), (1200, 300)], diamond=True)
label(1050, 322, "1")
label(1165, 270, "0..*")
polyline([(1120, 600), (1160, 600), (1840, 600), (1840, 680), (1870, 680)], diamond=True)
label(1130, 570, "1")
label(1800, 652, "0..*")
label(1460, 573, "inventario")
polyline([(1120, 820), (1200, 820)], diamond=True)
label(1127, 835, "1")
label(1158, 790, "0..*")

# The order refers to one client and one product; each can be used by many orders.
polyline([(1425, 460), (1425, 650)])
label(1436, 463, "1")
label(1436, 620, "0..*")
label(1460, 520, "realiza")
polyline([(1650, 820), (1870, 840)])
label(1660, 790, "0..*")
label(1802, 809, "1")
label(1710, 850, "incluye")

# Generalization arrows point from each concrete product to Producto.
polyline([(1875, 420), (1875, 600), (1980, 600), (1980, 650)], arrow="inheritance")
polyline([(2345, 420), (2345, 590), (2220, 590), (2220, 650)], arrow="inheritance")

# BenchmarkService compares algorithms and returns measured results.
polyline([(1080, 1030), (1160, 1030), (1160, 995), (1825, 995), (1825, 1240), (1870, 1240)], dashed=True, arrow="dependency")
label(1480, 963, "produce")
polyline([(1120, 1210), (1200, 1210)], dashed=True, arrow="dependency")
label(1128, 1180, "compara")

# Classes and selected members from the current Java sources.
draw_box(70, 520, 460, 410, "com.distribuidora", "Main",
         ["- ENTRADA: Scanner", "- DISTRIBUIDORA: Distribuidora"],
         ["+ main(args): void", "- mostrarMenu(): void", "- registrarProducto(): void", "- crearPedido(): void", "- ejecutarBenchmark(): void", "- mostrarResultadosBenchmark(lista): void"], ACCENTS["app"])

draw_box(600, 350, 520, 550, "com.distribuidora.servicio", "Distribuidora",
         ["- productos: ArrayList<Producto>", "- clientes: ArrayList<Cliente>", "- pedidos: ArrayList<Pedido>"],
         ["+ registrarProducto(p): void", "+ registrarCliente(c): void", "+ buscarProducto(codigo): Producto", "+ confirmarPedido(...): Pedido", "+ ordenarProductosPorPrecio(): ArrayList<Producto>", "«usa Algoritmos» para buscar/ordenar"], ACCENTS["service"])

draw_box(600, 1030, 520, 380, "com.distribuidora.servicio", "BenchmarkService",
         ["- TAMANOS: {100, 1.000, 10.000}", "- REPETICIONES: 5"],
         ["+ ejecutarPruebas(): ArrayList<ResultadoBenchmark>", "- calienta y mide búsquedas", "- compara selection sort / merge sort"], ACCENTS["service"])

draw_box(1200, 150, 450, 310, "com.distribuidora.modelo", "Cliente",
         ["- identificacion: String", "- nombre: String", "- contacto: String", "- pedidos: ArrayList<Pedido>"],
         ["+ agregarPedido(p): void", "+ getPedidos(): ArrayList<Pedido>"], ACCENTS["model"])

draw_box(1870, 650, 500, 390, "com.distribuidora.modelo", "Producto",
         ["- codigo: String", "- nombre: String", "- precioBaseCentavos: long", "- stock: int", "- pedidos: ArrayList<Pedido>"],
         ["+ getPrecioFinalCentavos(): long", "+ reducirStock(cantidad): void", "# calcularPrecioFinalCentavos(): long"], ACCENTS["model"], "abstract")

draw_box(1200, 1030, 550, 420, "com.distribuidora.algoritmos", "Algoritmos",
         ["«utility» · operaciones estáticas"],
         ["+ busquedaLineal(lista, codigo): Producto", "+ busquedaBinaria(indice, codigo): Producto", "+ ordenarSeleccionPorPrecio(lista): void", "+ mergeSortPorPrecio(lista): ArrayList<Producto>", "+ prepararIndicePorCodigo(lista): ArrayList<Producto>"], ACCENTS["algorithms"])

draw_box(1660, 150, 430, 270, "com.distribuidora.modelo", "ProductoAlimenticio",
         ["- fechaVencimiento: LocalDate"],
         ["+ estaVencido(fecha): boolean", "+ getDetalle(): String", "# calcularPrecioFinalCentavos(): long"], ACCENTS["model"])

draw_box(2130, 150, 430, 270, "com.distribuidora.modelo", "ProductoElectronico",
         ["- mesesGarantia: int"],
         ["+ getMesesGarantia(): int", "+ getDetalle(): String", "# calcularPrecioFinalCentavos(): long"], ACCENTS["model"])

draw_box(1200, 650, 450, 350, "com.distribuidora.modelo", "Pedido",
         ["- codigo: long", "- cliente: Cliente", "- producto: Producto", "- cantidad: int", "- fecha: LocalDateTime", "- importeTotalCentavos: long"],
         [], ACCENTS["model"])

draw_box(1870, 1120, 500, 330, "com.distribuidora.servicio", "ResultadoBenchmark",
         ["- cantidad: int", "- métricas de generación y copias", "- métricas de búsquedas", "- métricas de ordenamientos"],
         ["+ getters de resultados"], ACCENTS["service"])

svg.append("</svg>")
OUT.mkdir(parents=True, exist_ok=True)
img.crop(CROP).save(PNG, format="PNG", optimize=True)
print(f"Created {PNG}")
