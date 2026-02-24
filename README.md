# ShiftTracker

Aplicación móvil Android para la gestión y seguimiento de asistencias, turnos y grupos de actividades.

## Descripción

ShiftTracker es una solución completa cliente-servidor que permite:

- **Gestión de usuarios**: Usuarios comunes y administradores con diferentes permisos
- **Gestión de grupos**: Crear y administrar grupos de actividades (yoga, senderismo, cocina, etc.)
- **Control de asistencias**: Registro de entradas y seguimiento de participación
- **Periodos festivos y vacacionales**: Configuración de días no laborables por grupo o usuario
- **Notificaciones**: Sistema de alertas para administradores

## Arquitectura

```
ShiftTracker/
├── client/          # Aplicación Android Java
├── server/          # Servidor Java con sockets
└── sentencias.sql   # Script de base de datos MySQL
```

## Tecnologías

### Cliente (Android)
- **Lenguaje**: Java
- **UI**: Jetpack Compose + Material Design 3
- **Arquitectura**: MVVM con ViewBinding
- **Dependencias**:
  - AndroidX Navigation
  - WorkManager para tareas en segundo plano
  - Material CalendarView

### Servidor
- **Lenguaje**: Java
- **Comunicación**: Sockets TCP/IP
- **Patrón**: DAO (Data Access Object)

### Base de Datos
- **Motor**: MySQL
- **Tablas principales**: usuarios, grupos, asistencias, periodos_festivos, periodos_vacacionales

## Requisitos

### Cliente
- Android SDK 29+ (Android 10)
- Android Studio con soporte para Java

### Servidor
- Java JDK 8+
- MySQL Server

## Instalación

### Base de Datos
1. Crear una base de datos MySQL
2. Ejecutar el script `sentencias.sql` para crear las tablas e insertar datos de prueba

### Servidor
1. Importar el proyecto en tu IDE Java
2. Configurar la conexión a la base de datos en la clase DAO
3. Ejecutar `Servidor.java`

### Cliente Android
1. Abrir la carpeta `client/` en Android Studio
2. Configurar la IP del servidor en la aplicación
3. Compilar y ejecutar en un dispositivo/emulador Android

## Estructura del Proyecto

### Cliente
```
client/app/src/
├── main/
│   ├── java/com/example/shifttracker/
│   └── res/
```

### Servidor
```
server/src/com/example/shifttracker/
├── conexion_servidor/   # Manejo de conexiones socket
├── controlador/         # Lógica de negocio
├── dao/                 # Acceso a datos
└── pojo/                # Entidades (Usuario, Grupo, etc.)
```

## Documentación

- `Memoria ShiftTracker App.pdf` - Documentación técnica completa
- `Presentación.pptx` - Presentación del proyecto
