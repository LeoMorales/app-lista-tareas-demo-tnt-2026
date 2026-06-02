# Demo Lista de Tareas - TNT 2026

Aplicación Android desarrollada para demostrar la integración de diversas tecnologías modernas en el ecosistema móvil: 
Lista de tareas que combina persistencia local, consumo de servicios externos y geolocalización.

## Características

*   **Gestión de Tareas:** Creación, visualización y organización de pendientes.
*   **Persistencia Local:** Uso de base de datos para mantener la información.
*   **Integración con API:** Sincronización o consumo de datos externos mediante servicios REST.
*   **Mapas y Ubicación:** Visualización de puntos de interés y ubicación del usuario en tiempo real utilizando OSM.
*   **Interfaz:** Jetpack Compose y Material Design 3.


## Requisitos

*   Android Studio Panda (o superior).
*   SDK de Android 26 (Android 8.0) como mínimo.
*   Conexión a internet para la carga de mapas y datos remotos.
*   Permisos de ubicación habilitados en el dispositivo/emulador.

## Estructura del Proyecto

El código está organizado siguiendo principios de arquitectura limpia:

*   `data`: Implementación de repositorios, base de datos local (Room) y servicios de red (Retrofit).
*   `domain`: Modelos de datos y lógica de negocio (Use Cases).
*   `presentation / ui`: Pantallas, componentes de Compose y ViewModels.

