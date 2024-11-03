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
    private val selectedPairCards = mutableListOf<Card>()
    private val selectedTripletCards = mutableListOf<Card>()
    private val selectedQuadCards = mutableListOf<Card>()

    private val playedCards = mutableListOf<Card>()
    private var currentPlayerIndex = 0
    private val playerHands = listOf(player1Hand, player2Hand, player3Hand, player4Hand)
    private var lastPressedButton: Button? = null
    private var currentScreen = 0
    private var passedPlayersCount = 0
    private val passedPlayers = mutableSetOf<Int>()
    private var moveType = 0

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
                selectedCard?.let {
                    deselectCard(it)
                }

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
        selectedPairCards.clear() // Clear any previously selected cards
        currentScreen = 2
        setButtonPressedColour(twoOfAKindButton)
        cardTilePane.children.clear()

        val currentHand = playerHands[currentPlayerIndex]
        val pairs = currentHand.groupBy { it.value }.filter { it.value.size >= 2 }

        pairs.forEach { (value, cards) ->
            if (cards.size >= 2) {
                for (i in 0 until 2) {
                    val card = cards[i]
                    val imageView = ImageView(card.image)
                    imageView.fitHeight = 100.0
                    imageView.isPreserveRatio = true

                    // mouse click event for cards
                    imageView.setOnMouseClicked { event ->
                        // deselect
                        if (selectedPairCards.contains(card))
                        {
                            deselectCard(card)
                            selectedPairCards.remove(card)
                        }
                        // select card if 2 are not already selected
                        else if (selectedPairCards.size < 2)
                        {
                            selectCard(card)
                            selectedPairCards.add(card)
                        }
                    }

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

        selectedTripletCards.clear()

        val currentHand = playerHands[currentPlayerIndex]
        val triplets = currentHand.groupBy { it.value }.filter { it.value.size >= 3 }

        triplets.forEach { (_, cards) ->
            for (i in 0 until 3) {
                val card = cards[i]
                val imageView = ImageView(card.image)
                imageView.fitHeight = 100.0
                imageView.isPreserveRatio = true

                // mouse click event for cards
                imageView.setOnMouseClicked {
                    // deselect
                    if (selectedTripletCards.contains(card))
                    {
                        deselectCard(card)
                        selectedTripletCards.remove(card)
                    }
                    // select
                    else if (selectedTripletCards.size < 3)
                    {
                        selectedTripletCards.add(card)
                        selectCard(card)
                    }
                }

                cardTilePane.children.add(imageView)
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
        selectedQuadCards.clear()

        val currentHand = playerHands[currentPlayerIndex]
        val quads = currentHand.groupBy { it.value }.filter { it.value.size >= 4 }

        quads.forEach { (_, cards) ->
            for (i in 0 until 4) {
                val card = cards[i]
                val imageView = ImageView(card.image)
                imageView.fitHeight = 100.0
                imageView.isPreserveRatio = true

                // mouse click event for cards
                imageView.setOnMouseClicked {
                    // deselect
                    if (selectedQuadCards.contains(card))
                    {
                        deselectCard(card)
                        selectedQuadCards.remove(card)
                    }
                    // select
                    else if (selectedQuadCards.size < 4)
                    {
                        selectedQuadCards.add(card)
                        selectCard(card)
                    }
                }

                cardTilePane.children.add(imageView)
            }
        }
    }

    /**
     * Display cards that have been played on button press
     */
    @FXML
    fun viewDeckButtonPress(event: ActionEvent)
    {
        updateViewDeck()
    }

    /**
     * Update what the player sees on the deck
     */
    private fun updateViewDeck()
    {
        setButtonPressedColour(viewDeckButton)
        currentScreen = 0
        cardTilePane.children.clear()
        invalidPlayLabel.text = ""

        // singles round
        if (moveType == 1) {  // Single card
            val lastPlayedCard = playedCards.lastOrNull()
            if (lastPlayedCard != null) {
                val imageView = ImageView(lastPlayedCard.image)
                imageView.fitHeight = 100.0
                imageView.isPreserveRatio = true
                cardTilePane.children.add(imageView)
            }
        }
        // doubles round
        else if (moveType == 2)
        {
            if (playedCards.size >= 2)
            {
                val lastPlayedPair = playedCards.takeLast(2)
                lastPlayedPair.forEach { card ->
                    val imageView = ImageView(card.image)
                    imageView.fitHeight = 100.0
                    imageView.isPreserveRatio = true
                    cardTilePane.children.add(imageView)
                }
            }
        }
        // straights move
        else if (moveType == 3)
        {

        }
        // triples move
        else if (moveType == 4)
        {
            if (playedCards.size >= 3)
            {
                val lastPlayedTriplet = playedCards.takeLast(3)

                lastPlayedTriplet.forEach { card ->
                    val imageView = ImageView(card.image)
                    imageView.fitHeight = 100.0
                    imageView.isPreserveRatio = true
                    cardTilePane.children.add(imageView)
                }
            }
        }
        // quads move
        else if (moveType == 5)
        {
            if (playedCards.size >= 4)
            {
                val lastPlayedQuad = playedCards.takeLast(4)

                lastPlayedQuad.forEach { card ->
                    val imageView = ImageView(card.image)
                    imageView.fitHeight = 100.0
                    imageView.isPreserveRatio = true
                    cardTilePane.children.add(imageView)
                }
            }
        }
    }

    /**
     * Skip players turn on button press until next round
     */
    @FXML
    fun passButtonPress(event: ActionEvent?)
    {
        if (currentPlayerIndex !in passedPlayers)
        {
            passedPlayers.add(currentPlayerIndex)
            passedPlayersCount++

            if (passedPlayersCount >= (playerHands.size - 1))
            {
                endRound()
                return
            }

            do {
                currentPlayerIndex = (currentPlayerIndex + 1) % playerHands.size
            } while (currentPlayerIndex in passedPlayers)

            updateTurnLabel()
            updateViewDeck()
        }
    }

    /**
     * Ends round once 3 players have passes
     * Lets the last remaining player start the next round
     */
    private fun endRound()
    {
        currentPlayerIndex = (0 until playerHands.size).first { it !in passedPlayers }

        playedCards.clear()
        passedPlayers.clear()
        passedPlayersCount = 0

        updateTurnLabel()
        updateViewDeck()
        moveType = 0
    }


    /**
     * Play a card and go to next turn on button press
     * Card values in order
     * 3,4,5,6,7,8,9,10,j,k,q,a,2
     * spades, clubs, diamonds, hearts
     */
    @FXML
    fun playButtonPress(event: ActionEvent?)
    {
        // singles
        if (currentScreen == 1)
        {
            if (moveType == 2 || moveType == 3 || moveType == 4 || moveType == 5)
            {
                invalidPlayLabel.text = "Error: You can't play a single."
                return
            }

            if (selectedCard == null)
            {
                invalidPlayLabel.text = "Error: No cards selected."
                return
            }

            selectedCard?.let { card ->
                // check if the card played is valid against the played cards
                if (playedCards.isNotEmpty()) {
                    val lastPlayedCard = playedCards.last()
                    if (card <= lastPlayedCard) {
                        invalidPlayLabel.text = "Error: You must play a higher card."
                        return
                    }
                }

                // ALL CHECKS PASS //

                moveType = 1
                playedCards.add(card)
                playerHands[currentPlayerIndex].remove(card)
                selectedCard = null

                do {
                    currentPlayerIndex = (currentPlayerIndex + 1) % playerHands.size
                } while (currentPlayerIndex in passedPlayers)

                updateTurnLabel()
                setButtonPressedColour(viewDeckButton)
                updateViewDeck()
            }
        }
        // doubles
        else if (currentScreen == 2)
        {
            if (moveType == 1 || moveType == 3 || moveType == 4 || moveType == 5)
            {
                invalidPlayLabel.text = "Error: You can't play two of a kind"
                return
            }

            if (selectedPairCards.size != 2)
            {
                invalidPlayLabel.text = "Error: Two cards must be selected."
                return
            }

            val (card1, card2) = selectedPairCards

            if (card1.value != card2.value)
            {
                invalidPlayLabel.text = "Error: Cards are not the same."
                return
            }

            // check if pair is better than the last played pair
            if (playedCards.size >= 2)
            {
                val lastPlayedCard1 = playedCards[playedCards.size - 2]
                val lastPlayedCard2 = playedCards[playedCards.size - 1]

                val selectedValueIndex = valueOrder.indexOf(card1.value)
                val lastPlayedValueIndex = valueOrder.indexOf(lastPlayedCard1.value)

                // check by value
                if (selectedValueIndex < lastPlayedValueIndex)
                {
                    invalidPlayLabel.text = "Error: You must play a higher pair."
                    return
                }

                // check suit
                if (selectedValueIndex == lastPlayedValueIndex)
                {
                    val selectedHighestSuitIndex = maxOf(
                        suitOrder.indexOf(card1.suit),
                        suitOrder.indexOf(card2.suit)
                    )
                    val lastPlayedHighestSuitIndex = maxOf(
                        suitOrder.indexOf(lastPlayedCard1.suit),
                        suitOrder.indexOf(lastPlayedCard2.suit)
                    )

                    if (selectedHighestSuitIndex < lastPlayedHighestSuitIndex) {
                        invalidPlayLabel.text = "Error: You must play a higher pair."
                        return
                    }
                }
            }

            // ALL CHECKS PASS //

            moveType = 2

            playedCards.addAll(selectedPairCards)
            playerHands[currentPlayerIndex].removeAll(selectedPairCards)
            selectedPairCards.clear()

            do {
                currentPlayerIndex = (currentPlayerIndex + 1) % playerHands.size
            } while (currentPlayerIndex in passedPlayers)

            updateViewDeck()
            updateTurnLabel()
        }
        // straight
        else if (currentScreen == 3)
        {
            if (moveType == 2 || moveType == 1 || moveType == 4 || moveType == 5)
            {
                invalidPlayLabel.text = "Error: You can't play a straight."
                return
            }
        }
        // triples
        else if (currentScreen == 4)
        {
            if (moveType == 2 || moveType == 3 || moveType == 1 || moveType == 5)
            {
                invalidPlayLabel.text = "Error: You can't play three of a kind."
                return
            }

            if (selectedTripletCards.size != 3)
            {
                invalidPlayLabel.text = "Error: Three cards must be selected."
                return
            }

            val (card1, card2, card3) = selectedTripletCards

            if (!(card1.value == card2.value && card2.value == card3.value))
            {
                invalidPlayLabel.text = "Error: All cards must have the same value."
                return
            }

            if (playedCards.size >= 3)
            {
                val lastPlayedCard1 = playedCards[playedCards.size - 3]
                val lastPlayedCard2 = playedCards[playedCards.size - 2]
                val lastPlayedCard3 = playedCards[playedCards.size - 1]

                val selectedValueIndex = valueOrder.indexOf(card1.value)
                val lastPlayedValueIndex = valueOrder.indexOf(lastPlayedCard1.value)

                if (selectedValueIndex < lastPlayedValueIndex)
                {
                    invalidPlayLabel.text = "Error: You must play a higher three of a kind."
                    return
                }
            }

            // ALL CHECKS PASS //

            moveType = 4

            playedCards.addAll(selectedTripletCards)
            playerHands[currentPlayerIndex].removeAll(selectedTripletCards)
            selectedTripletCards.clear()

            do {
                currentPlayerIndex = (currentPlayerIndex + 1) % playerHands.size
            } while (currentPlayerIndex in passedPlayers)

            updateViewDeck()
            updateTurnLabel()

        }
        // quad
        else if (currentScreen == 5)
        {
            if (moveType == 2 || moveType == 3 || moveType == 4 || moveType == 1)
            {
                invalidPlayLabel.text = "Error: You can't play four of a kind."
                return
            }

            if (selectedQuadCards.size != 4)
            {
                invalidPlayLabel.text = "Error: Four cards must be selected."
                return
            }

            val (card1, card2, card3, card4) = selectedQuadCards

            if (!(card1.value == card2.value && card2.value == card3.value && card3.value == card4.value))
            {
                invalidPlayLabel.text = "Error: All cards must have the same value."
                return
            }

            if (playedCards.size >= 4)
            {
                val lastPlayedCard1 = playedCards[playedCards.size - 4]
                val lastPlayedCard2 = playedCards[playedCards.size - 3]
                val lastPlayedCard3 = playedCards[playedCards.size - 2]
                val lastPlayedCard4 = playedCards[playedCards.size - 1]

                val selectedValueIndex = valueOrder.indexOf(card1.value)
                val lastPlayedValueIndex = valueOrder.indexOf(lastPlayedCard1.value)

                if (selectedValueIndex < lastPlayedValueIndex)
                {
                    invalidPlayLabel.text = "Error: You must play a higher four of a kind."
                    return
                }
            }

            // ALL CHECKS PASS //

            moveType = 5

            playedCards.addAll(selectedQuadCards)
            playerHands[currentPlayerIndex].removeAll(selectedQuadCards)
            selectedQuadCards.clear()

            do {
                currentPlayerIndex = (currentPlayerIndex + 1) % playerHands.size
            } while (currentPlayerIndex in passedPlayers)

            updateViewDeck()
            updateTurnLabel()
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


