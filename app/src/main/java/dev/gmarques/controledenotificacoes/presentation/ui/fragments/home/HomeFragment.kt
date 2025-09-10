/*
 * MIT License
 *
 * Copyright (c) 2025 Gilian Marques Fernandes - linkedin.com/in/gilianmarques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package dev.gmarques.controledenotificacoes.presentation.ui.fragments.home

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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
import dev.gmarques.controledenotificacoes.domain.framework.contracts.SystemNotificationManager
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.presentation.model.ManageableNotification
import dev.gmarques.controledenotificacoes.presentation.model.ManagedAppWithRule
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import dev.gmarques.controledenotificacoes.presentation.ui.activities.PaneResizer
import dev.gmarques.controledenotificacoes.presentation.ui.activities.PaneResizer.PaneResizeListener
import dev.gmarques.controledenotificacoes.presentation.ui.activities.SlidingPaneController
import dev.gmarques.controledenotificacoes.presentation.ui.activities.SlidingPaneController.SlidingPaneControllerCallback
import dev.gmarques.controledenotificacoes.presentation.ui.activities.SlidingPaneController.SlidingPaneState
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.date_picker.DateTimePickerFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.manage_notifications.ManageNotificationsViewModel
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.manage_notifications.NotificationMenuAction
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.view_managed_app.ViewManagedAppFragment
import dev.gmarques.controledenotificacoes.presentation.ui.fragments.view_managed_app.ViewManagedAppFragment.Companion.NavigationCallback
import dev.gmarques.controledenotificacoes.presentation.utils.AnimatedClickListener
import dev.gmarques.controledenotificacoes.presentation.utils.AutoFitGridLayoutManager
import dev.gmarques.controledenotificacoes.presentation.utils.SlideTransition
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.addViewWithTwoStepsAnimation
import dev.gmarques.controledenotificacoes.presentation.utils.ViewExtFuns.rebindAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * Fragment responsável por exibir a lista de aplicativos controlados.
 */
@AndroidEntryPoint
class HomeFragment : MyFragment() {


    @Inject
    lateinit var systemNotificationManager: SystemNotificationManager

    private val viewModel: HomeViewModel by activityViewModels()

    /**permite obter as notificaçõesa tivas sem duplica codigo*/
    private val viewModelManageNotifications: ManageNotificationsViewModel by activityViewModels()

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: ManagedAppsAdapter

    /**Permite ajustar o tamanho do painel manualmente*/
    private var paneResizer: PaneResizer? = null

    /**Permite alternar a visibilidade do painel de detalhes*/
    private var slidingPaneController: SlidingPaneController? = null

    /**fragmento de detalhes*/
    private var mViewManagedAppFragment: ViewManagedAppFragment? = null

    companion object {
        private const val IS_APP_BAR_EXPANDED = "app_bar_expanded"
    }

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
            setupRecyclerView()
            setupPopUpMenu()
            observeViewModel()
            setupFabAddManagedApp()
            setupSearch()
            setupActiveNotificationsView()
            setupAppbar()
            setupForTablet()
            setupOnBackPressedListener()
        }

    }

    override fun onResume() {
        super.onResume()

        if (viewModel.currentAppOpen != null) openDetailsFragment(viewModel.currentAppOpen!!)

        lifecycleScope.launch {

            binding.containerWarnings.removeAllViews() // necessario por causa do bug da tela de bateria em alguns sistemas

            if (!requireMainActivity().isListenNotificationPermissionGranted()) {
                showListenNotificationWarning()
                return@launch
            }

            if (!requireMainActivity().isPostNotificationsPermissionEnable()) {
                showPostNotificationRestrictionsWarning()
                return@launch
            }

            if (!requireMainActivity().isAppInsetFromBatterySaving()) {
                delay(1500)
                if (!requireMainActivity().isAppInsetFromBatterySaving()) showBatteryRestrictionsWarning()
                return@launch
            }

        }
    }

    private fun setupAppbar() = with(binding) {

        val expanded =
            findNavController().currentBackStackEntry?.savedStateHandle?.get<Boolean>(IS_APP_BAR_EXPANDED) ?: true

        appbar.setExpanded(expanded, false)
        appbar.addOnOffsetChangedListener { _, verticalOffset ->
            findNavController().currentBackStackEntry?.savedStateHandle?.set(IS_APP_BAR_EXPANDED, verticalOffset == 0)
        }
    }

    private fun setupActiveNotificationsView() = with(binding) {

        if (rvNots == null) return@with

        val notsAdapter = ActiveNotificationsAdapter(object : ActiveNotificationsAdapter.Callback {
            override fun onMenuClicked(
                notification: ManageableNotification, ivMenu: AppCompatImageView
            ) {
                viewModelManageNotifications.createPopUpMenu(notification) { action ->
                    when (action) {
                        is NotificationMenuAction.PostNow -> viewModelManageNotifications.postSnoozedOrHiddenNotification(action.not)
                        is NotificationMenuAction.Snooze -> navigateToPickDateAndTime(action.not)
                        is NotificationMenuAction.Show -> viewModelManageNotifications.postSnoozedOrHiddenNotification(action.not)
                        is NotificationMenuAction.Hide -> viewModelManageNotifications.hideNotification(action.not)
                        is NotificationMenuAction.Manage -> navigateToAddManagedApp(action.not)
                        is NotificationMenuAction.RemoveFromDB -> viewModelManageNotifications.removeNotificationFromDB(action.not)
                        is NotificationMenuAction.Cancel -> viewModelManageNotifications.cancelNotification(action.not)
                        is NotificationMenuAction.Copy -> {
                            vibrator.success()
                            viewModelManageNotifications.copyTitleAndContent(action.not)
                        }
                    }
                }.show(this@HomeFragment.requireContext(), ivMenu)
            }
        })

        binding.rvNots!!.apply {

            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = notsAdapter
        }

        systemNotificationManager.doWhenConnected {

            // Só executa se a view existir
            val viewLifecycleOwner = viewLifecycleOwnerLiveData.value
            if (viewLifecycleOwner == null || !isAdded || view == null) {
                // Fragmento não está pronto, não mexe na UI
                return@doWhenConnected
            }

            lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    collectFlow(
                        systemNotificationManager.getActiveWithOngoingNotificationsFlow()
                    ) {

                        binding.llActiveNotifications?.isGone = it.isEmpty()

                        notsAdapter.submitList(it.map { actNot ->
                            ManageableNotification.from(system = actNot)
                        })
                    }
                }
            }
        }
    }

    private fun setupPopUpMenu() {

        val popupMenu = popupMenu {


            section {

                item {
                    label = getString(R.string.Gerenciar_notificacoes)
                    icon = R.drawable.vec_manage_notification
                    callback = {
                        navigateToManageNotificationsFragment()
                    }
                }
            }

            section {

                item {
                    label = getString(R.string.Op_es)
                    icon = R.drawable.vec_settings
                    callback = {
                        navigateToSettingsFragment()
                    }
                }

                item {
                    val echoEnabled = PreferencesImpl.echoEnabled.value
                    label = if (echoEnabled) getString(R.string.Desligar_echo) else getString(R.string.Ligar_echo)
                    icon = if (echoEnabled) R.drawable.vec_echo_off else R.drawable.vec_echo
                    callback = {
                        navigateToEchoFragment()
                    }
                }
            }

            section {

                item {
                    label = getString(R.string.Feedback)
                    icon = R.drawable.vec_feedback
                    callback = {
                        showHowToFeedbackDialog()
                    }
                }
            }

        }

        binding.ivMenu.setOnClickListener(AnimatedClickListener {
            popupMenu.show(this@HomeFragment.requireContext(), binding.ivMenu)
        })
    }

    /**
     * Configura um listener para o botão "voltar".
     * Em dispositivos de tela grande, se o fragmento [ViewManagedAppFragment] estiver visível,
     * ele é removido e as visualizações de tela única são restauradas. Caso contrário, a ação de "voltar" padrão é executada.
     */
    private fun setupOnBackPressedListener() {
        requireMainActivity()
            .onBackPressedDispatcher
            .addCallback(owner = this@HomeFragment) {
                if (mViewManagedAppFragment != null) {
                    closeDetailsFragment()
                    return@addCallback
                }
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
    }

    private fun setupActionBar() = binding.apply {

        val user = viewModel.getUser()

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

                findNavController().navigate(HomeFragmentDirections.toProfileFragment(), extras)

            })
        }
    }

    private fun setupFabAddManagedApp() = with(binding) {
        fabAdd.setOnClickListener(AnimatedClickListener {

            binding.edtSearch.setText("")
            val extras = FragmentNavigatorExtras(
                binding.fabAdd to binding.fabAdd.transitionName
            )
            findNavController().navigate(HomeFragmentDirections.toAddManagedAppsFragment(), extras)
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
            viewModel.getInstalledAppIcon(),
            ::navigateToViewManagedAppFragment
        )
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
        slidingPaneController?.addStateListener(this@HomeFragment, object : SlidingPaneControllerCallback {
            override fun onAnimationStarted(currentState: SlidingPaneState) {
                binding.rvApps.adapter = null
            }

            override fun onAnimationEnd(newState: SlidingPaneState) {
                adapter.setUseGridView(layoutManager.spanCount)
                binding.rvApps.adapter = adapter
            }
        })
    }

    private fun setupForTablet() = with(binding) {

        if (!App.largeScreenDevice) return@with

        if (masterContainer == null ||
            detailsContainer == null ||
            dragIndicator == null ||
            dragHandle == null
        ) {
            Log.e(
                "USUK",
                "MainActivity.setupForTablet: Essa view nao deve ser nula em dispositivos de tela grande dragIndicator: $dragIndicator dragHandle: $dragHandle"
            )
            return@with
        }
        paneResizer = PaneResizer(
            handleParent = dragIndicator,
            dragHandler = dragHandle,
            vibratorProvider = vibrator,
            listener = object : PaneResizeListener {
                /**
                 * Disparada quando o [PaneResizer] detecta alterações no tamanho do painel, para autalizar
                 * os valores no [SlidingPaneController] que por sua vez atualiza a posição do painel e salva em
                 * preferencias o novo valor padrao  do painel
                 */
                override fun onPaneResized(positionPercent: Float) {
                    slidingPaneController?.onPaneResizedByHand(positionPercent) ?: 0f
                }
            })
        slidingPaneController = SlidingPaneController(
            context = requireMainActivity(),
            masterPane = masterContainer,
            detailsPane = detailsContainer
        )

    }

    private fun setupSearch() {
        binding.edtSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.managedAppsWithRules.value.let {
                it?.let { adapter.submitList(it, text.toString().trim()) }
            }
        }
    }

    private fun navigateToEchoFragment() {
        findNavController().navigate(HomeFragmentDirections.toEchoFragment())
    }

    private fun navigateToSettingsFragment() {

        findNavController().navigate(HomeFragmentDirections.toSettingsFragment())
    }

    private fun navigateToManageNotificationsFragment() {
        findNavController().navigate(HomeFragmentDirections.toManageNotificationsFragment())
    }

    private fun navigateToPickDateAndTime(not: ManageableNotification) {

        setFragmentResultListener(DateTimePickerFragment.RESULT_KEY) { _, bundle ->
            val selectedTimestamp = bundle.getLong(DateTimePickerFragment.TIMESTAMP_KEY)
            viewModelManageNotifications.snoozeNotification(not, selectedTimestamp)
        }
        findNavController().navigate(HomeFragmentDirections.toDateTimePickerFragment(System.currentTimeMillis()))
    }

    private fun navigateToAddManagedApp(not: ManageableNotification) {
        findNavController().navigate(HomeFragmentDirections.toAddManagedAppsFragment(not.packageName))
    }

    private fun navigateToViewManagedAppFragment(app: ManagedAppWithRule) {

        binding.edtSearch.setText("")

        // para teblets
        if (App.largeScreenDevice) {
            openDetailsFragment(app)
            return
        }
        // Navegação padrão (Celular)
        findNavController().navigate(
            HomeFragmentDirections.toViewManagedAppFragment(app = app), FragmentNavigatorExtras(
                binding.ivProfilePicture to "view_app_icon",
                binding.tvUserName to "view_app_name",
                binding.ivMenu to "view_menu",
                binding.divider to "divider",
                binding.fabAdd to "fab",
            )
        )
    }

    /**
     * Abre o fragmento de detalhes para um aplicativo gerenciado específico.
     * Esta função é usada apenas em dispositivos de tela grande  onde
     * a lista de aplicativos e os detalhes de um aplicativo selecionado podem ser exibidos lado a lado.
     *
     * @param app O objeto [ManagedAppWithRule] contendo os dados do aplicativo a ser exibido.
     *
     * @see ViewManagedAppFragment
     */
    private fun openDetailsFragment(app: ManagedAppWithRule) {
        viewModel.currentAppOpen = app
        toggleSingleScreenViews(false)
        slidingPaneController?.showMasterAndDetails {
            mViewManagedAppFragment = ViewManagedAppFragment.newInstance(
                bundleOf("app" to app), object : NavigationCallback {

                    override fun navigateToEditRule(rule: Rule) {
                        findNavController().navigate(
                            HomeFragmentDirections.toAddRuleFragment(rule)
                        )
                    }

                    override fun navigateToSelectRule() {
                        findNavController().navigate(HomeFragmentDirections.toSelectRuleFragment())
                    }
                })
            parentFragmentManager.beginTransaction()
                .replace(R.id.details_fragment_host, mViewManagedAppFragment!!)
                .commit()

        }
        return
    }

    /**
     * Fecha o fragmento de detalhes.
     *
     * Esta função é usada apenas em dispositivos de tela grande onde
     * a lista de aplicativos e os detalhes de um aplicativo selecionado podem ser exibidos lado a lado.
     *
     * @see ViewManagedAppFragment
     */
    private fun closeDetailsFragment() {
        mViewManagedAppFragment?.let {
            viewModel.currentAppOpen = null
            slidingPaneController?.showOnlyMaster {
                parentFragmentManager
                    .beginTransaction()
                    .remove(mViewManagedAppFragment!!)
                    .commit()
                mViewManagedAppFragment = null
                toggleSingleScreenViews(true)
            }
        }
    }

    /**
     * Alterna a visibilidade dos elementos de interface que são específicos para a visualização em tela única.
     * Isso é usado em dispositivos de tela grande para esconder/mostrar elementos como o FAB e o menu
     * quando o painel de detalhes é exibido ou ocultado.
     * @param show Booleano indicando se os elementos devem ser exibidos (`true`) ou ocultos (`false`).
     */
    private fun toggleSingleScreenViews(show: Boolean) = with(binding) {
        fabAdd.isGone = show.not()
        ivMenu.isGone = show.not()
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

            if (slidingPaneController?.isAnimating == true) return@AutoFitGridLayoutManager
            adapter.setUseGridView(spanCount)
            /*
            * Reatribuo o adapter para que o RecyclerView recrie as views. Isso força a animação de transição entre
            * a visualização em lista e em grades com multiplas colunas.
            * Não é obrigatório reatribuir o adapter, o próprio LayoutManager ajusta as colunas e tamanhos.
            */
            binding.rvApps.rebindAdapter()
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
                binding.emptyView.isGone = apps?.isNotEmpty() == true
            }
        }
    }

    private fun showListenNotificationWarning() {

        val warningBinding = ViewWarningListenNotificationPermissionBinding.inflate(layoutInflater)

        warningBinding.chipPrivacy.setOnClickListener(AnimatedClickListener {
            MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.Sua_privacidade_importa))
                .setMessage(getString(R.string.O_conteudo_das_notifica_es_fica_salvo_apenas_no_seu_dispositivo_e_sob_nenhuma_circunst_ncia_compartilhado_com_terceiros))
                .setPositiveButton(getString(R.string.Entendi)) { dialog, _ ->
                }.setNegativeButton(getString(R.string.Ver_politica_de_privacidade)) { dialog, _ ->
                    val url = App.instance.remoteConfigValues.value?.privacyUrl

                    if (!url.isNullOrBlank()) {
                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                        startActivity(intent)
                    } else {
                        showErrorSnackBar(
                            getString(R.string.Nao_foi_possivel_obter_url_tente_novamente_em_alguns_instantes), binding.fabAdd
                        )
                    }

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
        MaterialAlertDialogBuilder(requireActivity()).setTitle(getString(R.string.Enviar_feedback)).setIcon(R.drawable.vec_info)
            .setMessage(getString(R.string.Como_voc_gostaria_de_enviar_seu_feedback))
            .setPositiveButton(getString(R.string.Comentar_na_play_store)) { _, _ ->
                requireMainActivity().openPlayStore()
            }.setNegativeButton(getString(R.string.enviar_um_e_mail_ao_desenvolvedor)) { _, _ ->
                requireMainActivity().openMailToSendFeedback()
            }.show()
    }

}


