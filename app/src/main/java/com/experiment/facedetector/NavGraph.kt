package com.experiment.facedetector

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.experiment.facedetector.ui.screen.FullImageScreen
import com.experiment.facedetector.ui.screen.GalleryScreen
import com.experiment.facedetector.ui.screen.SplashScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController)
        }
        composable("gallery") {
            GalleryScreen(
                onItemClick = { mediaId ->
                    navController.navigate("fullImage/$mediaId")
                })
        }
        composable(
            route = "fullImage/{mediaId}",
            arguments = listOf(navArgument("mediaId") { type = NavType.LongType })
        ) {
            val mediaId = it.arguments?.getLong("mediaId") ?: 0L
            FullImageScreen()
        }
    }
}
