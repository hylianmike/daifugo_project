package com.example.daifugo

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage

class GameApplication : Application()
{
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(GameApplication::class.java.getResource("home.fxml"))
        val scene = Scene(fxmlLoader.load())
        val icon = Image("file:src/main/resources/com/example/daifugo/icon.png")
        stage.title = "Daifugo!"
        stage.icons.add(icon)
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(GameApplication::class.java)
}