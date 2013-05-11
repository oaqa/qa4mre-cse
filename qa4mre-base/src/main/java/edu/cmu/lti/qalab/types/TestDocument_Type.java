
/* First created by JCasGen Wed Feb 20 05:53:44 EST 2013 */
package edu.cmu.lti.qalab.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;


/** 
 * Updated by JCasGen Thu May 09 16:57:30 EDT 2013
 * @generated */
public class TestDocument_Type extends SourceDocument_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (TestDocument_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = TestDocument_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new TestDocument(addr, TestDocument_Type.this);
  			   TestDocument_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new TestDocument(addr, TestDocument_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = TestDocument.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.cmu.lti.qalab.types.TestDocument");
 
  /** @generated */
  final Feature casFeat_qaList;
  /** @generated */
  final int     casFeatCode_qaList;
  /** @generated */ 
  public int getQaList(int addr) {
        if (featOkTst && casFeat_qaList == null)
      jcas.throwFeatMissing("qaList", "edu.cmu.lti.qalab.types.TestDocument");
    return ll_cas.ll_getRefValue(addr, casFeatCode_qaList);
  }
  /** @generated */    
  public void setQaList(int addr, int v) {
        if (featOkTst && casFeat_qaList == null)
      jcas.throwFeatMissing("qaList", "edu.cmu.lti.qalab.types.TestDocument");
    ll_cas.ll_setRefValue(addr, casFeatCode_qaList, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public TestDocument_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_qaList = jcas.getRequiredFeatureDE(casType, "qaList", "uima.cas.FSList", featOkTst);
    casFeatCode_qaList  = (null == casFeat_qaList) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_qaList).getCode();

  }
}



    