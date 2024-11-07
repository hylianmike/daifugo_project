package com.example.daifugo

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.TilePane
import javafx.scene.layout.HBox
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import javafx.animation.TranslateTransition
import javafx.util.Duration

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
    private val selectedStraightCards = mutableListOf<Card>()

    private val playedCards = mutableListOf<Card>()
    private var currentPlayerIndex = 0
    private val playerHands = listOf(player1Hand, player2Hand, player3Hand, player4Hand)
    private var lastPressedButton: Button? = null
    private var currentScreen = 0
    private var passedPlayersCount = 0
    private val passedPlayers = mutableSetOf<Int>()
    private var moveType = 0
    private var straightSize = 0
    private var winner = -1
    private var ranking = 1

    private lateinit var playerNames: List<String>

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

    @FXML
    private lateinit var spadeCards: HBox

    @FXML
    private lateinit var clubCards: HBox

    @FXML
    private lateinit var heartCards: HBox

    @FXML
    private lateinit var diamondCards: HBox

    /**
     * Update text for who's turn it is
     */
    private fun updateTurnLabel() {
        turnLabel.text = "${playerNames[currentPlayerIndex]}'s Turn"
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
        invalidPlayLabel.text = ""

        currentHand.forEachIndexed { index, card ->
            val imageView = ImageView(card.image)
            imageView.fitHeight = 100.0
            imageView.isPreserveRatio = true
            imageView.translateX = 800.0
            imageView.styleClass.add("card-image")

            // animation
            val transition = TranslateTransition(Duration.millis(400.0), imageView)
            transition.fromX = 800.0
            transition.toX = 0.0
            transition.delay = Duration.millis(75.0 * index)
            transition.play()

            // most click event for cards
            imageView.setOnMouseClicked{
                //deselect
                selectedCard?.let{
                    deselectCard(it, 1)
                }
                // select
                selectedCard = card
                selectCard(card, 1)
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
        selectedPairCards.clear()
        currentScreen = 2
        setButtonPressedColour(twoOfAKindButton)
        cardTilePane.children.clear()
        invalidPlayLabel.text = ""

        val currentHand = playerHands[currentPlayerIndex]
        val pairs = currentHand.groupBy { it.value }.filter { it.value.size >= 2 }
        var index = 0

        pairs.forEach { (_, cards) ->
            if (cards.size >= 2)
            {
                for (i in 0 until 2)
                {
                    val card = cards[i]
                    val imageView = ImageView(card.image)

                    imageView.fitHeight = 100.0
                    imageView.isPreserveRatio = true
                    imageView.translateX = 800.0
                    imageView.styleClass.add("card-image")

                    // animation
                    val transition = TranslateTransition(Duration.millis(400.0), imageView)
                    transition.fromX = 800.0
                    transition.toX = 0.0
                    transition.delay = Duration.millis(75.0 * index)
                    transition.play()

                    // mouse click event for cards
                    imageView.setOnMouseClicked {
                        // deselect
                        if (selectedPairCards.contains(card))
                        {
                            deselectCard(card, 2)
                            selectedPairCards.remove(card)
                        }
                        // select
                        else if (selectedPairCards.size < 2)
                        {
                            selectCard(card, 2)
                            selectedPairCards.add(card)
                        }
                    }

                    cardTilePane.children.add(imageView)
                    index++
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
        selectedStraightCards.clear()
        invalidPlayLabel.text = ""

        val currentHand = playerHands[currentPlayerIndex]
        val uniqueValues = currentHand.map { it.value }.distinct()
        val straights = mutableListOf<List<Card>>()
        var currentStraight = mutableListOf<Card>()
        var index = 0

        for (i in uniqueValues.indices)
        {
            val currentValue = uniqueValues[i]
            val card = currentHand.find { it.value == currentValue }

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
                imageView.translateX = 800.0
                imageView.styleClass.add("card-image")

                // animation
                val transition = TranslateTransition(Duration.millis(400.0), imageView)
                transition.fromX = 800.0
                transition.toX = 0.0
                transition.delay = Duration.millis(75.0 * index)
                transition.play()

                // mouse click event for cards
                imageView.setOnMouseClicked {
                    // deselect
                    if (selectedStraightCards.contains(card))
                    {
                        deselectCard(card, 1)
                        selectedStraightCards.remove(card)
                    }
                    // select
                    else
                    {
                        selectedStraightCards.add(card)
                        selectCard(card, 1)
                    }
                }

                cardTilePane.children.add(imageView)
                index++
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
        invalidPlayLabel.text = ""

        val currentHand = playerHands[currentPlayerIndex]
        val triplets = currentHand.groupBy { it.value }.filter { it.value.size >= 3 }
        var index = 0

        triplets.forEach { (_, cards) ->
            for (i in 0 until 3) {
                val card = cards[i]
                val imageView = ImageView(card.image)
                imageView.fitHeight = 100.0
                imageView.isPreserveRatio = true
                imageView.translateX = 800.0
                imageView.styleClass.add("card-image")

                // animation
                val transition = TranslateTransition(Duration.millis(400.0), imageView)
                transition.fromX = 800.0
                transition.toX = 0.0
                transition.delay = Duration.millis(75.0 * index)
                transition.play()

                // mouse click event for cards
                imageView.setOnMouseClicked {
                    // deselect
                    if (selectedTripletCards.contains(card))
                    {
                        deselectCard(card, 3)
                        selectedTripletCards.remove(card)
                    }
                    // select
                    else if (selectedTripletCards.size < 3)
                    {
                        selectedTripletCards.add(card)
                        selectCard(card, 3)
                    }
                }

                cardTilePane.children.add(imageView)
                index++
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
        invalidPlayLabel.text = ""

        val currentHand = playerHands[currentPlayerIndex]
        val quads = currentHand.groupBy { it.value }.filter { it.value.size >= 4 }
        var index = 0

        quads.forEach { (_, cards) ->
            for (i in 0 until 4) {
                val card = cards[i]
                val imageView = ImageView(card.image)
                imageView.fitHeight = 100.0
                imageView.isPreserveRatio = true
                imageView.translateX = 800.0
                imageView.styleClass.add("card-image")

                // animation
                val transition = TranslateTransition(Duration.millis(400.0), imageView)
                transition.fromX = 800.0
                transition.toX = 0.0
                transition.delay = Duration.millis(75.0 * index)
                transition.play()

                // mouse click event for cards
                imageView.setOnMouseClicked {
                    // deselect
                    if (selectedQuadCards.contains(card))
                    {
                        deselectCard(card, 4)
                        selectedQuadCards.remove(card)
                    }
                    // select
                    else if (selectedQuadCards.size < 4)
                    {
                        selectedQuadCards.add(card)
                        selectCard(card, 4)
                    }
                }

                cardTilePane.children.add(imageView)
                index++
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
        val animationDuration = Duration.millis(400.0)
        invalidPlayLabel.text = ""

        // singles round
        if (moveType == 1)
        {
            val lastPlayedCard = playedCards.lastOrNull()

            if (lastPlayedCard != null)
            {
                val imageView = ImageView(lastPlayedCard.image)
                imageView.fitHeight = 100.0
                imageView.isPreserveRatio = true
                imageView.translateX = 800.0

                // animation
                val transition = TranslateTransition(animationDuration, imageView)
                transition.fromX = 800.0
                transition.toX = 0.0
                transition.play()

                cardTilePane.children.add(imageView)
            }
        }
        // doubles round
        else if (moveType == 2)
        {
            if (playedCards.size >= 2)
            {
                val lastPlayedPair = playedCards.takeLast(2)
                lastPlayedPair.forEachIndexed { index, card ->
                    val imageView = ImageView(card.image)
                    imageView.fitHeight = 100.0
                    imageView.isPreserveRatio = true
                    imageView.translateX = 800.0

                    // animation
                    val transition = TranslateTransition(animationDuration, imageView)
                    transition.fromX = 800.0
                    transition.toX = 0.0
                    transition.delay = Duration.millis(75.0 * index)
                    transition.play()

                    cardTilePane.children.add(imageView)
                }
            }
        }
        // straights round
        else if (moveType == 3)
        {
            if (playedCards.size >= straightSize)
            {
                val lastPlayedStraight = playedCards.takeLast(straightSize).sortedBy { valueOrder.indexOf(it.value) }

                lastPlayedStraight.forEachIndexed() { index, card ->
                    val imageView = ImageView(card.image)
                    imageView.fitHeight = 100.0
                    imageView.isPreserveRatio = true
                    imageView.translateX = 800.0

                    // animation
                    val transition = TranslateTransition(animationDuration, imageView)
                    transition.fromX = 800.0
                    transition.toX = 0.0
                    transition.delay = Duration.millis(75.0 * index)
                    transition.play()

                    cardTilePane.children.add(imageView)
                }
            }
        }
        // triples move
        else if (moveType == 4)
        {
            if (playedCards.size >= 3)
            {
                val lastPlayedTriplet = playedCards.takeLast(3)

                lastPlayedTriplet.forEachIndexed() { index, card ->
                    val imageView = ImageView(card.image)
                    imageView.fitHeight = 100.0
                    imageView.isPreserveRatio = true
                    imageView.translateX = 800.0

                    // animation
                    val transition = TranslateTransition(animationDuration, imageView)
                    transition.fromX = 800.0
                    transition.toX = 0.0
                    transition.delay = Duration.millis(75.0 * index)
                    transition.play()

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

                lastPlayedQuad.forEachIndexed() { index, card ->
                    val imageView = ImageView(card.image)
                    imageView.fitHeight = 100.0
                    imageView.isPreserveRatio = true
                    imageView.translateX = 800.0

                    // animation
                    val transition = TranslateTransition(animationDuration, imageView)
                    transition.fromX = 800.0
                    transition.toX = 0.0
                    transition.delay = Duration.millis(75.0 * index)
                    transition.play()

                    cardTilePane.children.add(imageView)
                }
            }
        }

        for (card in playedCards)
        {
            when(card.suit)
            {
                "spades" -> spadeCards.children[valueOrder.indexOf(card.value)].opacity = 0.25
                "clubs" -> clubCards.children[valueOrder.indexOf(card.value)].opacity = 0.25
                "hearts" -> heartCards.children[valueOrder.indexOf(card.value)].opacity = 0.25
                "diamonds" -> diamondCards.children[valueOrder.indexOf(card.value)].opacity = 0.25
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
        moveType = 0
        straightSize = 0

        updateTurnLabel()
        updateViewDeck()

        invalidPlayLabel.text = "Round complete! ${playerNames[currentPlayerIndex]} begins next round now."
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

                if (playerHands[currentPlayerIndex].size == 0)
                    winner = currentPlayerIndex+1

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

            if (playerHands[currentPlayerIndex].size == 0)
                winner = currentPlayerIndex+1

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

            if (selectedStraightCards.size < 3)
            {
                invalidPlayLabel.text = "Error: At least three cards must be selected."
                return
            }

            val sortedSelectedCards = selectedStraightCards.sortedBy { valueOrder.indexOf(it.value) }

            for (i in 0 until sortedSelectedCards.size - 1)
            {
                val currentIndex = valueOrder.indexOf(sortedSelectedCards[i].value)
                val nextIndex = valueOrder.indexOf(sortedSelectedCards[i + 1].value)

                if (nextIndex != currentIndex + 1)
                {
                    invalidPlayLabel.text = "Error: Selected cards do not form a valid straight."
                    return
                }
            }

            if (straightSize >= 3)
            {
                if (selectedStraightCards.size != straightSize)
                {
                    invalidPlayLabel.text = "Error: Straight must be the same size as the deck."
                    return
                }

                val lastPlayedStraight = playedCards.takeLastWhile { valueOrder.contains(it.value) }
                val lastHighestCard = lastPlayedStraight.maxWithOrNull(compareBy({ valueOrder.indexOf(it.value) }, { suitOrder.indexOf(it.suit) }))
                val selectedHighestCard = selectedStraightCards.maxWithOrNull(compareBy({ valueOrder.indexOf(it.value) }, { suitOrder.indexOf(it.suit) }))

                if (selectedHighestCard != null && lastHighestCard != null)
                {
                    val selectedValueIndex = valueOrder.indexOf(selectedHighestCard.value)
                    val lastValueIndex = valueOrder.indexOf(lastHighestCard.value)

                    if (selectedValueIndex < lastValueIndex || (selectedValueIndex == lastValueIndex && suitOrder.indexOf(selectedHighestCard.suit) <= suitOrder.indexOf(lastHighestCard.suit)))
                    {
                        invalidPlayLabel.text = "Error: You must play a higher value straight."
                        return
                    }
                }
            }

            // ALL CHECKS PASS //

            moveType = 3
            straightSize = selectedStraightCards.size
            playedCards.addAll(selectedStraightCards)
            playerHands[currentPlayerIndex].removeAll(selectedStraightCards)
            selectedStraightCards.clear()

            if (playerHands[currentPlayerIndex].size == 0)
                winner = currentPlayerIndex+1

            do {
                currentPlayerIndex = (currentPlayerIndex + 1) % playerHands.size
            } while (currentPlayerIndex in passedPlayers)

            updateViewDeck()
            updateTurnLabel()
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

            if (playerHands[currentPlayerIndex].size == 0)
                winner = currentPlayerIndex+1

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

            if (playerHands[currentPlayerIndex].size == 0)
                winner = currentPlayerIndex+1

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

        // if a player has won, display a message saying so
        if (winner > 0) {
            val suffix = when (ranking) {
                1 -> "st"
                2 -> "nd"
                3 -> "rd"
                else -> "th"
            }
            invalidPlayLabel.text = "Player $winner wins $ranking$suffix place!"
            ranking++
            winner = -1
        }
    }

    /**
     * Runs on launch
     * Gets all cards, shuffles them, and deals them out to 4 players
     */
    override fun initialize(url: URL?, resourceBundle: ResourceBundle?)
    {
        invalidPlayLabel.text = ""
        setButtonPressedColour(viewDeckButton)
        playerNames = listOf("Player 1", "Player 2", "Player 3", "Player 4")
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
     * Highlight the selected card, grey out the others
     */
    private fun selectCard(card: Card, cardsToPlayCount: Int)
    {
        cardTilePane.children.filterIsInstance<ImageView>().find { it.image == card.image }?.apply {
            style = "-fx-effect: innershadow(gaussian, rgba(255, 215, 0, 0.8), 10, 0.0, 0, 0);"
            opacity = 1.0
        }

        // return count of how many cards have been selected
        val innershadowCount = cardTilePane.children.filterIsInstance<ImageView>().count { it.style.contains("innershadow") }
        // if user has selected the number of cards for their play type (ex. they have selected 3 cards and are playing 3 of a kind),
        //  the non-selected cards become greyed out
        if (innershadowCount >= cardsToPlayCount) {
            cardTilePane.children.filterIsInstance<ImageView>().filterNot { it.style.contains("innershadow") }.forEach {
                it.opacity = 0.8
            }
        }
    }

    /**
     * Unhighlight selected card, un-grey out the non-selected cards
     */
    private fun deselectCard(card: Card, cardsToPlayCount: Int)
    {
        cardTilePane.children.filterIsInstance<ImageView>().find { it.image == card.image }?.apply {
            style = ""
            opacity = 0.8
        }

        // return count of how many cards have been selected
        val innershadowCount = cardTilePane.children.filterIsInstance<ImageView>().count { it.style.contains("innershadow") }
        // If the number of cards with the inner shadow effect is less than the total cards to be played, remove the greyed-out effect
        if (cardTilePane.children.filterIsInstance<ImageView>().count { it.style.contains("innershadow") } < cardsToPlayCount) {
            cardTilePane.children.filterIsInstance<ImageView>().filterNot { it.style.contains("innershadow") }.forEach {
                    it.opacity = 1.0
            }
        }
    }

    fun setPlayerNames(p1Name: String, p2Name: String, p3Name: String, p4Name: String){
        playerNames = listOf(p1Name.ifEmpty { "Player 1" }, p2Name.ifEmpty { "Player 2" }, p3Name.ifEmpty { "Player 3" }, p4Name.ifEmpty { "Player 4" })
        updateTurnLabel()
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


