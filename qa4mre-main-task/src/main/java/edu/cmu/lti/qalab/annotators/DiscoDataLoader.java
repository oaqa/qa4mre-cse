package edu.cmu.lti.qalab.annotators;

import java.util.HashMap;

import javax.naming.Context;

import org.apache.uima.UimaContext;


import de.linguatools.disco.DISCO;

public class DiscoDataLoader {
	private static DiscoDataLoader instance = null;
	private HashMap<String, DISCO> loadMap = new HashMap<String, DISCO>();

	protected DiscoDataLoader(UimaContext context) {
		try {
			String wikiModel=(String)context.getConfigParameterValue("DISCO_WIKI_PATH");
			DISCO disco = new DISCO(wikiModel, false);
			loadMap.put(wikiModel, disco);
			String pubmedModel=(String)context.getConfigParameterValue("DISCO_PUBMED_PATH");
			DISCO disco1 = new DISCO(pubmedModel, false);
			loadMap.put(pubmedModel, disco1);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	public static DiscoDataLoader getInstance(UimaContext context)
    {
        if (instance == null)
        {
            synchronized (DiscoDataLoader.class)
            {
                if (instance == null)
                {
                    instance = new DiscoDataLoader(context);
                }
            }
        }

        return instance;
    }

	public DISCO getDiscoModel(String key){
		return loadMap.get(key);
	}

}
