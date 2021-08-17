package tw.com.louis383.coffeefinder.core.data.mapper

import tw.com.louis383.coffeefinder.core.data.dto.NetworkShop
import tw.com.louis383.coffeefinder.core.data.mapper.basic.Mapper
import tw.com.louis383.coffeefinder.core.domain.model.CoffeeShop
import tw.com.louis383.coffeefinder.core.domain.model.MapLocation
import javax.inject.Inject

class NetworkShopMapper @Inject constructor() : Mapper<NetworkShop, CoffeeShop> {
    override fun map(input: NetworkShop): CoffeeShop {
        return CoffeeShop(
            id = input.id,
            name = input.name,
            wifi = input.wifi,
            seat = input.seat,
            cheap = input.cheap,
            url = input.url,
            address = input.address,
            mapLocation = MapLocation(
                latitude = input.latitude,
                longitude = input.longitude
            ),
            distance = input.distance,
            limitedTime = input.limitedTime,
            socket = input.socket,
            standingDesk = input.standingDesk ?: false,
            mrt = input.mrt,
            openTime = input.openTime
        )
    }
}