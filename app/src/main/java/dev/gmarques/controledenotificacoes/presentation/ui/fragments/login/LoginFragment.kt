package dev.gmarques.controledenotificacoes.presentation.ui.fragments.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.viewModels
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentLoginBinding
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener

/**
 * Criado por Gilian Marques
 * Em domingo, 27 de abril de 2025 as 17:55.
 */
class LoginFragment : MyFragment() {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var binding: FragmentLoginBinding
    private val signInLauncher = setupSignInLauncher()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentLoginBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        awaitLottieAnim()
        setupFabs()
        setupTvGuest()
        observeViewModelEvents()
        observeStates()
    }

    private fun setupTvGuest() = with(binding) {
        tvGuest.setOnClickListener(AnimatedClickListener {
            viewModel.startLoginFlow(true)
        })

    }

    /**
     * Exibe a UI apenas quando a animação do lotie esta carregada
     */
    private fun awaitLottieAnim() {
        binding.lottieView.addLottieOnCompositionLoadedListener { binding.clContent.visibility = View.VISIBLE }
    }

    /**
     * Configura o ActivityResultLauncher para o fluxo de login do FirebaseUI.
     * @return O ActivityResultLauncher configurado.
     */
    private fun setupSignInLauncher(): ActivityResultLauncher<Intent?> {
        return registerForActivityResult(
            FirebaseAuthUIActivityResultContract()
        ) { result ->

            if (result.resultCode == android.app.Activity.RESULT_OK) viewModel.notifyLoginSuccess()
            else viewModel.notifyLoginFailed(result.idpResponse)
        }
    }


    /**
     * Observa os eventos do ViewModel para reagir a diferentes estados do fluxo de login.
     */
    private fun observeViewModelEvents() {
        collectFlow(viewModel.eventsFlow) { event ->
            when (event) {

                is LoginEvent.StartFlow -> {
                    startLogin(event.asGuest)
                }

                is LoginEvent.Success -> {
                    findNavControllerMain().navigate(
                        LoginFragmentDirections.toSplashFragment(event.user)
                    )
                }

                is LoginEvent.Error -> {
                    vibrator.error()
                }
            }
        }
    }

    /**
     * Adequa a interface de acordo com o estado do fluxo de login
     */
    private fun observeStates() {
        collectFlow(viewModel.statesFlow) { state ->
            when (state) {
                is State.Error -> {
                    binding.fabTryAgain.visibility = View.VISIBLE
                    binding.tvInfo.visibility = View.VISIBLE
                    binding.fabLogin.visibility = View.INVISIBLE
                    binding.tvGuest.visibility = View.INVISIBLE
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.tvInfo.text = state.message
                }

                State.Idle -> {
                    binding.fabTryAgain.visibility = View.INVISIBLE
                    binding.tvInfo.visibility = View.INVISIBLE
                    binding.fabLogin.visibility = View.VISIBLE
                    binding.tvGuest.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.INVISIBLE

                }

                State.LoginIn -> {
                    binding.tvInfo.visibility = View.INVISIBLE
                    binding.tvGuest.visibility = View.INVISIBLE
                    binding.fabLogin.visibility = View.INVISIBLE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.fabTryAgain.visibility = View.INVISIBLE

                }
            }
        }
    }


    /**
     * Configura os botões de ação flutuante (FABs) para iniciar o fluxo de login.
     */
    private fun setupFabs() {
        binding.fabLogin.setOnClickListener { viewModel.startLoginFlow(false) }
        binding.fabTryAgain.setOnClickListener { viewModel.startLoginFlow(false) }
    }


    /**
     * Inicia o fluxo de login usando o FirebaseUI.
     */
    private fun startLogin(asGuest: Boolean) {

        if (asGuest) {
            Firebase.auth.signInAnonymously()
                .addOnSuccessListener {
                    viewModel.notifyLoginSuccess()
                }
            return
        }

        val providers = listOf(
            AuthUI.IdpConfig.GoogleBuilder()
                .build()
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.ic_launcher_foreground)
            .setTheme(R.style.AppTheme)
            .build()

        signInLauncher.launch(signInIntent)
    }


}
