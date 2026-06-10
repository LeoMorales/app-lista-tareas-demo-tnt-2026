package com.example.demolistatareas.data.repository

import com.example.demolistatareas.domain.model.Anuncio
import com.example.demolistatareas.domain.repository.TableroRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await


/**
 * Implementación del repositorio de base de datos utilizando Cloud Firestore.
 * Transforma los documentos BSON/JSON alojados en la nube en entidades de dominio puras.
 *
 * @param firestore Instancia principal del cliente de Firestore.
 */
class TableroRepositoryFirebaseImpl (
    private val firestore: FirebaseFirestore
) : TableroRepository {

    // Referencia centralizada a la colección
    private val coleccionAnuncios = firestore.collection("anuncios")

    override fun obtenerAnuncios(): Flow<List<Anuncio>> = callbackFlow {
        // Se define la consulta base. Aunque el Caso de Uso ordena los datos en memoria,
        // es una buena práctica delegar el ordenamiento inicial al motor
        // de base de datos para optimizar la carga útil de red.
        val consulta = coleccionAnuncios.orderBy("fechaCreacion", Query.Direction.DESCENDING)

        // addSnapshotListener establece una conexión en tiempo real con la colección.
        // El bloque se ejecuta inmediatamente con el estado actual, y luego
        // se vuelve a disparar cada vez que los datos mutan en el servidor.
        val listenerRegistration = consulta.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // En caso de fallo de red o falta de permisos, se cierra el flujo emitiendo el error.
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                // Operación de mapeo: Se itera sobre cada documento crudo recibido
                // y se extraen sus campos para instanciar objetos Anuncio.
                val listaAnuncios = snapshot.documents.mapNotNull { documento ->
                    try {
                        Anuncio(
                            id = documento.id, // El ID se extrae de la metadata del documento
                            contenido = documento.getString("contenido") ?: "",
                            autorNombre = documento.getString("autorNombre") ?: "Anónimo",
                            autorId = documento.getString("autorId") ?: "",
                            fechaCreacion = documento.getLong("fechaCreacion") ?: 0L
                        )
                    } catch (e: Exception) {
                        // Si un documento posee una estructura corrupta o campos con tipos de datos
                        // incorrectos, se descarta silenciosamente para no interrumpir la lectura del resto.
                        null
                    }
                }

                // Se emite la lista procesada hacia la capa de presentación.
                trySend(listaAnuncios)
            }
        }

        // Se bloquea el cierre prematuro de la corrutina.
        // Cuando el observador (ej. ViewModel) se destruye, se ejecuta este bloque
        // para desconectar el listener y evitar consumo innecesario de batería y red.
        awaitClose {
            listenerRegistration.remove()
        }
    }

    override suspend fun publicarAnuncio(anuncio: Anuncio): Result<Unit> {
        return try {
            // Se transforma la entidad Anuncio a una estructura de mapa nativa de Kotlin,
            // que es el formato requerido por el SDK de Firestore para las inserciones.
            val mapaDatos = hashMapOf(
                "contenido" to anuncio.contenido,
                "autorNombre" to anuncio.autorNombre,
                "autorId" to anuncio.autorId,
                "fechaCreacion" to anuncio.fechaCreacion
            )

            // La función de extensión .await() suspende la ejecución hasta recibir confirmación
            // del servidor o de la caché local de que la escritura fue procesada.
            coleccionAnuncios.add(mapaDatos).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}