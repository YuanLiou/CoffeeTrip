package tw.com.louis383.coffeefinder.model.data.mapper

import tw.com.louis383.coffeefinder.model.data.dto.NetworkShop
import tw.com.louis383.coffeefinder.model.data.mapper.basic.ListMapper
import tw.com.louis383.coffeefinder.model.domain.model.CoffeeShop
import javax.inject.Inject

class NetworkShopListMapper @Inject constructor(mapper: NetworkShopMapper) :
    ListMapper<NetworkShop, CoffeeShop>(mapper)