package basic.preparation;

import java.io.Serializable;

/**
 * 単純なリンク
 * @author tori
 *
 */
public class SimpleLink extends AbstractLink implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * シンプルな無向リンクを作成する
	 * @param nodeFrom
	 * @param nodeTo
	 */
	public SimpleLink(Node nodeFrom, Node nodeTo) {
		super(nodeFrom, nodeTo);
		isDirected = false;
	}
	
	/**
	 * 無向かどうかを指定してリンクを作成する
	 * @param nodeFrom
	 * @param nodeTo
	 * @param isDirected
	 */
	public SimpleLink(Node nodeFrom, Node nodeTo, boolean isDirected) {
		super(nodeFrom, nodeTo);
		this.isDirected = isDirected;
	}
}