package tw.com.louis383.coffeefinder.model.data.mapper.basic

interface IListMapper<I, O> : Mapper<List<I>, List<O>>

open class ListMapper<I, O>(
    private val mapper: Mapper<I, O>
) : IListMapper<I, O> {
    override fun map(input: List<I>): List<O> {
        return input.map { mapper.map(it) }
    }
}