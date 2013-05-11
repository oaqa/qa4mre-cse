package edu.cmu.lti.qalab.candidate_sentence;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DepTreeInfo {
	
	node root;
	HashMap<String, node> nodeList;
	double k = 0.5;
	DepTreeInfo(String path) throws Exception {
		File fXmlFile = new File(path);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		System.out.println("Root element :"
				+ doc.getDocumentElement().getNodeName());
		createTree(doc.getDocumentElement());

	}

	void createTree(Node parent) {
		root = new node(null, parent.getNodeName(), 0);
		nodeList = new HashMap<String, node>();
		createTree(parent, root);
	}

	void createTree(Node parent, node root) {
		NodeList nList = parent.getChildNodes();
		if (nList.getLength() == 0)
			return;
		else
			root.childs = new ArrayList<node>();
		for (int i = 0; i < nList.getLength(); i++) {
			Node xmlNode = nList.item(i);
			String dep = xmlNode.getNodeName();
			if (dep.equals("#text"))
				continue;
			node n = new node(root, dep, root.deep + 1);
			root.childs.add(n);
			nodeList.put(n.dep, n);
			System.out.println(n.parent.dep + "->" + n.dep + "|D:" + n.deep);
			createTree(xmlNode, n);
		}
	}

	int getDist(String rel1, String rel2) {

		return 0;
	}
	boolean containsDep(String rel){
		node A = this.nodeList.get(rel.toLowerCase());
		return (A!=null);
	}
	double getScore(String target, String candidate) {
		target = target.replaceFirst("_.*", "");//FIXME simple assume that extra relation have _
		candidate = candidate.replaceFirst("_.*", "");
		if(target.equals(candidate))
			return 1.0;
		Integer[] deeps = new Integer[2];
		node sharedP = getSharedParent(target,candidate,deeps);
		if(sharedP==null){
			System.out.println(target+" "+candidate+" !!!!!!!!dep relation not found!!!!!!!!");
			return 0.0;
		}
		if(sharedP.dep.equals(target))
			return 1.0;
		double d1,d2,d;
		d = sharedP.deep;
		d1 = deeps[0]-d;
		d2 = deeps[1]-d;
		
		return k*d/(d1+d2);
	}

	node getSharedParent(String rel1, String rel2,Integer[] deeps) {

		node A = this.nodeList.get(rel1.toLowerCase());
		node B = this.nodeList.get(rel2.toLowerCase());
		if (A == null || B == null)
			return null;
		deeps[0] = A.deep;
		deeps[1] = B.deep;
		return getSharedParent(A, B);
	}
	
	node getSharedParent(node A, node B) {
		ArrayList<node> parentsOfA = new ArrayList<node>();
		node p = A;
		while (p!= null) {
			parentsOfA.add(p);
			p.dirty = true;
			p = p.parent;
		}
		p = B;

		while (p!= null) {
			if (p.dirty)
				break;
			p = p.parent;
		}
		for (node n : parentsOfA) {
			n.dirty = false;
		}
		return p;
	}

	class node {
		node(node parent, String dep, int deep) {
			this.parent = parent;
			this.dep = dep;
			this.deep = deep;
		}

		node parent = null;
		ArrayList<node> childs = null;
		String dep;
		int deep;
		boolean dirty = false;
	}

}
