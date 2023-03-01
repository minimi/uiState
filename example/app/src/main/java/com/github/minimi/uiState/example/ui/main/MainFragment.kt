package com.github.minimi.uiState.example.ui.main

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.get
import com.github.minimi.uiState.*
import com.github.minimi.uiState.example.R

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val button = view.findViewById<Button>(R.id.button)
        val button2 = view.findViewById<Button>(R.id.button2)
        val message = view.findViewById<TextView>(R.id.message)

        button.setOnClickListener {
            viewModel.giveMeExample()
        }

        button2.setOnClickListener {
            viewModel.giveMeAnotherExample()
        }

        uiState(viewModel.someExampleState) {
            success {
                message.text = it
            }
            loading {
                message.text = "Loading..."
            }
            error {
                message.text = it.message
            }
        }

        viewModel.someAnotherExampleState.observeUiState(viewLifecycleOwner) {
            success {
                message.text = it
            }
            loading {
                message.text = "Loading..."
                button2.isEnabled = false
            }
            error {
                message.text = it.message
                button2.isEnabled = true
            }
        }


        viewModel.someAnotherExampleState.observe(viewLifecycleOwner) { uiState ->
            uiState.onSuccess {
                message.text = it
            }.onLoading {
                message.text = "Loading..."
                button2.isEnabled = false
            }.onFailure {
                message.text = it.message
                button2.isEnabled = true
            }
        }

    }

}