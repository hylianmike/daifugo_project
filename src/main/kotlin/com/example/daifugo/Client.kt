package com.example.daifugo

import javafx.application.Platform
import java.io.IOException
import java.net.Socket
import java.io.ObjectInputStream

class Client(private val host: String, private val port: Int, private val homeController: HomeController)
{
    private var clientSocket: Socket? = null

    /**
     * This function attempts to connect the user to the server
     */
    fun connect()
    {
        try {
            clientSocket = Socket(host, port)
            println("Connected to server at $host:$port")

            // listen for updates from server
            Thread {
                try {
                    val inputStream = ObjectInputStream(clientSocket?.getInputStream())
                    while (true)
                    {
                        val updatedPlayerCount = inputStream.readObject() as Int
                        println("Updated player count: $updatedPlayerCount")

                        Platform.runLater {
                            homeController.updatePlayersCount()
                        }
                    }
                }
                catch (e: IOException) {
                    println("Error receiving player count: ${e.message}")
                }
            }.start()

        }
        catch (e: IOException) {
            println("Error connecting to server: ${e.message}")
        }
    }
}