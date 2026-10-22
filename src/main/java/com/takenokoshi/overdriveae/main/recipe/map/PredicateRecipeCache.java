package com.takenokoshi.overdriveae.main.recipe.map;

import java.util.function.Predicate;

public record PredicateRecipeCache<TYPE, RECIPE>(Predicate<TYPE> predicate, RECIPE value) implements Predicate<TYPE> {

    @Override
    public boolean test(TYPE t) {
        return predicate.test(t);
    }
}
