package basic.preparation;

import java.util.Set;

/**
 * ネットワーク上のノードを示すインターフェース
 * @author tori
 *
 */
public interface Node{

	/**
	 * このノードと接続している全てのノードを返す
	 * @return このノードと接続している全てのノード
	 */
	public Set<? extends Node> getLinkedNodeSet(); 
	
    /**
     * このノードが持つすべてのリンクを取得
     * @return このノードが持つすべてのリンク
     */
    public Set<? extends Link> getLinkSet();
    
	/**
	 * 対象ノードへのリンクを返す
	 * @param target 対象のノ-ド
	 * @return 対象へのリンク<br>
	 * 存在しなければnullを返す
	 */
	public Link getLink(Node target);

	/**
	 * あるノードとリンクしているかどうかを返す
	 * @param target リンクしているかどうかを返す
	 * @return リンクしていればtrue
	 */
	public boolean isLinkTo(Node target);
	
	/**
	 * 対象となるノードのリンクを削除する
	 * @param node
	 */
	public void removeLink(Node node);
	
	/**
	 * 対象となるリンクを削除する
	 * @param link
	 */
	public void removeLink(Link link);
	
	/**
	 * 全リンクを削除する
	 */
	public void removeAllLink();
	
	/**
	 * 指定したノードへのリンクを追加する<br>
	 * @param node
	 */
	public void addLink(Node node);
	
	/**
	 * 指定したリンクを追加する
	 * @param link
	 */
	public void addLink(Link link);
}
