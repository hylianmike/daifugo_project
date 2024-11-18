package com.example.daifugo

import javafx.animation.TranslateTransition
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.TilePane
import javafx.stage.Stage
import javafx.util.Duration
import java.net.URL
import java.util.*

class EndController: Initializable {

    @FXML
    private lateinit var rankOneLabel: Label

    @FXML
    private lateinit var rankTwoLabel: Label

    @FXML
    private lateinit var rankThreeLabel: Label

    @FXML
    private lateinit var rankFourLabel: Label

    @FXML
    private lateinit var cardTilePane: TilePane

    @FXML
    private lateinit var playOfTheGamePlayer: Label

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

    /**
     * function to set the largest hand that someone played in the display
     */
    fun setPlayOfTheGame(cards: List<Card>?, player: String) {
        val animationDuration = Duration.millis(400.0)
        cards?.forEachIndexed { index, card ->
            val imageView = ImageView(card.image)
            imageView.fitHeight = 100.0
            imageView.isPreserveRatio = true
            imageView.translateX = 800.0

            val transition = TranslateTransition(animationDuration, imageView)
            transition.fromX = 800.0
            transition.toX = 0.0
            transition.delay = Duration.millis(75.0 * index)
            transition.play()

            cardTilePane.children.add(imageView)
        }
        playOfTheGamePlayer.text = "By: $player"
    }

    fun mainMenu(actionEvent: ActionEvent) {
        val fxmlLoader = FXMLLoader(GameApplication::class.java.getResource("home.fxml"))
        val scene = Scene(fxmlLoader.load())
        val stage = (actionEvent.source as Node).scene.window as Stage
        stage.scene = scene
        stage.show()
    }

    fun playAgain(actionEvent: ActionEvent) {
        val fxmlLoader = FXMLLoader(GameApplication::class.java.getResource("game.fxml"))
        val scene = Scene(fxmlLoader.load())
        val controller: GameController = fxmlLoader.getController()
        controller.setPlayerNames(rankOneLabel.text.split(": ")[1], rankTwoLabel.text.split(": ")[1],
            rankThreeLabel.text.split(": ")[1], rankFourLabel.text.split(": ")[1])
        val stage = (actionEvent.source as Node).scene.window as Stage
        stage.scene = scene
        stage.show()
    }

    override fun initialize(p0: URL?, p1: ResourceBundle?) {}
}