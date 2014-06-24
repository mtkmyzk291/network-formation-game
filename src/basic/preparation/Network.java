package basic.preparation;

import java.util.Set;

/**
 * ネットワーク全体を示すインターフェース
 * @author tori
 *
 */
public interface Network {

	/**
	 * ネットワークに含まれるすべてのノードを返す
	 * 
	 * @return
	 */
	abstract public Set<? extends Node> getNodeSet();

	/**
	 * ネットワークに含まれる全てのLinkを返す
	 * @return
	 */
	abstract public Set<? extends Link> getLinkSet();

	/**
	 * このネットワークワールドに存在するノード数を返す
	 * @return
	 */
	public abstract int size();

}