package com.example.daifugo

import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.io.ObjectOutputStream
import java.io.ObjectInputStream

class Server(private val port: Int, private val homeController: HomeController)
{
    private var serverSocket: ServerSocket? = null
    private val clientSockets = mutableListOf<Socket>()
    private var players = 1

    /**
     * This function attempts to create a lobby
     */
    fun start()
    {
        try {
            serverSocket = ServerSocket(port)
            println("Server started on port $port")
            // listen for client connections
            Thread {
                while (true)
                {
                    val clientSocket = serverSocket?.accept()
                    println("Client connected: ${clientSocket?.remoteSocketAddress}")
                    clientSockets.add(clientSocket!!)
                    players++
                    broadcastPlayerCount()
                }
            }.start()
        }
        catch (e: IOException) {
            println("Error starting server: ${e.message}")
        }
    }

    /**
     * Broadcast player count to all connected clients
     */
    private fun broadcastPlayerCount()
    {
        clientSockets.forEach { socket ->
            try {
                val outputStream = ObjectOutputStream(socket.getOutputStream())
                outputStream.writeObject(players)
                outputStream.flush()
            }
            catch (e: IOException) {
                println("Error sending player count: ${e.message}")
            }
        }

        homeController.updatePlayersCount()
    }
}