package ru.deysa.plugin.base64;

import java.util.Objects;

public abstract class ComparableRunnable<T> implements Runnable {

    private T oldValue = null;

    @Override
    public void run() {
        T newValue = getValue();
        if (Objects.equals(oldValue, newValue)) {
            return;
        }
        oldValue = newValue;
        run(newValue);
    }

    public abstract void run(T newValue);

    public abstract T getValue();
}
