package com.eatradish.unitydemo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.eatradish.unitydemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val REQUEST_CODE_PERMISSIONS = 0x001
    private val MAX_NUMBER_REQUEST_PERMISSIONS = 1 //请求权限的最多次数
    private var mPermissionRequestCount: Int = 0

    // 应用程序需要的权限列表
    private val sPermissions = object : ArrayList<String>() {
        init {
            add(Manifest.permission.RECORD_AUDIO)//麦克风
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.openUnity3d.setOnClickListener {
            if (checkAllPermissions()) {
                startActivity(Intent(this, UnityPlayerActivity::class.java))
            } else {
                Toast.makeText(this, "没有麦克风权限", Toast.LENGTH_SHORT).show()
            }

        }
        requestPermissionsIfNecessary()
    }

    /**
     * 检查是否授予权限
     * 如果全部获取, 则直接过.
     * 如果权限缺失, 则提示Dialog
     *
     * @param requestCode  请求码
     * @param permissions  权限
     * @param grantResults 结果
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS && !checkAllPermissions())
            showMissingPermissionDialog()
    }

    /**
     * 检查目前应用是否有所有我们需要的权限
     * 如果全部获取, 则直接过.
     * 如果权限缺失, 就判断当前请求权限的次数是否已达到最大值，如果没有则再次请求权限，否则就提示Dialog
     */
    private fun requestPermissionsIfNecessary() {
        if (!checkAllPermissions()) {
            if (mPermissionRequestCount < MAX_NUMBER_REQUEST_PERMISSIONS) {
                mPermissionRequestCount += 1
                ActivityCompat.requestPermissions(
                    this,
                    sPermissions.toTypedArray(),
                    REQUEST_CODE_PERMISSIONS)
            } else {
                Toast.makeText(this, "缺失权限", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * 检查应用目前是否有这些权限
     */
    private fun checkAllPermissions(): Boolean {
        var hasPermissions = true
        for (permission in sPermissions) {
            hasPermissions = hasPermissions and (ContextCompat.checkSelfPermission(
                this, permission) == PackageManager.PERMISSION_GRANTED)
        }
        return hasPermissions
    }

    /**
     * 显示缺失权限提示
     */
    private fun showMissingPermissionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("帮助")
        builder.setMessage("当前应用缺少必要权限。\n \n 请点击 \"设置\"-\"权限\"-打开所需权限。")
        builder.setNegativeButton("取消") { _, _ -> }
        builder.setPositiveButton("设置") { _, _ ->
            // 跳转到当前应用对应的设置页面
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:" + this.packageName)
            startActivity(intent)
        }
        builder.setCancelable(false)
        builder.show()
    }

}