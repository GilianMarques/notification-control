package dev.gmarques.controledenotificacoes.presentation.ui.fragments.home


import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.core.view.isEmpty
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.transition.ChangeBounds
import androidx.transition.TransitionSet
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.github.zawadz88.materialpopupmenu.popupMenu
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.data.local.PreferencesImpl
import dev.gmarques.controledenotificacoes.databinding.FragmentHomeBinding
import dev.gmarques.controledenotificacoes.databinding.ViewWarningBatteryOptimizationsBinding
import dev.gmarques.controledenotificacoes.databinding.ViewWarningListenNotificationPermissionBinding
import dev.gmarques.controledenotificacoes.databinding.ViewWarningPostNotificationsPermissionBinding
import dev.gmarques.controledenotificacoes.domain.usecase.installed_apps.GetInstalledAppIconUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.user.GetUserUseCase
import dev.gmarques.controledenotificacoes.presentation.model.ManagedAppWithRule
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.ui.activities.SlidingPaneController
import dev.gmarques.controledenotificacoes.presentation.ui.activities.SlidingPaneController.SlidingPaneControllerCallback
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.AutoFitGridLayoutManager
import dev.gmarques.controledenotificacoes.presentation.utils.SlideTransition
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.addViewWithTwoStepsAnimation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * Fragment responsável por exibir a lista de aplicativos controlados.
 */
@AndroidEntryPoint
class HomeFragment : MyFragment() {

    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: ManagedAppsAdapter

    @Inject
    lateinit var getInstalledAppIconUseCase: GetInstalledAppIconUseCase

    @Inject
    lateinit var getUserUseCase: GetUserUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()

        // Transição de entrada
        sharedElementEnterTransition = TransitionSet().apply {
            addTransition(ChangeBounds())
            addTransition(SlideTransition())
            interpolator = AccelerateDecelerateInterpolator()
            duration = 420
        }

        // Transição de retorno
        sharedElementReturnTransition = TransitionSet().apply {
            addTransition(ChangeBounds())
            addTransition(SlideTransition())
            interpolator = AccelerateDecelerateInterpolator()
            duration = 350
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentHomeBinding.inflate(inflater, container, false).also {

        binding = it
        setupActionBar()
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            setupPopUpMenu()
            setupRecyclerView()
            observeViewModel()
            setupFabAddManagedApp()
            setupSearch()
        }
    }

    private fun setupPopUpMenu() {
        val popupMenu = popupMenu {


            section {

                item {
                    label = getString(R.string.Feedback)
                    icon = R.drawable.vec_feedback
                    callback = {
                        showHowToFeedbackDialog()
                    }
                }
            }

            section {

                item {
                    label = getString(R.string.Configuracoes)
                    icon = R.drawable.vec_settings
                    callback = {
                        navigateToSettingsFragment()
                    }
                }
            }

            section {

                item {
                    label = getString(R.string.Echo)
                    icon = R.drawable.vec_echo
                    callback = {
                        navigateToEchoFragment()
                    }
                }
            }

        }

        binding.ivMenu.setOnClickListener(AnimatedClickListener {
            popupMenu.show(this@HomeFragment.requireContext(), binding.ivMenu)
        })
    }

    private fun navigateToEchoFragment() {
        val navigate = { findNavControllerMain().navigate(HomeFragmentDirections.toEchoFragment()) }
        if (App.largeScreenDevice) requireMainActivity().slidingPaneController?.showOnlyMaster(navigate)
        else navigate()
    }

    private fun navigateToSettingsFragment() {

        val navigate = { findNavControllerMain().navigate(HomeFragmentDirections.toSettingsFragment()) }
        if (App.largeScreenDevice) requireMainActivity().slidingPaneController?.showOnlyMaster(navigate)
        else navigate()
    }

    private fun setupActionBar() = binding.apply {

        val user = getUserUseCase() ?: error("É necessário estar logado para chegar nesse ponto.")

        binding.tvUserName.text = user.name

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        tvGreetings.text = when (currentHour) {
            in 0..11 -> getString(R.string.Bom_dia)
            in 12..17 -> getString(R.string.Boa_tarde)
            else -> getString(R.string.Boa_noite)
        }

        user.photoUrl.let { photoUrl ->
            Glide.with(root.context).load(photoUrl).placeholder(R.drawable.ic_launcher_foreground)
                .transition(DrawableTransitionOptions.withCrossFade()).circleCrop().into(ivProfilePicture)
        }

        val views = listOf(ivProfilePicture, tvUserName, tvGreetings)

        views.forEach {
            it.setOnClickListener(AnimatedClickListener {
                val extras = FragmentNavigatorExtras(
                    tvUserName to tvUserName.transitionName,
                    ivProfilePicture to ivProfilePicture.transitionName,
                    divider to divider.transitionName,
                )
                val navigate = { findNavControllerMain().navigate(HomeFragmentDirections.toProfileFragment(), extras) }
                if (App.largeScreenDevice) requireMainActivity().slidingPaneController?.showOnlyMaster(navigate)
                else navigate()

            })
        }
    }

    private fun setupFabAddManagedApp() = with(binding) {
        fabAdd.setOnClickListener(AnimatedClickListener {
            requireMainActivity().slidingPaneController?.showOnlyMaster()

            binding.edtSearch.setText("")
            val extras = FragmentNavigatorExtras(
                binding.fabAdd to binding.fabAdd.transitionName
            )
            findNavControllerMain().navigate(HomeFragmentDirections.toAddManagedAppsFragment(), extras)
        })
    }

    /**
     * Configura o RecyclerView, seu adapter e a lógica de layout adaptativo para alternar
     * entre visualizações em lista e grade conforme a largura disponível ou estado do painel lateral.
     *
     * @see setupManagedAppsAdapter
     * @see createResponsiveLayoutManager
     * @see setupPaneAnimationListener
     */
    private fun setupRecyclerView() = with(binding) {
        setupManagedAppsAdapter()

        val layoutManager = createResponsiveLayoutManager()
        setupPaneAnimationListener(layoutManager)

        rvApps.layoutManager = layoutManager
        rvApps.adapter = adapter

        rvApps.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    /**
     * Instancia e configura o adapter que será usado pelo RecyclerView para exibir os apps gerenciados.
     *
     * @see createResponsiveLayoutManager
     */
    private fun setupManagedAppsAdapter() {
        adapter = ManagedAppsAdapter(
            getDrawable(R.drawable.vec_rule_permissive_small),
            getDrawable(R.drawable.vec_rule_restrictive_small),
            getDrawable(R.drawable.vec_dot_notification_indicator),
            getInstalledAppIconUseCase,
            ::navigateToViewManagedAppFragment
        )
    }

    /**
     * Cria um `AutoFitGridLayoutManager` que calcula automaticamente a quantidade de colunas
     * com base na largura disponível. Força o RecyclerView a recriar as views ao alternar
     * entre visualização em lista e em grade.
     *
     * Durante as animaçoes do  [SlidingPaneController] as mudanças de SpanCount do [AutoFitGridLayoutManager] são ignoradas
     * isso acontece porque [setupPaneAnimationListener] sabe quando o painel esta abrindo/fechando e atualiza o
     * adapter com spancount e o redefine no recyclerview. ISso gera uma transição suave entre as views de lista e grade.
     *
     * @return Um layout manager configurado.
     *
     * @see setupManagedAppsAdapter
     */
    private fun createResponsiveLayoutManager(): AutoFitGridLayoutManager {
        return AutoFitGridLayoutManager(requireContext(), 280) { spanCount ->

            if (requireMainActivity().slidingPaneController?.isAnimating == true) return@AutoFitGridLayoutManager

            val old = adapter.useGridView
            adapter.setUseGridView(spanCount)
            //Só é necessário reatribuir o adapter se tiver que alternar entre views de lista/grade, caso contrario o proprio
            //layoutmanager ajusta as colunas e tamanhos das views
            if (old != adapter.useGridView) { // TODO: testar
                binding.rvApps.adapter = null
                binding.rvApps.adapter = adapter
            }

        }
    }

    /**
     * Adiciona um listener ao `SlidingPaneController` para reatribuir o adapter nos momentos certos da
     * animação de abertura ou fechamento do painel, garantindo uma transição visual suave.
     *
     * @param layoutManager Layout manager usado no RecyclerView.
     *
     * @see createResponsiveLayoutManager
     */
    private fun setupPaneAnimationListener(layoutManager: AutoFitGridLayoutManager) {
        requireMainActivity().slidingPaneController
            ?.addStateListener(this@HomeFragment, object : SlidingPaneControllerCallback {
                override fun onAnimationStarted(currentState: SlidingPaneController.SlidingPaneState) {
                    binding.rvApps.adapter = null
                }

                override fun onAnimationEnd(newState: SlidingPaneController.SlidingPaneState) {
                    adapter.setUseGridView(layoutManager.spanCount)
                    binding.rvApps.adapter = adapter
                }
            })
    }
    private fun navigateToViewManagedAppFragment(app: ManagedAppWithRule) {

        binding.edtSearch.setText("")

        val extras = FragmentNavigatorExtras(
            binding.ivProfilePicture to "view_app_icon",
            binding.tvUserName to "view_app_name",
            binding.ivMenu to "view_menu",
            binding.divider to "divider",
            binding.fabAdd to "fab",
        )

        if (App.largeScreenDevice) lifecycleScope.launch {
            val navigate = {
                findNavControllerDetails()!!.navigate(
                    R.id.viewManagedAppFragment,
                    bundleOf("app" to app),
                    NavOptions
                        .Builder()
                        .setPopUpTo(R.id.viewManagedAppFragment, true)
                        .build()
                )
            }
            requireMainActivity().slidingPaneController?.showMasterAndDetails(navigate)

        } else {
            // Navegação padrão (Celular)
            findNavControllerMain().navigate(HomeFragmentDirections.toViewManagedAppFragment(app = app), extras)
        }
    }

    /**
     * Carrega os icones usados nas pelo recyclerview nas regras para indicar o tipo de regra (restritiva ou permissiva)
     */
    private fun getDrawable(id: Int): Drawable {
        return ResourcesCompat.getDrawable(resources, id, requireActivity().theme)
            ?: throw IllegalStateException("Drawable not found: $id")
    }

    /**
     * Observe a lista de ManagedAppWithRules no [HomeViewModel] e envia os dados para o [ManagedAppsAdapter]
     */
    private fun observeViewModel() = lifecycleScope.launch {
        collectFlow(viewModel.managedAppsWithRules) { apps ->

            adapter.submitList(apps)

            binding.progressBar.isGone = apps != null
            binding.edtSearch.isVisible = (apps?.size ?: 0) > 9

            lifecycleScope.launch {
                delay(300)
                binding.emptyView.isGone = apps?.size != 0

            }
        }
    }

    private fun setupSearch() {
        binding.edtSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.managedAppsWithRules.value.let {
                it?.let { adapter.submitList(it, text.toString().trim()) }
            }
        }
    }


    override fun onResume() {
        super.onResume()

        if (binding.containerWarnings.isEmpty()) lifecycleScope.launch {
            delay(500)

            if (!requireMainActivity().isNotificationListenerEnabled()) {
                showListenNotificationWarning()
                return@launch
            }

            if (!requireMainActivity().isAppInsetFromBatterySaving()) {
                showBatteryRestrictionsWarning()
                return@launch
            }

            if (!requireMainActivity().isPostNotificationsPermissionEnable()) {
                if (PreferencesImpl.showWarningCardPostNotification.isDefault()) {
                    showPostNotificationRestrictionsWarning()
                    return@launch
                }
            }
        }

    }

    private fun showListenNotificationWarning() {

        val warningBinding = ViewWarningListenNotificationPermissionBinding.inflate(layoutInflater)

        warningBinding.chipPrivacy.setOnClickListener(AnimatedClickListener {
            MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.Sua_privacidade_importa))
                .setMessage(getString(R.string.Sua_privacidade_esta_protegida))
                .setPositiveButton(getString(R.string.Entendi)) { dialog, _ ->
                }.setIcon(R.drawable.vec_info).show()
        })

        warningBinding.chipGivePermission.setOnClickListener(AnimatedClickListener {
            requireMainActivity().requestNotificationAccessPermission()
            removerWarning(warningBinding.root)
        })

        binding.containerWarnings.addViewWithTwoStepsAnimation(warningBinding.root)
    }

    private fun showBatteryRestrictionsWarning() {

        val warningBinding = ViewWarningBatteryOptimizationsBinding.inflate(layoutInflater)

        warningBinding.chipRemoveRestriction.setOnClickListener(AnimatedClickListener {
            requireMainActivity().requestIgnoreBatteryOptimizations()
            removerWarning(warningBinding.root)
        })

        warningBinding.chipRestrictionRemoved.setOnClickListener(AnimatedClickListener {
            removerWarning(warningBinding.root)
            //showDialogBatteryRestrictionRemoved()
        })

        binding.containerWarnings.addViewWithTwoStepsAnimation(warningBinding.root)
    }

    private fun showPostNotificationRestrictionsWarning() {

        val warningBinding = ViewWarningPostNotificationsPermissionBinding.inflate(layoutInflater)

        warningBinding.chipGivePermission.setOnClickListener(AnimatedClickListener {
            requireMainActivity().requestPostNotificationsPermission()
            removerWarning(warningBinding.root)
        })

        binding.containerWarnings.addViewWithTwoStepsAnimation(warningBinding.root)
    }

    private fun removerWarning(view: View) {
        lifecycleScope.launch {
            delay(1000)
            binding.containerWarnings.removeView(view)
        }
    }

    private fun showHowToFeedbackDialog() {
        MaterialAlertDialogBuilder(requireActivity()).setTitle(getString(R.string.Enviar_feedback))
            .setIcon(R.drawable.vec_info)
            .setMessage(getString(R.string.Como_voc_gostaria_de_enviar_seu_feedback))
            .setPositiveButton(getString(R.string.Comentar_na_play_store)) { _, _ ->
                openPlayStore()
            }.setNegativeButton(getString(R.string.enviar_um_e_mail_ao_desenvolvedor)) { _, _ ->
                openMailToSendFeedback()
            }.show()
    }

    private fun openMailToSendFeedback() {
        val email = App.instance.remoteConfigValues.value?.contactEmail
        if (email == null) return

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(
                Intent.EXTRA_TEXT,
                (getString(R.string.Insira_aqui_suas_duvidas_sugestoes_de_melhorias_e_funcionalidades_ou_problemas_que_ocorreram_durante_o_uso))
            )
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.Feedback_do_app))
        }

        if (intent.resolveActivity(App.instance.packageManager) != null) {
            App.instance.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } else {
            Toast.makeText(App.instance, getString(R.string.Nenhum_app_de_e_mail_encontrado), Toast.LENGTH_SHORT).show()
        }
    }

}


