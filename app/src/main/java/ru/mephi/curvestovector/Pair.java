package ru.mephi.curvestovector;

import java.io.Serializable;

public class Pair<T> implements Serializable {

    private static final long serialVersionUID = - 4322682019827188617L;

    private T value1;
    private T value2;

    public Pair (T val1, T val2) {
        value1 = val1;
        value2 = val2;
    }

    public T getValue1() {
        return value1;
    }

    public T getValue2() {
        return value2;
    }
}
