package com.juniperphoton.myersplash.di

import com.juniperphoton.myersplash.viewmodel.ImageListViewModel
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named
import javax.inject.Scope

@ImageScope
@Component(modules = [ImageModule::class], dependencies = [AppComponent::class])
interface ImageComponent {
    @Component.Builder
    interface Builder {
        fun appComponent(appComponent: AppComponent): Builder

        @BindsInstance
        fun type(@Named("type") type: Int): Builder

        fun build(): ImageComponent
    }

    fun inject(vm: ImageListViewModel)
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ImageScope