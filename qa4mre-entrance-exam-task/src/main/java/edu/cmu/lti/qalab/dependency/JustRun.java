package edu.cmu.lti.qalab.dependency;

import java.io.IOException;

import edu.cmu.lti.qalab.types.Dependency;



public class JustRun {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		/*DepTreeInfo tree = new DepTreeInfo ("data/dpTree.xml");
		System.out.println(tree.getScore("cop", "conj"));
		System.out.println(tree.getScore("auxpass", "ref"));
		System.out.println(tree.getScore("obj", "subj"));
		System.out.println(tree.getScore("obj", "dobj"));
		System.out.println(tree.getScore("iobj", "dobj"));
		System.out.println(tree.getScore("ccomp", "xcomp"));
		System.out.println(tree.getScore("amod", "tmod"));*/
		//Depsimilar d = new Depsimilar("en-PubMedOA-20070501","data/dpTree.xml");
		//createTest("dobj","go","shopping","dobj","buy","shoes" ,d);
		String a= "abc_with";
		a= a.replaceFirst("_.*", "");
		System.out.println(a);

	}
	
/*	public static void createTest(String rel,String g,String d,String _rel,String _g,String _d,Depsimilar t) throws IOException{
		Dependency a = createDep(rel,g,d);
		Dependency b = createDep(_rel,_g,_d);
		System.out.println(t.getScore(a, b));
	}
	
	public static Dependency createDep(String rel,String g,String d){
		Dependency a;
		a = new Dependency();
		a.setRelation(rel);
		Token_ t1 = new Token_();
		t1.setText(g);
		a.setGovernor(t1);
		Token_ t2 = new Token_();
		t2.setText(d);
		a.setDependent(t2);
		return a;
	}
	*/
	
}
