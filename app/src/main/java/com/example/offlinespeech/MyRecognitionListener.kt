package com.example.offlinespeech

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log

class MyRecognitionListener(cbu: CallBackUpdate) : RecognitionListener {

    private var data = String()
    private var callBackUpdate = cbu


    override fun onReadyForSpeech(params: Bundle) {
        Log.d("tag", "ready")
        data = String()
    }

    override fun onBeginningOfSpeech() {
        Log.d("tag", "beginning")
    }

    override fun onRmsChanged(rmsdB: Float) {
        //Log.d("tag", "rms")
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        Log.d("tag", "buff")
    }

    override fun onEndOfSpeech() {
        Log.d("tag", "endofs")
    }

    override fun onError(error: Int) {
        val tmp = errorToString(error)
        Log.d("tag", "onError:$tmp")
        callBackUpdate.onUpdate("Errore:\n$tmp")
    }

    override fun onResults(results: Bundle?) {

        Log.d("tag", "onResults")

        //con onPartialResult l'ultima parola viene saltata (da testare meglio)
        val stringResults = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val size = stringResults?.size

        data = size?.let { stringResults[it-1] } ?: ""
        callBackUpdate.onUpdate(data)

    }

    override fun onPartialResults(partialResults: Bundle?) {

        Log.d("tag", "onPartialResults")

        val stringResults = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (stringResults != null) {
            for( str in stringResults){
                callBackUpdate.onUpdate(str)
            }
        }

    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        Log.d("tag", "onEvent")
    }

    private fun errorToString(code: Int): String {
        return when (code) {
            SpeechRecognizer.ERROR_AUDIO -> "Errore di registrazione audio."
            SpeechRecognizer.ERROR_CLIENT -> "Errore lato cliente."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permessi insufficienti."
            SpeechRecognizer.ERROR_NETWORK -> "Errore di rete."
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Operazione di rete scaduta"
            SpeechRecognizer.ERROR_NO_MATCH -> "Nessun risultato di riconoscimento."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Servizio di riconoscimento occupato."
            SpeechRecognizer.ERROR_SERVER -> "Errore nel server."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Nessun input vocale."
            else -> "Errore non riconosciuto."
        }
    }
}