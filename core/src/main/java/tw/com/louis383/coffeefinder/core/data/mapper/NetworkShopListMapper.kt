package tw.com.louis383.coffeefinder.core.data.mapper

import tw.com.louis383.coffeefinder.core.data.dto.NetworkShop
import tw.com.louis383.coffeefinder.core.data.mapper.basic.ListMapper
import tw.com.louis383.coffeefinder.core.domain.model.CoffeeShop
import javax.inject.Inject

class NetworkShopListMapper @Inject constructor(mapper: NetworkShopMapper) :
    ListMapper<NetworkShop, CoffeeShop>(mapper)