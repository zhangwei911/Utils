package viz.commonlib.util

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.viz.tools.l
import java.util.*


class SpeechUtils(
    context: Context,
    language: Locale = Locale.CHINESE,
    pitch: Float = 1.0f,
    speechRate: Float = 0.5f,
    listener: UtteranceProgressListener? = null
) {
    enum class SPEED {
        LOW1("低", 0.1f),
        LOW2("中低", 0.5f),
        NORMAL("正常", 1.0f),
        HIGH1("中高", 2f),
        HIGH2("高", 10f);

        var speedName = ""
        var speedRate = 1.0f

        constructor(name: String, speed: Float) {
            speedName = name
            speedRate = speed
        }
    }

    private var textToSpeech: TextToSpeech? = null// TTS对象
    var customPitch: Float = 1.0f
        set(value) {
            field = value
            textToSpeech?.setPitch(field)
        }
    var customSpeechRate: Float = 1.0f
        set(value) {
            field = value
            textToSpeech?.setSpeechRate(field)
        }
    var customLanguage: Locale = Locale.CHINESE
        set(value) {
            field = value
            textToSpeech?.language = field
        }

    init {
        textToSpeech = TextToSpeech(context, TextToSpeech.OnInitListener { i ->
            if (i == TextToSpeech.SUCCESS) {
                textToSpeech!!.language = language
                textToSpeech?.setPitch(pitch)// 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
                textToSpeech?.setSpeechRate(speechRate)
                l.d("onInit: TTS引擎初始化成功")
            } else {
                l.d("onInit: TTS引擎初始化失败")
            }
        })
        textToSpeech?.setOnUtteranceProgressListener(listener)
    }

    fun speakText(text: String): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UniqueID") ?: -1
        } else {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null) ?: -2
        }
    }

    fun stop() {
        textToSpeech?.stop()
    }

    fun shutdown() {
        textToSpeech?.shutdown()
        textToSpeech = null
        singleton = null
    }

    companion object {
        private val TAG = "SpeechUtils"
        private var singleton: SpeechUtils? = null

        fun getInstance(
            context: Context,
            language: Locale = Locale.CHINESE,
            pitch: Float = 1.0f,
            speechRate: Float = 0.5f,
            listener: UtteranceProgressListener? = null
        ): SpeechUtils {
            if (singleton == null) {
                synchronized(SpeechUtils::class.java) {
                    if (singleton == null) {
                        singleton =
                            SpeechUtils(context, language, pitch, speechRate, listener)
                    }
                }
            }
            return singleton!!
        }
    }

}