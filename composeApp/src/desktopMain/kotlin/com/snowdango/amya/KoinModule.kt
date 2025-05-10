package com.snowdango.amya

import androidx.navigation.NavController
import com.snowdango.amya.domain.db.AppsDatabase
import com.snowdango.amya.domain.db.SettingsPreferences
import com.snowdango.amya.feature.addapp.AddAppViewModel
import com.snowdango.amya.feature.addtag.AddTagViewModel
import com.snowdango.amya.feature.all.AllViewModel
import com.snowdango.amya.feature.setting.LicensesViewModel
import com.snowdango.amya.feature.setting.SettingViewModel
import com.snowdango.amya.feature.tag.TagViewModel
import com.snowdango.amya.model.AppsModel
import com.snowdango.amya.model.SettingsModel
import com.snowdango.amya.model.TagModel
import com.snowdango.amya.platform.getDatabaseBuilder
import com.snowdango.amya.platform.getSettingsPreferences
import com.snowdango.amya.repository.apps.AppsDataStore
import com.snowdango.amya.repository.apps.AppsRepository
import com.snowdango.amya.repository.settings.SettingsDataStore
import com.snowdango.amya.repository.settings.SettingsRepository
import com.snowdango.amya.repository.tag.TagDataStore
import com.snowdango.amya.repository.tag.TagRepository
import com.snowdango.amya.route.RouteViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val module = module {
    // domain
    single<AppsDatabase> { getDatabaseBuilder().build() }
    single<SettingsPreferences> { getSettingsPreferences() }

    // repository
    factory<AppsDataStore> { AppsDataStore(get()) }
    factory<AppsRepository> { AppsRepository(get()) }
    factory<TagDataStore> { TagDataStore(get()) }
    factory<TagRepository> { TagRepository(get()) }
    factory<SettingsDataStore> { SettingsDataStore(get()) }
    factory<SettingsRepository> { SettingsRepository(get()) }

    // model
    factory<TagModel> { TagModel() }
    factory<AppsModel> { AppsModel() }
    factory<SettingsModel> { SettingsModel() }

    // route
    viewModel<RouteViewModel> { (navController: NavController) -> RouteViewModel(navController) }
    // addtag
    viewModel<AddTagViewModel> { AddTagViewModel() }
    // tag
    viewModel<TagViewModel> { (parentId: Long, childId: Long?) -> TagViewModel(parentId, childId) }
    // addapp
    viewModel<AddAppViewModel> { AddAppViewModel() }
    // all
    viewModel<AllViewModel> { AllViewModel() }
    // setting
    viewModel<SettingViewModel> { SettingViewModel() }
    // license
    viewModel<LicensesViewModel> { LicensesViewModel() }


    // main
    single<MainViewModel> { MainViewModel() }
    single<CoroutineScope> { CoroutineScope(Dispatchers.Default + Job()) }

}