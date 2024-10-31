package com.example.daifugo

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.Image
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class GameController : Initializable
{
    private val deck = mutableListOf<Card>()

    @FXML
    private lateinit var fourOfAKindButton: Button

    @FXML
    private lateinit var passButton: Button

    @FXML
    private lateinit var playButton: Button

    @FXML
    private lateinit var singlesButton: Button

    @FXML
    private lateinit var straightsButton: Button

    @FXML
    private lateinit var threeOfAKindButton: Button

    @FXML
    private lateinit var turnLabel: Label

    @FXML
    private lateinit var twoOfAKindButton: Button


    override fun initialize(url: URL?, resourceBundle: ResourceBundle?) {
        // Load all card images from the resources/cards folder
        val resourcePath = javaClass.getResource("/Cards")
        resourcePath?.let { path ->
            val dir = Paths.get(path.toURI())
            Files.list(dir).forEach { filePath ->
                val fileName = filePath.fileName.toString().removeSuffix(".png")
                val (value, suit) = parseFileName(fileName)
                deck.add(Card(value, suit, Image(filePath.toUri().toString())))
            }
            shuffleDeck()
        }
    }

    // Shuffle the deck
    private fun shuffleDeck() {
        deck.shuffle()
    }

    fun dealHands(): List<List<Card>> {
        val hands = List(4) { mutableListOf<Card>() }
        for ((index, card) in deck.withIndex()) {
            hands[index % 4].add(card)
        }
        return hands
    }

    private fun parseFileName(fileName: String): Pair<String, String> {
        val parts = fileName.split("_of_")
        return Pair(parts[0], parts[1])
    }
}

data class Card(val value: String, val suit: String, val image: Image)

