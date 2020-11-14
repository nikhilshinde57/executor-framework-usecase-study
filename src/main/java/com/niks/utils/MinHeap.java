package com.niks.utils;

import com.niks.constants.HelperConstants;
import com.niks.model.MinHeapNode;

public class MinHeap {

  private MinHeapNode[] harr; // Array of elements in heap
  private int heap_size; // Current number of elements in min heap

  // Constructor: Builds a heap from
  // a given array a[] of given size
  public MinHeap(MinHeapNode a[], int size) {
    heap_size = size;
    harr = a;
    int i = (heap_size - 1) / 2;
    while (i >= 0) {
      MinHeapify(i);
      i--;
    }
  }

  // A recursive method to heapify a subtree
  // with the root at given index This method
  // assumes that the subtrees are already heapified
  private void MinHeapify(final int i) {
    int l = left(i);
    int r = right(i);
    int smallest = i;
    if (l < heap_size && harr[l].getElement().toLowerCase().compareTo(harr[i].getElement().toLowerCase()) < 0) {
      smallest = l;
    }
    if (r < heap_size && harr[r].getElement().toLowerCase().compareTo(harr[smallest].getElement().toLowerCase()) < 0) {
      smallest = r;
    }
    if(smallest == i && harr[i].getElement()==""&& r<heap_size && l<heap_size &&  harr[l].getElement().toLowerCase().compareTo(harr[r].getElement().toLowerCase()) < 0){
      smallest = l;
    }
    else if(smallest == i && harr[i].getElement()=="" && r<heap_size && l<heap_size)
      smallest=r;
    if (smallest != i) {
      swap(harr, i, smallest);
      MinHeapify(smallest);
    }
  }

  // to get index of left child of node at index i
  private int left(final int i) {
    return (2 * i + 1);
  }

  // to get index of right child of node at index i
  private int right(final int i) {
    return (2 * i + 2);
  }

  // to get the root
  private MinHeapNode getMin() {
    if (heap_size <= 0) {
      //Heap underflow
      return null;
    }
    return harr[0];
  }

  // to replace root with new node
  // "root" and heapify() new root
  private void replaceMin(final MinHeapNode root) {
    harr[0] = root;
    MinHeapify(0);
  }

  // A utility function to swap two min heap nodes
  private void swap(final MinHeapNode[] arr, final int i, final int j) {
    MinHeapNode temp = arr[i];
    arr[i] = arr[j];
    arr[j] = temp;
  }


  // This function takes an array of
  // arrays as an argument and All
  // arrays are assumed to be sorted.
  // It merges them together and
  // prints the final sorted output.
  public static String[] mergeKSortedArrays(final Object[][] arr,final int k) {
    MinHeapNode[] hArr = new MinHeapNode[k];
    int resultSize = 0;
    for (int i = 0; i < k; i++) {
      MinHeapNode node = new MinHeapNode((String) arr[i][0], i, 1);
      hArr[i] = node;
      resultSize += arr[i].length;
    }

    // Create a min heap with k heap nodes. Every heap node
    // has first element of an array
    MinHeap mh = new MinHeap(hArr, k);

    String[] result = new String[resultSize];     // To store output array

    // Now one by one get the minimum element from min
    // heap and replace it with next element of its array
    for (int i = 0; i < resultSize; i++) {
      MinHeapNode root;
      root = mh.getMin();
      result[i] = root.getElement();

      // Find the next element that will replace current
      // root of heap. The next element belongs to same
      // array as the current root.
      if (root.getNextElementIndexToBePick() < arr[root.getCurrentElementIndexToBePick()].length) {
        root.setElement((String) arr[root.getCurrentElementIndexToBePick()][(root.getNextElementIndexToBePick())]);
        root.setNextElementIndexToBePick(root.getNextElementIndexToBePick() + 1);
      }

      // If root was the last element of its array
      else {
        root.setElement(HelperConstants.MAX_STRING_VALUE);
      }

      // Replace root with next element of array
      mh.replaceMin(root);
    }
    return result;
  }
}
