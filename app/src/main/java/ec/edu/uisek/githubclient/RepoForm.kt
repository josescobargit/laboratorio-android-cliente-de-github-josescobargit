package ec.edu.uisek.githubclient

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ec.edu.uisek.githubclient.databinding.ActivityRepoFormBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.models.RepoRequest
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepoForm : AppCompatActivity() {

    private lateinit var binding: ActivityRepoFormBinding
    private var repoToEdit: Repo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRepoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Recuperar repo enviado desde MainActivity
        repoToEdit = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("EXTRA_REPO", Repo::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("EXTRA_REPO") as? Repo
        }

        // 2. Modo edición o creación
        if (repoToEdit != null) {
            prepareEditMode(repoToEdit!!)
        } else {
            prepareCreateMode()
        }

        binding.cancelButton.setOnClickListener { finish() }
    }

    // ================================================
    // MODO EDICIÓN
    // ================================================
    private fun prepareEditMode(repo: Repo) {
        supportActionBar?.title = "Editar repositorio"
        binding.saveButton.text = "Actualizar"

        binding.repoNameInput.setText(repo.name)
        binding.repoDescriptionInput.setText(repo.description)

        // 🔒 No se permite cambiar el nombre del repo
        binding.repoNameInput.isEnabled = false
        binding.repoNameInput.alpha = 0.5f

        binding.saveButton.setOnClickListener {
            updateRepo(repo)
        }
    }

    // ================================================
    // MODO CREAR
    // ================================================
    private fun prepareCreateMode() {
        supportActionBar?.title = "Nuevo repositorio"
        binding.saveButton.text = "Crear"

        binding.repoNameInput.isEnabled = true
        binding.repoNameInput.alpha = 1f

        binding.saveButton.setOnClickListener {
            createRepo()
        }
    }

    // ================================================
    // VALIDACIÓN
    // ================================================
    private fun validateForm(): Boolean {
        val repoName = binding.repoNameInput.text.toString().trim()

        if (repoName.isBlank()) {
            binding.repoNameInput.error = "El nombre del repositorio es requerido"
            return false
        }
        if (repoName.contains(" ")) {
            binding.repoNameInput.error = "El nombre no puede contener espacios"
            return false
        }
        return true
    }

    // ================================================
    // CREAR REPO
    // ================================================
    private fun createRepo() {
        if (!validateForm()) return

        val repoName = binding.repoNameInput.text.toString().trim()
        val repoDescription = binding.repoDescriptionInput.text.toString().trim()
        val repoRequest = RepoRequest(repoName, repoDescription)

        RetrofitClient.gitHubApiService.addRepo(repoRequest) // <--- CORREGIDO
            .enqueue(object : Callback<Repo> {
                override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                    if (response.isSuccessful) {
                        showMessage("Repositorio creado exitosamente")
                        finish()
                    } else {
                        showMessage("Error al crear: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Repo>, t: Throwable) {
                    showMessage("Error: ${t.message}")
                    Log.e("RepoForm", "createRepo error", t)
                }
            })
    }

    // ================================================
    // ACTUALIZAR REPO
    // ================================================
    private fun updateRepo(originalRepo: Repo) {
        if (!validateForm()) return

        val newDescription = binding.repoDescriptionInput.text.toString().trim()

        // 🔥 No intentamos cambiar el nombre
        val repoRequest = RepoRequest(
            name = originalRepo.name,
            description = newDescription
        )

        RetrofitClient.gitHubApiService.updateRepo(
            owner = originalRepo.owner.login,
            repoName = originalRepo.name,
            repoRequest = repoRequest
        ).enqueue(object : Callback<Repo> {

            override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                if (response.isSuccessful) {
                    showMessage("Repositorio actualizado exitosamente")
                    finish()
                } else {
                    showMessage("Error al actualizar: ${response.code()}")
                    Log.e("RepoForm", "Error body: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Repo>, t: Throwable) {
                showMessage("Error al actualizar: ${t.message}")
                Log.e("RepoForm", "updateRepo error", t)
            }
        })
    }

    // ================================================
    // MENSAJES
    // ================================================
    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}