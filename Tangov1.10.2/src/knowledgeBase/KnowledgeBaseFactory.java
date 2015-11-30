package knowledgeBase;

import properties.PropertiesStore;
import utilities.FoilException;

public class KnowledgeBaseFactory {
	
	private static KnowledgeBaseFactory instance = null;
	private KnowledgeBaseOperations kbop = null;
	private BindingsOperations bop = null;
//	private KnowledgeBaseType kbtype = null;
	
	private KnowledgeBaseFactory() {
		String type = "PROLOG";
		try {
			type = PropertiesStore.getProperty("knowledgeBaseEngine");
		} catch (FoilException e) {
			e.printStackTrace();
		}
		if (type.equals("PROLOG")) {
//			this.kbtype = KnowledgeBaseType.PROLOG; // by default
			kbop = new PrologOperations();
			bop = new SwiProlog();
		}
		else if (type.equals("HANDCRAFTED")){
//			this.kbtype = KnowledgeBaseType.HANDCRAFTED;
			kbop = new HandCraftedOperations();
			bop = new HandCrafted(kbop);
		}
	}
	
	public synchronized static KnowledgeBaseFactory getInstance() {
		if (instance == null) {
			instance = new KnowledgeBaseFactory();
		}
//		else {
//			if (instance.kbtype.equals(KnowledgeBaseType.PROLOG))
//				instance.kbop.reload();
//			else if (instance.kbtype.equals(KnowledgeBaseType.HANDCRAFTED))
//				instance.kbop.reload();
//		}
		return instance;
	}
	
	public synchronized static KnowledgeBaseFactory reload(String fileName) {
		if (instance == null)
			instance = new KnowledgeBaseFactory();
		else
			instance.kbop.reload(fileName);
		return instance;
	}
	
	public KnowledgeBaseOperations getKnowledgeBaseOperations() {
		return kbop;
	}

	public BindingsOperations getBindingsOperations() {
		return bop;
	}
}