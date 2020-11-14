package com.niks.model;

import lombok.Data;

@Data
public class MinHeapNode {

   private String element; // The element to be stored

  // index of the array from
  // which the element is taken
  private int currentElementIndexToBePick;

  // index of the next element
  // to be picked from array
  private int nextElementIndexToBePick;

  public MinHeapNode(String element, int currentElementIndexToBePick, int nextElementIndexToBePick)
  {
    this.element = element;
    this.currentElementIndexToBePick = currentElementIndexToBePick;
    this.nextElementIndexToBePick = nextElementIndexToBePick;
  }
}
