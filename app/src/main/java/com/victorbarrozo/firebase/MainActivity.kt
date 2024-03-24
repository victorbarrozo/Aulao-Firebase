package com.victorbarrozo.firebase

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.victorbarrozo.firebase.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    val binding by lazy{
        ActivityMainBinding.inflate(layoutInflater)
    }
    val autenticar by lazy {
        FirebaseAuth.getInstance()
    }
    val db by lazy{
        FirebaseFirestore.getInstance()
    }

    override fun onStart() {
        super.onStart()
       // verificarUsuarioLogado()
        logarUsuario()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.btnExecultar.setOnClickListener {
          logarUsuario()
           // salvaDados()
           // atualizarRemoverDados()
            //salvaDados()
           //cadastrarUsuario()
           // salvarDadosUsuario("")
            //listaDados()
           // pesquisarUsuarios()
        }

    }
    private fun pesquisarUsuarios() {
        val refUsuario = db
            .collection("usuarios")

            //            FILTROS DE POSQUISA

            //.whereEqualTo("nome", "Paulo victor")
            //.whereNotEqualTo("nome", "Paulo victor")
            //.whereIn("nome", listOf("Tanylly nunes", "Paulo victor"))
            //.whereNotIn("nome", listOf("Tanylly nunes"))
            //.whereArrayContains("conhecimentos", "")
            //.whereGreaterThan("idade", "1")
            //.whereGreaterThanOrEqualTo("idade", "34")
            //.whereLessThan("idade", "34")
            //.whereLessThan("idade", "7")
            //.orderBy("idade", Query.Direction.ASCENDING)
            .orderBy("idade", Query.Direction.ASCENDING)

            refUsuario.addSnapshotListener { querySnapshot, erro ->

            val listaDocumens = querySnapshot?.documents

            var listaResultados = ""
            listaDocumens?.forEach{documentSnapshot ->
                val dados = documentSnapshot?.data
                if (dados != null){
                    val nome = dados["nome"]
                    val idade = dados["idade"]

                    listaResultados += "nome: $nome idade: $idade\n"

                }
                binding.textResultado.text = listaResultados
            }
        }
    }
    private fun salvarDadosUsuario(nome: String, idade: String) {
        val idUsuarioLogado = autenticar.currentUser?.uid
        if (idUsuarioLogado != null){

            val dados = mapOf(
                "nome" to nome,
                "idade" to idade
            )
            db
                .collection("usuarios")
                .document(idUsuarioLogado)
                .set(dados)
        }
    }
    private fun atualizarRemoverDados() {
        var lista = mapOf(
            "nome" to "Tanylly",
            "idade" to "34",
            //"CPF" to "12544..."
        )
        val referenciaUsuario = db
            .collection("Usuario")
           // .document("1")
        //referenciaAna.set(lista)
        //referenciaAna.update
        referenciaUsuario
            //.delete()
            .add( lista )
            .addOnSuccessListener {
                exibirMensagem("Usuario atualizado com sucesso")
            }.addOnFailureListener {
                exibirMensagem("Falha ao atualizar o usuario")
            }

    }
    private fun listaDados(){
        val idUsuarioLogado = autenticar.currentUser?.uid
        if (idUsuarioLogado != null){
            val referenciaUsuario = db
                .collection("usuarios")
                //.document(idUsuarioLogado)
           /* referenciaUsuario
                .get()
                .addOnSuccessListener {documentSnapshot ->
                    val dados = documentSnapshot.data
                    if (dados != null){
                        val nome = dados["nome"]
                        val idade = dados ["idade"]
                        val texto = "nome: $nome - Idade: $idade"

                        binding.textResultado.text = texto
                    }
                }.addOnFailureListener {
            }*/

            referenciaUsuario.addSnapshotListener { querySnapshot, erro ->
               /* val dados = documentSnapshot?.data
                if (dados != null){
                    val nome = dados["nome"]
                    val idade = dados ["idade"]
                    val texto = "nome: $nome - Idade: $idade"

                    binding.textResultado.text = texto
                }*/

                val listaDocuments = querySnapshot?.documents
                var listaResultaro = ""
                listaDocuments?.forEach{ documentSnapshot ->
                    val dados = documentSnapshot.data
                    if (dados != null) {
                        val nome = dados["nome"]
                        val idade = dados["idade"]

                        listaResultaro += "Nome: $nome - idade: $idade\n "

                    }
                    binding.textResultado.text = listaResultaro
                }
            }
        }
    }
    private fun salvaDados() {

        var lista = mapOf(
            "nome" to "Victor",
            "idade" to "34"
        )

        db
            .collection("Usuario")
            .document("2")
            .set(lista)
            .addOnSuccessListener {
                exibirMensagem("Usuario salvo com sucesso")
            }.addOnFailureListener {exception ->
                val erro = exception.message
                exibirMensagem("Erro ao salvar usuario: $erro")
            }
    }
    private fun cadastrarUsuario() {
        val email = "tanylly2@gmail.com"
        val senha = "T@any.1989"
        val nome = "Tanylly nunes"
        val idade = "34"

        val autenticacao = autenticar.createUserWithEmailAndPassword(
            email, senha
        ).addOnSuccessListener { authResult ->
            val email = authResult.user?.email
            var idUsuario = authResult.user?.uid

            if (idUsuario != null) {
                salvarDadosUsuario(nome, idade)
            }


            //exibirMensagem("Sucesso ao cadastrar usúario")
            binding.textResultado.text = "Sucesso ao cadastrar usúario $email - $idUsuario"
        }.addOnFailureListener {exception ->
            val mensagemErro = exception.message
            binding.textResultado.text = "Erro ao cadastrar usúario $mensagemErro"
        }
    }
    private fun logarUsuario() {
        val email =  "victor@gmail.com"
        val senha = "P@ulo.1990"

        autenticar.signInWithEmailAndPassword(
            email, senha
        ).addOnSuccessListener {
            binding.textResultado.text = "Sucesso ao logar usúario "
            exibirMensagem("Sucesso ao logar usúario")
            startActivity(Intent(this, UploadImagemActivity::class.java))


        }.addOnFailureListener {
            binding.textResultado.text = "Erro ao logar usuario"

        }

    }
    private fun verificarUsuarioLogado() {

        //autenticar.signOut()
        val usuario = autenticar.currentUser
        if (usuario!= null){
            exibirMensagem("Usúario está logado")
            startActivity(Intent(this, PrincipalActivity::class.java))
        }else{
            exibirMensagem("Usúario não está logado")

        }
    }
    private fun exibirMensagem(text: String) {
        Toast.makeText(this, "$text", Toast.LENGTH_SHORT).show()

    }
}