package edu.cmu.lti.qalab.dependency;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.uima.jcas.cas.FSList;

import de.linguatools.disco.DISCO;
import edu.cmu.lti.qalab.types.*;

public class Depsimilar {
	DISCO disco;
	DepTreeInfo tree;
	String filter = "";
	String postiveFilter=null;
	public Depsimilar(String path,String depTreePath) throws Exception{
		disco = new DISCO(path, false);
		tree = new DepTreeInfo(depTreePath);
		
	}
	
	public double getScore(Sentence target,Sentence candidate) throws IOException{
		ArrayList<Dependency> targtList = this.getDepencyListFromSentence(target) ;
		ArrayList<Dependency> candiList = this.getDepencyListFromSentence(candidate) ;
		
		double score = 0.0;
		int validTarget = 0;
		for(Dependency t:targtList){
			String rel = t.getRelation();
			rel = rel.replaceFirst("_.*", "");
			if (!tree.containsDep(rel))
				continue;
			if(disco.frequency(t.getGovernor().getText())==0||disco.frequency(t.getDependent().getText())==0){
				//System.out.println(t.getGovernor().getText()+" "+ t.getDependent().getText());
				continue;
			}
			if(filter.contains(rel)||(postiveFilter!=null&&!postiveFilter.contains(rel)))
				continue;
			double max = 0.0;
			validTarget++;
			Dependency maxC = null;
			for(Dependency c:candiList){
				double tmp = this.getScore(t, c);
				if(tmp>max){
					maxC = c;
					max = tmp;
				}
					
				
			}
			score+=max;	
			String d1 = t.getRelation()+"("+t.getGovernor().getText() +","+t.getDependent().getText()+")";
			String d2 = "";
			if (maxC!=null)
				d2 = maxC.getRelation()+"("+maxC.getGovernor().getText() +","+maxC.getDependent().getText()+")";
			System.out.println(max+ " "+d1+" <---> "+d2);
			
		}
		if(validTarget==0)
			return -1;
		return 1.0*score/validTarget;
	}
	
	ArrayList<Dependency> getDepencyListFromSentence(Sentence cent) {

		FSList depList = cent.getDependencyList();
		ArrayList<Dependency> dependencyList = new ArrayList<Dependency>();
		int i = 0;
		while (true) {

			Dependency dependency = null;
			try {
				dependency = (Dependency) depList.getNthElement(i);
			} catch (Exception e) {
				break;
			}
			dependencyList.add(dependency);
			i++;
		}

		return dependencyList;
	}
	public double getScore(Dependency parent,Dependency child) throws IOException{
		double score1;
		double score2;
		String[] pword = new String[2];
		String[] sword = new String[2];
		pword[0] = parent.getGovernor().getText();
		pword[1] = parent.getDependent().getText();
		sword[0] = child.getGovernor().getText();
		sword[1] = child.getDependent().getText();
		double[][] s = new double[2][2];
		for(int i=0;i<2;i++){
			for(int j=0;j<2;j++){
				s[i][j] = Math.max(disco.firstOrderSimilarity(pword[i], sword[j]), disco.secondOrderSimilarity(pword[i], sword[j]));
			}
		}
		
		double tmp1 = Math.max(s[0][0],s[0][1]);
		double tmp2 = Math.max(s[1][0], s[1][1]);
		if(tmp1==-1)
			tmp1 = 0;
		if(tmp2==-1)
			tmp2 = 0;
		
		score1= (tmp1+tmp2)/2;
		score2 = tree.getScore(parent.getRelation(), child.getRelation());
		//System.out.println(score1+" "+score2);
		return score1*score2;
	}
}
