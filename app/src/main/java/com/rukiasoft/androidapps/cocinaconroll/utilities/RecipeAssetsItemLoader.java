package com.rukiasoft.androidapps.cocinaconroll.utilities;

import android.content.Context;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

//TODO - eliminar dependencia xml
public class RecipeAssetsItemLoader {

    private Document doc;
    Context context;

    public RecipeAssetsItemLoader(InputStream file, Context context) throws IOException, SAXException, ParserConfigurationException {
        doc = XMLFactory.getDocument(file);
        this.context = context;
    }

    public List<String> getItems(){
        ArrayList<String> items = new ArrayList<>();

        NodeList list = doc.getDocumentElement().getElementsByTagName("item");

        for(int i=0;i<list.getLength();i++) {
            Node e = list.item(i);
            String name = e.getAttributes().getNamedItem("name").getNodeValue();
            items.add(name);
        }

        return items;
    }

}
