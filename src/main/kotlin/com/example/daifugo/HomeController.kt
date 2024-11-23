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
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.URL
import java.util.*
import java.net.ServerSocket
import java.net.Socket
import java.net.DatagramSocket
import java.net.DatagramPacket
import java.net.InetAddress
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
    private var players = 0
    private var hosting = false
    private var serverSocket: ServerSocket? = null
    private var gameStarted = false
    private var clientSockets = mutableListOf<Socket>()

    /*********************** HOSTING A LOBBY ************************/
    @FXML
    fun lanHostLobbyButtonPress(event: ActionEvent)
    {
        if(!hosting) {
            println("Hosting lobby at $host:$port")

            try {
                // make server
                serverSocket = ServerSocket(port)
                errorLabel.text = "Server started on $host:$port"
                println("Server started on $host:$port")
                updatePlayersCount()
                //acceptClients()
            } catch (e: IOException) {
                errorLabel.text = "Error starting server: ${e.message}"
                println("Error starting server: ${e.message}")
            }
            //broadcastServer()

            //hide the playgameButton
            playGameButton.isVisible = false
            lanJoinLobbyButton.isVisible = false

            //change the text of the host lobby button to "cancel hosting"
            lanHostLobbyButton.text = "Cancel Hosting"

        }
        else{
            try{
                //server.close()
                //close the server socket
            }
            catch(e: IOException){
                errorLabel.text = "Error closing server: ${e.message}"
                println("Error closing server: ${e.message}")
            }
            //change the text of the host lobby button to "host lobby"
            lanHostLobbyButton.text = "Host Lobby"
            //show the playgameButton
            playGameButton.isVisible = true
            lanJoinLobbyButton.isVisible = true
        }
        hosting = !hosting
    }



    /**
     * Pressed when the host wants to start the game
     */
    @FXML
    fun lanStartGameButtonPress(event: ActionEvent)
    {
        gameStarted = true
        println("Starting game...")
        //broadcast to all clients that the game has started
        clientSockets.forEach { socket ->
            try {
                val outputStream = ObjectOutputStream(socket.getOutputStream())
                outputStream.writeObject("start")
                outputStream.flush()
            } catch (e: IOException) {
                println("Error sending start message: ${e.message}")
            }
        }
    }

    private fun acceptClients() {
        println("Accepting clients...")
        while (!gameStarted){
            val clientSocket = serverSocket?.accept()
            println("Client connected: ${clientSocket?.remoteSocketAddress}")
            updatePlayersCount()
            broadcastPlayerCount()
        }
    }

    private fun broadcastPlayerCount() {
        clientSockets.forEach { socket ->
            try {
                val outputStream = ObjectOutputStream(socket.getOutputStream())
                outputStream.writeObject(players)
                outputStream.flush()
            } catch (e: IOException) {
                println("Error sending player count: ${e.message}")
            }
        }
        updatePlayersCount()
    }

    /************************** JOINING A GAME **************************/

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
            val socket = Socket(host, port)

            println("Connected to server at $host:$port")
            lanPlayersLabel.text = "${players}/4"
        }
        catch (e: IOException) {
            errorLabel.text = "Error joining server: ${e.message}"
            println("Error joining server: ${e.message}")
        }
    }

    fun clientSideConnection() {
        val socket = Socket(host, port)
        val inputStream = ObjectInputStream(socket.getInputStream())
        val players = inputStream.readObject() as Int
        println("Received player count: $players")
    }

    /***************************** OTHER *******************************/
    /**
     * Update player count
     */
    fun updatePlayersCount()
    {
        players++
        println("Players: $players")

        // Use Platform.runLater to update the UI on the JavaFX thread
        Platform.runLater {
            lanPlayersLabel.text = "${players}/4"
        }
    }

    fun broadcastServer() {
        val localHost = InetAddress.getLocalHost()
        println("Local host: $localHost")

        val socket = DatagramSocket()
        val message = "GameServer"
        val packet = DatagramPacket(
            message.toByteArray(),
            message.length,
            InetAddress.getByName("255.255.255.255"),
            port
        )
        socket.send(packet)
        println("Broadcasted on ")
        socket.close()
    }

    fun listenForBroadcast() {
        val socket = DatagramSocket(port)  // Listen on the same port used for broadcasting
        val buffer = ByteArray(1024)  // Buffer to store incoming messages
        val packet = DatagramPacket(buffer, buffer.size)

        println("Listening for broadcasts on port 8888...")
        while (true) {
            socket.receive(packet)  // Wait for a broadcast message
            val message = String(packet.data, 0, packet.length)  // Extract the message
            println("Received message: '$message' from ${packet.address.hostAddress}")
        }
    }
}