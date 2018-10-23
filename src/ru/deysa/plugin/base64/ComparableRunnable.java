package ru.deysa.plugin.base64;

import java.util.Objects;

public abstract class ComparableRunnable<T> implements Runnable {

    private T oldValue = null;

    @Override
    public void run() {
        T value = getValue();
        if (Objects.equals(oldValue, value)) {
            return;
        }
        oldValue = value;
        try {
            run(value);
        } catch (Exception ignore) {
            //ignore
        }
    }

    public abstract void run(T value) throws Exception;

    public abstract T getValue();

}
