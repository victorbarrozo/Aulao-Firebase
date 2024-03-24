package com.victorbarrozo.firebase

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.victorbarrozo.firebase.databinding.ActivityUploadImagemBinding
import java.io.ByteArrayOutputStream
import java.util.UUID

class UploadImagemActivity : AppCompatActivity() {

    private var uriImagemSelecionada: Uri? = null
    private var bitmapImagemSelecionada: Bitmap? = null
    private var temPermissaoCamera = false
    private var temPermissaoGaleria = false

    val bindind by lazy {
        ActivityUploadImagemBinding.inflate(layoutInflater)
    }
    val armazenamento by lazy {
        FirebaseStorage.getInstance()
    }
    val autenticar by lazy {
        FirebaseAuth.getInstance()
    }
    val db by lazy{
        FirebaseFirestore.getInstance()
    }
    private val  permissoes= listOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.ACCESS_COARSE_LOCATION

    )


                        //ABRIR E USAR GALERIA

    private val abrirGaleria = registerForActivityResult(
        ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            bindind.imageSelecionada.setImageURI( uri )
            uriImagemSelecionada = uri
            Toast.makeText(
                this,
                "Imagem selecionada",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                "Nenhuma imagem selecionada",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

                        //ABRIR E USAR CAMERA

    private val abrirCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
        // ActivityResultContracts.TakePicture()
    ){ activityResult ->
       // if (activityResult.resultCode == RESULT_OK){ }else{}
        bitmapImagemSelecionada =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activityResult.data?.extras?.getParcelable("data", Bitmap::class.java)
        }else{
            activityResult.data?.extras?.getParcelable("data")
        }
        bindind.imageSelecionada.setImageBitmap(bitmapImagemSelecionada)


    }

   /* override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.i("permissao_app", "requestCode: $requestCode")

        permissions.forEachIndexed  {indice, valor ->
            Log.i("permissao_app", "permission: $indice) $valor" )
        }
        grantResults.forEachIndexed  {indice, valor ->
            Log.i("permissao_app", "concedida: $indice) $valor" )
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  enableEdgeToEdge()
        setContentView(bindind.root)
                            //PERMISSOES

       // Permissoes.requisitarPermissoes(this, permissoes,  100 )
        solicitarPermissoes()

                             //SISTEMA

        bindind.btnGaleria.setOnClickListener {

            if (temPermissaoGaleria){
                abrirGaleria.launch("image/*")//Mime type :É o tipo de aquivo que deve ser utilizado
            }else{
                Toast.makeText(this, "Você não tem Permisão", Toast.LENGTH_SHORT).show()
            }
        }
        bindind.btnUpload.setOnClickListener {


        }
        bindind.btnRecuperar.setOnClickListener {
            recuperarImagemFirebase()
        }
        bindind.btnCamera.setOnClickListener {

            if (temPermissaoCamera){
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                abrirCamera.launch(intent)
            }else{
                Toast.makeText(this, "Você não tem Permisão", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun solicitarPermissoes() {

        val permissoesNegadas = mutableListOf<String>()
        temPermissaoCamera = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        temPermissaoGaleria = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        if ( !temPermissaoCamera)
            permissoesNegadas.add(android.Manifest.permission.CAMERA)
        if ( !temPermissaoGaleria)
            permissoesNegadas.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permissoesNegadas.isNotEmpty()){
            val gerenciarPermissoes = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ){ permissoes: Map<String, Boolean>->
                Log.i("novas_permissoes", "permissoes: $permissoes")
                temPermissaoCamera = permissoes [android.Manifest.permission.CAMERA]?:
                temPermissaoCamera
                temPermissaoGaleria = permissoes [android.Manifest.permission.READ_EXTERNAL_STORAGE]?:
                temPermissaoGaleria

            }
            gerenciarPermissoes.launch(permissoesNegadas.toTypedArray())
        }
    }
    //RECUPERA FOTO DO FIREBASE

    private fun recuperarImagemFirebase() {
        val idUsuarioLogado = autenticar.currentUser?.uid
        if (idUsuarioLogado != null) {
            armazenamento.getReference("fotos")
                .child(idUsuarioLogado)
                .child("foto.jpg")
                .downloadUrl
                .addOnSuccessListener { uri ->
                    Picasso.get()
                        .load(uri)
                        .into(bindind.imageRecuperada)
                    Toast.makeText(this, "sucesso", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "falha", Toast.LENGTH_SHORT).show()
                }
        }

    }
                        //FAZ UPLOAD DA CAMERA PARA O FIREBASE

    private fun uploadCamera() {
        val idUsuarioLogado = autenticar.currentUser?.uid
        val nomeImagem = UUID.randomUUID().toString()

        val outputStream = ByteArrayOutputStream()
        bitmapImagemSelecionada?.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            outputStream
        )

        if ( bitmapImagemSelecionada != null && idUsuarioLogado != null) {
            armazenamento
                .getReference("fotos")
                .child(idUsuarioLogado)
                .child("foto.jpg")
                .putBytes( outputStream.toByteArray() )
                .addOnSuccessListener {task ->

                    Toast.makeText(this,
                        "Sucesso ao fazer Upload",
                        Toast.LENGTH_SHORT).
                    show()

                    task.metadata?.reference?.downloadUrl
                        ?.addOnSuccessListener { urlFirebase ->

                            Toast.makeText(this,
                                urlFirebase.toString(),
                                Toast.LENGTH_SHORT).
                            show()

                        }?.addOnFailureListener {erro ->

                            Toast.makeText(this,
                                "Erro ao fazer Upload",
                                Toast.LENGTH_SHORT).
                            show()
                        }

                }.addOnFailureListener {erro ->

                    Toast.makeText(this,
                        "Erro ao fazer Upload",
                        Toast.LENGTH_SHORT).
                    show()
                }
        }
    }
                        //FAZ UPLOAD DA GALERIA PARA O FIREBASE

    private fun uploadGaleria() {
        val idUsuarioLogado = autenticar.currentUser?.uid
        val nomeImagem = UUID.randomUUID().toString()
        if ( uriImagemSelecionada != null && idUsuarioLogado != null) {
            armazenamento
                .getReference("fotos")
                .child(idUsuarioLogado)
                .child("foto.jpg")
                .putFile( uriImagemSelecionada!! )
                .addOnSuccessListener {task ->

                    Toast.makeText(this,
                        "Sucesso ao fazer Upload",
                        Toast.LENGTH_SHORT).
                    show()

                    task.metadata?.reference?.downloadUrl
                        ?.addOnSuccessListener { urlFirebase ->

                            Toast.makeText(this,
                                urlFirebase.toString(),
                                Toast.LENGTH_SHORT).
                            show()

                        }?.addOnFailureListener {erro ->

                            Toast.makeText(this,
                                "Erro ao fazer Upload",
                                Toast.LENGTH_SHORT).
                            show()
                        }

                }.addOnFailureListener {erro ->

                    Toast.makeText(this,
                        "Erro ao fazer Upload",
                        Toast.LENGTH_SHORT).
                    show()
                }
        }
    }
}
