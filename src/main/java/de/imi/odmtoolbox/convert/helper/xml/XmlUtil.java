package de.imi.odmtoolbox.convert.helper.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

public final class XmlUtil {
    private XmlUtil(){}

    static final class NodeListWrapper<T extends Node> extends AbstractList<T> implements RandomAccess {
        private final NodeList list;


        NodeListWrapper(NodeList l) {
            list=l;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T get(int index) {
            return (T) list.item(index);
        }

        @Override
        public int size() {
            return list.getLength();
        }
    }

    public static List<Node> asList(NodeList n) {
        return n.getLength() == 0 ? Collections.emptyList() : new NodeListWrapper<>(n);
    }

    public static List<Element> asElementList(NodeList n) {
        return n.getLength() == 0 ? Collections.emptyList() : new NodeListWrapper<>(n);
    }
}
