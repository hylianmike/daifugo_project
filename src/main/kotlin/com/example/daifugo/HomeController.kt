package com.example.daifugo

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.stage.Stage
import java.net.URL
import java.util.*

class HomeController: Initializable {

    @FXML
    private lateinit var p1Field: TextField

    @FXML
    private lateinit var p2Field: TextField

    @FXML
    private lateinit var p3Field: TextField

    @FXML
    private lateinit var p4Field: TextField

    @FXML
    fun playGame(actionEvent: ActionEvent) {
        val fxmlLoader = FXMLLoader(GameApplication::class.java.getResource("game.fxml"))
        val scene = Scene(fxmlLoader.load())
        val controller: GameController = fxmlLoader.getController()
        controller.setPlayerNames(p1Field.textProperty().value, p2Field.textProperty().value, p3Field.textProperty().value, p4Field.textProperty().value)
        val stage = (actionEvent.source as Node).scene.window as Stage
        stage.scene = scene
        stage.show()
    }

    override fun initialize(p0: URL?, p1: ResourceBundle?) {}
}