package edu.uw.info314.xmlrpc.server;

import java.util.ArrayList;
import java.util.List;

public class XMLNode {
  String elementName;
  String textContent;
  Boolean isRoot;
  List<XMLNode> children;

  public XMLNode() {
    this("", "", false);
  }

  public XMLNode(String elementName) {
    this(elementName, "", false);
  }

  public XMLNode(String elementName, String textContent) {
    this(elementName, textContent, false);
  }

  public XMLNode(String elementName, String textContent, Boolean isRoot) {
    this.elementName = elementName;
    this.textContent = textContent;
    this.isRoot = isRoot;
    children = new ArrayList<>();
  }

  public XMLNode addChild(XMLNode node) {
    children.add(node);
    return node;
  }

  public XMLNode addChild(String elementName) {
    return addChild(elementName, "");
  }

  public XMLNode addChild(String elementName, String textContent) {
    XMLNode newNode = new XMLNode(elementName, textContent);
    children.add(newNode);
    return newNode;
  }

  public String toString() {
    StringBuilder childrenContent = new StringBuilder();
    String header = "";
    if (isRoot) {
      header = "<?xml version=\"1.0\"?>";
    }
    for (XMLNode child : children) {
      childrenContent.append(child.toString());
    }
    return header + "<" + elementName + ">" + textContent + childrenContent + "</" + elementName + ">";
  }
}
