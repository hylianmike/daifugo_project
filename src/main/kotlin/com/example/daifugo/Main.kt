package com.example.daifugo

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

class GameApplication : Application()
{
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(GameApplication::class.java.getResource("home.fxml"))
        val scene = Scene(fxmlLoader.load())
        stage.title = "Daifugo!"
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(GameApplication::class.java)
}