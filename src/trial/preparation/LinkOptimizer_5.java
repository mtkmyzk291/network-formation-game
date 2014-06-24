package trial.preparation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import basic.preparation.*;

public class LinkOptimizer_5 {

	static double sigma = 0.5; // 減衰率
	static double linkedCost = 1.0; // 直接リンクするコスト

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NetworkGenerator generator = new NetworkGenerator();

		LinkOptimizer_5 lo = new LinkOptimizer_5();
		List<Agent> agentList = new ArrayList<>();

		int allLink = 4;

		for (int i = 0; i < 100; i++) { // iの上限でエージェント数変更
			agentList.add(new Agent());
		}

		for (int p = 0; p < 100; p++) {
			System.out.println(p + "回目");
			for (Agent agent : agentList) {
				agent.setInfoValue(lo.setInfoValue(agent.getInfoValue())); // 各エージェントの持つ情報の価値を設定
			}

			 generator.createTwoWaySmallWorldConstLinkNum(agentList, allLink,
			 0.1, 0.05, 0.05); // 全リンク数、張り替え率、リンクのランダムさ、リンクの単方向性
//			generator.createCnnModel(agentList, 4);

			// // ここから期待値用[ここは要る]
			// try {
			// Writer writer = new FileWriter("140114_expectation.txt", true);
			// BufferedWriter bw = new BufferedWriter(writer);
			//
			// bw.write("From\tTo\tNew Links\tExpectation\n");
			//
			// bw.close();
			// writer.close();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			// // ここまで期待値用
			//
			// // ここからノードリスト
			// for (Agent node : agentList) {
			// try {
			// Writer writer = new FileWriter("140114_list.txt", true);
			// BufferedWriter bw = new BufferedWriter(writer);
			//
			// bw.write(node.getId() + "\t");
			//
			// for (Node linkedNode : node.getLinkedNodeSet()) {
			// Agent linkedAgent = (Agent) linkedNode;
			// bw.write(linkedAgent.getId() + "\t");
			// }
			//
			// bw.newLine();
			// bw.close();
			// writer.close();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			// }
			// // ここまでノードリスト

			int agentNum = agentList.size();
			double firstNWExpectation = 0.0;
			int[][] countedDistanceAry = calcCountedDistanceAry(agentNum, agentList);
			// ネットワーク全体の得点算出
			firstNWExpectation = calculateExpectation(agentList, countedDistanceAry);
			// System.out.println("初期NWの得点：" + firstNWExpectation);
			SimpleNetwork<Agent, Link> network = new SimpleNetwork<Agent, Link>(agentList);
			double cv = network.getClusterValue();
			double path = network.getAveragePathLength();

			double ass = network.getAssortativity();
			double pow = network.getDegreeDistributionMap().getPowerIndex();
			double link = network.getDegreeDistributionMap().getAverageDegree();
			// System.out.printf("%.3f\t%.3f\t%.3f\t%.3f\t%.3f\t\n", cv, path,
			// ass, pow, link);

			// ここから期待値[ここは要る]
			try {
				Writer writer = new FileWriter("140203_nwExpectation.txt", true);
				BufferedWriter bw = new BufferedWriter(writer);

				bw.write(firstNWExpectation + "\t" + cv + "\t" + path + "\t" + ass + "\t" + pow + "\t" + link + "\n");

				bw.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// ここまで期待値

			chooseNewLink(agentNum, agentList);
		}
	}

	private final Random rand;

	public LinkOptimizer_5(Random rand) {
		this.rand = rand;
	}

	public LinkOptimizer_5() {
		this.rand = new Random();
	}

	private double setInfoValue(double infoValue) {
		infoValue = rand.nextDouble();
		return infoValue;
	}

	/*
	 * 2ノード間のステップ数を求める
	 */
	public static int[][] calcCountedDistanceAry(int agentNum, List<Agent> agentList) {

		// 2エージェント間の経路長の初期化
		int[][] distanceArray = new int[agentNum][agentNum];
		for (Agent agent : agentList) {
			for (Agent target : agentList) {
				if (target.isLinkTo(agent)) {
					distanceArray[agent.getId()][target.getId()] = 1;
				} else if (target == agent) {
					distanceArray[agent.getId()][target.getId()] = 0;
				} else {
					distanceArray[agent.getId()][target.getId()] = 10000;
				}
			}
		}

		// フロイドのアルゴリズムによる最短経路長算出
		int[][] countedDistanceAry = new int[agentNum][agentNum];
		countedDistanceAry = floydAlgo(agentList, distanceArray);

		return countedDistanceAry;

	}

	/*
	 * フロイドのアルゴリズムを用いて最短経路長を求める calcCountedDistanceAryから呼び出し
	 */
	public static int[][] floydAlgo(List<Agent> agentList, int[][] distanceArray) {
		int agentNum = agentList.size();
		for (int k = 0; k < agentNum; k++) {
			for (int i = 0; i < agentNum; i++) {
				for (int j = 0; j < agentNum; j++) {
					// k回目のステップにおける、ノードiからノードkまでの距離（暫定）
					int distance_i_k_j = distanceArray[i][k] + distanceArray[k][j];

					if (distance_i_k_j < distanceArray[i][j]) {
						distanceArray[i][j] = distance_i_k_j;
					}
				}
			}
		}
		return distanceArray;
	}

	/*
	 * NW全体の期待値を計算する
	 */
	public static double calculateExpectation(List<Agent> agentList, int[][] countedDistanceAry) {
		int agentNum = agentList.size();
		double nwScore = 0.0;
		double[][] scoreAry = new double[agentNum][agentNum];
		double[] expectation = new double[agentNum];
		for (Agent agent : agentList) {
			for (Agent target : agentList) {
				if (countedDistanceAry[agent.getId()][target.getId()] == 0) {
					continue;
				} else if (countedDistanceAry[agent.getId()][target.getId()] == 1) {
					double myScore = target.getInfoValue() * (sigma - linkedCost);
					scoreAry[agent.getId()][target.getId()] += myScore;
					agent.score = myScore;

				} else {
					double myScore = target.getInfoValue()
							* (Math.pow(sigma, countedDistanceAry[agent.getId()][target.getId()]));
					scoreAry[agent.getId()][target.getId()] += myScore;
					agent.score = myScore;
				}

				expectation[agent.getId()] += scoreAry[agent.getId()][target.getId()];
			}
			try {
				Writer writer = new FileWriter("140203_Expectation.txt", true);
				BufferedWriter bw = new BufferedWriter(writer);

				bw.write(expectation[agent.getId()] + "\n");

				bw.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			Writer writer = new FileWriter("140203_Expectation.txt", true);
			BufferedWriter bw = new BufferedWriter(writer);

			bw.newLine();

			bw.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (Agent agent : agentList) {
			nwScore += expectation[agent.getId()];
		}
		return nwScore;
	}

	/*
	 * あるノードからの期待値を計算する
	 */
	public static double calculateEachExpect(Agent receiver, Agent sender, Set<? extends Node> nodeSet,
			int[][] countedDistanceAry) {
		double nodeExpect = 0.0;
		if (countedDistanceAry[receiver.getId()][sender.getId()] == 1) {
			nodeExpect = sender.getInfoValue() * (sigma - linkedCost);
		} else if (countedDistanceAry[receiver.getId()][sender.getId()] != 0) {
			nodeExpect = sender.getInfoValue()
					* (Math.pow(sigma, countedDistanceAry[receiver.getId()][sender.getId()]));
		}
		return nodeExpect;
	}

	public static void chooseNewLink(int agentNum, List<Agent> agentList) {

		// ここから
		List<Agent> copyList = new ArrayList<>();
		for (int k = 0; k < agentList.size(); k++) {
			Agent copyAgent = new Agent();

			int copyId = agentList.get(k).getId();
			double copyInfoValue = agentList.get(k).getInfoValue();

			copyList.add(copyAgent);
			copyList.get(k).setId(copyId);
			copyList.get(k).setInfoValue(copyInfoValue);
		}
		for (int j = 0; j < copyList.size(); j++) {
			Set<Agent> copyLinkedAgentSet = new HashSet<>();
			for (Node copyLinkedNode : agentList.get(j).getLinkedNodeSet()) {
				Agent copyLinkedAgent = (Agent) copyLinkedNode;
				int copyLinkedAgentId = copyLinkedAgent.getId();
				copyLinkedAgentSet.add(copyList.get(copyLinkedAgentId));
			}
			for (Agent copyLinkedAgent : copyLinkedAgentSet) {
				copyList.get(j).addLink(copyLinkedAgent);
			}
		}
		// ここまでがコピーリスト作成

		int maxLinkNum = 10;
		for (int p = 0; p < maxLinkNum; p++) {
			List<Integer> nextLinkList = new ArrayList<>(); // get(i)で、idにiを持つエージェントが次にリンクを作成すべきエージェントのidを得られる
			// 各エージェントが次にリンクを作成すべきエージェントを決定する
			for (int i = 0; i < copyList.size(); i++) {
				int myLinkNum = copyList.get(i).getLinkedNodeSet().size();
				if (myLinkNum < maxLinkNum) {
					// 自分と1ステップでリンクしているノードのハッシュマップを作成
					HashMap<Integer, Agent> linkedAgentMap = new HashMap<Integer, Agent>();
					for (Node node : copyList.get(i).getLinkedNodeSet()) {
						Agent neighbor = (Agent) node;
						linkedAgentMap.put(neighbor.getId(), neighbor);
					}

					// 自分と2ステップでリンクしているノードのリストを作成
					Set<Agent> twostepAgentSet = new HashSet<>();
					for (Integer id : linkedAgentMap.keySet()) {
						Agent neighbor = linkedAgentMap.get(id);
						Set<Agent> targetNodeSet = new HashSet<>();
						for (Node hisLinked : neighbor.getLinkedNodeSet()) {
							Agent linked = (Agent) hisLinked;
							targetNodeSet.add(linked);
						}
						for (Node nextNode : targetNodeSet) {
							Agent target = (Agent) nextNode;
							if (target.getId() != copyList.get(i).getId()
									&& !linkedAgentMap.containsKey(target.getId())) {
								twostepAgentSet.add(target);
							}
						}
					}

					// 2ステップ先のノードのうち、最も期待値の高いものを求める
					int[][] currentDistanceAry = calcCountedDistanceAry(agentNum, copyList); // ノード同士の距離を入れる行列
					double preMax = 0.0; // Maxの期待値を入れるための初期化
					int preId = 10000; // Maxの期待値を得られるエージェントのIdを入れるための初期化
					for (Agent sender : twostepAgentSet) {
						double eachExpect = calculateEachExpect(copyList.get(i), sender, twostepAgentSet,
								currentDistanceAry);
						// System.out.println("sender:"+sender.getId()+"score:"+eachExpect);
						if (preMax < eachExpect) {
							preMax = eachExpect;
							preId = sender.getId();
						}
					}
					nextLinkList.add(preId);
				} else {
					nextLinkList.add(10000);
				}
			}

			// 全エージェントに1つずつリンクを追加
			for (int i = 0; i < copyList.size(); i++) {
				if (nextLinkList.get(i) > copyList.size()) {
					continue;
				} else {
					Agent receiver = copyList.get(i);
					Agent newLinker = copyList.get(nextLinkList.get(i));
					receiver.addLink(newLinker);
				}
			}

			int[][] countedDistanceAry = calcCountedDistanceAry(agentNum, copyList);
			double trialExpectation = 0.0;
			trialExpectation = calculateExpectation(copyList, countedDistanceAry);
			// System.out.println("score：" + trialExpectation);

			SimpleNetwork<Agent, Link> network = new SimpleNetwork<Agent, Link>(copyList);
			double cv = network.getClusterValue();
			double path = network.getAveragePathLength();

			double ass = network.getAssortativity();
			double pow = network.getDegreeDistributionMap().getPowerIndex();
			double link = network.getDegreeDistributionMap().getAverageDegree();
			// System.out.printf("%.3f\t%.3f\t%.3f\t%.3f\t%.3f\t\n", cv, path,
			// ass, pow, link);

			// ここから期待値[ここは要る]
			try {
				Writer writer = new FileWriter("140203_nwExpectation.txt", true);
				BufferedWriter bw = new BufferedWriter(writer);

				bw.write(trialExpectation + "\t" + cv + "\t" + path + "\t" + ass + "\t" + pow + "\t" + link + "\n");

				bw.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
