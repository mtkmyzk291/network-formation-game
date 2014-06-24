//package trial.preparation;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class game {
//
//	public static void main(String[] args) {
//
//		List<Agent> agentList = new ArrayList<>();
//
//		// エージェントを作成
//		for (int i = 0; i < 5; i++) {
//			agentList.add(new Agent());
//		}
//		// リンクを張る
//		agentList.get(0).addLink(agentList.get(1));
//		agentList.get(0).addLink(agentList.get(2));
//		agentList.get(1).addLink(agentList.get(0));
//		agentList.get(1).addLink(agentList.get(2));
//		agentList.get(1).addLink(agentList.get(3));
//		agentList.get(2).addLink(agentList.get(0));
//		agentList.get(2).addLink(agentList.get(1));
//		agentList.get(2).addLink(agentList.get(4));
//		agentList.get(3).addLink(agentList.get(1));
//		agentList.get(3).addLink(agentList.get(4));
//		agentList.get(4).addLink(agentList.get(2));
//		agentList.get(4).addLink(agentList.get(3));
//
//		// // 最短経路を求める
//		// int distance = dijkstra(agentList, agentList.get(4),
//		// agentList.get(0));
//		// System.out.println("メソッドから求めた"+agentList.get(4).getId()+"から"+agentList.get(0).getId()+"までの距離は"+distance);
//
//		// 距離を入れる配列
//		int arraySize = agentList.size() * 3;
//		int[] distanceArray = new int[arraySize];
//		for (Agent receiver : agentList) {
//			System.out.println("receiver:" + receiver.getId());
//			for (Agent sender : agentList) {
//				System.out.println("sender:" + sender.getId());
//				for(Agent resetAgent : agentList){
//					resetAgent.fixed = false;
//					resetAgent.cost = 1000000000;
//				}
//				int distance = dijkstra(agentList, receiver, sender);
//				switch (distance) {
//				case 1:
//					int arrayNum1 = sender.getId() * 3;
//					distanceArray[arrayNum1]++;
//				case 2:
//					int arrayNum2 = sender.getId() * 3 + 1;
//					distanceArray[arrayNum2]++;
//				case 3:
//					int arrayNum3 = sender.getId() * 3 + 2;
//					distanceArray[arrayNum3]++;
//				}
//			}
//		}
//		
//		for (int i = 0; i < distanceArray.length; i++) {
//			System.out.println("distanceArray" +distanceArray[i]);
//		}
//		
//
//		double[] scoreArray = countScore(agentList, distanceArray);
//		double totalScore = 0;
//		for (int i = 0; i < scoreArray.length; i++) {
//			totalScore = scoreArray[i]++;
//		}
//		System.out.println("ネットワーク全体の得点は" + totalScore);
//	}
//
//	public static void floydAlgo(int[][] distanceArray){
//		
//	}
//	
//	public static int dijkstra(List<Agent> agentList, Agent src, Agent dst) {
//
//		int distance = 0;
//
//		src.cost = 0; // 始点のコストを0に
//		src.fixed = true; // 始点を検証済みに
//		List<Agent> passedList = new ArrayList<>();// 経由したノードのリスト
//		passedList.add(src);
//
//		while (true) {
//			Agent prev = passedList.get(passedList.size() - 1); // 一つ前で経由したノードを取得
//			int minCost = 1000000000; // コストを確定させるまでのダミー
//			Agent passed = null; // 経由ノードを確定させるまでのダミー
//			
//			for (Agent agent : agentList) {
//				System.out.println(agent.getId()+"を検証する");
//				if (prev == dst){
//					System.out.println("目的地を既に経由している");
//					break;
//				} else if (prev.isLinkTo(agent)) { // 一つ前で経由したノードとリンクしているノードのうち
//					
//					if (agent.getFixed() == false) { // 未検証のものを考える
//						
//					if (agent == dst){
//						passed = agent;
//						System.out.println("直接リンクしている");
//						break;
//					} 
//					
//						System.out.println(agent.getId() + "は" + prev.getId()
//								+ "とリンクしている");
//						int newCost = prev.getCost() + 1;
//						if (agent.getCost() > newCost) {
//							agent.cost = newCost; // コストの書き換え
//							// 暫定的な経由地とする
//							if (minCost > agent.cost) {
//								minCost = agent.cost;
//								passed = agent;
//								System.out.println(agent.getId() + "は暫定的な経由地");
//							} else {
//								System.out.println(agent.getId()+"は経由地ではない");
//							}
//						}
//					}
//				} else {
//					continue;
//				}
//			}
//
//			if (passed == null) {
//				System.out.println("経由地なし");
//				break;
//			} else {
//				// 経由地を確定させる
//				passed.fixed = true;
//				passedList.add(passed);
//				prev = passed;
//				System.out.println(prev.getId() + "は経由地として確定");
//
//				if (prev.getId() == dst.getId()) {
//					distance = passedList.size() - 1;
//					System.out.println("距離は" + distance);
//					break;
//				}
//			}
//		}
//		return distance;
//
//	}
//
//	public static double[] countScore(List<Agent> agentList, int[] distanceArray) {
//		double delta = 0.5;
//		double linkCost = 0.1;
//
//		double[] scoreArray = new double[agentList.size()];
//		for (Agent agent : agentList) {
//			int arrayNum1 = distanceArray[agent.getId() * 3];
//			int arrayNum2 = distanceArray[agent.getId() * 3 + 1];
//			int arrayNum3 = distanceArray[agent.getId() * 3 + 2];
//			scoreArray[agent.getId()] = arrayNum1 * (delta - linkCost)
//					+ arrayNum2 * Math.pow(delta, 2) + arrayNum3
//					* Math.pow(delta, 3);
//			System.out.println(agent.getId() + "の得点は"
//					+ scoreArray[agent.getId()]);
//		}
//
//		return scoreArray;
//	}
//
//}
