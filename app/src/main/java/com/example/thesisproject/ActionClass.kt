package com.example.thesisproject

import android.content.Intent
import android.os.Build
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class ActionClass() {

    fun startSpeechRecognition(recognizer: SpeechRecognizer,listener: MyRecognitionListener ){

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        //informa l'intent di usare un modello free-form e non web-based
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        //Specifica la lingua per la quale effettuare la recognition
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.ITALY
        )

        //Specifica l'utilizzo del riconoscitore offline ( valido solo per api >= 23)
        if(Build.VERSION.SDK_INT >= 23) {
            Log.d("tag", "sdk23+")
            //intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }

        //abilitiamo i risultati parziali per avere una modalità "live" di riconoscimento
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

        //tempo di silenzio per chiamare onEndOfSpeech
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000 )

        recognizer.setRecognitionListener(listener)
        recognizer.startListening(intent)

    }

}