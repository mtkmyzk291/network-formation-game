package basic.preparation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 基本的な機能のみを持つシンプルなネットワークを作成する
 * @author tori
 *
 * @param <N> ノードのタイプ
 * @param <L>　リンクのタイプ
 */
public class SimpleNetwork<N extends Node, L extends Link> extends AbstractNetwork {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Set<N> nodeSet;
	Set<L> linkSet;
	
	/**
	 * NodeのCollectionを指定して作成
	 * @param nodeCollection ネットワークに存在するノード
	 */
	public SimpleNetwork(Collection<N> nodeCollection) {
		nodeSet = new HashSet<N>(nodeCollection);
		linkSet = new HashSet<L>();
		for(N n:nodeCollection){
			linkSet.addAll((Set<L>)n.getLinkSet());
		}

	}

	public SimpleNetwork(Network network){
		this((Set<N>) network.getNodeSet());
	}
	
	@Override
	public Set<L> getLinkSet() {
		return linkSet;
	}

	@Override
	public Set<N> getNodeSet() {
		return nodeSet;
	}

}
