package de.krd.lmapraktikum_datacollector.utils

import androidx.lifecycle.MutableLiveData

class MutableLiveDataList<T> : MutableLiveData<MutableList<T>>() {
    val value = mutableListOf<T>()

    init {
        setValue(value)
    }

    fun add(element: T) {
        value.add(element)
        notifyObserver();
    }

    fun addAll(elements: Collection<T>) {
        value.addAll(elements);
        notifyObserver();
    }

    fun clear() {
        value.clear();
        notifyObserver();
    }

    fun getSize(): Int? {
        return value.size
    }

    private fun notifyObserver() {
        postValue(value);
    }
}