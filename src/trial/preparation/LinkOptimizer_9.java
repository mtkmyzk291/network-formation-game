package trial.preparation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import basic.preparation.*;

public class LinkOptimizer_9 {

	static double sigma = 0.9; // 減衰率
	static double linkedCost = 0.1; // 直接リンクするコスト

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NetworkGenerator generator = new NetworkGenerator();

		LinkOptimizer_9 lo = new LinkOptimizer_9();
		List<Agent> agentList = new ArrayList<>();

		int allLink = 4;
		int counter = 20; // 1NWあたりの繰り返し回数

		for (int i = 0; i < 100; i++) { // iの上限でエージェント数変更
			agentList.add(new Agent());
		}
		for (int p = 0; p < 5; p++) {
			System.out.println(p + "回目");
			for (Agent agent : agentList) {
				agent.setInfoValue(lo.setInfoValue(agent.getInfoValue())); // 各エージェントの持つ情報の価値を設定

				// System.out.println(agent.infoValue);
			}

			// generator.createSmallWorldConstLinkNum(agentList, allLink, 0);
			// generator.createCnnModel(agentList, 4);
			// generator.createBarabasiModel(agentList, 4, 2);

			generator.createTwoWaySmallWorldConstLinkNum(agentList, allLink, 0.0, 0.0, 0.05); // 全リンク数、張り替え率、リンクのランダムさ、リンクの単方向性
			// generator.createRegularNetwork(agentList, allLink);
			// generator.createRandomNetwork(agentList, allLink);

			for (int j = 0; j < agentList.size(); j++) {
				System.out.println("linkpair_start");
				try {
					Writer writer = new FileWriter("140228_linkpair.txt", true);
					BufferedWriter bw = new BufferedWriter(writer);

					bw.write(agentList.get(j).getId() + "\t\t" + agentList.get(j).getLinkedNodeSet().size() + "\n");

					bw.close();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				for (Node linked : agentList.get(j).getLinkedNodeSet()) {
					Agent linkedA = (Agent) linked;
					try {
						Writer writer = new FileWriter("140228_linkpair.txt", true);
						BufferedWriter bw = new BufferedWriter(writer);

						bw.write(agentList.get(j).getId() + "\t" + linkedA.getId() + "\n");

						bw.close();
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			try {
				Writer writer = new FileWriter("140228_linkpair.txt", true);
				BufferedWriter bw = new BufferedWriter(writer);

				bw.newLine();

				bw.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				Writer writer = new FileWriter("140228_newLink.txt", true);
				BufferedWriter bw = new BufferedWriter(writer);
				bw.newLine();
				bw.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			int agentNum = agentList.size();
			double[] expectationAry = new double[agentNum];
			int[][] countedDistanceAry = calcCountedDistanceAry(agentNum, agentList);
			double firstNWExpectation = 0.0;
			// ネットワーク全体の得点算出
			for (int q = 0; q < agentList.size(); q++) {
				firstNWExpectation += calcMyExpectFirst(agentList.get(q), agentList, countedDistanceAry);
			}
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
				Writer writer = new FileWriter("140228_nwExpectation.txt", true);
				BufferedWriter bw = new BufferedWriter(writer);

				bw.write(firstNWExpectation + "\t" + cv + "\t" + path + "\t" + ass + "\t" + pow + "\t" + link + "\n");

				bw.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

//			try {
//				Writer writer = new FileWriter("140228_nwExpectation_random.txt", true);
//				BufferedWriter bw = new BufferedWriter(writer);
//
//				bw.write(firstNWExpectation + "\t" + cv + "\t" + path + "\t" + ass + "\t" + pow + "\t" + link + "\n");
//
//				bw.close();
//				writer.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			// ここまで期待値

			// chooseNewLinkSimple(agentNum, agentList, expectationAry,
			// counter);
			chooseNewLinkCompare(agentNum, agentList, expectationAry, counter);
			// chooseNewLinkOmni(agentNum, agentList, expectationAry, counter);
//			chooseNewLinkRandom(agentNum, agentList, counter);
		}
		System.out.println("finish");
	}

	private final Random rand;

	public LinkOptimizer_9(Random rand) {
		this.rand = rand;
	}

	public LinkOptimizer_9() {
		this.rand = new Random();
	}

	private double setInfoValue(double infoValue) {
		do {
			infoValue = rand.nextGaussian();
		} while (infoValue < 0);
		infoValue = 10 * infoValue;
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
				if (agent.isLinkTo(target)) {
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
	 * あるノードからの情報の期待値（直接リンクコストを含まない）を計算する
	 */
	public static double setPassedInfoValue(Agent receiver, int k, Agent sender, int distance) {
		double passedInfoExpect = 0.0;
		double delta = 0.0;
		if (k == 1) {
			delta = sigma;
		} else {
			delta = sigma / (Math.log(k));
		}

		if (distance == 1) {
			passedInfoExpect = sender.getInfoValue() * delta;
		} else if (distance != 0) {
			passedInfoExpect = sender.getInfoValue() * (Math.pow(delta, distance));
		}
		return passedInfoExpect;
	}

	public static double calcMyExpectFirst(Agent receiver, List<Agent> agentList, int[][] countedDistanceAry) {
		int agentNum = agentList.size();
		double[] scoreAry = new double[agentNum];
		double expectation = 0.0;

		for (Agent sender : agentList) {
			double a = setPassedInfoValue(receiver, receiver.getLinkedNodeSet().size(), sender,
					countedDistanceAry[receiver.getId()][sender.getId()]);
//			try {
//				Writer writer = new FileWriter("140228_each.txt", true);
//				BufferedWriter bw = new BufferedWriter(writer);
//
//				bw.write(receiver.getId() + "\t" + sender.getId() + "\t" + a + "\n");
//
//				bw.close();
//				writer.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			scoreAry[sender.getId()] += a;
			expectation += scoreAry[sender.getId()];
		}
		int linkedNum = receiver.getLinkedNodeSet().size();
		expectation -= linkedCost * linkedNum;
		try {
			Writer writer = new FileWriter("140228_Expectation.txt", true);
			BufferedWriter bw = new BufferedWriter(writer);

			bw.write(expectation + "\t" + linkedNum + "\n");

			bw.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

//		try {
//			Writer writer = new FileWriter("140228_Expectation_random.txt", true);
//			BufferedWriter bw = new BufferedWriter(writer);
//
//			bw.write(expectation + "\t" + linkedNum + "\n");
//
//			bw.close();
//			writer.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		return expectation;
	}

	/*
	 * NW全体の期待値を計算する
	 */
	public static double calcNWExpect(List<Agent> agentList, double[] expectation) {
		double nwScore = 0.0;
		for (Agent agent : agentList) {
			nwScore += expectation[agent.getId()];
		}
		return nwScore;
	}

	/*
	 * あるノードの得られる期待値の和
	 */
	public static double calcMyExpectNoWrite(Agent receiver, List<Agent> agentList, int[][] countedDistanceAry) {
		int agentNum = agentList.size();
		double[] scoreAry = new double[agentNum];
		double expectation = 0.0;

		for (Agent sender : agentList) {
			double a = setPassedInfoValue(receiver, receiver.getLinkedNodeSet().size(), sender,
					countedDistanceAry[receiver.getId()][sender.getId()]);
			// try {
			// Writer writer = new FileWriter("140228_each.txt", true);
			// BufferedWriter bw = new BufferedWriter(writer);
			//
			// bw.write(receiver.getId() + "\t" + sender.getId() + "\t" + a +
			// "\n");
			//
			// bw.close();
			// writer.close();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			scoreAry[sender.getId()] += a;
			expectation += scoreAry[sender.getId()];
		}
		int linkedNum = receiver.getLinkedNodeSet().size();
		expectation -= linkedCost * linkedNum;
		return expectation;
	}

	public static double calcMyExpect(Agent receiver, List<Agent> agentList, int[][] countedDistanceAry) {
		int agentNum = agentList.size();
		double[] scoreAry = new double[agentNum];
		double expectation = 0.0;

		for (Agent sender : agentList) {
			double a = setPassedInfoValue(receiver, receiver.getLinkedNodeSet().size(), sender,
					countedDistanceAry[receiver.getId()][sender.getId()]);
//			try {
//				Writer writer = new FileWriter("140228_each.txt", true);
//				BufferedWriter bw = new BufferedWriter(writer);
//
//				bw.write(receiver.getId() + "\t" + sender.getId() + "\t" + a + "\n");
//
//				bw.close();
//				writer.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			scoreAry[sender.getId()] += a;
			expectation += scoreAry[sender.getId()];
		}
		int linkedNum = receiver.getLinkedNodeSet().size();
		expectation -= linkedCost * linkedNum;
		try {
			Writer writer = new FileWriter("140228_Expectation.txt", true);
			BufferedWriter bw = new BufferedWriter(writer);

			bw.write(expectation + "\t" + linkedNum + "\n");

			bw.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return expectation;
	}

	public static double calcMyExpectRandom(Agent receiver, List<Agent> agentList, int[][] countedDistanceAry) {
		int agentNum = agentList.size();
		double[] scoreAry = new double[agentNum];
		double expectation = 0.0;

		for (Agent sender : agentList) {
			double a = setPassedInfoValue(receiver, receiver.getLinkedNodeSet().size(), sender,
					countedDistanceAry[receiver.getId()][sender.getId()]);
//			try {
//				Writer writer = new FileWriter("140228_each.txt", true);
//				BufferedWriter bw = new BufferedWriter(writer);
//
//				bw.write(receiver.getId() + "\t" + sender.getId() + "\t" + a + "\n");
//
//				bw.close();
//				writer.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			scoreAry[sender.getId()] += a;
			expectation += scoreAry[sender.getId()];
		}
		int linkedNum = receiver.getLinkedNodeSet().size();
		expectation -= linkedCost * linkedNum;
		try {
			Writer writer = new FileWriter("140228_Expectation_random.txt", true);
			BufferedWriter bw = new BufferedWriter(writer);

			bw.write(expectation + "\t" + linkedNum + "\n");

			bw.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return expectation;
	}

	/*
	 * あるノードからの期待値を計算する
	 */
	public static double calcOneToOneExpect(Agent receiver, Agent sender, Set<? extends Node> nodeSet,
			int[][] countedDistanceAry) {
		int k = receiver.getLinkedNodeSet().size();
		double nodeExpect = setPassedInfoValue(receiver, receiver.getLinkedNodeSet().size(), sender,
				countedDistanceAry[receiver.getId()][sender.getId()]) - linkedCost * k;
		return nodeExpect;
	}

	public static List<Agent> makeCopyList(List<Agent> agentList) {
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
		return copyList;
	}

	/*
	 * 自分と2ステップでリンクしているノードのセットを作成
	 */
	public static Set<Agent> makeTwostepAgentSet(Agent receiver) {
		// 自分と1ステップでリンクしているノードのハッシュマップを作成
		HashMap<Integer, Agent> linkedAgentMap = new HashMap<Integer, Agent>();
		for (Node node : receiver.getLinkedNodeSet()) {
			Agent neighbor = (Agent) node;
			linkedAgentMap.put(neighbor.getId(), neighbor);
		}

		// 自分と2ステップでリンクしているノードのセットを作成
		Set<Agent> twostepAgentSet = new LinkedHashSet<>();
		for (Integer id : linkedAgentMap.keySet()) {
			Agent neighbor = linkedAgentMap.get(id);
			Set<Agent> targetNodeSet = new HashSet<>();
			for (Node hisLinked : neighbor.getLinkedNodeSet()) {
				Agent linked = (Agent) hisLinked;
				targetNodeSet.add(linked);
			}
			for (Node nextNode : targetNodeSet) {
				Agent target = (Agent) nextNode;
				if (target.getId() != receiver.getId() && !linkedAgentMap.containsKey(target.getId())) {
					twostepAgentSet.add(target);
				}
			}
		}
		return twostepAgentSet;
	}

	public static void chooseNewLinkOmni(int agentNum, List<Agent> agentList, double[] expectationAry, int maxLinkNum) {

		List<Agent> copyList = makeCopyList(agentList);

		for (int p = 0; p < maxLinkNum; p++) {

			int[][] preCountedDistanceAry = calcCountedDistanceAry(agentNum, copyList);
			double[] lastExpectAry = new double[agentNum];

			List<Integer> nextLinkList = new ArrayList<>(); // get(i)で、idにiを持つエージェントが次にリンクを作成すべきエージェントのidを得られる
			// 各エージェントが次にリンクを作成すべきエージェントを決定する
			for (int i = 0; i < copyList.size(); i++) {
				lastExpectAry[i] = calcMyExpectNoWrite(copyList.get(i), copyList, preCountedDistanceAry);

				Set<Agent> twostepAgentSet = makeTwostepAgentSet(copyList.get(i));
				// 2ステップ先のノードのうち、正で最も期待値の高いものを求める
				int[][] currentDistanceAry = calcCountedDistanceAry(agentNum, copyList); // ノード同士の距離を入れる行列
				double preMax = -100.0; // Maxの期待値を入れるための初期化
				int preId = 10000; // Maxの期待値を得られるエージェントのIdを入れるための初期化
				for (Agent sender : twostepAgentSet) {
					double eachExpect = setPassedInfoValue(copyList.get(i), copyList.get(i).getLinkedNodeSet().size(),
							sender, currentDistanceAry[copyList.get(i).getId()][sender.getId()]);
					// System.out.println("sender:" + sender.getId() + "score:"
					// + eachExpect);
					if (preMax < eachExpect) {
						preMax = eachExpect;
						preId = sender.getId();
					}
				}

				if (preMax <= 0.0) {
					// System.out.println("期待値が負");
					nextLinkList.add(10000);
				} else {
					nextLinkList.add(preId);
				}
			}

			// 全エージェントに1つずつリンクを追加
			for (int i = 0; i < copyList.size(); i++) {
				double testMyExpect = 0.0;

				if (nextLinkList.get(i) < copyList.size()) {

					List<Agent> dammyList = makeCopyList(agentList);
					Agent receiver = dammyList.get(i);
					Agent candidate = dammyList.get(nextLinkList.get(i));
					receiver.addLink(candidate);
					int[][] testCountedDistanceAry = calcCountedDistanceAry(agentNum, dammyList);
					testMyExpect = calcMyExpectNoWrite(receiver, dammyList, testCountedDistanceAry);
					if (testMyExpect > lastExpectAry[i]) {// コストを上回る期待値をそのノードから得られれば新規リンク作成
						copyList.get(nextLinkList.get(i)).addLink(copyList.get(i));
					}
				}
				// try {
				// Writer writer = new FileWriter("140228_test.txt", true);
				// BufferedWriter bw = new BufferedWriter(writer);
				//
				// bw.write(lastExpectAry[i] + "\t" + testMyExpect + "\n");
				//
				// bw.close();
				// writer.close();
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
			}

			// エージェントリストをコピーリストに置き換え
			agentList = makeCopyList(copyList);

			for (int i = 0; i < agentList.size(); i++) {
				// ここから新規リンク[ここは要る]
				// try {
				// Writer writer = new FileWriter("140228_newLink.txt", true);
				// BufferedWriter bw = new BufferedWriter(writer);
				//
				// bw.write(agentList.get(i).getId() + "\t" +
				// agentList.get(nextLinkList.get(i)).getId() + "\t"
				// + agentList.get(i).getLinkedNodeSet().size() + "\n");
				//
				// bw.close();
				// writer.close();
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				// ここまで新規リンク
			}
			// ここから新規リンク[ここは要る]
			// try {
			// Writer writer = new FileWriter("140228_newLink.txt", true);
			// BufferedWriter bw = new BufferedWriter(writer);
			// bw.newLine();
			// bw.close();
			// writer.close();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			// ここまで新規リンク

			int[][] countedDistanceAry = calcCountedDistanceAry(agentNum, agentList);
			double trialExpectation = 0.0;
			// ネットワーク全体の得点算出
			for (int q = 0; q < agentList.size(); q++) {
				trialExpectation += calcMyExpect(agentList.get(q), agentList, countedDistanceAry);
			}
			// System.out.println("score：" + trialExpectation);

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
				Writer writer = new FileWriter("140228_nwExpectation.txt", true);
				BufferedWriter bw = new BufferedWriter(writer);

				bw.write(trialExpectation + "\t" + cv + "\t" + path + "\t" + ass + "\t" + pow + "\t" + link + "\n");

				bw.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// ここまで期待値
			if (p == (maxLinkNum - 1)) {
				// ここから新規リンク[ここは要る]
				// try {
				// Writer writer = new FileWriter("140228_newLink.txt", true);
				// BufferedWriter bw = new BufferedWriter(writer);
				// bw.write("finish\n");
				// bw.close();
				// writer.close();
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				// ここまで新規リンク
			}
		}

	}

	public static void chooseNewLinkCompare(int agentNum, List<Agent> agentList, double[] expectationAry, int maxLinkNum) {

		List<Agent> copyList = makeCopyList(agentList);

		for (int p = 0; p < maxLinkNum; p++) {

			int[][] preCountedDistanceAry = calcCountedDistanceAry(agentNum, copyList);
			double[] lastExpectAry = new double[agentNum];

			List<Integer> nextLinkList = new ArrayList<>(); // get(i)で、idにiを持つエージェントが次にリンクを作成すべきエージェントのidを得られる
			List<Double> nextPlusList = new ArrayList<>();// 次にリンクを作成すべきエージェントから今回得られた期待値

			// 各エージェントが次にリンクを作成すべきエージェントを決定する
			for (int i = 0; i < copyList.size(); i++) {
				lastExpectAry[i] = calcMyExpectNoWrite(copyList.get(i), copyList, preCountedDistanceAry);
				Set<Agent> twostepAgentSet = makeTwostepAgentSet(copyList.get(i));
				// 2ステップ先のノードのうち、正で最も期待値の高いものを求める
				int[][] currentDistanceAry = calcCountedDistanceAry(agentNum, copyList); // ノード同士の距離を入れる行列
				double preMax = -100.0; // Maxの期待値を入れるための初期化
				int preId = 10000; // Maxの期待値を得られるエージェントのIdを入れるための初期化
				for (Agent sender : twostepAgentSet) {
					double eachExpect = setPassedInfoValue(copyList.get(i), copyList.get(i).getLinkedNodeSet().size(),
							sender, currentDistanceAry[copyList.get(i).getId()][sender.getId()]);
					// System.out.println("sender:" + sender.getId() + "score:"
					// + eachExpect);
					if (preMax < eachExpect) {
						preMax = eachExpect;
						preId = sender.getId();
					}
				}

				if (preMax <= 0.0) {
					// System.out.println("期待値が負");
					nextLinkList.add(10000);
					nextPlusList.add(0.0);
				} else {
					nextLinkList.add(preId);
					nextPlusList.add(preMax);
				}
			}

			// 全エージェントに1つずつリンクを追加
			for (int i = 0; i < copyList.size(); i++) {
				if (nextLinkList.get(i) < copyList.size()) {

					Agent receiver = copyList.get(i);
					int currentK = receiver.getLinkedNodeSet().size();
					Agent candidate = copyList.get(nextLinkList.get(i));
					double nextExpect = setPassedInfoValue(receiver, (currentK + 1), candidate, 1);// リンクを増やした場合のそのノードから得られる期待値

					if ((nextExpect - linkedCost) > nextPlusList.get(i)) {// 前回を上回る期待値をそのノードから得られれば新規リンク作成
						receiver.addLink(candidate);
					}
				}
			}

			// エージェントリストをコピーリストに置き換え
			agentList = makeCopyList(copyList);

			int[][] countedDistanceAry = calcCountedDistanceAry(agentNum, agentList);
			double trialExpectation = 0.0;
			// ネットワーク全体の得点算出
			for (int q = 0; q < agentList.size(); q++) {
				trialExpectation += calcMyExpect(agentList.get(q), agentList, countedDistanceAry);
			}
			// System.out.println("score：" + trialExpectation);

			SimpleNetwork<Agent, Link> network = new SimpleNetwork<Agent, Link>(agentList);
			double cv = network.getClusterValue();
			double path = network.getAveragePathLength();

			double ass = network.getAssortativity();
			double pow = network.getDegreeDistributionMap().getPowerIndex();
			double link = network.getDegreeDistributionMap().getAverageDegree();
			// System.out.printf("%.3f\t%.3f\t%.3f\t%.3f\t%.3f\t\n", cv, path,
			// ass, pow, link);

			for (int i = 0; i < agentList.size(); i++) {
				if (nextLinkList.get(i) >= agentList.size()) {
					// ここから新規リンク[ここは要る]
					try {
						Writer writer = new FileWriter("140228_newLink.txt", true);
						BufferedWriter bw = new BufferedWriter(writer);

						bw.write(agentList.get(i).getId() + "\tN/O\t" + agentList.get(i).getLinkedNodeSet().size()
								+ "\n");

						bw.close();
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					// ここから新規リンク[ここは要る]
					try {
						Writer writer = new FileWriter("140228_newLink.txt", true);
						BufferedWriter bw = new BufferedWriter(writer);

						bw.write(agentList.get(i).getId() + "\t" + agentList.get(nextLinkList.get(i)).getId() + "\t"
								+ agentList.get(i).getLinkedNodeSet().size() + "\n");

						bw.close();
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				double testMyExpect = calcMyExpectNoWrite(agentList.get(i), agentList, countedDistanceAry);
				// ここまで新規リンク
				try {
					Writer writer = new FileWriter("140228_test.txt", true);
					BufferedWriter bw = new BufferedWriter(writer);

					bw.write(lastExpectAry[i] + "\t" + testMyExpect + "\n");

					bw.close();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// ここから新規リンク[ここは要る]
			try {
				Writer writer = new FileWriter("140228_newLink.txt", true);
				BufferedWriter bw = new BufferedWriter(writer);
				bw.newLine();
				bw.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// ここまで新規リンク

			// ここから期待値[ここは要る]
			try {
				Writer writer = new FileWriter("140228_nwExpectation.txt", true);
				BufferedWriter bw = new BufferedWriter(writer);

				bw.write(trialExpectation + "\t" + cv + "\t" + path + "\t" + ass + "\t" + pow + "\t" + link + "\n");

				bw.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// ここまで期待値
			if (p == (maxLinkNum - 1)) {
				// ここから新規リンク[ここは要る]
				try {
					Writer writer = new FileWriter("140228_newLink.txt", true);
					BufferedWriter bw = new BufferedWriter(writer);
					bw.write("finish\n");
					bw.close();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				// ここまで新規リンク
			}

		}
		try {
			Writer writer = new FileWriter("140228_Expectation.txt", true);
			BufferedWriter bw = new BufferedWriter(writer);

			bw.newLine();

			bw.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void chooseNewLinkSimple(int agentNum, List<Agent> agentList, double[] expectationAry, int maxLinkNum) {

		List<Agent> copyList = makeCopyList(agentList);

		for (int p = 0; p < maxLinkNum; p++) {

			int[][] preCountedDistanceAry = calcCountedDistanceAry(agentNum, copyList);
			double[] lastExpectAry = new double[agentNum];

			List<Integer> nextLinkList = new ArrayList<>(); // get(i)で、idにiを持つエージェントが次にリンクを作成すべきエージェントのidを得られる
			// 各エージェントが次にリンクを作成すべきエージェントを決定する
			for (int i = 0; i < copyList.size(); i++) {
				lastExpectAry[i] = calcMyExpectNoWrite(copyList.get(i), copyList, preCountedDistanceAry);

				Set<Agent> twostepAgentSet = makeTwostepAgentSet(copyList.get(i));
				// 2ステップ先のノードのうち、正で最も期待値の高いものを求める
				int[][] currentDistanceAry = calcCountedDistanceAry(agentNum, copyList); // ノード同士の距離を入れる行列
				double preMax = -100.0; // Maxの期待値を入れるための初期化
				int preId = 10000; // Maxの期待値を得られるエージェントのIdを入れるための初期化
				for (Agent sender : twostepAgentSet) {
					double eachExpect = setPassedInfoValue(copyList.get(i), copyList.get(i).getLinkedNodeSet().size(),
							sender, currentDistanceAry[copyList.get(i).getId()][sender.getId()]);
					// System.out.println("sender:" + sender.getId() + "score:"
					// + eachExpect);
					if (preMax < eachExpect) {
						preMax = eachExpect;
						preId = sender.getId();
					}
				}

				if (preMax <= 0.0) {
					// System.out.println("期待値が負");
					nextLinkList.add(10000);
				} else {
					nextLinkList.add(preId);
				}
			}

			// 全エージェントに1つずつリンクを追加
			for (int i = 0; i < copyList.size(); i++) {
				if (nextLinkList.get(i) < copyList.size()) {

					Agent receiver = copyList.get(i);
					int currentK = receiver.getLinkedNodeSet().size();
					Agent candidate = copyList.get(nextLinkList.get(i));
					double nextExpect = setPassedInfoValue(receiver, (currentK + 1), candidate, 1);// リンクを増やした場合のそのノードから得られる期待値

					if (nextExpect > linkedCost) {// コストを上回る期待値をそのノードから得られれば新規リンク作成
						// System.out.println("before linknum:receiver:"+receiver.getLinkedNodeSet().size()+"sender"+candidate.getLinkedNodeSet().size());
						receiver.addLink(candidate);
						// System.out.println("after linknum:receiver:"+receiver.getLinkedNodeSet().size()+"sender"+candidate.getLinkedNodeSet().size());
						// System.out.println("receiver isLinkTo sender:"+receiver.isLinkTo(candidate)+"sender isLinkTo receiver:"+candidate.isLinkTo(receiver));
					}
				}
			}

			// エージェントリストをコピーリストに置き換え
			agentList = makeCopyList(copyList);

			int[][] countedDistanceAry = calcCountedDistanceAry(agentNum, agentList);
			double trialExpectation = 0.0;
			// ネットワーク全体の得点算出
			for (int q = 0; q < agentList.size(); q++) {
				trialExpectation += calcMyExpect(agentList.get(q), agentList, countedDistanceAry);
			}
			// System.out.println("score：" + trialExpectation);

			SimpleNetwork<Agent, Link> network = new SimpleNetwork<Agent, Link>(agentList);
			double cv = network.getClusterValue();
			double path = network.getAveragePathLength();

			double ass = network.getAssortativity();
			double pow = network.getDegreeDistributionMap().getPowerIndex();
			double link = network.getDegreeDistributionMap().getAverageDegree();
			// System.out.printf("%.3f\t%.3f\t%.3f\t%.3f\t%.3f\t\n", cv, path,
			// ass, pow, link);
			for (int i = 0; i < agentList.size(); i++) {
				if (nextLinkList.get(i) > agentList.size()) {
					// ここから新規リンク[ここは要る]
					// try {
					// Writer writer = new FileWriter("140228_newLink.txt",
					// true);
					// BufferedWriter bw = new BufferedWriter(writer);
					//
					// bw.write(agentList.get(i).getId() + "\tN/O\t" +
					// agentList.get(i).getLinkedNodeSet().size()
					// + "\n");
					//
					// bw.close();
					// writer.close();
					// } catch (IOException e) {
					// e.printStackTrace();
					// }
				} else {
					// ここから新規リンク[ここは要る]
					// try {
					// Writer writer = new FileWriter("140228_newLink.txt",
					// true);
					// BufferedWriter bw = new BufferedWriter(writer);
					//
					// bw.write(agentList.get(i).getId() + "\t" +
					// agentList.get(nextLinkList.get(i)).getId() + "\t"
					// + agentList.get(i).getLinkedNodeSet().size() + "\n");
					//
					// bw.close();
					// writer.close();
					// } catch (IOException e) {
					// e.printStackTrace();
					// }
				}
				double testMyExpect = calcMyExpectNoWrite(agentList.get(i), agentList, countedDistanceAry);
				// ここまで新規リンク
				// try {
				// Writer writer = new FileWriter("140228_test.txt", true);
				// BufferedWriter bw = new BufferedWriter(writer);
				//
				// bw.write(lastExpectAry[i] + "\t" + testMyExpect + "\n");
				//
				// bw.close();
				// writer.close();
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				System.out.println("linkpair_final");
				for (Node linked : agentList.get(i).getLinkedNodeSet()) {
					Agent linkedA = (Agent) linked;
					try {
						Writer writer = new FileWriter("140228_linkpair.txt", true);
						BufferedWriter bw = new BufferedWriter(writer);

						bw.write(agentList.get(i).getId() + "\t" + linkedA.getId() + "\n");

						bw.close();
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			try {
				Writer writer = new FileWriter("140228_linkpair.txt", true);
				BufferedWriter bw = new BufferedWriter(writer);

				bw.newLine();

				bw.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// ここから期待値[ここは要る]
			try {
				Writer writer = new FileWriter("140228_nwExpectation.txt", true);
				BufferedWriter bw = new BufferedWriter(writer);

				bw.write(trialExpectation + "\t" + cv + "\t" + path + "\t" + ass + "\t" + pow + "\t" + link + "\n");

				bw.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// ここまで期待値
			if (p == (maxLinkNum - 1)) {
				// ここから新規リンク[ここは要る]
				// try {
				// Writer writer = new FileWriter("140228_newLink.txt", true);
				// BufferedWriter bw = new BufferedWriter(writer);
				// bw.write("finish\n");
				// bw.close();
				// writer.close();
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				// ここまで新規リンク
			}
		}

	}

	public static void chooseNewLinkRandom(int agentNum, List<Agent> agentList, int maxLinkNum) {

		List<Agent> copyList = makeCopyList(agentList);

		for (int p = 0; p < maxLinkNum; p++) {
			List<Integer> nextLinkList = new ArrayList<>(); // get(i)で、idにiを持つエージェントが次にリンクを作成すべきエージェントのidを得られる

			// 各エージェントが次にリンクを作成すべきエージェントを決定する
			for (int i = 0; i < copyList.size(); i++) {
				Set<Agent> twostepAgentSet = makeTwostepAgentSet(copyList.get(i));

				// 2ステップ先のノードからランダムに1つ選ぶ
				int preId = 10000;
				if (twostepAgentSet.size() > 0) {
					Random rand = new Random();
					int randomAgentNum = rand.nextInt(twostepAgentSet.size());
					int counter = 0;
					for (Agent randomA : twostepAgentSet) {
						preId = randomA.getId();
						while (counter < randomAgentNum) {
							counter++;
							break;
						}
					}
				}
				nextLinkList.add(preId);
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
			double[] newExpectationAry = new double[copyList.size()];
			double trialExpectation = 0.0;
			// ネットワーク全体の得点算出
			for (int q = 0; q < copyList.size(); q++) {
				trialExpectation += calcMyExpectNoWrite(copyList.get(q), copyList, countedDistanceAry);
			}
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
				Writer writer = new FileWriter("140228_nwExpectation_random.txt", true);
				BufferedWriter bw = new BufferedWriter(writer);

				bw.write(trialExpectation + "\t" + cv + "\t" + path + "\t" + ass + "\t" + pow + "\t" + link + "\n");

				bw.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// ここまで期待値
		}
		try {
			Writer writer = new FileWriter("140228_Expectation_random.txt", true);
			BufferedWriter bw = new BufferedWriter(writer);

			bw.newLine();

			bw.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
