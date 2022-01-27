package ru.mikhailsv.support;

import lombok.Data;

@Data
public final class Pair<T> {
    private T first;
    private T second;
}
