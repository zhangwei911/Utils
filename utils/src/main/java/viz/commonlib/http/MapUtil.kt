package viz.commonlib.http

class MapUtil<K, V> {
    var map = HashMap<K, V>()
    fun add(key: K, value: V): MapUtil<K, V> {
        map[key] = value
        return this
    }

    fun addAll(mapIn: Map<K, V>): MapUtil<K, V> {
        map.putAll(mapIn)
        return this
    }
}