from copy import deepcopy
from pathlib import Path
from zipfile import ZipFile
from lxml import etree


REFERENCE = Path(r"C:\xampp\htdocs\integrador\.tmp-informe\adaptacion-vscode\reference.docx")
OUTPUT = Path(r"C:\xampp\htdocs\integrador\informe-tecnico-adaptado-vscode.docx")
DOCUMENT_PART = "word/document.xml"
W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
XML = "http://www.w3.org/XML/1998/namespace"
NS = {"w": W}


def q(name):
    return f"{{{W}}}{name}"


def run_text(run):
    return "".join((node.text or "") for node in run.findall(".//w:t", NS))


def run_is_bold(run):
    props = run.find("w:rPr", NS)
    if props is None:
        return False
    value = props.find("w:b", NS)
    return value is not None and value.get(q("val"), "1").lower() not in {"0", "false", "off"}


def run_is_white(run):
    props = run.find("w:rPr", NS)
    if props is None:
        return False
    color = props.find("w:color", NS)
    return color is not None and color.get(q("val"), "").upper() == "FFFFFF"


def choose_run_properties(paragraph, bold=False):
    runs = paragraph.findall("w:r", NS)
    candidates = [r for r in runs if run_text(r).strip()]
    if bold:
        chosen = next((r for r in candidates if run_is_bold(r)), None)
    else:
        chosen = next((r for r in candidates if not run_is_bold(r) and not run_is_white(r)), None)
    if chosen is None and candidates:
        chosen = candidates[0]
    props = chosen.find("w:rPr", NS) if chosen is not None else None
    return deepcopy(props) if props is not None else None


def make_run(text, props=None, code=False):
    run = etree.Element(q("r"))
    run_props = deepcopy(props) if props is not None else etree.Element(q("rPr"))
    if code:
        fonts = run_props.find(q("rFonts"))
        if fonts is None:
            fonts = etree.SubElement(run_props, q("rFonts"))
        for key in ("ascii", "hAnsi", "eastAsia", "cs"):
            fonts.set(q(key), "Consolas")
        size = run_props.find(q("sz"))
        if size is None:
            size = etree.SubElement(run_props, q("sz"))
        size.set(q("val"), "18")
        color = run_props.find(q("color"))
        if color is None:
            color = etree.SubElement(run_props, q("color"))
        color.set(q("val"), "17324D")
    if len(run_props):
        run.append(run_props)
    lines = text.split("\n")
    for index, line in enumerate(lines):
        if index:
            etree.SubElement(run, q("br"))
        if line:
            node = etree.SubElement(run, q("t"))
            if line[:1].isspace() or line[-1:].isspace():
                node.set(f"{{{XML}}}space", "preserve")
            node.text = line
    return run


def replace_paragraph(paragraph, parts, code=False):
    """Replace text runs while retaining the paragraph's real Word style and pPr."""
    base_props = choose_run_properties(paragraph, bold=False)
    bold_props = choose_run_properties(paragraph, bold=True)
    for child in list(paragraph):
        if child.tag != q("pPr"):
            paragraph.remove(child)
    for text, bold in parts:
        if not text:
            continue
        props = bold_props if bold and bold_props is not None else base_props
        paragraph.append(make_run(text, props, code=code))


def plain(text):
    return [(text, False)]


def set_cell(cell, text):
    paragraphs = cell.findall(".//w:p", NS)
    if len(paragraphs) != 1:
        raise ValueError(f"Expected one paragraph in table cell, found {len(paragraphs)}")
    replace_paragraph(paragraphs[0], plain(text))


def update_table(table, rows):
    actual_rows = table.findall("w:tr", NS)
    if len(actual_rows) != len(rows):
        raise ValueError(f"Table row count changed: {len(actual_rows)} != {len(rows)}")
    for row_xml, row_values in zip(actual_rows, rows):
        cells = row_xml.findall("w:tc", NS)
        if len(cells) != len(row_values):
            raise ValueError(f"Table column count changed: {len(cells)} != {len(row_values)}")
        for cell_xml, value in zip(cells, row_values):
            set_cell(cell_xml, value)


if OUTPUT.exists():
    raise FileExistsError(f"Refusing to overwrite existing output: {OUTPUT}")

with ZipFile(REFERENCE, "r") as source:
    parts = {item.filename: source.read(item.filename) for item in source.infolist()}
    source_items = source.infolist()

parser = etree.XMLParser(remove_blank_text=False, resolve_entities=False, no_network=True)
tree = etree.fromstring(parts[DOCUMENT_PART], parser)
body = tree.find(".//w:body", NS)
top_level = list(body)
paragraphs = [element for element in top_level if element.tag == q("p")]
tables = [element for element in top_level if element.tag == q("tbl")]
if len(paragraphs) != 57 or len(tables) != 5:
    raise ValueError(f"Unexpected template structure: {len(paragraphs)} paragraphs, {len(tables)} tables")

# Preserve the cover, heading styles and inline emphasis while updating the report.
replacements = {
    3: plain("INSTITUTO SUPERIOR DEL PROFESORADO DE PASCANAS"),
    4: plain("MATERIA: PROGRAMACIÓN 2"),
    5: plain("ALUMNO: JUAN IGNACIO ARIAS ANDRÁDEZ."),
    6: [("ACTIVIDAD: PROYECTO 4\nDISTRIBUIDORA", False)],
    7: plain("CARRERA: 2.º AÑO TECNICATURA EN DESARROLLO DE SOFTWARE."),
    13: plain("Sistema de gestión de inventario y pedidos de una distribuidora"),
    15: plain("Descripción del problema"),
    16: plain(
        "El sistema gestiona el inventario y los pedidos de una distribuidora de productos alimenticios y electrónicos. "
        "La versión actual es una aplicación Java de consola: permite registrar clientes y productos, consultar y ordenar "
        "el catálogo, confirmar pedidos, actualizar existencias y revisar estadísticas. Se utiliza desde la terminal "
        "integrada de Visual Studio Code y no necesita navegador ni servidor web."
    ),
    17: plain("Diseño orientado a objetos"),
    18: [
        ("Producto", True),
        (" es una clase abstracta que reúne código, nombre, precio base en centavos, stock y pedidos asociados. ", False),
        ("ProductoAlimenticio", True),
        (" y ", False),
        ("ProductoElectronico", True),
        (" heredan de ella y calculan el precio final con reglas propias. El polimorfismo permite que el pedido obtenga el importe sin conocer la subclase concreta.", False),
    ],
    19: [
        ("Distribuidora", True),
        (" administra en memoria colecciones ArrayList de productos, clientes y pedidos. Cada Pedido vincula un Cliente y un Producto; Cliente y Producto conservan los pedidos asociados. Las listas expuestas son copias y el stock se modifica mediante operaciones validadas.", False),
    ],
    20: plain("Responsabilidades principales"),
    22: plain("Algoritmos implementados"),
    23: plain(
        "El catálogo usa ArrayList. Algoritmos implementa búsquedas y ordenamientos mediante ciclos y recursión propios; "
        "el proyecto no delega estas tareas en métodos de biblioteca que ordenen o busquen los productos."
    ),
    24: plain(
        "La búsqueda lineal compara los códigos en el orden actual hasta encontrar el producto o agotar la lista. "
        "No requiere preparar un índice y puede revisar n elementos. Es la búsqueda que usa el menú para localizar productos."
    ),
    25: plain(
        "La búsqueda binaria necesita una copia ordenada por código. En cada paso compara el código medio y descarta "
        "la mitad que no puede contenerlo, por lo que requiere O(log n) comparaciones. Antes se construye el índice con "
        "merge sort, cuyo costo se mide por separado."
    ),
    26: plain(
        "Selection sort busca el elemento que debe ocupar cada posición y lo intercambia con el actual. Mantiene "
        "O(n²) comparaciones y usa espacio adicional constante; en esta aplicación funciona como referencia del benchmark."
    ),
    27: plain(
        "Merge sort divide la lista, ordena sus mitades y luego las mezcla. El menú ordena el catálogo por precio final "
        "descendente y, en caso de empate, por código ascendente. También prepara el índice de la búsqueda binaria. "
        "Su costo es O(n log n) y requiere memoria auxiliar lineal."
    ),
    28: plain("Complejidad temporal"),
    30: plain(
        "La búsqueda lineal puede requerir O(n) comparaciones; la binaria requiere O(log n) sobre una lista ya ordenada, "
        "con mejor caso O(1). Preparar el índice por código añade O(n log n). Selection sort cuesta O(n²) y merge sort "
        "O(n log n), con espacio auxiliar O(n) para la mezcla. En el menú, la búsqueda operativa es lineal y el "
        "ordenamiento del catálogo usa merge sort; la búsqueda binaria se compara en el benchmark."
    ),
    31: plain("Mediciones de rendimiento"),
    32: plain(
        "BenchmarkService genera conjuntos de 100, 1.000 y 10.000 productos y calienta las rutas de los algoritmos "
        "antes de medir. Cada resultado promedia cinco repeticiones; cada medición de búsqueda ejecuta 1.000 consultas "
        "por repetición. Para las dos búsquedas se usa el mismo código existente, cercano al centro del conjunto, y el "
        "mismo código ausente, mayor que el último código generado. La generación de datos, las copias y la preparación "
        "del índice se informan aparte. Los ordenamientos reciben copias del mismo conjunto y el programa verifica que "
        "produzcan el mismo orden."
    ),
    33: plain("Preparación de datos, promedio en nanosegundos"),
    36: plain("Búsqueda, promedio en nanosegundos por consulta"),
    39: plain("Ordenamiento de copias, promedio en nanosegundos"),
    42: plain(
        "Los tiempos dependen del procesador, del sistema operativo, de la JVM, del calentamiento y de la carga del equipo. "
        "Las cifras describen una ejecución de referencia, no valores universales; las complejidades indican tendencias "
        "cuando aumenta el tamaño de los datos."
    ),
    43: plain("Validaciones y compilación desde VS Code"),
    44: plain(
        "Antes de confirmar un pedido se valida que existan el cliente y el producto, que la cantidad sea positiva, que "
        "haya stock suficiente y que el alimento no esté vencido. También se rechazan códigos de producto e identificaciones "
        "de cliente duplicados. El importe se calcula antes de descontar existencias; si una validación falla, el pedido "
        "no se registra ni se modifica el stock. Los errores controlados devuelven al menú."
    ),
    45: plain(
        "Los precios se guardan en centavos enteros. El alimento aplica un descuento del 5 %; el electrónico agrega un "
        "recargo del 1 % por cada seis meses de garantía, admitida entre 0 y 360 meses. Los importes se calculan con "
        "BigDecimal y redondeo HALF_UP."
    ),
    46: plain(
        "Para ejecutar la versión actual desde VS Code, abre la carpeta DistribuidoraJava y usa la terminal integrada "
        "de PowerShell. Se requiere un JDK con java y javac disponibles en PATH. Compila todos los archivos fuente y "
        "luego inicia el menú con estos comandos:"
    ),
    47: plain(
        "$javaFiles = Get-ChildItem .\\src\\main\\java -Recurse -Filter *.java\n"
        "javac -encoding UTF-8 -d .\\out $javaFiles.FullName\n"
        "java -cp .\\out com.distribuidora.Main\n"
        "java -cp .\\out com.distribuidora.Main --benchmark"
    ),
    49: plain("Conclusión e interpretación"),
    50: plain(
        "En la ejecución de referencia con 10.000 productos, selection sort tomó 2.106.055.560 ns (aprox. 2,11 s) y "
        "merge sort 14.465.380 ns (aprox. 14,47 ms). En esa medición, selection sort tardó cerca de 146 veces más. "
        "La diferencia concuerda con sus costos O(n²) y O(n log n), respectivamente."
    ),
    51: plain(
        "Para el código ausente y 10.000 productos, la búsqueda lineal promedió 455.568,90 ns por consulta y la binaria "
        "480,92 ns una vez preparado el índice. La preparación del índice tomó 6,37 ms en esa misma medición. Por eso, "
        "la búsqueda binaria resulta conveniente cuando el índice se reutiliza; la búsqueda lineal evita ese costo inicial."
    ),
    52: plain(
        "El benchmark usa datos sintéticos y una consulta existente cercana al centro más otra ausente que supera el último "
        "código. No representa todas las distribuciones de consultas. El calentamiento reduce el efecto de la compilación "
        "dinámica, pero no elimina la variación causada por el entorno de ejecución."
    ),
    55: plain("Conclusión"),
    56: plain(
        "La versión actual es una aplicación Java de consola que se ejecuta desde la terminal integrada de VS Code. "
        "Merge sort ordena el catálogo y la búsqueda lineal resuelve las consultas del menú; la búsqueda binaria muestra "
        "su ventaja cuando el índice ya está preparado y se reutiliza. El sistema no guarda datos al cerrar: productos, "
        "clientes y pedidos solo permanecen en memoria durante la ejecución. No requiere navegador, XAMPP, Apache ni Maven."
    ),
}

for paragraph_index, parts_to_write in replacements.items():
    replace_paragraph(paragraphs[paragraph_index], parts_to_write, code=(paragraph_index == 47))

# Keep the five source tables and their exact geometry; align the interpretation with these readings.
update_table(tables[0], [
    ["Clase", "Responsabilidad"],
    ["Producto / subclases", "Datos comunes, vencimiento o garantía y cálculo polimórfico del precio final."],
    ["Cliente / Pedido", "Datos del cliente y vínculo con producto, cantidad, fecha e importe de la venta."],
    ["Distribuidora", "Colecciones en memoria, registros, validaciones, pedidos, existencias y estadísticas."],
    ["Algoritmos", "Búsqueda lineal y binaria, selection sort y merge sort implementados en el proyecto."],
    ["BenchmarkService / ResultadoBenchmark", "Datos de prueba, calentamiento, cronometraje, promedios y resultados."],
    ["Main", "Menú de consola, lectura de datos y acceso a la opción de benchmark."],
])
update_table(tables[1], [
    ["Algoritmo", "Mejor caso", "Promedio", "Peor caso", "Espacio"],
    ["Búsqueda lineal", "O(1)", "O(n)", "O(n)", "O(1)"],
    ["Búsqueda binaria", "O(1)", "O(log n)", "O(log n)", "O(1)"],
    ["Selection sort", "O(n²)", "O(n²)", "O(n²)", "O(1)"],
    ["Merge sort", "O(n log n)", "O(n log n)", "O(n log n)", "O(n)"],
])
update_table(tables[2], [
    ["n", "Generación", "Copia índice", "Ordenar índice", "Copia selección", "Copia merge"],
    ["100", "51.513.020", "1.440", "29.940", "1.940", "2.340"],
    ["1.000", "83.866.820", "2.760", "316.020", "4.380", "12.300"],
    ["10.000", "224.590.500", "27.480", "6.371.380", "34.020", "36.280"],
])
update_table(tables[3], [
    ["n", "Lineal existe", "Binaria existe", "Lineal ausente", "Binaria ausente"],
    ["100", "13.287,92", "530,80", "5.664,40", "2.761,74"],
    ["1.000", "22.981,92", "356,86", "37.601,40", "366,12"],
    ["10.000", "172.375,62", "492,26", "455.568,90", "480,92"],
])
update_table(tables[4], [
    ["n", "Selection sort", "Merge sort"],
    ["100", "8.894.000", "29.660"],
    ["1.000", "9.491.900", "448.920"],
    ["10.000", "2.106.055.560", "14.465.380"],
])

parts[DOCUMENT_PART] = etree.tostring(tree, xml_declaration=True, encoding="UTF-8", standalone=True)
OUTPUT.parent.mkdir(parents=True, exist_ok=True)
with ZipFile(OUTPUT, "w") as target:
    for item in source_items:
        target.writestr(item, parts[item.filename])

with ZipFile(OUTPUT, "r") as check:
    bad_part = check.testzip()
    if bad_part:
        raise ValueError(f"Corrupt DOCX package part: {bad_part}")
    if set(check.namelist()) != set(parts):
        raise ValueError("Package part inventory changed unexpectedly")

print(f"Created: {OUTPUT}")
print(f"Updated paragraphs: {len(replacements)}; preserved tables: {len(tables)}")
print(f"Package parts retained: {len(parts)}")
