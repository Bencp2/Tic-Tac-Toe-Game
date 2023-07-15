package com.example.tic_tac_toe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.tic_tac_toe.databinding.FragmentUserSelectionBinding

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER




class UserSelection : Fragment() {
    private var _binding: FragmentUserSelectionBinding? = null
    private val binding get() = _binding!!
    private lateinit var gameType: GAME_MODE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { gameType = it.get("gameMode") as GAME_MODE }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentUserSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Sets top text based on game type and asks user to pick who to play as
        binding.apply {
            initialText(modeTitle, selectionText)
            XOption.setOnClickListener { toNextPage(USER_MARKER.USER_IS_X) }
            OOption.setOnClickListener { toNextPage(USER_MARKER.USER_IS_O) }
        }

    }

    private fun initialText(modeTitle: TextView, selectionText: TextView){
        when(gameType){
            GAME_MODE.EASY_MODE-> {
                modeTitle.text = getString(R.string.easy_mode)
                modeTitle.contentDescription = getString(R.string.easy_mode)
            }
            GAME_MODE.MEDIUM_MODE-> {
                modeTitle.text = getString(R.string.medium_mode)
                modeTitle.contentDescription = getString(R.string.medium_mode)
            }
            else->{
                modeTitle.text = getString(R.string.hard_mode)
                modeTitle.contentDescription = getString(R.string.hard_mode)
            }
        }
    }

    //Directs player to single player game page with the game type and selected player choice
    private fun toNextPage(buttonChoice: USER_MARKER){
        val action = UserSelectionDirections.actionUserSelectionToGame(gameMode = gameType, playerChoice = buttonChoice)
        findNavController().navigate(action)

    }

}