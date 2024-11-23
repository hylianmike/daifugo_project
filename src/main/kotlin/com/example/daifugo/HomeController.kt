package com.example.daifugo

import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.stage.Stage
import java.io.IOException
import java.net.URL
import java.util.*
import kotlin.concurrent.thread

class HomeController: Initializable
{
    @FXML
    private lateinit var playGameButton: Button

    @FXML
    private lateinit var p1Field: TextField

    @FXML
    private lateinit var p2Field: TextField

    @FXML
    private lateinit var p3Field: TextField

    @FXML
    private lateinit var p4Field: TextField

    @FXML
    private lateinit var errorLabel: Label

    @FXML
    private lateinit var lanHostLobbyButton: Button

    @FXML
    private lateinit var lanJoinLobbyButton: Button

    @FXML
    private lateinit var lanNameLabel: TextField

    @FXML
    private lateinit var lanPlayersLabel: Label

    @FXML
    private lateinit var lanStartGameButton: Button

    ////////////////////
    //// LOCAL PLAY ////
    ////////////////////
    @FXML
    fun playGame(actionEvent: ActionEvent)
    {
        val fxmlLoader = FXMLLoader(GameApplication::class.java.getResource("game.fxml"))
        val scene = Scene(fxmlLoader.load())
        val controller: GameController = fxmlLoader.getController()
        controller.setPlayerNames(
            p1Field.textProperty().value,
            p2Field.textProperty().value,
            p3Field.textProperty().value,
            p4Field.textProperty().value
        )
        val stage = (actionEvent.source as Node).scene.window as Stage
        stage.scene = scene
        stage.show()
    }

    override fun initialize(p0: URL?, p1: ResourceBundle?) {}

    /////////////////////
    //// ONLINE PLAY ////
    /////////////////////

    private val port = 12345
    private val host = ""  // SET TO THE HOST IP
    private var server: Server? = null
    private var client: Client? = null
    private var players = 1
    private var hosting = false

    /**
     * On press, lobby is created
     */
    @FXML
    fun lanHostLobbyButtonPress(event: ActionEvent)
    {
        if(!hosting) {

            println("Hosting lobby at $host:$port")
            try {
                // make server
                server = Server(port, this)
                server?.start()
                errorLabel.text = "Server started on $host:$port"
                println("Server started on $host:$port")
                updatePlayersCount()
            } catch (e: IOException) {
                errorLabel.text = "Error starting server: ${e.message}"
                println("Error starting server: ${e.message}")
            }

            //hide the playgameButton
            playGameButton.isVisible = false
            lanJoinLobbyButton.isVisible = false

            //change the text of the host lobby button to "cancel hosting"
            lanHostLobbyButton.text = "Cancel Hosting"


        }
        else
        {
            //stop tha server and reset the UI
            errorLabel.text = "Server stopped"
            println("Server stopped")
            lanPlayersLabel.text = "0/4"
            players = 0
            playGameButton.isVisible = true
            lanJoinLobbyButton.isVisible = true
            lanHostLobbyButton.text = "Host Lobby"
        }
        hosting = !hosting
    }

    /**
     * On press, user joins lobby
     */
    @FXML
    fun lanJoinLobbyButtonPress(event: ActionEvent)
    {
        val playerName = lanNameLabel.text
        println("Trying to join lobby at $host:$port as $playerName")

        try {
            // initialize client and connect to host
            client = Client(host, port, this)
            client?.connect()
            println("Connected to server at $host:$port")
            lanPlayersLabel.text = "${players}/4"
        }
        catch (e: IOException) {
            errorLabel.text = "Error joining server: ${e.message}"
            println("Error joining server: ${e.message}")
        }
    }

    /**
     * Pressed when the host wants to start the game
     */
    @FXML
    fun lanStartGameButtonPress(event: ActionEvent)
    {

    }

    /**
     * Update player count
     */
    fun updatePlayersCount()
    {
        players++

        // Use Platform.runLater to update the UI on the JavaFX thread
        Platform.runLater {
            lanPlayersLabel.text = "${players}/4"
        }
    }
}