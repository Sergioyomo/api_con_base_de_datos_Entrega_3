package com.sgomez.navegaciondetalle.ui.screen.DetailScreen

import android.icu.text.CaseMap.Title
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import com.sgomez.navegaciondetalle.data.AuthManager
import com.sgomez.navegaciondetalle.data.FirestoreManager
import com.sgomez.navegaciondetalle.data.model.Favorito
import com.sgomez.navegaciondetalle.model.MediaItem
import com.sgomez.navegaciondetalle.data.repositories.RemoteConectecition
import com.sgomez.navegaciondetalle.data.repositories.model.Result
import com.sgomez.navegaciondetalle.data.repositories.model.toMediaItem

import kotlinx.coroutines.launch

@Composable
fun DetailScreen(name: String,firestoreManager: FirestoreManager,auth: AuthManager) {
    // Estado para almacenar el resultado del Digimon
    val digimonState = remember { mutableStateOf<Result?>(null) }
    val favoritoState = remember { mutableStateOf<Favorito?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Realizar la llamada a la API en un entorno asincrónico
    LaunchedEffect(name) {
        coroutineScope.launch {
            try {
                val digimon = RemoteConectecition.service.getDigimon(name).get(0)
                val result = Result(digimon.name, digimon.img, digimon.level)
                digimonState.value = result
            } catch (e: Exception) {
                Log.e("DetailScreen", "Error fetching Digimon: ${e.message}")
            }
        }
    }

    LaunchedEffect(name) {
        coroutineScope.launch {
            try {
                val userId =auth.getCurrentUser()?.uid
                val favorito = firestoreManager.getFavoritoByNombre(name, userId.toString())
                favoritoState.value = favorito
            } catch (e: Exception) {
                Log.e("DetailScreen", "Error fetching favorite: ${e.message}")
            }
        }
    }

    fun toggleFavorito() {
        coroutineScope.launch {
            val favorito = favoritoState.value
            if (favorito != null) {
                firestoreManager.deleteFavoritoById(favorito.id)
                favoritoState.value = null
            } else {
                val newFavorito = Favorito(nombre = name,userId=auth.getCurrentUser()?.uid,favorito=true)
                firestoreManager.addFavorito(newFavorito)
                val userId =auth.getCurrentUser()?.uid
                val favoritoNew = firestoreManager.getFavoritoByNombre(name, userId.toString())
                favoritoState.value = favoritoNew
            }
        }
    }



    // Mostrar el contenido cuando los datos estén listos
    digimonState.value?.let { result ->
        val mediaItem = result.toMediaItem()
        if (mediaItem != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column (horizontalAlignment = Alignment.CenterHorizontally){
                    ImagenFull(mediaItem)
                    TitleName(mediaItem)
                    TitleLevel(mediaItem)
                    IconButton(onClick = { toggleFavorito() }) {
                        // Cambiar el icono de acuerdo al estado de favorito
                        val icon: ImageVector = if (favoritoState.value != null) {
                            Icons.Filled.Favorite  // Corazón relleno
                        } else {
                            Icons.Outlined.FavoriteBorder  // Corazón vacío
                        }
                        Icon(imageVector = icon, contentDescription = "Favorito",tint = Color.Red)
                    }

                }

            }

        }
    } ?: run {
        // Mostrar un indicador de carga o mensaje de error mientras se cargan los datos
        CircularProgressIndicator()
    }
}

@Composable
fun ImagenFull(item: MediaItem, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(600.dp)
            .padding(bottom = 16.dp)
    ) {

        Image(
            painter = rememberAsyncImagePainter(
                model = item.img,
                imageLoader = ImageLoader.Builder(context).crossfade(true).build()
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

    }
}

@Composable
fun TitleName(item: MediaItem) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.Cyan)
            .padding(16.dp)
    ) {
        Text(
            text = "Nombre: "+item.name,
            style = MaterialTheme.typography.labelLarge,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun TitleLevel(item: MediaItem) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.Green)
            .padding(16.dp)
    ) {
        Text(
            text = "Nivel: " + item.level,
            style = MaterialTheme.typography.labelLarge,
            overflow = TextOverflow.Ellipsis,
        )
    }
}