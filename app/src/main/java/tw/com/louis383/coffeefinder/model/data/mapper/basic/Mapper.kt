package tw.com.louis383.coffeefinder.model.data.mapper.basic

interface Mapper<I, O> {
    fun map(input: I): O
}