package org.module1;


import java.util.Objects;

public class SimpleHashMap<K, V> implements SimpleMap<K, V> {
    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;

    private static class Node<K, V> {
        final K key;
        V value;
        final int hash;
        Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

    }

    private Node<K, V>[] table;
    private int size;
    private int threshold;

    @SuppressWarnings("unchecked")
    public SimpleHashMap() {
        this.table = (Node<K, V>[]) new Node[DEFAULT_CAPACITY];
        this.threshold = (int) (DEFAULT_CAPACITY * LOAD_FACTOR);
    }

    /**
     * Хеш-функция.
     */
    private int hash(Object key) {
        if (key == null) return 0;
        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    /**
     * Определение индекса корзины.
     */
    private int indexFor(int hash, int length) {
        return hash & (length - 1);
    }

    /**
     * Основной метод добавления/обновления
     */
    @Override
    public V put(K key, V value) {
        int hash = hash(key);
        int index = indexFor(hash, table.length);

        for (Node<K, V> node = table[index]; node != null; node = node.next) {
            // Сравниваем и hash, и equals для корректности
            if (node.hash == hash && Objects.equals(key, node.key)) {
                V oldValue = node.value;
                node.value = value;

                return oldValue;
            }
        }

        addNode(hash, key, value, index);
        return null;

    }

    /**
     * Добавление нового узла с проверкой необходимости resize
     */
    private void addNode(int hash, K key, V value, int index) {
        Node<K, V> newNode = new Node<>(hash, key, value, table[index]);
        table[index] = newNode;  // Вставка в начало списка - O(1)

        if (++size > threshold) {
            resize();
        }
    }

    /**
     * Получение значения по ключу
     */
    @Override
    public V get(Object key) {
        int hash = hash(key);
        int index = indexFor(hash, table.length);

        for (Node<K, V> node = table[index]; node != null; node = node.next) {
            if (node.hash == hash && Objects.equals(key, node.key)) {

                return node.value;
            }
        }
        return null;
    }

    /**
     * Удаление элемента
     */
    @Override
    public V remove(Object key) {
        int hash = hash(key);
        int index = indexFor(hash, table.length);

        Node<K, V> prev = null;
        Node<K, V> node = table[index];

        while (node != null) {
            if (node.hash == hash && Objects.equals(key, node.key)) {
                if (prev == null) {
                    table[index] = node.next;
                } else {
                    prev.next = node.next;
                }
                size--;

                return node.value;
            }
            prev = node;
            node = node.next;
        }
        return null;
    }

    /**
     * Изменение размера таблицы
     */
    @SuppressWarnings("unchecked")
    private void resize() {
        Node<K, V>[] oldTable = table;
        int newCapacity = oldTable.length * 2;

        table = (Node<K, V>[]) new Node[newCapacity];
        threshold = (int) (newCapacity * LOAD_FACTOR);

        for (Node<K, V> headNode : oldTable) {
            Node<K, V> node = headNode;
            while (node != null) {
                Node<K, V> next = node.next;

                int newIndex = indexFor(node.hash, newCapacity);
                node.next = table[newIndex];
                table[newIndex] = node;

                node = next;
            }
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null || (get(key) == null && containsNullValue(key));
    }

    private boolean containsNullValue(Object key) {
        int hash = hash(key);
        int index = indexFor(hash, table.length);
        for (Node<K, V> node = table[index]; node != null; node = node.next) {
            if (node.hash == hash && Objects.equals(key, node.key)) {

                return true;
            }
        }
        return false;
    }
}