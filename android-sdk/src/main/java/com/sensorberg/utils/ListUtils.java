package com.sensorberg.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ListUtils {


    public static <A> ArrayList<A> distinct(Collection<A> map) {
        return new ArrayList<>(new HashSet<>(map));
    }

    public interface Filter<A> {
        boolean matches(A object);
    }

    /**
     * do somthing with the key of the map on the value (store it in the object)
     * @param <K>
     * @param <S>
     */
    public interface MapKeyConverter<K,S> {
        void useMapKey(S Key, K object);
    }

    public interface Mapper<INPUT, OUTPUT>{
        OUTPUT map(INPUT input);
    }

    public interface KeyProvider<OBJECT, KEY_TYPE> {
        KEY_TYPE map(OBJECT resolveAction);
    }

    public static <INPUT, OUTPUT> ArrayList<OUTPUT> map(List<INPUT> inputs, Mapper<INPUT, OUTPUT> mapper){
        ArrayList<OUTPUT> value = new ArrayList<>();
        for (INPUT object : inputs) {
            OUTPUT mappedObject  = mapper.map(object);
            if (mappedObject != null) {
                value.add(mappedObject);
            }
        }
        return value;
    }

    public static <MAP_VALUE_TYPE, MAP_KEY_TYPE> List<MAP_VALUE_TYPE> filter(Map<MAP_KEY_TYPE, MAP_VALUE_TYPE> map, Filter<MAP_VALUE_TYPE> filter, MapKeyConverter<MAP_VALUE_TYPE, MAP_KEY_TYPE> mapKeyConverter) {
        ArrayList<MAP_VALUE_TYPE> value = new ArrayList<>();
        Set<MAP_KEY_TYPE> keySet = map.keySet();
        for (MAP_KEY_TYPE key : keySet) {
            MAP_VALUE_TYPE object = map.get(key);
            if (filter.matches(object)){
                value.add(object);
                mapKeyConverter.useMapKey(key, object);
            }
        }
        return value;
    }

    public static <K> List<K> filter(List<K> list, Filter<K> filter){
        ArrayList<K> value = new ArrayList<>();
        for(K object : list){
            if (filter.matches(object)){
                value.add(object);
            }
        }
        return value;
    }

    public static <K> List<K> filter(Set<K> set, Filter<K> filter){
        ArrayList<K> value = new ArrayList<>();
        synchronized (set) {
            for (K object : set) {
                if (filter.matches(object)) {
                    value.add(object);
                }
            }
        }
        return value;
    }

    public static <OBJECT, KEY_TYPE> Map<KEY_TYPE, OBJECT> toMap(List<OBJECT> instantActions, KeyProvider<OBJECT, KEY_TYPE> keyProvider) {
        Map<KEY_TYPE, OBJECT> value = new HashMap<>();
        for (OBJECT instantAction : instantActions) {
            value.put(keyProvider.map(instantAction), instantAction);
        }
        return value;
    }
}
