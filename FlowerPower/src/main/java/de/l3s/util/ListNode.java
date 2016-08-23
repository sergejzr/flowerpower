package de.l3s.util;

public class ListNode<T> {
    public ListNode<T> next;
    public T data;

    public ListNode(T data, ListNode<T> next) {
        this.next = next;
        this.data = data;
    }
}
