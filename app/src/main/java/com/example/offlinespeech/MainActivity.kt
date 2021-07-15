package com.example.offlinespeech

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import java.util.*
import android.speech.tts.TextToSpeech


class MainActivity : AppCompatActivity() {

    private lateinit var stt :FloatingActionButton
    private lateinit var tts :FloatingActionButton
    private lateinit var textField :TextInputEditText

    private val REQUEST_RECORD_AUDIO_PERMISSION = 124

    private var recognizer : SpeechRecognizer? = null
    private var callBackUpdate = object : CallBackUpdate {
        override fun onUpdate(result: String) { updateText(result) }
        override fun onError(error: String) { showError(error) }
        override fun onFinished() { resetButton() }
    }
    private val listener = MyRecognitionListener(callBackUpdate,this)

    private var textToSpeech : TextToSpeech? = null
    private val utteranceId = this.hashCode().toString()+""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textField = findViewById(R.id.textField)
        stt = findViewById(R.id.speechToText)
        tts = findViewById(R.id.textToSpeech)


        //Riconoscimento vocale
        stt.setOnClickListener{
            //Permessi
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                requestRecordPermission()
            else {
                //Controlla che il servizio sia disponibile al dispositivo
                if(SpeechRecognizer.isRecognitionAvailable(applicationContext)) {
                    recognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext)
                    stt.isEnabled = false
                    startSpeechRecognition()
                }
                else {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.errore)
                        .setMessage(R.string.des_err)
                        .show()
                }
            }

        }

        //Sintesi vocale
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

    private fun startTextSynthesis() {
        val text = textField.text.toString()

        textToSpeech = TextToSpeech(applicationContext) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech!!.language = Locale.ITALY
                textToSpeech!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            } else {
                showError(getString(R.string.synthesisErr))
            }
        }
    }

    private fun updateText(data :String){
        textField.setText(data)
    }

    private fun showError(data :String){
        val contextView :LinearLayout = findViewById(R.id.contextView)
        Snackbar.make(contextView, data, Snackbar.LENGTH_LONG)
            .show()
    }

    private fun resetButton(){
        stt.isEnabled = true
    }
}