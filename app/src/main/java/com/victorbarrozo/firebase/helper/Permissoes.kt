package com.victorbarrozo.firebase.helper

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Permissoes {

    companion object {
        fun requisitarPermissoes(activity: Activity, permissoes: List<String>, requestCode: Int) {

                //SOLICITAR PERMISSOES QUE JA FORAM NEGADAS

            permissoes.forEach {permissao ->
                val naoTemPermissao= mutableListOf<String>()
               val temPermissao = ContextCompat.checkSelfPermission(
                    activity, permissao
               ) == PackageManager.PERMISSION_GRANTED
                if (!temPermissao)
                    naoTemPermissao.add(permissao)
            }
                             //SOLICITAR PERMISSOES

            ActivityCompat.requestPermissions(
                activity, permissoes.toTypedArray(), requestCode
            )
        }
    }
}