package com.example.offlinespeech

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var stt :FloatingActionButton
    private lateinit var tts :FloatingActionButton
    private lateinit var textField :TextInputEditText

    private val REQUEST_RECORD_AUDIO_PERMISSION = 124


    private var recognizer : SpeechRecognizer? = null
    private var callBackUpdate = object : CallBackUpdate {
        override fun onUpdate(result: String) {
            updateText(result)
        }
    }
    private val listener = MyRecognitionListener(callBackUpdate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textField = findViewById(R.id.textField)
        stt = findViewById(R.id.speechToText)
        tts = findViewById(R.id.textToSpeech)


        stt.setOnClickListener{
            //check for permission granted
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                requestRecordPermission()
            else {
                if(SpeechRecognizer.isRecognitionAvailable(applicationContext)) {
                    recognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext)
                    startSpeechRecognition()
                }
                //snackbar? alertdialog? servizio non disponibile per il dispositivo corrente
            }

        }
    }

    override fun onPause() {
        super.onPause()
        recognizer?.destroy()
    }

    private fun requestRecordPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION)
        {
            if(grantResults.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Log.i("tag", "record permission has been DENIED.")
            }
            else {
                Log.i("tag", "record permission has been GRANTED.")
            }
        }
        else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun startSpeechRecognition(){

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        //informa l'intent di usare un modello free-form e non web-based
        //>>>devo effettuare il catch di ActivityNotFoundException.
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        //Specifica la lingua per la quale effettuare la recognition
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.ITALY
        )

        //abilitiamo i risultati parziali per avere una modalità "live" di riconoscimento
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

        //tempo di silenzio per chiamare onEndOfSpeech
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000 )

        recognizer!!.setRecognitionListener(listener)
        recognizer!!.startListening(intent)

    }

    private fun updateText(data :String){
        textField.setText(data)
    }
}