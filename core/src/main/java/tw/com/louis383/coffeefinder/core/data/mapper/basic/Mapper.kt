package tw.com.louis383.coffeefinder.core.data.mapper.basic

interface Mapper<I, O> {
    fun map(input: I): O
}