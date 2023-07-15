package com.example.tic_tac_toe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.tic_tac_toe.databinding.FragmentMainPageBinding




class MainPage : Fragment() {
    private var _binding: FragmentMainPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //menu buttons
        binding.apply {
            easyMode.setOnClickListener{ toNextPage(GAME_MODE.EASY_MODE)}
            mediumMode.setOnClickListener{ toNextPage(GAME_MODE.MEDIUM_MODE)}
            hardMode.setOnClickListener{ toNextPage(GAME_MODE.HARD_MODE) }
            twoPlayerMode.setOnClickListener{ toGamePage() }

        }

    }

    //Directs user to single player selection page with the game mode type
    private fun toNextPage(buttonMode: GAME_MODE){
        val action = MainPageDirections.actionMainPageToUserSelection(gameMode = buttonMode)
        findNavController().navigate(action)
    }

    //Directs user to two player game page
    private fun toGamePage(){
        val action = MainPageDirections.actionMainPageToGame(gameMode = GAME_MODE.TWO_PLAYER_MODE, playerChoice = USER_MARKER.USER_IS_X)
        findNavController().navigate(action)
    }


}