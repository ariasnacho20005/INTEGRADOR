# Informe adaptado desde la referencia

## Referencia

- DOCX retenido: `C:\Users\arias\Downloads\informe-tecnico-corregido.docx`
- Copia de trabajo: `C:\xampp\htdocs\integrador\.tmp-informe\adaptacion-vscode\reference.docx`
- SHA-256: `c2d31ec5f6862d0eac1f096da022df1a5fc6efb8f26d801294139f3a3d947208`
- Sección: una sección continua, orientación vertical, papel A4 (8,27 × 11,69 pulgadas).
- Márgenes: izquierda 0,69; derecha 0,39; arriba 0,78; abajo 0,49 pulgadas. Distancias de encabezado y pie: 0,17 y 0,32 pulgadas.
- Revisión de render: `render_docx.py` no pudo iniciar porque `soffice.exe` no está instalado ni disponible en `PATH`. El número de páginas y la inspección visual quedan sin resolver en este equipo.

## Sistema visual y componentes

- Portada académica centrada con datos de institución, materia, alumno, actividad y carrera; conserva la imagen incrustada existente.
- Una columna, tipografía base Arial 12 pt, texto negro. El nombre de la institución usa ADLaM Display 24 pt en negrita.
- Jerarquía de títulos de Word: Heading 1, Heading 2 y Heading 3 en negrita; párrafos de contenido en Body Text.
- Cinco tablas sin ajuste automático: responsabilidades (7 × 2), complejidad (5 × 5), preparación (4 × 6), búsquedas (4 × 5) y ordenamientos (4 × 3). Se conservan los anchos, bordes, sombreados y estilos de las celdas.
- Paquetes retenidos: estilos, numeración, tema, relaciones, imagen, notas al pie/final, configuración y propiedades personalizadas. Solo se reemplaza el contenido de `word/document.xml`; las propiedades del documento se conservan.

## Flujo y mapa de contenido

1. Datos académicos y título del informe.
2. Descripción del problema y solución actual como aplicación Java de consola operada desde la terminal integrada de Visual Studio Code.
3. Diseño orientado a objetos y tabla de responsabilidades.
4. Algoritmos implementados, explicación de complejidad y tabla comparativa.
5. Metodología del benchmark y tablas de preparación, búsquedas y ordenamientos. Los valores se conservan como una ejecución de referencia; la interpretación se alinea con los datos de las tablas.
6. Validaciones, reglas monetarias y comandos para compilar, ejecutar y lanzar el benchmark desde PowerShell en VS Code.
7. Interpretación de resultados y conclusión con las limitaciones de datos en memoria.

## Fidelidad y cambios autorizados

- La referencia permanece intacta y el nuevo archivo se crea en una ruta distinta.
- Se mantienen geometría de página, estilos, portada, imagen, estructura de tablas y elementos de paquete no editados.
- Se actualiza la descripción funcional según el código Java actual, se corrige la errata `SUPEROR`, se incorpora el flujo de ejecución desde VS Code y se corrige la interpretación de rendimiento para que use las cifras de las tablas.
- No se agregan afirmaciones de pruebas nuevas ni dependencias que no aparecen en el código o los README.
