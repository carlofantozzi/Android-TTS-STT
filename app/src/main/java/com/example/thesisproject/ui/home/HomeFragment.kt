package com.example.thesisproject.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.thesisproject.ActionClass
import com.example.thesisproject.CallBackUpdate
import com.example.thesisproject.MyRecognitionListener
import com.example.thesisproject.R
import com.example.thesisproject.databinding.FragmentHomeBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var stt : FloatingActionButton
    private lateinit var tts : FloatingActionButton
    private lateinit var textField : TextInputEditText
    private lateinit var contextView : LinearLayout

    private var recognizer : SpeechRecognizer? = null
    private var callBackUpdate = object : CallBackUpdate {
        override fun onUpdate(result: String) { updateText(result) }
        override fun onError(error: String) { showError(error) }
        override fun onFinished() { resetButton() }
    }
    private var listener : MyRecognitionListener? = null

    private var textToSpeech : TextToSpeech? = null
    private val utteranceId = this.hashCode().toString()+""



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /*homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        */
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //val textField: TextView = binding.textField
        /*homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })*/

        textField = binding.textField
        stt = binding.speechToText
        tts = binding.textToSpeech
        contextView = binding.contextView

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(SpeechRecognizer.isRecognitionAvailable(activity)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(activity)
            listener = MyRecognitionListener(callBackUpdate, activity)
        }

        val action = ActionClass()


        stt.setOnClickListener{
            Log.d("tag","stt")

            //Controlla che il servizio sia disponibile al dispositivo
            if(recognizer != null){
                Log.d("tag","rec")
                stt.isEnabled = false
                action.startSpeechRecognition(recognizer!!,listener!!)
            }
            else {
                AlertDialog.Builder(activity)
                    .setTitle(R.string.errore)
                    .setMessage(R.string.des_err)
                    .show()
            }


        }

        tts.setOnClickListener {
            startTextSynthesis()
            Log.d("tag", textField.text.toString())
        }
    }

    override fun onPause() {
        super.onPause()
        recognizer?.destroy()
        textToSpeech?.shutdown()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun startTextSynthesis() {
        val text = textField.text.toString()

        if(textToSpeech?.isSpeaking == true){
            showError(getString(R.string.occupato))
        }
        else {
            textToSpeech = TextToSpeech(activity) { status ->
                if (status != TextToSpeech.ERROR) {
                    textToSpeech!!.language = Locale.ITALY
                    textToSpeech!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                } else {
                    showError(getString(R.string.synthesisErr))
                }
            }
        }
    }


    private fun updateText(data :String){
        textField.setText(data)
    }

    private fun showError(data :String){
        Snackbar.make(contextView, data, Snackbar.LENGTH_LONG)
            .show()
    }

    private fun resetButton(){
        stt.isEnabled = true
    }


}