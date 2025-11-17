package ec.edu.uisek.githubclient.services

import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.models.RepoRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubApiService {

    @GET("user/repos")
    fun getRepos(
        @Query("sort") sort: String = "created",
        @Query("direction") direction: String = "desc"
    ): Call<List<Repo>>

    // Mantengo tu nombre: addRepo (no createRepo)
    @POST("user/repos")
    fun addRepo(
        @Body repoRequest: RepoRequest
    ): Call<Repo>

    // NUEVA FUNCIÓN: actualizar repos (PATCH)
    @PATCH("repos/{owner}/{repo}")
    fun updateRepo(
        @Path("owner") owner: String,
        @Path("repo") repoName: String,
        @Body repoRequest: RepoRequest
    ): Call<Repo>

    // NUEVA FUNCIÓN: eliminar repos (DELETE)
    @DELETE("repos/{owner}/{repo}")
    fun deleteRepo(
        @Path("owner") owner: String,
        @Path("repo") repoName: String
    ): Call<Void>
}
