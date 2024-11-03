package com.example.daifugo

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.TilePane
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class GameController : Initializable
{
    private val deck = mutableListOf<Card>()

    private val player1Hand = mutableListOf<Card>()
    private val player2Hand = mutableListOf<Card>()
    private val player3Hand = mutableListOf<Card>()
    private val player4Hand = mutableListOf<Card>()

    private val valueOrder = listOf("3", "4", "5", "6", "7", "8", "9", "10", "jack", "queen", "king", "ace", "2")
    private val suitOrder = listOf("spades", "clubs", "diamonds", "hearts")

    private var selectedCard: Card? = null
    private val playedCards = mutableListOf<Card>()
    private var currentPlayerIndex = 0
    private val playerHands = listOf(player1Hand, player2Hand, player3Hand, player4Hand)
    private var lastPressedButton: Button? = null
    private var currentScreen = 0

    @FXML
    private lateinit var cardTilePane: TilePane

    @FXML
    private lateinit var invalidPlayLabel: Label

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

    /**
     * Update text for who's turn it is
     */
    private fun updateTurnLabel() {
        turnLabel.text = "Player ${currentPlayerIndex + 1}'s Turn"
    }

    @FXML
    private lateinit var twoOfAKindButton: Button

    @FXML
    private lateinit var viewDeckButton: Button

    /**
     * Changes the colour of the selected button
     */
    private fun setButtonPressedColour(button: Button)
    {
        lastPressedButton?.styleClass?.remove("button-pressed")
        button.styleClass.add("button-pressed")
        lastPressedButton = button
    }

    /**
     * Display all cards on button press
     */
    @FXML
    fun singlesButtonPress(event: ActionEvent)
    {
        selectedCard = null
        currentScreen = 1
        setButtonPressedColour(singlesButton)
        cardTilePane.children.clear()
        val currentHand = playerHands[currentPlayerIndex]

        currentHand.forEach { card ->
            val imageView = ImageView(card.image)
            imageView.fitHeight = 100.0
            imageView.isPreserveRatio = true

            // mouse click event for cards
            imageView.setOnMouseClicked { event ->
                // Deselect previously selected card
                selectedCard?.let {
                    deselectCard(it)
                }

                // Select new card
                selectedCard = card
                selectCard(card)
            }

            cardTilePane.children.add(imageView)
        }
    }

    /**
     * Display all cards that are two of a kind on button press
     */
    @FXML
    fun twoOfAKindButtonPress(event: ActionEvent)
    {
        currentScreen = 2
        setButtonPressedColour(twoOfAKindButton)
        cardTilePane.children.clear()

        val pairs = player1Hand.groupBy { it.value }.filter { it.value.size >= 2 }

        pairs.forEach { (value, cards) ->
            if (cards.size >= 2)
            {
                for (i in 0 until 2)
                {
                    val imageView = ImageView(cards[i].image)
                    imageView.fitHeight = 100.0
                    imageView.isPreserveRatio = true
                    cardTilePane.children.add(imageView)
                }
            }
        }
    }

    /**
     * Display all straights on button press that are at least 3 in length
     */
    @FXML
    fun straightsButtonPress(event: ActionEvent)
    {
        currentScreen = 3
        setButtonPressedColour(straightsButton)
        cardTilePane.children.clear()

        val uniqueValues = player1Hand.map { it.value }.distinct()
        val straights = mutableListOf<List<Card>>()
        var currentStraight = mutableListOf<Card>()

        for (i in uniqueValues.indices)
        {
            val currentValue = uniqueValues[i]
            val card = player1Hand.find { it.value == currentValue }

            if (card != null)
            {
                currentStraight.add(card)

                if (i == uniqueValues.lastIndex || valueOrder.indexOf(uniqueValues[i + 1]) != valueOrder.indexOf(currentValue) + 1)
                {
                    if (currentStraight.size >= 3)
                    {
                        straights.add(currentStraight.toList())
                    }

                    currentStraight.clear()
                }
            }
        }

        straights.forEach { straight ->
            straight.forEach { card ->
                val imageView = ImageView(card.image)
                imageView.fitHeight = 100.0
                imageView.isPreserveRatio = true
                cardTilePane.children.add(imageView)
            }
        }
    }

    /**
     * Display all cards that are three of a kind on button press
     */
    @FXML
    fun threeOfAKindButtonPress(event: ActionEvent)
    {
        currentScreen = 4
        setButtonPressedColour(threeOfAKindButton)
        cardTilePane.children.clear()

        val triplets = player1Hand.groupBy { it.value }.filter { it.value.size >= 3 }

        triplets.forEach { (value, cards) ->
            if (cards.size >= 3)
            {
                for (i in 0 until 3)
                {
                    val imageView = ImageView(cards[i].image)
                    imageView.fitHeight = 100.0
                    imageView.isPreserveRatio = true
                    cardTilePane.children.add(imageView)
                }
            }
        }
    }

    /**
     * Display all cards that are four of a kind on button press
     */
    @FXML
    fun fourOfAKindButtonPress(event: ActionEvent)
    {
        currentScreen = 5
        setButtonPressedColour(fourOfAKindButton)
        cardTilePane.children.clear()

        val quads = player1Hand.groupBy { it.value }.filter { it.value.size >= 4 }

        quads.forEach { (value, cards) ->
            if (cards.size >= 4)
            {
                for (i in 0 until 4)
                {
                    val imageView = ImageView(cards[i].image)
                    imageView.fitHeight = 100.0
                    imageView.isPreserveRatio = true
                    cardTilePane.children.add(imageView)
                }
            }
        }
    }

    /**
     * Display cards that have been played on button press
     */
    @FXML
    fun viewDeckButtonPress(event: ActionEvent)
    {
        setButtonPressedColour(viewDeckButton)
        updateViewDeck()
    }

    /**
     * Skip players turn on button press
     */
    @FXML
    fun passButtonPress(event: ActionEvent?)
    {

    }

    /**
     * Play a card and go to next turn on button press
     * TODO - only let player play card if it is of higher value
     * TODO - only let player play card if it's the same type (e.x 4 straight, single, three of a kind, etc)
     */
    @FXML
    fun playButtonPress(event: ActionEvent?)
    {
        // singles
        if (currentScreen == 1)
        {
            if (selectedCard == null)
            {
                invalidPlayLabel.text = "Error: No cards selected."
                return
            }

            selectedCard?.let { card ->
                playedCards.add(card)
                playerHands[currentPlayerIndex].remove(card)
                selectedCard = null

                currentPlayerIndex = (currentPlayerIndex + 1) % playerHands.size
                updateTurnLabel()
                setButtonPressedColour(viewDeckButton)
                updateViewDeck()
            }
        }
        // doubles
        else if (currentScreen == 2)
        {

        }
        // straight
        else if (currentScreen == 3)
        {

        }
        // triples
        else if (currentScreen == 4)
        {

        }
        // quad
        else if (currentScreen == 5)
        {

        }
        // IF TIME PERMITS
        // kill piggy
        else if (currentScreen == 6)
        {

        }
        // on deck screen
        else
        {
            invalidPlayLabel.text = "Error: No cards selected."
        }
    }

    /**
     * Update what the player sees on the deck
     */
    private fun updateViewDeck()
    {
        currentScreen = 0
        cardTilePane.children.clear()
        invalidPlayLabel.text = ""
        val lastPlayedCard = playedCards.lastOrNull()

        if (lastPlayedCard != null)
        {
            val imageView = ImageView(lastPlayedCard.image)
            imageView.fitHeight = 100.0
            imageView.isPreserveRatio = true

            cardTilePane.children.add(imageView)
        }
    }

    /**
     * Runs on launch
     * Gets all cards, shuffles them, and deals them out to 4 players
     */
    override fun initialize(url: URL?, resourceBundle: ResourceBundle?)
    {
        invalidPlayLabel.text = ""
        updateTurnLabel()
        val resourcePath = javaClass.getResource("/com/example/daifugo/Cards")

        resourcePath?.let { path ->
            val dir = Paths.get(path.toURI())
            Files.list(dir).forEach { filePath ->
                val fileName = filePath.fileName.toString().removeSuffix(".png")
                val (value, suit) = parseFileName(fileName)
                deck.add(Card(value, suit, Image(filePath.toUri().toString())))
            }

            shuffleDeck()
            dealHands()
        }
    }

    /**
     * Shuffle deck
     */
    private fun shuffleDeck()
    {
        deck.shuffle()
    }

    /**
     * Deals 13 cards to each player and sorts them accordingly
     */
    private fun dealHands()
    {
        for (i in deck.indices)
        {
            when (i % 4)
            {
                0 -> player1Hand.add(deck[i])
                1 -> player2Hand.add(deck[i])
                2 -> player3Hand.add(deck[i])
                3 -> player4Hand.add(deck[i])
            }
        }

        player1Hand.sort()
        player2Hand.sort()
        player3Hand.sort()
        player4Hand.sort()
    }

    /**
     * Parse filename to extract card value and card suit
     */
    private fun parseFileName(fileName: String): Pair<String, String>
    {
        val parts = fileName.split("_of_")
        return Pair(parts[0], parts[1])
    }

    /**
     * Highlight the selected card
     */
    private fun selectCard(card: Card)
    {
        cardTilePane.children.filterIsInstance<ImageView>().find { it.image == card.image }?.apply {
            style = "-fx-effect: innershadow(gaussian, rgba(255, 215, 0, 0.8), 10, 0.0, 0, 0);"
        }
    }

    /**
     * Unhighlight selected card
     */
    private fun deselectCard(card: Card)
    {
        cardTilePane.children.filterIsInstance<ImageView>().find { it.image == card.image }?.apply {
            style = ""
        }
    }
}

/**
 * Card Class
 * stores value, suit, and image
 */
data class Card(val value: String, val suit: String, val image: Image) : Comparable<Card>
{
    private val valueOrder = listOf("3", "4", "5", "6", "7", "8", "9", "10", "jack", "queen", "king", "ace", "2")
    private val suitOrder = listOf("spades", "clubs", "diamonds", "hearts")

    override fun compareTo(other: Card): Int
    {
        val thisValueIndex = valueOrder.indexOf(this.value)
        val otherValueIndex = valueOrder.indexOf(other.value)

        if (thisValueIndex != otherValueIndex)
        {
            return thisValueIndex.compareTo(otherValueIndex)
        }

        val thisSuitIndex = suitOrder.indexOf(this.suit)
        val otherSuitIndex = suitOrder.indexOf(other.suit)

        return thisSuitIndex.compareTo(otherSuitIndex)
    }

    /**
     * Returns name of card
     */
    override fun toString(): String
    {
        return "$value of $suit"
    }
}


