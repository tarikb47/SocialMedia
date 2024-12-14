package com.example.hook.presentation.authentication.helpers

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import com.example.hook.R
import com.example.hook.databinding.SuccessToastBinding

object ToastHelper {
    private var isToastVisible = false

    fun showError(context: Context, message: String, layoutInflater: LayoutInflater) {
        if (isToastVisible) return
        isToastVisible = true
        val errorToastBinding = SuccessToastBinding.inflate(layoutInflater)
        errorToastBinding.toastMessage.text = message
        errorToastBinding.root.setBackgroundResource(R.drawable.error_toast_background)

        Toast(context).apply {
            duration = Toast.LENGTH_LONG
            view = errorToastBinding.root
            setGravity(Gravity.TOP, 0, 100)
            show()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            isToastVisible = false
        }, 3500)
    }

    fun showSuccess(context: Context, message: String, layoutInflater: LayoutInflater) {
        if (isToastVisible) return
        isToastVisible = true
        val toastBinding = SuccessToastBinding.inflate(layoutInflater)
        toastBinding.toastMessage.text = message

        Toast(context).apply {
            duration = Toast.LENGTH_LONG
            view = toastBinding.root
            setGravity(Gravity.TOP, 0, 100)
            show()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            isToastVisible = false
        }, 3500)
    }
}