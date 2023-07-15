package com.example.tic_tac_toe

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.example.tic_tac_toe.databinding.FragmentGameBinding
import kotlinx.coroutines.*

/**
 * @author Benjamin Pearlstein
 */


import java.util.concurrent.TimeUnit

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

//Contains the types of Users
enum class USER_MARKER {USER_UNKNOWN, USER_IS_X , USER_IS_O}

//Contains the types of game progress in tic-tac-toe
enum class GAME_RESULTS {STILL_PLAYING ,X_WIN, O_WIN, CATS_GAME}

//Contains the types of game modes for the app
enum class GAME_MODE {EASY_MODE, MEDIUM_MODE, HARD_MODE, TWO_PLAYER_MODE }

//The location of a cell on the game board
class Cell (var row: Int, var column: Int)


class Game : Fragment() {

    //Used to start and maintain page
    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    // The game mode that user is currently playing in. It's data originates from the menu page
    private lateinit var gameType: GAME_MODE

    // Who player chose to play as.
    private lateinit var playerChoice: USER_MARKER

    // The player that is currently playing.
    private var currentPlayer: USER_MARKER = USER_MARKER.USER_IS_X

    // The game board data
    private var boardArray = Array(3){ Array(3) { USER_MARKER.USER_UNKNOWN } }

    //The array to hold all cells on the game board
    private lateinit var cellArray: Array<Array<Button>>

    // Allows the program to determine when the first turn occurs and ends.
    private var firstTurn = true

    // The amount of moves the computer has already made
    private var computerMoves = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            gameType = it.get("gameMode") as GAME_MODE
            playerChoice = it.get("playerChoice") as USER_MARKER
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
         _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     *  Creates the initial state of the game page
     *
     * @param view
     *  The view of the page
     *
     * @param savedInstanceState
     *  The saved state of the page
     *
     * Will always create game initial text, 2D binding array, and button listeners
     * if (Game Type == Two Player Mode), then twoPlayerMode()
     * else, then singlePlayerMode()
     */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Makes the initial top text
        initializeUserIndicator()

        //Creates 2D array of cell buttons from the game board
        createBindingArray()
        binding.apply {

            //resets the page and game data
            resetButton.setOnClickListener { resetPage() }

            //sends user back to menu page
            homePageButton.setOnClickListener { backToMenu() }
        }

        //Directs the user to single player or two player version based on the chosen game type
        if(gameType == GAME_MODE.TWO_PLAYER_MODE){
            startTwoPlayerGame()
        }else{
            startSinglePlayerGame()
        }
    }

    /**
     * Creates the initial text for the game page
     *
     * if (Game Type == Two Player Mode), then "Player X is up!" in red
     * else, then "You are up!" depending on Player's Choice. "X" is red and "O" is blue
     */
    //Sets the top text of the page based on the chosen game type
    private fun initializeUserIndicator(){
        if(gameType == GAME_MODE.TWO_PLAYER_MODE){
            binding.userIndicator.text = getString(R.string.two_player_xup)
            setUserIndicatorAppearance(R.style.xAppearance)
        }else{
            binding.userIndicator.text = getString(R.string.single_player_up)
            if(playerChoice == USER_MARKER.USER_IS_X){
                setUserIndicatorAppearance(R.style.xAppearance)
            }else{
                setUserIndicatorAppearance(R.style.oAppearance)
            }
        }
    }

    /**
     * fills the array containing the cells of the game board
     *
     * Will always do the same task
     */
    private fun createBindingArray(){
        cellArray = Array(3){ Array(3) { binding.cell11 } }
        cellArray[0][0] = binding.cell11
        cellArray[0][1] = binding.cell12
        cellArray[0][2] = binding.cell13
        cellArray[1][0] = binding.cell21
        cellArray[1][1] = binding.cell22
        cellArray[1][2] = binding.cell23
        cellArray[2][0] = binding.cell31
        cellArray[2][1] = binding.cell32
        cellArray[2][2] = binding.cell33
    }


    /**
     * Sets the top game text style differently based on the build version
     *
     * @param resId
     *  The style to be applied to the textView
     *
     *  if (Version < 23), then use older method of changing textView text appearance
     *  else, then use new method of changing text appearance
     */
    private fun setUserIndicatorAppearance( @StyleRes resId: Int){
        if (Build.VERSION.SDK_INT < 23) {
            binding.userIndicator.setTextAppearance(context, resId)
        }else{
            binding.userIndicator.setTextAppearance(resId)
        }
    }

    /**
     *  Sets a cell style differently based on the build version
     *
     * @param resId
     *  The style to be applied to the textView
     *
     *  if (Version < 23), then use older method of changing button text appearance
     *  else, then use new method of changing text appearance
     */
    private fun setBoardCellAppearance(view: Button, @StyleRes resId: Int){
        if (Build.VERSION.SDK_INT < 23) {
            view.setTextAppearance(context, resId)
        }else{
            view.setTextAppearance(resId)
        }
    }


    /**
     * Resets the game page and the game board data
     *
     *  Sets all of the game data back to their initial states.
     *  Resets the game page by detaching and reattaching it to the fragment manager.
     */
    private fun resetPage(){

        //sets all elements on the board back to 0 (empty)
        for(array in boardArray){
            array.fill(USER_MARKER.USER_UNKNOWN)
        }
        currentPlayer = USER_MARKER.USER_IS_X
        firstTurn = true
        computerMoves = 0

        //Finds the current fragment and resets it
        context?.let {
            val fragmentManager = (context as? AppCompatActivity)?.supportFragmentManager
            fragmentManager?.let {
                val currentFragment = fragmentManager.findFragmentById(R.id.nav_host_fragment)
                currentFragment?.let{
                    //The fragment must be detached and then reattached to the fragment manager
                    var fragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.detach(it)
                    fragmentTransaction.commit()
                    fragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.attach(it)
                    fragmentTransaction.commit()
                }
            }
        }
    }


    /**
     *  Directs the user back to the menu page
     */
    private fun backToMenu(){
        val action = GameDirections.actionGameToMainPage()
        findNavController().navigate(action)
    }


    /**
     * Sets up the single player game modes based on the player's choice
     */
    private fun startSinglePlayerGame(){


            //When player chose "O", a computer depending on the game type will play first.
            if(firstTurn && playerChoice != currentPlayer){
                if(gameType == GAME_MODE.EASY_MODE || gameType == GAME_MODE.MEDIUM_MODE){
                    easyModeComputerMove()
                }else{
                    hardModeComputerMove()
                }
                firstTurn = false
            }

            /*
            A cell on the game board must be clickable in order for the user to select it
            When a cell is clickable, clicking on the cell will start the process of implementing
            that choice onto the board and into the game data
            **/
            for(array in 0..2){
                for(element in 0..2){
                    if(cellArray[array][element].isClickable){
                        cellArray[array][element].setOnClickListener{ placeMoveAndEvaluate(cellArray[array][element], Cell(array, element))}
                    }
                }
            }
    }


    /**
     * Sets up the two player game mode
     */
    private fun startTwoPlayerGame(){
            // Clicking on a cell will start the process of implementing that choice onto the board and into the game data
            for(array in 0..2){
                for(element in 0..2){
                    cellArray[array][element].setOnClickListener{ placeMoveAndEvaluate(cellArray[array][element], Cell(array, element))}
                    }
            }
    }

    /**
     * The computer player evaluates the board and picks a random valid location
     *
     * @return results
     *      The state of the game after the computer has moved
     */
    private fun easyModeComputerMove(): GAME_RESULTS{
        // The final coordinates on the board of the computer's choice
        val rowAndColumn = Cell(0, 0)

        //Map all available elements to a given array
        val arrayMap = mutableMapOf<Int, MutableList<Int>>()

        //Lists all available arrays
        val arrayRows = mutableListOf<Int>()

        //Is the result of the game after the computer moves
        var results = GAME_RESULTS.STILL_PLAYING

        //Checks for every available space on the game board and stores them
        for(array in 0..2){
            val arrayElements = mutableListOf<Int>()
            for(element in 0..2){
                if(boardArray[array][element] == USER_MARKER.USER_UNKNOWN){
                    if(!arrayRows.contains(array)){
                        arrayMap[array] = arrayElements
                       arrayRows.add(array)
                    }
                    arrayElements.add(element)
                }
            }
            arrayMap[array] = arrayElements
        }

        //Will get random move if there are available moves
        if(arrayRows.isNotEmpty()) {
            arrayRows.shuffle()
            val arrayIndexes: MutableList<Int> = arrayMap[arrayRows.first()]!!
            arrayIndexes.shuffle()
            rowAndColumn.row = arrayRows.first()
            rowAndColumn.column = arrayIndexes.first()

            //Will return the game result of making the chosen move
            results = placeMoveAndEvaluate(cellArray[rowAndColumn.row][rowAndColumn.column], rowAndColumn)
        }
        return results
    }

    private fun mediumModeComputerMove(): GAME_RESULTS{
        val results: GAME_RESULTS

        val validatedMoves = mutableListOf<Cell>()
        var count: Int
        var unvalidatedMove = Cell(0,0)
        var computerMoveFound: Boolean
        //Evaluates the Rows
        for(array in 0..2){
            computerMoveFound = false
            count = 0
            for(element in 0..2){
                if(!computerMoveFound){
                    if(boardArray[array][element] != playerChoice && boardArray[array][element] != USER_MARKER.USER_UNKNOWN){
                        computerMoveFound = true
                    }else if(boardArray[array][element] == playerChoice){
                        count++
                    }else if(boardArray[array][element] == USER_MARKER.USER_UNKNOWN){
                        unvalidatedMove = Cell(array, element)
                    }
                }
            }
            if(count == 2 && !computerMoveFound){
                validatedMoves.add(unvalidatedMove)
            }
        }


        for(element in 0..2){
            computerMoveFound = false
            count = 0
            for(array in 0..2){
                if(!computerMoveFound){
                    if(boardArray[array][element] != playerChoice && boardArray[array][element] != USER_MARKER.USER_UNKNOWN){
                        computerMoveFound = true
                    }else if(boardArray[array][element] == playerChoice){
                        count++
                    }else if(boardArray[array][element] == USER_MARKER.USER_UNKNOWN){
                        unvalidatedMove = Cell(array, element)
                    }
                }
            }
            if(count == 2 && !computerMoveFound){
                validatedMoves.add(unvalidatedMove)
            }
        }

        computerMoveFound = false
        count = 0
        for(element in 0..2){
            if(!computerMoveFound){
                if(boardArray[element][element] != playerChoice && boardArray[element][element] != USER_MARKER.USER_UNKNOWN){
                    computerMoveFound = true
                }else if(boardArray[element][element] == playerChoice){
                    count++
                }else if(boardArray[element][element] == USER_MARKER.USER_UNKNOWN){
                    unvalidatedMove = Cell(element, element)
                }
            }
        }
        if(count == 2 && !computerMoveFound){
            validatedMoves.add(unvalidatedMove)
        }

        computerMoveFound = false
        count = 0
        for(element in 0..2){
            if(!computerMoveFound){
                if(boardArray[2 - element][element] != playerChoice && boardArray[2 -element][element] != USER_MARKER.USER_UNKNOWN){
                    computerMoveFound = true
                }else if(boardArray[2 -element][element] == playerChoice){
                    count++
                }else if(boardArray[2 -element][element] == USER_MARKER.USER_UNKNOWN){
                    unvalidatedMove = Cell(2 - element, element)
                }
            }
        }
        if(count == 2 && !computerMoveFound){
            validatedMoves.add(unvalidatedMove)
        }

        results = if(validatedMoves.isEmpty()){
            easyModeComputerMove()
        }else{
            validatedMoves.shuffle()
            val rowAndColumn = validatedMoves.first()
            placeMoveAndEvaluate(cellArray[rowAndColumn.row][rowAndColumn.column], rowAndColumn)
        }

        return results
    }

    /**
     * The computer player evaluates every possible move from the current board state
     * and makes the best option
     *
     * @return results
     *      The state of the game after the computer has moved
     */
    private fun hardModeComputerMove(): GAME_RESULTS{

        // Help determines the best available option for a given move
        var highestScore = -1000

        //Is the result of the game after the computer moves
        var results = GAME_RESULTS.STILL_PLAYING

        //stores all moves that are equally beneficial to the computer
        val moveOptions = mutableListOf<Cell>()

        // How many moves have been played
        val depth = 1

        //When this is the computers first move
        if(computerMoves == 0){
           if(playerChoice == USER_MARKER.USER_IS_O){

               //Since players goes second, the computer picks on of the four corners
               val bestCords = listOf(0, 2)
               moveOptions.add(Cell(bestCords.random(), bestCords.random()))
           }else{
               /* Since player goes first, the computer picks the center or one of the four corners
                of the board depending on the user's move */
               if(boardArray[1][1] == USER_MARKER.USER_UNKNOWN){
                   moveOptions.add(Cell(1, 1))
               }else{
                   val bestCords = listOf(0, 2)
                   moveOptions.add(Cell(bestCords.random(), bestCords.random()))
               }
           }
        }else{

            //Evaluates every possible move for the computer on the current move
            for(array in 0..2){
                for (element in 0..2){
                    if(boardArray[array][element] == USER_MARKER.USER_UNKNOWN){

                        //Sets move to computer's move
                        if(playerChoice == USER_MARKER.USER_IS_X){
                            boardArray[array][element] = USER_MARKER.USER_IS_O
                        }else{
                            boardArray[array][element] = USER_MARKER.USER_IS_X
                        }

                        //gets the point score of the current move possibility
                        val score = miniMax(boardArray, false, depth, currentPlayer)

                        //removes computer test move
                        boardArray[array][element] = USER_MARKER.USER_UNKNOWN

                        //Will determine if the point score is the current largest score to determine if it shall be stored
                        if(score > highestScore){
                            highestScore = score
                            moveOptions.clear()
                            moveOptions.add(Cell(array, element))
                        }else if(score == highestScore){
                            moveOptions.add(Cell(array, element))
                        }
                    }
                }
            }
        }

        //Will get and implement a move only when there is a possible move
        if(moveOptions.isNotEmpty()){
            moveOptions.shuffle()
            val rowAndColumn = moveOptions.first()
            results = placeMoveAndEvaluate(cellArray[rowAndColumn.row][rowAndColumn.column], rowAndColumn)
        }
        computerMoves++
        return results
    }


    /**
     * Evaluates every possible move from the current board state and return
     * the score from the best or worst option
     *
     * @param board
     *      The current state of the board that is being evaluated
     * @param isMaximizing
     *      Is used to differentiate moves from the computer and the player
     * @param depth
     *      The amount of moves that have been made on the board
     * @param player
     *      The current player who is making a move on the board
     * @return bestPoints
     *      The score for the best or worst option on the board for a given player
     */
    private fun miniMax(board: Array<Array<USER_MARKER>>, isMaximizing: Boolean, depth: Int, player: USER_MARKER): Int{
        //The score based on the evaluated board
        var score :Int
        val nextPlayer = if(player == USER_MARKER.USER_IS_X){
            USER_MARKER.USER_IS_O
        }else{
            USER_MARKER.USER_IS_X
        }

        // Help determines the best or worst available option for a given move
        var bestScore: Int

        //Will keep evaluating moves until the given scenario is a finished game
        when(endGameCheck(board, player)){
            GAME_RESULTS.STILL_PLAYING->{

                //If a computer move is being evaluated
                if(isMaximizing){
                    bestScore = -1000

                    //Evaluates every possible move of the current move for the computer
                    for(array in 0..2) {
                        for (element in 0..2) {
                            if (board[array][element] == USER_MARKER.USER_UNKNOWN){
                                if(playerChoice == USER_MARKER.USER_IS_X){
                                    board[array][element] = USER_MARKER.USER_IS_O
                                }else{
                                    board[array][element] = USER_MARKER.USER_IS_X
                                }

                                //Gets points for the possible move's worst available move
                                score = miniMax(board, false, depth + 1, nextPlayer)

                                board[array][element] = USER_MARKER.USER_UNKNOWN
                                if(score > bestScore){
                                    bestScore = score
                                }
                            }
                        }
                    }

                //If a player move is being evaluated
                }else{
                    bestScore = 1000

                    //Evaluates every possible move of the current move for the player
                    for(array in 0..2) {
                        for (element in 0..2) {
                            if (board[array][element] == USER_MARKER.USER_UNKNOWN){
                                if(playerChoice == USER_MARKER.USER_IS_X){
                                    board[array][element] = USER_MARKER.USER_IS_X
                                }else{
                                    board[array][element] = USER_MARKER.USER_IS_O
                                }

                                //Gets points for the possible move's best available move
                                score = miniMax(board, true, depth + 1, nextPlayer)
                                board[array][element] = USER_MARKER.USER_UNKNOWN
                                if(score < bestScore){
                                    bestScore = score
                                }
                            }
                        }
                    }
                }
            }
            // When player "X" has won
            GAME_RESULTS.X_WIN-> {
                bestScore = if(playerChoice == USER_MARKER.USER_IS_X){
                    //Player Wins
                    depth - 10
                }else{
                    //Computer Wins
                    10 - depth
                }
            }
            // When player "O" has won
            GAME_RESULTS.O_WIN-> {
                bestScore = if(playerChoice == USER_MARKER.USER_IS_X){
                    //Computer Wins
                    10 - depth
                }else{
                    //Player Wins
                    depth - 10
                }

            }
            //Cat's Game
            else-> bestScore = 0
        }
        return bestScore
    }


    /**
     * Changes a chosen cell on the game board to display "X" or "O"'s move
     * and then evaluates the board before setting up the next move
     *
     * @param cell
     *      The cell button that the player or computer wants to use as their move
     * @param rowAndColumn
     *      The specific row and column of of the cell on the board
     * @return results
     *      The current state of the board after the move has been made
     */
    private fun placeMoveAndEvaluate(cell: Button, rowAndColumn: Cell): GAME_RESULTS{
        //Is the result of the game after a move
        var results = GAME_RESULTS.STILL_PLAYING
        if(currentPlayer == USER_MARKER.USER_IS_X) {
            //changes the values of the given cell on the board to player "X"
            cell.text = getString(R.string.x)
            setBoardCellAppearance(cell, R.style.xAppearance)
            cell.contentDescription = getString(R.string.x)
            cell.isClickable = false
            boardArray[rowAndColumn.row][rowAndColumn.column] = USER_MARKER.USER_IS_X
        }else {
            //changes the values of the given cell on the board to player "O"
            cell.text = getString(R.string.o)
            setBoardCellAppearance(cell, R.style.oAppearance)
            cell.contentDescription = getString(R.string.o)
            cell.isClickable = false
            boardArray[rowAndColumn.row][rowAndColumn.column] = USER_MARKER.USER_IS_O
        }
        when(endGameCheck(boardArray, currentPlayer)){
            GAME_RESULTS.STILL_PLAYING -> {
                if(currentPlayer == USER_MARKER.USER_IS_X){
                    prepareForNextMove(R.style.oAppearance, R.string.two_player_oup)
                }else{
                    prepareForNextMove(R.style.xAppearance, R.string.two_player_xup)
                }
            }

            //X wins the game after making a move
            GAME_RESULTS.X_WIN->{
                displayResultOfXWin()
                results = GAME_RESULTS.X_WIN
            }

            //O wins the game after making a move
            GAME_RESULTS.O_WIN-> {
                displayResultOfOWin()
                results = GAME_RESULTS.O_WIN
            }
            //Result is a cats game after X has made a move
            GAME_RESULTS.CATS_GAME->{
                displayResultOfCatsGame()
                results = GAME_RESULTS.CATS_GAME
            }
        }
        return results
    }

    /**
     * Prepares the game for the computer's move, then performs the computers move
     *
     * @param resId
     *      The style identifier used for userIndicator's text
     */
    private fun prepareForComputerMove(@StyleRes resId: Int){
        //Used to determine if computer's move has made to game end
        var computerCheck: GAME_RESULTS
        binding.userIndicator.text = getString(R.string.computers_turn)
        setUserIndicatorAppearance(resId)
        lockAndLoadBoys()
        val tempButton = Button(context)
        tempButton.background = binding.resetButton.background
        binding.resetButton.setBackgroundResource(R.drawable.resetbutton_disabled)
        binding.resetButton.isEnabled = false

        binding.resetButton.isClickable = false

        //Creates a delay before the computer makes a move
        CoroutineScope(Dispatchers.IO).launch {
            delay(TimeUnit.MILLISECONDS.toMillis(2_000))
            withContext(Dispatchers.Main) {
                firstTurn = false
                computerCheck = when (gameType) {
                    GAME_MODE.EASY_MODE -> {
                        easyModeComputerMove()
                    }
                    GAME_MODE.MEDIUM_MODE -> {
                        mediumModeComputerMove()
                    }
                    else -> {
                        hardModeComputerMove()
                    }
                }
                //will unlock unused cells if the game hasn't finished yet
                if (computerCheck == GAME_RESULTS.STILL_PLAYING){
                    unlockUnusedCells()
                }
                binding.resetButton.background = tempButton.background

                binding.resetButton.isEnabled = true
                binding.resetButton.isClickable = true

            }
        }
    }

    /**
     * Prepares the game for the next move
     *
     * @param styId
     *      The style identifier used for userIndicator's text
     * @param strId
     *      The string identifier used to set userIndicator's text
     */
    private fun prepareForNextMove(@StyleRes styId: Int, @StringRes strId: Int){
        if(gameType == GAME_MODE.TWO_PLAYER_MODE){
            binding.userIndicator.text = getString(strId)
            setUserIndicatorAppearance(styId)
            switchCurrentPlayer()
        }else{

            //Player is not the current player, thus will be the players turn
            if(playerChoice != currentPlayer) {
                binding.userIndicator.text = getString(R.string.single_player_up)
                setUserIndicatorAppearance(styId)
                switchCurrentPlayer()

            //Player is the current player, thus will be the computers turn
            }else{
                switchCurrentPlayer()
                prepareForComputerMove(styId)
            }
        }
    }

    /**
     * switches the current player status
     */
    private fun switchCurrentPlayer(){
        currentPlayer = if(currentPlayer == USER_MARKER.USER_IS_X){
            USER_MARKER.USER_IS_O
        }else{
            USER_MARKER.USER_IS_X
        }
    }

    /**
     * Displays the result of X winning and locks board
     */
    private fun displayResultOfXWin(){
        if(gameType == GAME_MODE.TWO_PLAYER_MODE){
            //Displays that Player "X" wins and locks all cells
            binding.userIndicator.text = getString(R.string.two_player_xwin)
            setUserIndicatorAppearance(R.style.xAppearance)
        }else{
            if(playerChoice == USER_MARKER.USER_IS_X){
                //Displays that the player wins and locks all cells
                binding.userIndicator.text = getString(R.string.single_player_win)
                setUserIndicatorAppearance(R.style.xAppearance)
            }else{
                //Displays that the player lost and locks all cells
                binding.userIndicator.text = getString(R.string.single_player_lose)
                setUserIndicatorAppearance(R.style.oAppearance)
            }
        }
        lockAndLoadBoys()
    }

    /**
     * Displays the result of O winning and locks the board
     */
    private fun displayResultOfOWin(){
        if(gameType == GAME_MODE.TWO_PLAYER_MODE){

            //Displays that Player "O" wins and locks all cells
            binding.userIndicator.text = getString(R.string.two_player_owin)
            setUserIndicatorAppearance(R.style.oAppearance)
        }else{
            if(playerChoice == USER_MARKER.USER_IS_X){

                //Displays that the player lost and locks all cells
                binding.userIndicator.text = getString(R.string.single_player_lose)
                setUserIndicatorAppearance(R.style.xAppearance)
            }else{

                //Displays that the player wins and locks all cells
                binding.userIndicator.text = getString(R.string.single_player_win)
                setUserIndicatorAppearance(R.style.oAppearance)
            }
        }
        lockAndLoadBoys()
    }

    /**
     * Displays the result of a cat's game and locks the board
     */
    private fun displayResultOfCatsGame(){
        if(gameType == GAME_MODE.TWO_PLAYER_MODE){

            //Displays that the game was a cat's game and locks all cells
            binding.userIndicator.text = getString(R.string.two_player_cats_game)
            setUserIndicatorAppearance(R.style.Theme_TicTacToe)
        }else{

            //Displays that the game was a cat's game and locks all cells
            binding.userIndicator.text = getString(R.string.single_player_cats_game)
            setUserIndicatorAppearance(R.style.Theme_TicTacToe)
        }
        lockAndLoadBoys()
    }

    /**
     * When both players have not chosen a specific cell for a move
     * during a game, that cell will become clickable again
     */
    private fun unlockUnusedCells(){
        for(array in 0..2){
            for(element in 0..2){
                if(cellArray[array][element].contentDescription != "X" && cellArray[array][element].contentDescription != "O"){
                    cellArray[array][element].setOnClickListener{ placeMoveAndEvaluate(cellArray[array][element], Cell(array, element))}
                }
            }
        }
    }

    /**
     * Makes all cells on the board non-clickable
     */
    private fun lockAndLoadBoys(){
        for(array in 0..2){
            for(element in 0..2){
                cellArray[array][element].isClickable = false
            }
        }
    }

    /**
     * Evaluates the status of the game for a given game board
     *
     * @param board
     *      The board to be evaluated
     * @param player
     *      The player to be accessed
     * @return gameProgress
     *      The status of the game according the given board
     */
    private fun endGameCheck(board: Array<Array<USER_MARKER>>, player: USER_MARKER):GAME_RESULTS{

        //Is the value based on the progress of the game, is originally set to 0 for still playing
        var gameProgress = GAME_RESULTS.STILL_PLAYING
        if(player == USER_MARKER.USER_IS_X){

            //If Player X wins
            if(victoryCheck(board, USER_MARKER.USER_IS_X)){
                gameProgress = GAME_RESULTS.X_WIN

                //If Cats Game
            }else if(tieCheck(board)){
                gameProgress = GAME_RESULTS.CATS_GAME
            }
        }else{
            if(victoryCheck(board, USER_MARKER.USER_IS_O)){

                //If Player O wins
                gameProgress = GAME_RESULTS.O_WIN
            }
        }
        return gameProgress
    }


    /**
     * determines if there is no winner to the game (cat's game)
     *
     * @param board
     *      The board to be evaluated for a cat's game
     * @return tie
     *      The value that is used to determine if the given board is a cat's game
     */
    private fun tieCheck(board: Array<Array<USER_MARKER>>): Boolean{
        var tie = true
        for (n in board){
            if(n.contains(USER_MARKER.USER_UNKNOWN)){
                tie = false
            }
        }
        return tie
    }


    /**
     * Evaluates all possible ways of the given player winning and determines if they have won or not
     *
     * @param board
     *      The board to be evaluated
     * @param player
     *      The player to be checked if they have won or not
     *
     */
    private fun victoryCheck(board: Array<Array<USER_MARKER>>, player: USER_MARKER): Boolean{

        var victory = false

        //The player that is not the player being checked for victory

        val arrayCheck = Array(3) { USER_MARKER.USER_UNKNOWN }
        val otherPlayer: USER_MARKER = if(player == USER_MARKER.USER_IS_X){
            USER_MARKER.USER_IS_O
        }else{
            USER_MARKER.USER_IS_X
        }

        //checks if any rows are only filled with the player being checked for victory
        for(n in board){
            if(!n.contains(otherPlayer) && !n.contains(USER_MARKER.USER_UNKNOWN)){
                victory = true
            }
        }

        //checks if any columns are only filled with the player being checked for victory
        if(!victory) {
            for (element in 0..2) {
                for (array in 0..2) {
                    arrayCheck[array] = board[array][element]
                }
                if (!arrayCheck.contains(otherPlayer) && !arrayCheck.contains(USER_MARKER.USER_UNKNOWN)) {
                    victory = true
                }
                arrayCheck.fill(USER_MARKER.USER_UNKNOWN)
            }
        }

        //checks if the diagonal moving down right is only filled with the player being checked for victory
        if(!victory) {
            for (element in 0..2) {
                arrayCheck[element] = board[element][element]
            }
            if (!arrayCheck.contains(otherPlayer) && !arrayCheck.contains(USER_MARKER.USER_UNKNOWN)) {
                victory = true
            }
            arrayCheck.fill(USER_MARKER.USER_UNKNOWN)
        }

        //checks if the diagonal moving down left is only filled with the player being checked for victory
        if(!victory) {
            for (element in 0..2) {
                arrayCheck[element] = board[2 - element][element]
            }
            if (!arrayCheck.contains(otherPlayer) && !arrayCheck.contains(USER_MARKER.USER_UNKNOWN)) {
                victory = true
            }
            arrayCheck.fill(USER_MARKER.USER_UNKNOWN)
        }
            return victory
    }
}
