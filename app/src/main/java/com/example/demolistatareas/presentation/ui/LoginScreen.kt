package com.example.demolistatareas.presentation.ui

import com.example.demolistatareas.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.demolistatareas.presentation.viewmodel.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

/**
 * Pantalla dedicada a la autenticación de usuarios.
 * Gestiona el estado local del formulario (email y contraseña) y delega
 * la validación de credenciales al ViewModel.
 * Tambien delega a la plataforma la petición de un token de Google.
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginExitoso: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var estaCargando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val contexto = LocalContext.current
    // Resolvemos el ID del cliente de autenticador Google
    val webClientId = stringResource(R.string.default_web_client_id)

    // Inicializamos Credential Manager
    val credentialManager = remember { CredentialManager.create(contexto) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Acceso a Lista de Tareas",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !estaCargando
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = clave,
            onValueChange = { clave = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !estaCargando
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (mensajeError != null) {
            Text(
                text = mensajeError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = {
                estaCargando = true
                mensajeError = null
                coroutineScope.launch {
                    val resultado = viewModel.iniciarSesion(email, clave)
                    estaCargando = false
                    if (resultado.isSuccess) {
                        onLoginExitoso()
                    } else {
                        mensajeError = "Credenciales inválidas o error de red."
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = email.isNotBlank() && clave.isNotBlank() && !estaCargando
        ) {
            if (estaCargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Iniciar Sesión")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(0.5f))
        Spacer(modifier = Modifier.height(24.dp))

        // Botón de Google usando Credential Manager
        OutlinedButton(
            onClick = {
                coroutineScope.launch {
                    estaCargando = true
                    mensajeError = null

                    try {
                        // 1. Configurar la opción de Google ID (ID Token)
                        val googleIdOption = GetGoogleIdOption.Builder()
                            // El Server Client ID es necesario para que el backend (Firebase) valide la identidad
                            .setServerClientId(webClientId)
                            // false: muestra todas las cuentas; true: solo las ya vinculadas a esta app
                            .setFilterByAuthorizedAccounts(false)
                            // Permite selección automática si solo hay una cuenta disponible
                            .setAutoSelectEnabled(true)
                            .build()

                        // 2. Crear la solicitud global agrupando las opciones (Google, contraseñas, etc.)
                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                        // 3. Ejecutar la solicitud: despliega la interfaz nativa (Bottom Sheet) de Android
                        val result = credentialManager.getCredential(
                            context = contexto,
                            request = request
                        )

                        // 4. Extraer la credencial del resultado obtenido
                        val credential = result.credential
                        
                        // Verificamos si la credencial es de tipo Google ID Token (personalizada)
                        if (credential is CustomCredential && 
                            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            
                            // Convertimos los datos crudos en un objeto usable de credencial de Google
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            // Este idToken (JWT) es el que enviamos a Firebase para el login final
                            val idToken = googleIdTokenCredential.idToken
                            
                            // Intentamos iniciar sesión en Firebase con el token recibido
                            val authResult = viewModel.iniciarSesionConGoogle(idToken)
                            if (authResult.isSuccess) {
                                onLoginExitoso()
                            } else {
                                mensajeError = "Error al validar con Firebase."
                            }
                        }
                    } catch (e: GetCredentialException) {
                        mensajeError = "Inicio de sesión cancelado o fallido."
                    } catch (e: Exception) {
                        mensajeError = "Ocurrió un error inesperado."
                    } finally {
                        estaCargando = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !estaCargando
        ) {
            Text("Continuar con Google")
        }
    }
}
