package analysis.toolkit.demo.sentiment.dictionary.liwc.lang;

public class Category {
	private String name;
	private int id;
	
	/**
	 * constructor specifying the name and the id
	 * @param name the name
	 * @param id the id
	 */
	public Category(String name, int id){
		this.name = name;
		this.id = id;
	}

	/**
	 * gets the id
	 * @return the id
	 */
	public int getID() {
		return id;
	}

	/**
	 * gets the name
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	public String toString(){
		return name + "(" + id + ")";
	}
}
