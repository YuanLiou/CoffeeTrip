package tw.com.louis383.coffeefinder.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tw.com.louis383.coffeefinder.core.domain.repository.CoffeeShopRepository
import tw.com.louis383.coffeefinder.core.domain.repository.CoffeeShopRepositoryImpl

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {

    @Binds
    abstract fun bindCoffeeShopRepository(
        coffeeShopRepositoryImpl: CoffeeShopRepositoryImpl
    ): CoffeeShopRepository
}