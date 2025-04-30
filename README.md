# PicTroViews

PicTroViews es una aplicación móvil para Android que permite capturar fotografías, obtener la ubicación actual y la hora, y guardar las imágenes en la galería del dispositivo con un nombre que incluye la fecha, hora y coordenadas GPS.

## Características

- **Captura de Fotografías**: Usa la cámara del dispositivo para capturar imágenes.
- **Obtención de Ubicación**: Obtiene la ubicación actual del usuario mediante GPS.
- **Registro de Hora**: Registra la hora exacta en que se tomó la fotografía.
- **Guardado en la Galería**: Guarda las fotos en la galería con un nombre que incluye:
  - Fecha en formato `dd-MM-yyyy`
  - Hora en formato `HH-mm`
  - Coordenadas GPS en formato `LAT<latitud>_LON<longitud>`
- **Visualización en Mapa**: Muestra un marcador en un mapa indicando la ubicación donde se tomó la foto.

## Requisitos

- Android Studio instalado.
- Dispositivo Android con GPS y cámara.
- Permisos de ubicación, cámara y almacenamiento habilitados.

## Instalación

1. Clona este repositorio en tu máquina local:
   ```bash
   git clone https://github.com/mBrownC/PicTroViews.git
   ```
2. Abre el proyecto en Android Studio.
3. Conecta un dispositivo Android o configura un emulador.
4. Compila y ejecuta la aplicación.

## Uso

Al iniciar la aplicación, asegúrate de que los permisos de ubicación, cámara y almacenamiento estén habilitados.

1. Presiona el botón para capturar una foto.
2. La aplicación:
   - Obtendrá tu ubicación actual.
   - Capturará la foto.
   - Guardará la foto en la galería con un nombre que incluye la fecha, hora y coordenadas GPS.
3. La ubicación de la foto se mostrará en un mapa dentro de la aplicación.

## Estructura del Proyecto

PicTroViews/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/ # Código Java/Kotlin de la aplicación
│   │   │   ├── res/  # Recursos como layouts, strings, imágenes, etc.
│   │   │   └── AndroidManifest.xml # Archivo de configuración de permisos y componentes
│   │   └── test/ # Pruebas unitarias
│   └── build.gradle # Configuración de compilación del módulo
├── gradle/ # Configuración de Gradle
├── build.gradle # Configuración global de Gradle
├── settings.gradle # Configuración de módulos del proyecto
└── README.md  # Documentación del proyecto

## Permisos

La aplicación requiere los siguientes permisos, definidos en el archivo `AndroidManifest.xml`:

- `ACCESS_FINE_LOCATION`: Para obtener la ubicación precisa.
- `ACCESS_COARSE_LOCATION`: Para obtener una ubicación aproximada.
- `CAMERA`: Para capturar fotografías.
- `WRITE_EXTERNAL_STORAGE`: Para guardar las fotos en la galería.
- `INTERNET`: Para mostrar mapas.

## Posibles Mejoras

- Agregar soporte para múltiples idiomas.
- Mejorar la interfaz de usuario.
- Implementar almacenamiento en la nube para las fotos.

## Contribuciones

¡Las contribuciones son bienvenidas! Si deseas contribuir, por favor abre un issue o envía un pull request.

## Licencia

Este proyecto está bajo la licencia MIT.

¡Gracias por usar PicTroViews!
