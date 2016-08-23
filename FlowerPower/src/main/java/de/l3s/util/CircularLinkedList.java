package de.l3s.util;



public class CircularLinkedList<T> {
    private ListNode<T> head = null;
    private int numberOfElements = 0;
    private ListNode<T> actualElement = null;
    private int index = 0;

    public boolean isEmpty() {
        return (numberOfElements == 0);
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public void insertFirst(T data) {
        if (!(isEmpty())) {
            index++;
        }
        ListNode<T> listNode = new ListNode<T>(data, head);
        head = listNode;
        numberOfElements++;
    }

    public void insertAfterActual(T data) {
    	if(actualElement==null){
    		insertFirst(data);
    	}else{
        ListNode<T> listNode = new ListNode<T>(data, actualElement.next);
        actualElement.next = listNode;
    	
        numberOfElements++;
    	}
    }

    public boolean deleteFirst() {
        if (isEmpty())
            return false;
        if (index > 0)
            index--;
        head = head.next;
        numberOfElements--;
        return true;
    }

    public boolean deleteActualElement() {
        if (index > 0) {
            numberOfElements--;
            index--;
            ListNode<T> listNode = head;
            while (listNode.next.equals(actualElement) == false)
                listNode = listNode.next;
            listNode.next = actualElement.next;
            actualElement = listNode;
            return true;
        }
        else {
            actualElement = head.next;
            index = 0;
            return deleteFirst();
        }
    }

    public boolean goToNextElement() {
        if (isEmpty())
            return false;
        index = (index + 1) % numberOfElements;
        if (index == 0)
            actualElement = head;
        else
            actualElement = actualElement.next;
        return true;
    }

    public Object getActualElementData() {
        return actualElement.data;
    }

    public void setActualElementData(T data) {
        actualElement.data = data;
    }
}


