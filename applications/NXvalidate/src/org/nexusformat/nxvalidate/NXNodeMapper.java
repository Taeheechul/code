/* NeXus - Neutron & X-ray Common Data Format
 *
 * NeXus file validation GUI tool.
 *
 * Copyright (C) 2010 Stephen Rankin
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For further information, see <http://www.neutron.anl.gov/NeXus/>
 *
 * NXNodeMapper.java
 *
 */
package org.nexusformat.nxvalidate;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Stephen Rankin
 */
public class NXNodeMapper implements MutableTreeNode {

    private Node domNode = null;
    private String nodeName = null;
    private boolean isRoot = false;
    private boolean isLeaf = false;
    private boolean isDocument = false;
    private boolean allowsChildren = false;
    private ArrayList<NXNodeMapper> documents = null;
    static final int ELEMENT_TYPE = Node.ELEMENT_NODE;
    private int childCount = 0;
    private ArrayList<Node> children = null;
    private NXNodeMapper root = null;
    private File nxsFile = null;
    private File nxdlFile = null;
    private File reducedFile = null;
    private File resultsFile = null;
    private Document reducedDoc = null;
    private Document resultsDoc = null;
    private boolean badNode = false;
    private Object userObject = null;

    // Construct an Adapter node from a DOM node
    public NXNodeMapper(Node node, boolean isDocument, File nxsFile) {

        this.isDocument = isDocument;
        domNode = node;

        allowsChildren = true;
        if (node.getNodeType() != ELEMENT_TYPE) {
            isLeaf = true;
        }

        this.nxsFile = nxsFile;
        nodeName = nxsFile.getAbsolutePath();
        children = getElements();
    }

    // Construct an Adapter node from a DOM node
    public NXNodeMapper(Node node, boolean isDocument, String nodeName) {

        this.isDocument = isDocument;
        domNode = node;

        allowsChildren = true;
        if (node.getNodeType() != ELEMENT_TYPE) {
            isLeaf = true;
        }

        this.nodeName = nodeName;
        children = getElements();
    }

    public NXNodeMapper(String nodeName) {
        this.nodeName = nodeName;
        isRoot = true;
        allowsChildren = true;
        documents = new ArrayList<NXNodeMapper>();
    }

    /**
     * Check to see if this node is the root node.
     * @return true is it is the root node, false otherwise.
     */
    public boolean isRoot() {
        return isRoot;
    }

    /**
     * Check to see if this node is a document, i.e the list of nodes directly
     * under the root note that are the open NXS documents.
     *
     * @return true if the node is a document node.
     */
    public boolean isDocument() {
        return isDocument;
    }

    /**
     * Set the flag that says that this node is a documents node i.e. one of the
     * list of nodes directly under the root note that are the open NXS documents.
     * @param isDocument a flag which is true if the node is a document node.
     */
    public void setDocument(boolean isDocument) {
        this.isDocument = isDocument;
    }

    /**
     * Set the root node that this node belongs to.
     * @param the root node.
     */
    public void setRoot(NXNodeMapper root) {
        this.root = root;
    }

    /**
     * Get the NXS file belonging to the root node. Any other node should return
     * null
     * @return the NXS file as a File object.
     */
    public File getNXSFile() {
        return nxsFile;
    }

    /**
     * Get the NXDL file belonging to the root node. Any other node should return
     * null
     * @return the NXDL file as a File object.
     */
    public File getNXDLFile() {
        return nxdlFile;
    }

    /**
     * Set the NXDL file belonging to the root node. Should not be set.
     * @param the NXDL file as a File object.
     */
    public void setNXDLFile(File nxdlFile) {
        this.nxdlFile = nxdlFile;
    }

    /**
     * Get the resultant file produced after NXConvert has been run on the
     * NXS file
     * (reduced to XML).
     * @return the NXConvert reduced file.
     */
    public File getReducedFile() {
        return reducedFile;
    }

    /**
     * Set the resultant file produced after NXConvert has been run on the
     * NXS file.
     * @param the NXConvert reduced file.
     */
    public void setReducedFile(File reducedFile) {
        this.reducedFile = reducedFile;
    }

    /**
     * Get the resultant file produced after the validation procedure has been
     * performed.
     * @param the validation results file.
     */
    public File getResultsFile() {
        return resultsFile;
    }

    /**
     * Get the resultant file produced after the validation procedure has been
     * performed.
     * @param the validation results file.
     */
    public void setResultsFile(File resultsFile) {
        this.resultsFile = resultsFile;
    }

    /**
     * Get the resultant DOM document produced after the validation procedure
     * has been performed.
     * @return the validation results DOM document.
     */
    public Document getResultsDoc() {
        return resultsDoc;
    }

    /**
     * Get the resultant DOM document produced after the validation procedure
     * has been performed.
     * @return the validation results DOM document.
     */
    public void setResultsDoc(Document resultsDoc) {
        this.resultsDoc = resultsDoc;
    }

    public Document getReducedDoc() {
        return reducedDoc;
    }

    public void setReducedDoc(Document reducedDoc) {
        this.reducedDoc = reducedDoc;
    }

    public void setBadNode(boolean badNode) {
        this.badNode = badNode;
    }

    public boolean getBadNode() {
        return badNode;
    }

    public Node getDomNode() {
        return domNode;
    }

    public void checkBadNode() {
        if (!isRoot) {
            Boolean bad = (Boolean) domNode.getUserData("bad");
            if (bad != null) {
                this.badNode = bad.booleanValue();
            }
        }
    }

    public void resetNode() {

        badNode = false;

        domNode.setUserData("texts", null, null);
        domNode.setUserData("tests", null, null);
        domNode.setUserData("diags", null, null);
        domNode.setUserData("diagatts", null, null);
        domNode.setUserData("bad", new Boolean(false), null);
    }

    public ArrayList<String> getNodeTexts() {
        if (!isRoot) {
            return (ArrayList<String>) domNode.getUserData("texts");
        } else {
            return null;
        }
    }

    public ArrayList<String> getNodeTests() {
        if (!isRoot) {
            return (ArrayList<String>) domNode.getUserData("tests");
        } else {
            return null;
        }
    }

    public ArrayList<String> getNodeDiags() {
        if (!isRoot) {
            return (ArrayList<String>) domNode.getUserData("diags");
        } else {
            return null;
        }
    }

    public ArrayList<String> getNodeDiagAtts() {
        if (!isRoot) {
            return (ArrayList<String>) domNode.getUserData("diagatts");
        } else {
            return null;
        }
    }

    // Return the node name
    @Override
    public String toString() {
        return nodeName;
    }

    public int getIndex(TreeNode child1) {

        NXNodeMapper child = (NXNodeMapper) child1;
        int count = getChildCount();
        if (isRoot) {

            if (documents.contains(child)) {

                return documents.indexOf(child);
            }

        } else {
            for (int i = 0; i < count; i++) {
                NXNodeMapper n = this.getChildAt(i);

                if (child.domNode == n.domNode) {
                    return i;
                }
            }
        }
        return -1; // Should never get here.
    }

    public NXNodeMapper getChildAt(int searchIndex) {

        if (isRoot) {

            return documents.get(searchIndex);

        } else {
            //Note: JTree index is zero-based.
            Node node = domNode.getChildNodes().item(searchIndex);

            // Return Nth displayable node
            int elementNodeIndex = 0;

            for (int i = 0; i < domNode.getChildNodes().getLength(); i++) {
                node = domNode.getChildNodes().item(i);

                if ((node.getNodeType() == ELEMENT_TYPE)
                        && (elementNodeIndex++ == searchIndex)) {
                    break;
                }
            }
            return new NXNodeMapper(node, false, node.getNodeName());
        }

    }

    public int getChildCount() {

        if (isRoot) {

            return documents.size();

        }

        return childCount;

    }

    public Enumeration children() {
        return new children();

    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public boolean getAllowsChildren() {
        return allowsChildren;
    }

    public TreeNode getParent() {

        if (isRoot) {
            return null;
        } else if (isDocument) {

            return root;

        } else {
            return new NXNodeMapper(domNode.getParentNode(), false,
                    domNode.getNodeName());
        }
    }

    public String[] getAttributeList() {

        if (isRoot) {
            return new String[0];
        }

        ArrayList<String> atts = new ArrayList<String>();

        if (domNode.hasAttributes()) {

            NamedNodeMap att = domNode.getAttributes();
            int na = domNode.getAttributes().getLength();

            for (int i = 0; i < na; ++i) {
                atts.add(att.item(i).getNodeName() + " = " + att.item(i).getNodeValue());
            }

        }
        return atts.toArray(new String[0]);
    }

    public String getValue() {

        if (isRoot) {
            return "";
        }

        if (domNode.getNodeType() == ELEMENT_TYPE) {
            return getTextValue(domNode);
        }

        if (domNode.getTextContent() != null) {
            return domNode.getTextContent().trim();
        }

        return "";

    }

    private ArrayList<Node> getElements() {

        ArrayList<Node> nodes = new ArrayList<Node>();

        for (int i = 0; i < domNode.getChildNodes().getLength(); i++) {
            Node node = domNode.getChildNodes().item(i);

            if (node.getNodeType() == ELEMENT_TYPE) {

                nodes.add(node);
                ++childCount;
            }
        }
        return nodes;
    }

    private String getTextValue(Node node) {

        if (node.hasChildNodes()) {

            NodeList nodes = node.getChildNodes();

            for (int i = 0; i < nodes.getLength(); ++i) {
                if (nodes.item(i).getNodeType() == Node.TEXT_NODE) {
                    return nodes.item(i).getTextContent().trim();
                }
            }

        }
        return "";
    }

    public ArrayList<NXNodeMapper> getOpenNodes(){
        return documents;
    }

    public void removeAllNodes(){
        documents.clear();
    }

    private class children implements Enumeration {

        private int count = 0;
        private boolean more = true;
        private Node node = null;

        public boolean hasMoreElements() {
            if (count < children.size()) {
                more = true;
            } else {
                more = false;
            }

            return more;

        }

        public Object nextElement() {
            count++;

            if (children.size() < count) {
                node = children.get(count);
                return node;
            } else {
                throw new NoSuchElementException();
            }

        }
    }

    public void insert(MutableTreeNode child, int index) {

        NXNodeMapper childNode = (NXNodeMapper) child;

        if (isRoot) {
            documents.add(index, childNode);
        } else{
            NodeList list = domNode.getChildNodes();

            for (int i = 0; i < list.getLength(); ++i) {
                if(i == index){
                    domNode.insertBefore(childNode.domNode, list.item(i));
                }
            }
        }
    }

    public void remove(int index) {
        if (isRoot) {

            documents.remove(index);

        } else{

            NodeList list = domNode.getChildNodes();

            for (int i = 0; i < list.getLength(); ++i) {
                if(i == index){
                    domNode.removeChild(list.item(index));
                }
            }

        }
    }

    public void remove(MutableTreeNode node) {

        NXNodeMapper childNode = (NXNodeMapper) node;

        if (isRoot) {
            documents.remove((NXNodeMapper) node);
        }else{
            NodeList list = domNode.getChildNodes();

            for (int i = 0; i < list.getLength(); ++i) {
                if(list.item(i).isSameNode(childNode.domNode)){
                    domNode.removeChild(childNode.domNode);
                }
            }
        }
    }

    public void removeFromParent() {
        if (isRoot) {
           return;
        } else{
           NXNodeMapper parentNode = (NXNodeMapper)getParent();
           parentNode.remove(this);
        }
    }

    public void setParent(MutableTreeNode newParent) {



    }

    public void setUserObject(Object object) {

        userObject = object;

    }
}