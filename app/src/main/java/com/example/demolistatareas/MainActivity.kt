package com.example.demolistatareas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.demolistatareas.data.db.AppDatabase
import com.example.demolistatareas.data.remote.api.JsonPlaceholderApi
import com.example.demolistatareas.data.repository.AuthRepositoryFirebaseImpl
import com.example.demolistatareas.data.repository.TableroRepositoryFirebaseImpl
import com.example.demolistatareas.data.repository.TareaLaboralRepositoryImpl
import com.example.demolistatareas.data.repository.TareaRepositoryRoomImpl
import com.example.demolistatareas.domain.usecase.AgregarTareaUseCase
import com.example.demolistatareas.domain.usecase.AlternarEstadoTareaUseCase
import com.example.demolistatareas.domain.usecase.ObtenerAnunciosUseCase
import com.example.demolistatareas.domain.usecase.ObtenerTareasSugeridasUseCase
import com.example.demolistatareas.domain.usecase.ObtenerTareasUseCase
import com.example.demolistatareas.domain.usecase.PublicarAnuncioUseCase
import com.example.demolistatareas.presentation.navigation.AppNavigation
import com.example.demolistatareas.presentation.viewmodel.AuthViewModel
import com.example.demolistatareas.presentation.viewmodel.TableroViewModel
import com.example.demolistatareas.presentation.viewmodel.TareasLaboralesViewModel
import com.example.demolistatareas.presentation.viewmodel.TareasViewModel
import com.example.demolistatareas.ui.theme.DemoListaTareasTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


// Punto de entrada de la app. Acá configuramos todo y conectamos las capas inyectando las dependencias a mano.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        // Firebase 1. Inicialización de clientes nativos de infraestructura (Firebase)
        val firebaseAuth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        // 1a. Inicializamos la base de datos Room
        val database = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "tareas_db"
            ).fallbackToDestructiveMigration(false).build()

        // 1b. Configuramos el Cliente HTTP (Retrofit)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(JsonPlaceholderApi::class.java)

        //  Construcción de la Capa de Datos (Repositorios)
        //val repositorio = TareaRepositoryInMemoryImpl()
        val repositorio = TareaRepositoryRoomImpl(database.tareaDao())
        val repositorioRed = TareaLaboralRepositoryImpl(api)
        // Firebase paso #2
        val authRepository = AuthRepositoryFirebaseImpl(firebaseAuth)
        val tableroRepository = TableroRepositoryFirebaseImpl(firestore)

        // Construcción de la Capa de Dominio (Casos de Uso)
        val obtenerTareasUseCase = ObtenerTareasUseCase(repositorio)
        val agregarTareaUseCase = AgregarTareaUseCase(repositorio)
        val alternarEstadoTareaUseCase = AlternarEstadoTareaUseCase(repositorio)
        // Firebase paso #3
        val obtenerAnunciosUseCase = ObtenerAnunciosUseCase(tableroRepository)
        val publicarAnuncioUseCase = PublicarAnuncioUseCase(tableroRepository)

        // 3b. Caso de uso para las sugerencias de la red
        val obtenerTareasSugeridasUseCase = ObtenerTareasSugeridasUseCase(repositorioRed)

        // 4. Configuramos el Factory para el ViewModel (Presentación)
        // Esto es para instanciar ViewModels parametrizados.
        // Requisito indispensable cuando un ViewModel posee constructores con argumentos.
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(TareasViewModel::class.java) -> {
                        TareasViewModel(obtenerTareasUseCase, agregarTareaUseCase, alternarEstadoTareaUseCase) as T
                    }
                    modelClass.isAssignableFrom(TareasLaboralesViewModel::class.java) -> {
                        TareasLaboralesViewModel(obtenerTareasSugeridasUseCase) as T
                    }
                    // Firebase paso #4. ViewModels parametrizados para feature firebase
                    modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                        return AuthViewModel(authRepository) as T
                    }
                    modelClass.isAssignableFrom(TableroViewModel::class.java) -> {
                        return TableroViewModel(obtenerAnunciosUseCase, publicarAnuncioUseCase) as T
                    }
                    else -> throw IllegalArgumentException("ViewModel no reconocido")
                }
            }
        }

        setContent {
            DemoListaTareasTheme() {
                // Delegamos la gestión visual y de estado al grafo de navegación,
                // inyectando el factory con los Casos de Uso.
                AppNavigation(viewModelFactory = factory)
            }
        }
    }
}