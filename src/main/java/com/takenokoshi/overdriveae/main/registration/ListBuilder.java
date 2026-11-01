package com.takenokoshi.overdriveae.main.registration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;

public class ListBuilder<T> {

    private final List<T> list;

    public ListBuilder(List<T> list) {
        this.list = list;
    }

    public ListBuilder() {
        this(new ArrayList<>());
    }

    public static <T> ListBuilder<T> builder() {
        return new ListBuilder<>();
    }

    public static <T> ListBuilder<T> builder(List<T> list) {
        return new ListBuilder<>(list);
    }

    public ListBuilder<T> add(T element) {
        list.add(element);
        return this;
    }

    public ListBuilder<T> add(int min, int max, IntFunction<T> function) {
        for (int i = min; i < max + 1; i++) {
            list.add(function.apply(i));
        }
        return this;
    }

    public List<T> build() {
        return list;
    }

    public List<T> buildUnmodifiable() {
        return Collections.unmodifiableList(list);
    }

}
