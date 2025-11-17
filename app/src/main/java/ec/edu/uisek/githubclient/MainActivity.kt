package ec.edu.uisek.githubclient

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import ec.edu.uisek.githubclient.databinding.ActivityMainBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.services.GithubApiService
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var reposAdapter: ReposAdapter
    private val apiService: GithubApiService = RetrofitClient.gitHubApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        binding.newRepoFab.setOnClickListener {
            displayNewRepoForm()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchRepositories()
    }

    // ----------------------------------------------------------------------
    // RECYCLER VIEW
    // ----------------------------------------------------------------------
    private fun setupRecyclerView() {
        reposAdapter = ReposAdapter(
            onEditClicked = { repo ->
                displayEditRepoForm(repo)
            },
            onDeleteClicked = { repo ->
                showDeleteConfirmationDialog(repo)
            }
        )
        binding.reposRecyclerView.adapter = reposAdapter
    }

    // ----------------------------------------------------------------------
    // ELIMINAR - CONFIRMACIÓN
    // ----------------------------------------------------------------------
    private fun showDeleteConfirmationDialog(repo: Repo) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar repositorio")
            .setMessage("¿Deseas eliminar '${repo.name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteRepository(repo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ----------------------------------------------------------------------
    // ELIMINAR - API
    // ----------------------------------------------------------------------
    private fun deleteRepository(repo: Repo) {
        val call = apiService.deleteRepo(repo.owner.login, repo.name)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showMessage("Repositorio eliminado correctamente")
                    fetchRepositories()
                } else {
                    showMessage("Error al eliminar: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showMessage("Error de conexión: ${t.message}")
            }
        })
    }

    // ----------------------------------------------------------------------
    // OBTENER REPOS
    // ----------------------------------------------------------------------
    private fun fetchRepositories() {
        val call = apiService.getRepos()

        call.enqueue(object: Callback<List<Repo>> {
            override fun onResponse(call: Call<List<Repo>>, response: Response<List<Repo>>) {
                if (response.isSuccessful) {
                    val repos = response.body()
                    if (repos != null && repos.isNotEmpty()) {
                        reposAdapter.updateRepositories(repos)
                    } else {
                        showMessage("No se encontraron repositorios")
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "No Autorizado"
                        403 -> "Prohibido"
                        404 -> "No Encontrado"
                        else -> "Error ${response.code()}"
                    }
                    showMessage("Error: $errorMessage")
                }
            }

            override fun onFailure(call: Call<List<Repo>>, t: Throwable) {
                showMessage("No se pudieron cargar los repositorios")
            }
        })
    }

    // ----------------------------------------------------------------------
    // FORMULARIOS
    // ----------------------------------------------------------------------
    private fun displayEditRepoForm(repo: Repo) {
        val intent = Intent(this, RepoForm::class.java).apply {
            putExtra("EXTRA_REPO", repo)
        }
        startActivity(intent)
    }

    private fun displayNewRepoForm() {
        val intent = Intent(this, RepoForm::class.java)
        startActivity(intent)
    }

    // ----------------------------------------------------------------------
    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
