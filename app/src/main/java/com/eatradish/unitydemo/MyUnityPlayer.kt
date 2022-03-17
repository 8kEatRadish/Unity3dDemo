package com.eatradish.unitydemo

import android.content.Context
import com.unity3d.player.IUnityPlayerLifecycleEvents
import com.unity3d.player.UnityPlayer

class MyUnityPlayer @JvmOverloads constructor(
    context: Context, iUnityPlayerLifecycleEvents: IUnityPlayerLifecycleEvents? = null
) : UnityPlayer(context, iUnityPlayerLifecycleEvents) {
    override fun kill() {

    }
}
