package com.example.daifugo

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.stage.Stage
import java.net.URL
import java.util.*

class EndController: Initializable {

    @FXML
    private lateinit var playAgainButton: Button

    @FXML
    private lateinit var rankOneLabel: Label

    @FXML
    private lateinit var rankTwoLabel: Label

    @FXML
    private lateinit var rankThreeLabel: Label

    @FXML
    private lateinit var rankFourLabel: Label


    fun setRankLabels(first: String, second: String, third: String, fourth: String) {
        rankOneLabel.text = "#1: $first"
        rankTwoLabel.text = "#2: $second"
        rankThreeLabel.text = "#3: $third"
        rankFourLabel.text = "#4: $fourth"
    }

    fun lanSetRankLabels(first: String, second: String) {
        rankOneLabel.text = "#1: $first"
        rankTwoLabel.text = "#2: $second"
    }

    fun playAgain(actionEvent: ActionEvent) {
        val fxmlLoader = FXMLLoader(GameApplication::class.java.getResource("home.fxml"))
        val scene = Scene(fxmlLoader.load())
        val stage = (actionEvent.source as Node).scene.window as Stage
        stage.scene = scene
        stage.show()
    }

    override fun initialize(p0: URL?, p1: ResourceBundle?) {}
}