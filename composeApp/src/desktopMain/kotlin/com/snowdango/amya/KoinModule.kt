package com.snowdango.amya

import androidx.lifecycle.viewmodel.compose.viewModel
import com.snowdango.amya.domain.db.AppsDatabase
import com.snowdango.amya.feature.addtag.AddTagViewModel
import com.snowdango.amya.feature.tag.TagViewModel
import com.snowdango.amya.model.AppsModel
import com.snowdango.amya.model.TagModel
import com.snowdango.amya.repository.apps.AppsDataStore
import com.snowdango.amya.repository.apps.AppsRepository
import com.snowdango.amya.repository.tag.TagDataStore
import com.snowdango.amya.repository.tag.TagRepository
import com.snowdango.amya.route.RouteViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val module = module {
    // domain
    single<AppsDatabase> { getDatabaseBuilder() }

    // repository
    factory<AppsDataStore> { AppsDataStore(get()) }
    factory<AppsRepository> { AppsRepository(get()) }
    factory<TagDataStore> { TagDataStore(get()) }
    factory<TagRepository> { TagRepository(get()) }

    // model
    factory<TagModel> { TagModel() }
    factory<AppsModel> { AppsModel() }


    // route
    viewModel<RouteViewModel> { RouteViewModel() }

    // addtag
    viewModel<AddTagViewModel> { AddTagViewModel() }

    // tag
    viewModel<TagViewModel> { param -> TagViewModel(param.get(), param.get()) }

}