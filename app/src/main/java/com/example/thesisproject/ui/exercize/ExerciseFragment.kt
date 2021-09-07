package com.example.thesisproject.ui.exercize

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.thesisproject.*
import com.example.thesisproject.databinding.FragmentExerciseBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import java.util.*


class ExerciseFragment : Fragment() {

    private var _binding: FragmentExerciseBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var stt : FloatingActionButton
    private lateinit var tts : FloatingActionButton
    private lateinit var textField : TextInputEditText
    private lateinit var contextView : LinearLayout
    private lateinit var textView : TextView
    private lateinit var button : Button

    private var recognizer : SpeechRecognizer? = null
    private var callBackUpdate = object : CallBackUpdate {
        override fun onUpdate(result: String) { updateText(result) }
        override fun onError(error: String) { showError(error) }
        override fun onFinished() { resetButton() }
    }
    private var listener : MyRecognitionListener? = null

    private var textToSpeech : TextToSpeech? = null
    private val utteranceId = this.hashCode().toString()+""

    private var exercise = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentExerciseBinding.inflate(inflater, container, false)
        val root: View = binding.root

        stt = binding.speechToText
        tts = binding.textToSpeech
        textField = binding.textField
        contextView = binding.contextView
        textView = binding.textView
        button = binding.buttonDone

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref: SharedPreferences? = activity?.getPreferences(Context.MODE_PRIVATE)
        if (sharedPref != null) {
            exercise = sharedPref.getInt(getString(R.string.preferences), -1)
        }

        if(exercise != -1){
            AlertDialog.Builder(activity)
                .setTitle(R.string.tornato)
                .setMessage(R.string.recap)
                .setPositiveButton(R.string.si){
                        _, _ -> textView.text = resources.getStringArray(R.array.exercises)[exercise]
                        listenQ()
                }
                .setNegativeButton(R.string.no){
                        _, _ -> exercise = -1
                        listenQ()
                }
                .setCancelable(false)
                .show()
        }
        else{
            listenQ()
        }

        if(SpeechRecognizer.isRecognitionAvailable(activity)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(activity)
            listener = MyRecognitionListener(callBackUpdate, activity)
        }

        val action = ActionClass()

        tts.setOnClickListener{
            listenQ()
        }

        stt.setOnClickListener {
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

        var answer : String
        var convalida : String

        button.setOnClickListener {
            answer = textField.text.toString()
            Log.d("exercise", exercise.toString())
            convalida = if(exercise == -1) resources.getString(R.string.tut_ans) else resources.getStringArray(R.array.answers)[exercise]
            if(answer.contains(convalida, true)){
                AlertDialog.Builder(activity)
                    .setTitle(R.string.alertTitle)
                    .setMessage(R.string.alertMex)
                    .setPositiveButton(R.string.alertOk){
                        dialog, it ->
                        exercise = if(exercise+1>11) -1 else exercise+1
                        Log.d("exercise", exercise.toString())
                        textView.text = if (exercise == -1) resources.getString(R.string.tutorial) else resources.getStringArray(R.array.exercises)[exercise]
                        textField.text?.clear()
                        listenQ()
                    }
                    .setCancelable(false)
                    .show()
            }
            else{
                AlertDialog.Builder(activity)
                    .setTitle(R.string.neg_alertTitle)
                    .setMessage(R.string.neg_alertMex)
                    .setPositiveButton(R.string.alertOk, null)
                    .setCancelable(false)
                    .show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        recognizer?.destroy()
        textToSpeech?.shutdown()
        activity?.getPreferences(Context.MODE_PRIVATE)
            ?.edit()?.putInt(getString(R.string.preferences), exercise)?.apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun listenQ(){
        startTextSynthesis()
    }

    private fun startTextSynthesis() {

        val text = textView.text.toString()

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