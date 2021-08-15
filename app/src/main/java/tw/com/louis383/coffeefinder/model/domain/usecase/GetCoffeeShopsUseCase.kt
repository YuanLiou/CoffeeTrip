package tw.com.louis383.coffeefinder.model.domain.usecase

import android.location.Location
import tw.com.louis383.coffeefinder.model.domain.repository.CoffeeShopRepository
import javax.inject.Inject

class GetCoffeeShopsUseCase @Inject constructor(
    private val coffeeShopRepository: CoffeeShopRepository
) {
    suspend operator fun invoke(location: Location, range: Int) = coffeeShopRepository.getNearByCoffeeShopsAsync(location, range)
}