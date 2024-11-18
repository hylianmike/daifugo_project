# COMP-4411 Programming Languages - Final Project: Daifugo
*By [Michael Rosanelli](https://github.com/hylianmike), [Jackson Douma](https://github.com/JackDouma), [Chris Veilleux](https://github.com/chris-veilleux), and [Liam Slappendel](https://github.com/LiamSlappendel)*

# Project Description
### What is our project?
Our project is based on the card game *Daifugo*, which is a 4 player turn based game. We used Kotlin and JavaFX to develop our project, and it is played locally on 1 device. We began implementing the ability to play the game on multiple devices over LAN, listed as one of the “if time permits” features on our project proposal, and while significant progress was made and remains in the source code, it is not fully functioning at this time.
### Why did we chose to make this project?
We found out about the *Daifugo* card game during our time as 4th years during a large time gap in between classes. We would play it often, and it quickly became one of our favourite things to do. A thought many of us had was why not make it in code, since we’re all computer science students, and have some experience making games? So we figured this would be as good a time as ever to finally commit to doing it.
### Gameplay/Rules
* The goal of the game is to play all 13 first before any of your opponents.
* On the opening turn, the player can play any single card, a pair, 3 of a kind, 4 of a kind, or a straight of 3 or greater.
* The following players must then play the same type of combination of cards(e.x if the first player plays a pair, all other plays must be a pair during the round), but they must be of greater value.
* The order of card value is 3,4,5,6,7,8,9,10,jack,queen,king,ace,2
* The order of suit value is spade, club, diamond, heart
* Once no players are able to play or choose not to play, the round ends and the next one begins with the player to play the last card to get the opening move
* We also added the unlikely circumstances into the game, which allows a player to “Kill a Piggy”, or eliminate a player that placed a 2, if they have 4 of a kind or 3 pairs in a straight.

# Project Dependencies
### Core Dependencies
* Kotlin 1.5.31
* JavaFX 16
* BootstrapFX 0.4.0
### Build and Compilation Plugins
* Maven Compiler Plugin 3.8.1
* Kotlin Maven Plugin 1.5.31
* JavaFX Maven Plugin 0.0.8

# Running the Game
1. Clone the repository to your local machine (must have access to this repository on GitHub), or download the code manually, and open in the IntelliJ IDEA IDE:
   ```
   git clone https://github.com/hylianmike/daifugo_project.git
   ```
2. To build and run the project in IntelliJ IDEA, you simply need to open the Main.kt file (src/main/kotlin/com/example/daifugo/Main.kt) and run the main() function:

   *insert screenshot of play button in intellij beside the main function*
   
   Or press the play button at the top of the IDE:
   
   *insert screenshot of play button in intellij at the top of the IDE*

# Playing the Game
### Home scene
*When the program launches, the Home scene is shown.*

*insert annotated (with numbers) screenshot of the home scene pointing out the location of all the things described below*

1. *Daifugo* goes by many different names depending on your region.
2. Inputs for the 4 players’ names.
3. Button to start the local game, navigating to the Game scene.
4. Player name input, button to join an existing lobby, button to host a new lobby, and button to start a game with other devices over LAN (feature not fully functioning).

### Game scene
*The Game scene is where the bulk of the game is played.*

*insert annotated (with numbers) screenshot of the game scene pointing out the location of all the things described below*

1. Message informs the players who’s turn it is.
2. Message shows what cards are yet to be played, greying out cards that have been played.
3. View Deck button shows what cards are currently on the table, or what cards were played by the previous player. At the start of a round, no cards will be shown.
4. Play options buttons show what cards a player has in their hand, organized by play types (singles, two of a kind, etc.).
5. Player is to select the card(s) they wish to play by clicking on them. Selected cards become highlighted, and once you have selected enough cards to constitute a valid play, the other cards become greyed out.
6. Play button plays the cards the player has selected and advances to the next player’s turn. If the player is attempting to play a sequence that violates the game rules, an applicable error message will appear at the bottom in red, and the player must revise their selection accordingly.
7. The pass button can be used to pass a turn, in the event that the player has no cards to play that would satisfy the game rules.

Once a player runs out of cards, they have won and an appropriate message is displayed.

*insert screenshot of game scene with winning message highlighted*

Gameplay continues according to the game rules until all but one user has run out of cards, at which time the program advances to the end scene.

### End scene
*The End scene is shown after the game is complete, displaying results of the game and options for starting a new one.*

*insert annotated (with numbers) screenshot of the end scene pointing out the location of all the things described below*

1. Rankings are awarded based on the order that players won in.
2. Play of the Game shows which player played the largest amount of cards at once, and what those cards were.
3. Main Menu button navigates back to the Home scene, allowing users to enter player names again and start a new game.
4. Play Again button navigates back to the Game scene, allowing the same players to begin a new game.
