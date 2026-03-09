# ShiftTracker

ShiftTracker es una aplicación pensada para simplificar el control de asistencia en organizaciones utilizando tecnología NFC.
La idea surgió al observar que muchos sistemas de fichaje siguen siendo lentos, poco intuitivos o fáciles de manipular.

Con esta aplicación, un usuario solo tiene que acercar una etiqueta NFC a su dispositivo para registrar su entrada o salida. El objetivo es que el proceso sea rápido, claro y difícil de falsificar.

Este proyecto fue desarrollado como proyecto final del ciclo **Desarrollo de Aplicaciones Multiplataforma (DAM)**.

---

# Motivación del proyecto

En muchos entornos todavía se utilizan hojas de firmas, tarjetas manuales o sistemas digitales poco cómodos para registrar la asistencia.

Quería construir algo que:

* fuera sencillo de usar
* funcionara desde un móvil
* redujera el tiempo necesario para fichar
* evitara problemas de fraude o errores

La tecnología NFC encajaba perfectamente con este objetivo.

---

# Cómo funciona

El sistema se basa en tres elementos principales:

**1. Usuario**

Cada usuario tiene asociada una etiqueta NFC.

**2. Dispositivo**

Un dispositivo Android con NFC detecta la etiqueta cuando se acerca al teléfono.

**3. Registro**

La aplicación registra automáticamente el fichaje y lo guarda en el sistema.

De esta manera el proceso completo dura apenas unos segundos.

---

# Funcionalidades principales

### Registro de asistencia con NFC

Los usuarios pueden registrar su entrada o salida acercando su etiqueta NFC al dispositivo.

### Gestión de usuarios

El sistema permite registrar y administrar usuarios dentro de la aplicación.

### Visualización de asistencia

Los registros de asistencia pueden consultarse de forma clara, permitiendo ver cuándo ha fichado cada usuario.

### Interfaz simple

Se buscó mantener una interfaz clara y fácil de usar para evitar confusiones.

---

# Arquitectura del proyecto

La aplicación sigue una estructura modular típica de aplicaciones Android:

* Capa de interfaz de usuario
* Lógica de negocio
* Gestión de almacenamiento de datos
* Integración con NFC

Esto facilita que el proyecto pueda ampliarse en el futuro con nuevas funcionalidades.

---

# Capturas de la aplicación

## Pantalla principal

![Pantalla principal](<img width="309" height="692" alt="image" src="https://github.com/user-attachments/assets/325dbeaf-9791-464a-aa65-49f3e87c00f9" />)

## Registro de asistencia

![Registro NFC](<img width="237" height="522" alt="image" src="https://github.com/user-attachments/assets/91773891-bfd6-43ad-9f6f-c38b2727dc95" />)

## Historial de fichajes

![Historial](<img width="311" height="688" alt="image" src="https://github.com/user-attachments/assets/839a40e8-b001-4499-9b56-059c974b1858" />)

---

# Tecnologías utilizadas

* Android
* Java
