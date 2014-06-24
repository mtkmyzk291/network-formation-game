package trial.preparation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import basic.preparation.NetworkGenerator;
import basic.preparation.Node;

public class LinkOptimizer {

	static double sigma = 0.5; // 減衰率
	static double linkedCost = 0.1; // 直接リンクするコスト

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		LinkOptimizer lo = new LinkOptimizer();
		List<Agent> agentList = new ArrayList<>();

		int allLink = 50;

		for (int i = 0; i < 100; i++) { // iの上限でエージェント数変更
			agentList.add(new Agent());
		}

		for (Agent agent : agentList) {
			lo.setInfoValue(agent.getInfoValue()); // 各エージェントの持つ情報の価値を設定
		}

		// System.out.println("created list");

		NetworkGenerator generator = new NetworkGenerator();
		generator.createTwoWaySmallWorldConstLinkNum(agentList, allLink, 0.05,
				0.05, 0.05); // 全リンク数、張り替え率、リンクのランダムさ、リンクの単方向性

		// System.out.println("created nw");

		int agentNum = agentList.size();

		System.out.println("初期NWの得点：" + calculate(agentNum, agentList));
		double firstExpectation = calculate(agentNum, agentList);

		try {
			Writer writer = new FileWriter("test.txt", true);
			BufferedWriter bw = new BufferedWriter(writer);

			bw.write("allLink=" + allLink+"\n");
			bw.write("newLinkNum\tNWException\n");
			bw.write("0\t" + firstExpectation);
			bw.newLine();

			bw.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// for (int i = 0; i < agentNum; i++) {
		// for (Node test : agentList.get(i).getLinkedNodeSet()) {
		// Agent testAgent = (Agent) test;
		// System.out.println(testAgent.getId());
		// }
		// System.out.println();
		// }
		chooseNewLink(agentNum, agentList);
	}

	private void setInfoValue(double infoValue) {
		Random rand = new Random();
		infoValue = rand.nextInt();
	}

	public static double calculate(int agentNum, List<Agent> agentList) {
		double networkExpectation = 0.0;
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

		// System.out.println("created ary");

		// フロイドのアルゴリズムによる最短経路長算出
		int[][] countedDistanceAry = new int[agentNum][agentNum];
		countedDistanceAry = floydAlgo(agentList, distanceArray);
		// for (Agent agent : agentList) {
		// for (Agent target : agentList) {
		// System.out.println("(" + agent.getId() + "," + target.getId()
		// + ")="
		// + countedDistanceAry[agent.getId()][target.getId()]);
		// }
		// }

		// System.out.println("counted distance");

		// ネットワーク全体の得点算出
		networkExpectation = calculateExpectation(agentList, countedDistanceAry);
		// System.out.println(networkExpectation);
		return networkExpectation;
	}

	public static int[][] floydAlgo(List<Agent> agentList, int[][] distanceArray) {
		int agentNum = agentList.size();
		for (int k = 0; k < agentNum; k++) {
			for (int i = 0; i < agentNum; i++) {
				for (int j = 0; j < agentNum; j++) {
					// k回目のステップにおける、ノードiからノードkまでの距離（暫定）
					int distance_i_k_j = distanceArray[i][k]
							+ distanceArray[k][j];

					if (distance_i_k_j < distanceArray[i][j]) {
						distanceArray[i][j] = distance_i_k_j;
					}
				}
			}
		}
		return distanceArray;
	}

	public static double calculateExpectation(List<Agent> agentList,
			int[][] countedDistanceAry) {
		int agentNum = agentList.size();
		double nwScore = 0.0;
		double[][] scoreAry = new double[agentNum][agentNum];
		double[] expectation = new double[agentNum];
		for (Agent agent : agentList) {
			for (Agent target : agentList) {
				if (countedDistanceAry[agent.getId()][target.getId()] == 0) {
					continue;
				} else if (countedDistanceAry[agent.getId()][target.getId()] == 1) {
					scoreAry[agent.getId()][target.getId()] += target
							.getInfoValue() * (sigma - linkedCost);
				} else {
					scoreAry[agent.getId()][target.getId()] += (Math.pow(sigma,
							countedDistanceAry[agent.getId()][target.getId()]));
				}
				expectation[agent.getId()] += scoreAry[agent.getId()][target
						.getId()];
			}
		}

		for (Agent agent : agentList) {
			nwScore += expectation[agent.getId()];
		}
		return nwScore;
	}

	public static void chooseNewLink(int agentNum, List<Agent> agentList) {
		double trialExpectation = 0.0;

		List<Agent> copyList = new ArrayList<>();
		for (Agent copy : agentList) {
			copyList.add(copy);
		}

		for (Agent copyAgent : copyList) {
			HashSet<String> combiSet = new HashSet<>();
			ArrayList<Node> twostepNodeNumList = new ArrayList<>();

			List<Node> linkedNodeList = new ArrayList<>();
			for (Node node : copyAgent.getLinkedNodeSet()) {
				Agent neighbor = (Agent) node;
				linkedNodeList.add(neighbor);
			}

			for (Node node : linkedNodeList) {

				Agent neighbor = (Agent) node;
				Set<? extends Node> targetNodeSet = neighbor.getLinkedNodeSet();
				Set<Node> twostepNodeSet = new HashSet<>();
				for (Node nextNode : targetNodeSet) {
					Agent target = (Agent) nextNode;
					twostepNodeSet.add(target);
				}

				for (Node nod : twostepNodeSet) {
					twostepNodeNumList.add(nod);
				}
				// for (int i = 0; i < twostepNodeNumAry.length; i++) {
				// System.out.println("新規リンク候補："
				// + ((Agent) twostepNodeNumAry[i]).getId());
				// }
			}

			 System.out.println("候補数：" + twostepNodeNumList.size());
			combiSet = calculateCombi(twostepNodeNumList.size());
			// System.out.println("総組み合わせ数：" + combiSet.size());

			List<Agent> dammyList = new ArrayList<>();
			for (Agent dammy : agentList) {
				dammyList.add(dammy);
			}

			for (String str : combiSet) {
				for (Agent dammyAgent : dammyList) {
					List<Integer> newLinkCombiList = new ArrayList<Integer>();
					int k = (str.length() + 1) / 5;
					// System.out.println("k=" + k);
					for (int i = 0; i < k; i++) {
						String substr = str.substring(5 * i + 1, 5 * i + 4);
						int subint = Integer.parseInt(substr);
						newLinkCombiList.add(subint);
					}

					// for (Integer num : newLinkCombiList) {
					// System.out.print(num + "\t");
					// }
					// System.out.println();

					int counter = 0;
					for (int i = 0; i < newLinkCombiList.size(); i++) {
						Agent candidate = (Agent) twostepNodeNumList.get(i);
						if (!dammyAgent.isLinkTo(candidate)) {
							counter++;
						}
					}
					if (counter == newLinkCombiList.size()) {
						for (int i = 0; i < newLinkCombiList.size(); i++) {
							Agent newNode = (Agent) twostepNodeNumList.get(i);
							// System.out.print("次の候補に新規リンク：" +
							// candidate.getId());
							dammyAgent.addLink(newNode);
						}
						System.out.println("リンク張り替え後NWの得点："
								+ calculate(agentNum, dammyList));
						trialExpectation = calculate(agentNum, dammyList);

						try {
							Writer writer = new FileWriter("test.txt", true);
							BufferedWriter bw = new BufferedWriter(writer);

							bw.write(twostepNodeNumList.size() + "\t"
									+ trialExpectation);
							bw.newLine();

							bw.close();
							writer.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					// for (int i = 0; i < agentNum; i++) {
					// for (Node test : agentList.get(i).getLinkedNodeSet()) {
					// Agent testAgent = (Agent) test;
					// System.out.println(testAgent.getId());
					// }
					// System.out.println();
					// }
				}
			}
		}
	}

	static int fact(int x) {
		int f = 1;

		for (int i = 1; i <= x; i++) {
			f = f * i;
		}
		if (f == 0) {
			f = 1;
		}
		return f;
	}

	public static HashSet<String> calculateCombi(int nodeNum) {
		
		int combiNum = 0;
		if (nodeNum != 0) {
			for (int i = 1; i <= nodeNum; i++) {
				int combination = fact(nodeNum) / (fact(i) * fact(nodeNum - i));
				combiNum += combination;
			}
		}
		// System.out.println("combinum=" + combiNum);

		HashSet<String> combiSet = new HashSet<String>();
		HashSet<String> pairSet = new HashSet<String>();

		for (int j = 0; j < nodeNum; j++) {
			String first = Integer.toString(j + 100000);
			first = first.substring(first.length() - 4);
			StringBuffer buf1 = new StringBuffer();
			buf1.append(first);
			String s1 = buf1.toString();
			combiSet.add(s1);
			for (int i = 0; i < nodeNum; i++) {
				if (i > j) {
					String second = Integer.toString(i + 100000);
					second = second.substring(second.length() - 4);
					StringBuffer buf2 = new StringBuffer();
					buf2.append(first + "_" + second);
					String s2 = buf2.toString();
					combiSet.add(s2);
					pairSet.add(s2);
				}
			}
		}
		// System.out.println(combiSet.size());
		// System.out.println(pairSet.size());

		HashSet<String> finalCombiSet = new HashSet<String>();
		finalCombiSet.addAll(combiSet);

		while (finalCombiSet.size() < combiNum) {
			// System.out.println("combiset=" + combiSet.size());
			for (String combi : combiSet) {
				String secondLast = combi.substring(combi.length() - 4);
				for (String nextCombi : pairSet) {
					String lastFirst = nextCombi.substring(0, 4);
					if (lastFirst.matches(secondLast)) {
						StringBuffer buf = new StringBuffer();
						String last = nextCombi
								.substring(nextCombi.length() - 4);
						buf.append(combi + "_" + last);
						String s = buf.toString();
						if (finalCombiSet.contains(s)) {
							continue;
						} else {
							finalCombiSet.add(s);
							// System.out.println("final=" +
							// finalCombiSet.size());
							// System.out.println("combiset=" +
							// combiSet.size());
						}
					}
				}
			}
			// System.out.println("finish");
			combiSet.removeAll(combiSet);
			combiSet.addAll(finalCombiSet);
		}

		// for (String combi : finalCombiSet) {
		// System.out.println(combi);
		// }
		return finalCombiSet;
	}

}
