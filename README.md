# 🏷️ Sistema de Subastas Cliente/Servidor en Java

Proyecto final desarrollado en **Java** que implementa un sistema de **subastas distribuidas** bajo un modelo **cliente-servidor**, utilizando **sockets TCP** y **multithreading**.  
El servidor administra las subastas y coordina la comunicación entre los distintos clientes, quienes pueden participar como **subastadores** o **ofertantes**.

---

## 📘 Descripción general

El sistema simula un entorno de subastas en tiempo real.  
Los **clientes** se conectan a un **servidor central** que gestiona la lógica principal: creación de subastas, recepción de ofertas y adjudicación del artículo al mejor postor.  

Cada cliente puede elegir su rol al conectarse:
- 🧑‍⚖️ **Subastador**: crea artículos y abre/cierra subastas.
- 💸 **Participante**: realiza ofertas sobre los artículos activos.

El servidor utiliza **hilos (threads)** para manejar múltiples clientes simultáneamente, asegurando que todas las conexiones y mensajes sean procesados de forma concurrente y sin bloqueo.

---

## ⚙️ Características principales

- Comunicación **cliente-servidor** mediante **sockets TCP**.  
- **Manejo concurrente de múltiples clientes** mediante hilos.  
- Interfaz de usuario **por consola**, con menús y mensajes en tiempo real.  
- Roles diferenciados:
  - **Servidor**: administra las subastas, controla estados, recibe y retransmite mensajes.
  - **Cliente**: se conecta al servidor, elige su rol, y participa activamente.  
- Validaciones:
  - Solo se puede pujar en subastas abiertas.
  - Las ofertas deben superar la puja actual.
  - Solo el subastador puede cerrar la subasta.  
- Sistema de notificaciones en consola: cada cliente recibe actualizaciones de estado y resultados de las subastas en curso.  
- Implementación modular y orientada a objetos.  

Cada cliente se ejecuta en su propio proceso y mantiene una conexión TCP persistente con el servidor.  
El servidor crea un **nuevo hilo por cada cliente conectado**, permitiendo interacciones simultáneas.

---

## 🔄 Flujo de funcionamiento

1. **Inicio del servidor**  
   El servidor se ejecuta y queda a la espera de conexiones de clientes (`ServerSocket`).

2. **Conexión de clientes**  
   Los clientes se conectan indicando la IP y el puerto del servidor (`Socket`).

3. **Selección de rol**  
   Cada cliente elige si actuará como **subastador** o **participante**.

4. **Subasta activa**  
   - El subastador crea un artículo y abre la subasta.  
   - Los participantes envían ofertas al servidor.  
   - El servidor notifica a todos los clientes conectados sobre las pujas en tiempo real.

5. **Cierre y adjudicación**  
   - El subastador cierra la subasta.  
   - El servidor determina al ganador (mayor oferta) y comunica el resultado a todos los clientes.  

---

## 🚀 Ejecución

### 🖥️ Servidor

1. Compilar y ejecutar el servidor:
   ```bash
   javac servidor/Servidor.java
   java servidor.Servidor

2. El servidor quedará escuchando conexiones en un puerto (por defecto 5000 o definido en código).

### 💻 Cliente

1. En otra terminal (o en otra máquina dentro de la red local):
  ```bash
  javac cliente/Cliente.java
  java cliente.Cliente
  ```
2. Ingresar la IP y puerto del servidor.

3. Elegir el rol (subastador o participante) y comenzar la interacción.

### 🧠 Conceptos aplicados

  - Sockets TCP/IP: comunicación bidireccional entre procesos.
  - Programación concurrente: manejo de múltiples clientes en paralelo con Thread.
  - Sincronización y gestión de recursos compartidos (por ejemplo, acceso a la lista de subastas).
  - Diseño modular y orientado a objetos.
  - Separación de responsabilidades: lógica de red, modelo de dominio y presentación.

### 💡 Mejoras futuras

  - Persistencia de subastas en base de datos o archivo.
  - Interfaz gráfica para clientes (JavaFX / Swing).
  - Autenticación de usuarios y roles.
  - Logs del servidor y monitoreo de conexiones.
  - Subastas en tiempo real con interfaz dinámica.

### 👨‍💻 Autores
  - Octavio Pendino, Matías Haspert
  - Proyecto final de Taller de programación III – Comunicación Cliente/Servidor
### 📅 Año: 2024



