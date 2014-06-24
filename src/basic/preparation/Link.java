package basic.preparation;

import java.util.List;

/**
 * 
 * ネットワークにおけるノード間のつながりをあらわすinterface
 * 
 * @author tori
 *
 */
public interface Link {
	/**
	 * 始点終点
	 * @return 始点
	 */
	public List<? extends Node> getNodeList();

	/**
	 * 有効グラフかどうか
	 * @return false
	 */
	public boolean isDirected();
	
	/**
	 * このリンクの強さ
	 * @return リンクの強さ
	 */
	public double getPower();

	/**
	 * もう片方のリンク
	 * @param node
	 * @return このリンクが持つ，もう片方のリンク
	 * @throws NoNodeException 指定したノードがリンクに含まれていない
	 */
	public Node otherNode(Node node)throws NoNodeException;
	
}
