package tw.com.louis383.coffeefinder.model.data.mapper

import com.google.android.libraries.maps.model.LatLng
import tw.com.louis383.coffeefinder.model.data.dto.NetworkShop
import tw.com.louis383.coffeefinder.model.data.mapper.basic.Mapper
import tw.com.louis383.coffeefinder.model.domain.model.CoffeeShop
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
            location = LatLng(input.latitude, input.longitude),
            distance = input.distance,
            limitedTime = input.limitedTime,
            socket = input.socket,
            standingDesk = input.standingDesk ?: false,
            mrt = input.mrt,
            openTime = input.openTime
        )
    }
}