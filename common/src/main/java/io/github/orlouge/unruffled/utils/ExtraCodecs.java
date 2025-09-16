package io.github.orlouge.unruffled.utils;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ExtraCodecs {
    public static <K, V> Codec<Map<K, V>> mapAsListOfPairs(Codec<K> key, Codec<V> value) {
        return Codec.list(recordPair(key, value)).xmap(
            listOfPairs -> listOfPairs.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)),
            map -> map.entrySet().stream().map(e2 -> new Pair<>(e2. getKey(), e2.getValue())).toList()
        );
    }

    public static <K, V> Codec<LinkedHashMap<K, V>> orderedMapAsListOfPairs(Codec<K> key, Codec<V> value) {
        return Codec.list(recordPair(key, value)).xmap(
            listOfPairs -> listOfPairs.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, (x, y) -> y, LinkedHashMap::new)),
            map -> map.entrySet().stream().map(e2 -> new Pair<>(e2. getKey(), e2.getValue())).toList()
        );
    }
    
    public static <T> Codec<T> wrapInRecord(Codec<T> codec) {
        return RecordCodecBuilder.create(instance -> instance.group(codec.fieldOf("value").forGetter(x -> x)).apply(instance, x -> x));
    }

    public static <K, V> Codec<Pair<K, V>> recordPair(Codec<K> key, Codec<V> value) {
        return RecordCodecBuilder.create(instance -> instance.group(
            key.fieldOf("key").forGetter(Pair::getFirst),
            value.fieldOf("value").forGetter(Pair::getSecond)
        ).apply(instance, Pair::new));
    }
}
