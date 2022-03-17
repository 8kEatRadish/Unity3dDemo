package com.eatradish.unitydemo

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.net.Uri
import android.net.rtp.AudioStream
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.eatradish.unitydemo.databinding.ActivityUnityPlayerBinding
import com.unity3d.player.IUnityPlayerLifecycleEvents
import com.unity3d.player.MultiWindowSupport
import com.unity3d.player.UnityPlayer
import kotlinx.coroutines.*
import java.lang.StringBuilder
import java.util.concurrent.atomic.AtomicInteger

class UnityPlayerActivity : AppCompatActivity(), IUnityPlayerLifecycleEvents {
    private lateinit var myUnityPlayer: MyUnityPlayer
    private lateinit var binding: ActivityUnityPlayerBinding
    private final val TAG: String = UnityPlayerActivity::class.java.simpleName
    private val gameObjectName = "music"
    private val unity3dMethodName = "ControlAnimator"
    private var mediaPlayer: MediaPlayer? = null
    private var visualizer: Visualizer? = null
    private val musicFile = "music/莫文蔚-这世界那么多人.mp3"
    private val musicLrc = "music/莫文蔚-这世界那么多人.lrc"

    private var type = 2

    private var line: AtomicInteger = AtomicInteger(-1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_unity_player)
        playMusic()
        myUnityPlayer = MyUnityPlayer(this, this)
        binding.back.setOnClickListener {
            when (type) {
                // 播放音频
                1 -> {
                    binding.back.text = "暂停"
                    UnityPlayer.UnitySendMessage(gameObjectName, unity3dMethodName, type.toString())
                    mediaPlayer?.start()
                    type++
                }
                // 暂停音频
                2 -> {
                    binding.back.text = "播放"
                    UnityPlayer.UnitySendMessage(gameObjectName, unity3dMethodName, type.toString())
                    mediaPlayer?.pause()
                    type = 1
                }
            }
        }
        val cmdLine = updateUnityCommandLineArguments(intent.getStringExtra("unity"))
        intent.putExtra("unity", cmdLine)
        binding.unityPlayer.addView(myUnityPlayer.view)
        myUnityPlayer.requestFocus()


        val lyricInfo = setupLyricResource(assets.open(musicLrc), "UTF-8")
        val stringBuilder = StringBuilder()
        lyricInfo.song_lines.forEach {
            stringBuilder.append(it.content).append("\n")
        }
        Log.d(TAG, "onCreate: lrc = ${stringBuilder.toString()}")

        GlobalScope.launch(Dispatchers.IO) {

            while (true) {

                if (mediaPlayer != null) {
                    val index = getCurrentPosition(mediaPlayer!!, lyricInfo.song_lines)
                    withContext(Dispatchers.Main) {

                        if (line.get() != index) {
                            runAnimation(lyricInfo.song_lines[index].content)
                            line.set(index)
                        }
                    }
                }

                delay(100)
            }
        }

        binding.lrc.text = stringBuilder.toString()
    }

    @SuppressLint("Recycle")
    private fun runAnimation(content: String) {
        val objectAnimatorY = ObjectAnimator.ofFloat(binding.lrc, "translationY", 0f, 100f)
        val objectAnimatorY2 = ObjectAnimator.ofFloat(binding.lrc, "translationY", -100f, 0f)
        val objectAnimatorA = ObjectAnimator.ofFloat(binding.lrc, "alpha", 1f, 0f, 0f)
        val objectAnimatorA2 = ObjectAnimator.ofFloat(binding.lrc, "alpha", 0f, 0f, 1f)

        val animatorSet = AnimatorSet()
        animatorSet.duration = 400
        animatorSet.interpolator = LinearInterpolator()
        animatorSet.play(objectAnimatorY).with(objectAnimatorA)
        animatorSet.play(objectAnimatorY2).after(objectAnimatorY)
        animatorSet.play(objectAnimatorY2).with(objectAnimatorA2)
        animatorSet.start()
        binding.lrc.postOnAnimationDelayed({
            binding.lrc.text = content
        }, 200)
    }

    private fun playMusic() {
        val afd = assets.openFd(musicFile)
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            isLooping = true
            setOnPreparedListener {
                start()
                visualizer = Visualizer(this.audioSessionId)
                visualizer?.apply {
                    captureSize = Visualizer.getCaptureSizeRange()[1]
                    Log.d(TAG, "playMusic: captureSize = ${Visualizer.getCaptureSizeRange()[1]}")
                    setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {
                            binding.visualizerView.updateVisualizer(waveform)
                        }

                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            TODO("Not yet implemented")
                        }

                    }, Visualizer.getMaxCaptureRate() / 2, true, false)
                    enabled = true
                }
                setOnCompletionListener {
                    visualizer?.enabled = false
                }
            }
            prepare()
        }
    }

    // Override this in your custom UnityPlayerActivity to tweak the command line arguments passed to the Unity Android Player
    // The command line arguments are passed as a string, separated by spaces
    // UnityPlayerActivity calls this from 'onCreate'
    // Supported: -force-gles20, -force-gles30, -force-gles31, -force-gles31aep, -force-gles32, -force-gles, -force-vulkan
    // See https://docs.unity3d.com/Manual/CommandLineArguments.html
    // @param cmdLine the current command line arguments, may be null
    // @return the modified command line string or null
    protected fun updateUnityCommandLineArguments(cmdLine: String?): String? {
        return cmdLine
    }

    override fun onStart() {
        super.onStart()
        if (!MultiWindowSupport.getAllowResizableWindow(this))
            return;

        myUnityPlayer.resume()
    }

    override fun onResume() {
        super.onResume()
        if (MultiWindowSupport.getAllowResizableWindow(this))
            return;
        myUnityPlayer.resume()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.stop()
        visualizer?.release()
        mediaPlayer?.release()
        mediaPlayer = null
        if (MultiWindowSupport.getAllowResizableWindow(this))
            return;
        myUnityPlayer.destroy()
    }

    override fun onStop() {
        super.onStop()
        if (!MultiWindowSupport.getAllowResizableWindow(this))
            return;

        myUnityPlayer.destroy();
    }


    override fun onDestroy() {
        myUnityPlayer.destroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        myUnityPlayer.lowMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == TRIM_MEMORY_RUNNING_CRITICAL) {
            myUnityPlayer.lowMemory();
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        myUnityPlayer.configurationChanged(newConfig)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        myUnityPlayer.windowFocusChanged(hasFocus)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_MULTIPLE)
            return myUnityPlayer.injectEvent(event);
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean =
        myUnityPlayer.onKeyUp(keyCode, event)

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean =
        myUnityPlayer.onKeyDown(keyCode, event)

    override fun onTouchEvent(event: MotionEvent?): Boolean = myUnityPlayer.onTouchEvent(event)

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean =
        myUnityPlayer.onGenericMotionEvent(event)


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // To support deep linking, we need to make sure that the client can get access to
        // the last sent intent. The clients access this through a JNI api that allows them
        // to get the intent set on launch. To update that after launch we have to manually
        // replace the intent with the one caught here.
        setIntent(intent)
        myUnityPlayer.newIntent(intent)
    }

    // When Unity player unloaded move task to background
    override fun onUnityPlayerUnloaded() {
        moveTaskToBack(true);
        Log.d(TAG, "onUnityPlayerUnloaded")
    }

    // Callback before Unity player process is killed
    override fun onUnityPlayerQuitted() {
        Log.d(TAG, "onUnityPlayerQuitted")
    }
}