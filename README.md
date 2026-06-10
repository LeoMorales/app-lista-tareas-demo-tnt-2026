# Demo Lista de Tareas - TNT 2026

Esta aplicación Android es un proyecto integral diseñado para demostrar la implementación de tecnologías modernas, desde la gestión de datos en la nube hasta la geolocalización avanzada. 

La app permite organizar tareas, autenticarse de forma segura y visualizar puntos de interés en un mapa dinámico, combinando persistencia local y sincronización remota.

## Características principales

*   **Autenticación Avanzada:** Integración con Firebase Auth y Google Sign-In utilizando el nuevo **Credential Manager** de Android.
*   **Sincronización en la Nube:** Almacenamiento y recuperación de datos en tiempo real mediante **Firebase Firestore**.
*   **Gestión de Tareas:** Flujo completo de creación, edición y organización de actividades.
*   **Persistencia Local:** Uso de **Room Database** para asegurar el funcionamiento offline.
*   **Mapas y Ubicación:** Integración de mapas con **OSMDroid** (OpenStreetMap) y servicios de ubicación de Google Play Services.
*   **Consumo de APIs:** Integración con servicios externos mediante **Retrofit** y GSON.
*   **Interfaz Moderna:** UI declarativa construida totalmente con **Jetpack Compose** y Material Design 3.

## Stack

*   **Backend & Auth:** Firebase (Authentication & Firestore).
*   **Local Data:** Room Database.
*   **Networking:** Retrofit.
*   **Maps:** OSMDroid + Play Services Location.
*   **Arquitectura:** MVVM con principios de Clean Architecture.
*   **Lenguaje:** Kotlin con Corrutinas y Flow para el manejo de estados asíncronos.

## Requisitos de ejecución

*   **Android Studio Ladybug** (o superior).
*   **SDK de Android 26** (Android 8.0) como mínimo.
*   Conexión a internet para la carga de mapas y sincronización de Firebase.
*   Permisos de ubicación habilitados en el dispositivo o emulador.

## Estructura del Proyecto

El código está organizado siguiendo principios de arquitectura limpia para facilitar su mantenimiento:

*   `data`: Implementación de repositorios, fuentes de datos (Room, Firestore, Retrofit) y mappers.
*   `domain`: Modelos de datos de negocio y casos de uso (Use Cases).
*   `presentation / ui`: Pantallas de Compose, componentes reutilizables y ViewModels.

