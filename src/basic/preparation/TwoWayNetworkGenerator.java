package basic.preparation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TwoWayNetworkGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		for (int j = 0; j < 10; j++) {
			List<Node> nodeList = new ArrayList<Node>();
			for (int i = 0; i < 200; i++) {
				nodeList.add(new SimpleNode());
			}
			TwoWayNetworkGenerator twGenerator = new TwoWayNetworkGenerator();
			// generator.createRegularNetwork(nodeList, 6);
			// generator.createSmallWorldConstLinkNum(nodeList, 6, 0.1);
//			generator.createTwoWaySmallWorldConstLinkNum(nodeList, 6, 0.1);
//			generator.createSmallWorld(nodeList, 6, 0.1);
			twGenerator.createTwoWaySmallWorld(nodeList, 6, 0.1);
			
			SimpleNetwork<Node, Link> network = new SimpleNetwork<Node, Link>(nodeList);
			double cv = network.getClusterValue();
			double path = network.getAveragePathLength();

			double ass = network.getAssortativity();
			double pow = network.getDegreeDistributionMap().getPowerIndex();
			double link = network.getDegreeDistributionMap().getAverageDegree();
			System.out.printf("%.3f\t%.3f\t%.3f\t%.3f\t%.3f\t\n", cv, path, ass, pow, link);

			// System.out.printf(network.getDegreeDistributionMap().toString());

			// for (Node node : nodeList) {
			// for (int l = 0; l < nodeList.size(); l++) {
			// Node target = nodeList.get(l);
			// if (target.isLinkTo(node)) {
			// System.out.println(node + "\t" + target);
			// }
			// }
			// }
		}

	}

	private final Random rand;

	public TwoWayNetworkGenerator(Random rand) {
		this.rand = rand;
	}

	public TwoWayNetworkGenerator() {
		this.rand = new Random();
	}

	// 全リンクが双方向結合となっているレギュラーネットワークを作成（リンク数は別々にカウント）
	public void createRegularNetwork(List<? extends Node> nodeList, int linkNum) {
		for (Node node : nodeList) {
			node.removeAllLink();
		}
		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.get(i);
			for (int k = 1; k <= linkNum / 2; k++) {
				Node target = nodeList.get((i + k) % nodeList.size());
				node.addLink(target);
				target.addLink(node);
			}
		}
	}

	// 全リンクが単方向結合となっているレギュラーネットワークを作成（リンク数は偶奇いずれも可）
	public void createTwoWayRegularNetwork(List<? extends Node> nodeList, int linkNum) {
		for (Node node : nodeList) {
			node.removeAllLink();
		}
		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.get(i);
			for (int k = 1; k <= linkNum; k++) {
				Node target = nodeList.get((i + k) % nodeList.size());
				double choice = rand.nextDouble();
				if (choice > 0.5) {
					node.addLink(target);
				} else {
					target.addLink(node);
				}
			}
		}
	}

	/**
	 * ランダムネットワークを作成する
	 * 
	 * @param nodeList
	 *            レギュラーネットワークを張るノードのリスト
	 * @param linkNum
	 *            リンク数．偶数である必要がある．
	 */
	public void createRandomNetwork(List<? extends Node> nodeList, int linkNum) {
		createSmallWorldConstLinkNum(nodeList, linkNum, 1.0);
	}

	// 全リンクが双方向結合となっているスモールワールドネットワーク（WSモデル）を作成（リンク数は別々にカウント）
	public void createSmallWorldConstLinkNum(List<? extends Node> nodeList, int linkNum, double changeRate) {
		createRegularNetwork(nodeList, linkNum);
		int changeNum = (int) (Math.round(nodeList.size() * linkNum * changeRate));

		for (int i = 0; i < changeNum; i++) {
			Node node1 = nodeList.get(rand.nextInt(nodeList.size()));
			List<Link> linkList1 = new ArrayList<Link>(node1.getLinkSet());
			Link link1 = linkList1.get(rand.nextInt(linkList1.size()));
			Node target1 = link1.otherNode(node1);

			Node node2 = nodeList.get(rand.nextInt(nodeList.size()));
			List<Link> linkList2 = new ArrayList<Link>(node2.getLinkSet());
			Link link2 = linkList2.get(rand.nextInt(linkList2.size()));
			Node target2 = link2.otherNode(node2);

			if (node1 == node2 || target1 == target2) {
				i--;
				continue;
			}
			if (node1 == target2 || node2 == target1) {
				i--;
				continue;
			}
			if (node1.isLinkTo(target2) || node2.isLinkTo(target1)) {
				i--;
				continue;
			}

			node1.removeLink(target1);
			target1.removeLink(node1);
			node2.removeLink(target2);
			target2.removeLink(node2);

			node1.addLink(target2);
			target2.addLink(node1);
			node2.addLink(target1);
			target1.addLink(node2);

		}

	}

	// 全リンクが単方向結合となっているスモールワールドネットワーク（WSモデル）を作成
	public void createTwoWaySmallWorldConstLinkNum(List<? extends Node> nodeList, int linkNum, double changeRate) {
		createTwoWayRegularNetwork(nodeList, linkNum);
		int changeNum = (int) (Math.round(nodeList.size() * linkNum * changeRate));

		for (int i = 0; i < changeNum; i++) {
			Node node1 = nodeList.get(rand.nextInt(nodeList.size()));
			List<Link> linkList1 = new ArrayList<Link>(node1.getLinkSet());
			Link link1 = linkList1.get(rand.nextInt(linkList1.size()));
			Node target1 = link1.otherNode(node1);

			Node node2 = nodeList.get(rand.nextInt(nodeList.size()));
			List<Link> linkList2 = new ArrayList<Link>(node2.getLinkSet());
			Link link2 = linkList2.get(rand.nextInt(linkList2.size()));
			Node target2 = link2.otherNode(node2);

			if (node1 == node2 || target1 == target2) {
				i--;
				continue;
			}
			if (node1 == target2 || node2 == target1) {
				i--;
				continue;
			}
			if (node1.isLinkTo(target2) || node2.isLinkTo(target1)) {
				i--;
				continue;
			}

			node1.removeLink(target1);
			node2.removeLink(target2);

			node1.addLink(target2);
			node2.addLink(target1);

		}

	}

	/**
	 * 張替え法によるSmallWorldの作成．<br>
	 * 
	 * @param nodeList
	 *            ネットワークに存在するノード
	 * @param linkNum
	 *            リンク数
	 * @param changeRate
	 *            変更率(0.0-1.0)
	 */
	public void createSmallWorld(List<? extends Node> nodeList, int linkNum, double changeRate) {
		createRegularNetwork(nodeList, linkNum);
		int changeNum = (int) (Math.round(nodeList.size() * linkNum * changeRate));

		for (int i = 0; i < changeNum; i++) {
			Node node1 = nodeList.get(rand.nextInt(nodeList.size()));
			List<Link> linkList1 = new ArrayList<Link>(node1.getLinkSet());
			if (linkList1.size() == 1) {
				i--;
				continue;
			}
			Link link1 = linkList1.get(rand.nextInt(linkList1.size()));
			Node target1 = link1.otherNode(node1);
			if (target1.getLinkSet().size() == 1) {
				i--;
				continue;
			}

			Node node2 = nodeList.get(rand.nextInt(nodeList.size()));

			if (node1 == node2 || target1 == node2) {
				i--;
				continue;
			}
			if (node1.isLinkTo(node2)) {
				i--;
				continue;
			}

			node1.removeLink(target1);
			target1.removeLink(node1);

			node1.addLink(node2);
			node2.addLink(node1);

		}
	}

	// 単方向結合
	public void createTwoWaySmallWorld(List<? extends Node> nodeList, int linkNum, double changeRate) {
		createTwoWayRegularNetwork(nodeList, linkNum);
		int changeNum = (int) (Math.round(nodeList.size() * linkNum * changeRate));

		for (int i = 0; i < changeNum; i++) {
			Node node1 = nodeList.get(rand.nextInt(nodeList.size()));
			List<Link> linkList1 = new ArrayList<Link>(node1.getLinkSet());
			if (linkList1.size() == 1) {
				i--;
				continue;
			}
			Link link1 = linkList1.get(rand.nextInt(linkList1.size()));
			Node target1 = link1.otherNode(node1);
			if (target1.getLinkSet().size() == 1) {
				i--;
				continue;
			}

			Node node2 = nodeList.get(rand.nextInt(nodeList.size()));

			if (node1 == node2 || target1 == node2) {
				i--;
				continue;
			}
			if (node1.isLinkTo(node2)) {
				i--;
				continue;
			}

			node1.removeLink(target1);

			node1.addLink(node2);

		}
	}

	// 双方向結合
	public void createCnnModel(List<? extends Node> nodeList, int averageLinkNum) {
		List<Node> list = new ArrayList<Node>(nodeList);
		for (Node node : list) {
			node.removeAllLink();
		}
		Collections.shuffle(list);

		List<Link> candidateLinkList = new ArrayList<Link>();
		int linkNum = 0;
		int idx = 1;
		while (idx < list.size()) {
			double remainNode = list.size() - idx;
			double remainLink = list.size() * averageLinkNum - linkNum - remainNode;
			double nodeRate = remainNode / (remainLink);
			if (nodeRate < 0) {
				System.exit(0);
			}
			if (rand.nextDouble() < nodeRate) {
				Node node = list.get(idx);
				Node target = list.get(rand.nextInt(idx));
				node.addLink(target);
				target.addLink(node);
				linkNum += 2;

				for (Node candidate : target.getLinkedNodeSet()) {
					if (node != candidate) {
						candidateLinkList.add(new SimpleLink(node, candidate));
					}
				}
				idx++;
			} else if (!candidateLinkList.isEmpty()) {
				Link link = candidateLinkList.get(rand.nextInt(candidateLinkList.size()));
				List<? extends Node> linkedNode = link.getNodeList();
				linkedNode.get(0).addLink(linkedNode.get(1));
				linkedNode.get(1).addLink(linkedNode.get(0));
				candidateLinkList.remove(link);
				linkNum += 2;
			}
		}

	}

	// 単方向結合
	public void createTwoWayCnnModel(List<? extends Node> nodeList, int averageLinkNum) {
		List<Node> list = new ArrayList<Node>(nodeList);
		for (Node node : list) {
			node.removeAllLink();
		}
		Collections.shuffle(list);

		List<Link> candidateLinkList = new ArrayList<Link>();
		int linkNum = 0;
		int idx = 1;
		while (idx < list.size()) {
			double remainNode = list.size() - idx;
			double remainLink = list.size() * averageLinkNum - linkNum - remainNode;
			double nodeRate = remainNode / (remainLink);
			if (nodeRate < 0) {
				System.exit(0);
			}
			if (rand.nextDouble() < nodeRate) {
				Node node = list.get(idx);
				Node target = list.get(rand.nextInt(idx));
				node.addLink(target);
				target.addLink(node);
				linkNum += 2;

				for (Node candidate : target.getLinkedNodeSet()) {
					if (node != candidate) {
						candidateLinkList.add(new SimpleLink(node, candidate));
					}
				}
				idx++;
			} else if (!candidateLinkList.isEmpty()) {
				Link link = candidateLinkList.get(rand.nextInt(candidateLinkList.size()));
				List<? extends Node> linkedNode = link.getNodeList();
				linkedNode.get(0).addLink(linkedNode.get(1));
				linkedNode.get(1).addLink(linkedNode.get(0));
				candidateLinkList.remove(link);
				linkNum += 2;
			}
		}

	}

	/**
	 * バラバシの理論に従ってスケールフリーネットワークを作成する
	 * 
	 * @param nodeList
	 *            ノードのリスト
	 * @param firstCluster
	 *            初期完全グラフ構成ノード数
	 * @param linkNum
	 *            　新規ノードが張るリンク数
	 */
	public void createBarabasiModel(List<? extends Node> nodeList, int firstCluster, int linkNum) {
		List<Node> list = new ArrayList<Node>(nodeList);
		for (Node node : list) {
			node.removeAllLink();
		}
		Collections.shuffle(list);

		createCompleteNetwork(list.subList(0, firstCluster));
		RouletSelector<Node> rs = new RouletSelector<Node>(rand);
		for (int j = 0; j < firstCluster; j++) {
			Node t = list.get(j);
			rs.put(t, (double) t.getLinkSet().size());
		}
		for (int i = firstCluster; i < list.size(); i++) {
			Node node = list.get(i);
			for (int j = 0; j < linkNum; j++) {
				Node target = rs.next();
				if (node.isLinkTo(target)) {
					j--;
					continue;
				}
				node.addLink(target);
				target.addLink(node);

				rs.put(node, rs.get(node) + 1);
				rs.put(target, rs.get(target) + 1);

				// System.out.println(node+"="+target);
			}
		}

	}

	/**
	 * 完全グラフを作成する
	 * 
	 * @param nodeList
	 *            完全グラフを作るノードのリスト
	 */
	public void createCompleteNetwork(List<? extends Node> nodeList) {
		for (Node node : nodeList) {
			node.removeAllLink();
		}

		for (Node node1 : nodeList) {
			for (Node node2 : nodeList) {
				if (node1 == node2) {
					continue;
				}
				node1.addLink(node2);
				node2.addLink(node1);
			}
		}
	}

	/**
	 * 任意の対象についてルーレット選択を行うクラス
	 * 
	 * @author tori
	 * 
	 * @param <T>
	 */
	static public class RouletSelector<T> {

		private Map<T, Double> rouletMap;

		double totalScore;
		Random rand;

		public RouletSelector(Random rand) {
			rouletMap = new HashMap<T, Double>();
			totalScore = 0;
			this.rand = rand;
		}

		/**
		 * ルーレット選択によって選択されたKeyを返す<br>
		 * 中身が空の時はnullを返す
		 * 
		 * @return
		 */
		public T next() {
			double totalScore = getTotalScore();
			double remain = rand.nextDouble() * totalScore;
			List<T> rouletList = new ArrayList<T>(rouletMap.keySet());

			for (T t : rouletList) {
				remain -= rouletMap.get(t);
				if (remain <= 0) {
					return t;
				}
			}
			return null;
		}

		/**
		 * トータルのスコアを返す
		 * 
		 * @return
		 */
		public double getTotalScore() {

			return totalScore;
		}

		public Double put(T key, Double value) {
			if (value < 0) {
				throw new IllegalArgumentException("value must be positive or zero but " + value);
			}
			if (rouletMap.containsKey(key)) {
				totalScore -= rouletMap.get(key);
			}
			totalScore += value;
			return rouletMap.put(key, value);
		}

		public Double get(Object key) {
			if (rouletMap.containsKey(key)) {
				return rouletMap.get(key);
			} else {
				return 0.0;
			}
		}
	}
}