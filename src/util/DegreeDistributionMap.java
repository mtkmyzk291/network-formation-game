package util;import java.util.HashMap;import java.util.Map;import java.util.Set;import java.util.SortedMap;import java.util.TreeMap;import basic.preparation.*;/** * æ¬¡æ°å?¸?è¡¨ãã¯ã©ã¹<br> *  * @author ishida */public class DegreeDistributionMap {	/**	 * ãã?æ¬¡æ°å?¸??å?¨ãªãNetwork	 */	protected final Network world;	/**	 * æ¬¡æ°å?¸?è¡¨ãã?ã??<æ¬¡æ°ã??åº¦>	 */	protected final SortedMap<Integer, Double> degreeMap = new TreeMap<Integer, Double>();	/**	 * æ¬¡æ°å?¸??xyè»¸ã®å¤ã®å¯¾æ°ãã¨ã£ãã?ã??<log(æ¬¡æ°)ãlog(é »åº¦)>	 */	protected final Map<Double, Double> logarithmMap = new HashMap<Double, Double>();	protected DegreeDistributionMap(){		world = null;	}		/**	 * ã³ã³ã¹ãã©ã¯ã¿	 * 	 * @param world	 */	public DegreeDistributionMap(Network world) {		this.world = world;		makeDegreeMap();		makeLogarithmMap();	}	/**	 * æ¬¡æ°å?¸??ä¸¡è»¸ãå¯¾æ°ã«å¤æããããããä½æ?	 */	protected void makeLogarithmMap() {		for (Integer num : degreeMap.keySet()) {			logarithmMap.put(Math.log10(num.doubleValue()), Math.log10(degreeMap.get(num)));		}	}	/**	 * NetworkWorldãå?ã«degreeMapãä½æ?	 */	private void makeDegreeMap() {		int i = 0;		int size = world.size();		int[] degree = new int[size];		for (Node node : world.getNodeSet()) {			degree[i] = node.getLinkSet().size();			i++;		}		for (i = 0; i < size; i++) {			int key = degree[i];			if (!degreeMap.containsKey(key)) {				degreeMap.put(key, 1.0);			} else {				double value = degreeMap.get(key);				degreeMap.put(key, value + 1);			}		}		for (Integer key : degreeMap.keySet()) {			degreeMap.put(key, degreeMap.get(key) / size);		}	}	/**	 * å¼æ°ã§æ¸¡ãããæ°ã®æ¬¡æ°ãæã¤ã¨ã¼ã¸ã§ã³ããã?ãã©ã?ãè¿ã(å§è­²ã¡ã½ã?)	 * 	 * @param key	 * @return	 */	public boolean containsKey(Object key) {		return degreeMap.containsKey(key);	}	/**	 * keyã«å¯¾å¿ããè¦ç´?è¿ã(å§è­²ã¡ã½ã?)	 * 	 * @param key	 * @return	 */	public double get(Object key) {		try{			return degreeMap.get(key);		}catch(NullPointerException e){			return 0;		}	}	/**	 * æ¬¡æ°å?¸?æ¨æºå?åã«åºåãã¾ãã?	 */	@Override	public String toString() {		StringBuilder sb = new StringBuilder();		for (int i = 0; i <= getMaxDegree(); i++) {			if (degreeMap.containsKey(i)) {				sb.append(i + "," + degreeMap.get(i) + "\n");			}		}		return sb.toString();	}	/**	 * æ¬¡æ°å?¸?«ãããæå¤§æ¬¡æ°ãè¿ãã¾ãã?	 * 	 * @return æ?¤§æ¬¡æ°	 */	public int getMaxDegree() {		if(degreeMap.isEmpty()){			return 0;		}		return degreeMap.lastKey();	}	/**	 * å¹³å?¬¡æ°ãè¿ãã¾ãã?	 * 	 * @return å¹³å?¬¡æ°	 */	public double getAverageDegree() {		int degreeSum = 0;		for (Node node : world.getNodeSet()) {			degreeSum += node.getLinkSet().size();		}		return (double) degreeSum / world.size();	}	/**	 * æ¬¡æ°å?¸??æ¨æºåå·®ãè¿ãã¾ãã?	 * 	 * @return æ¨æºåå·®	 */	public double getStandardDeviation() {		double averageDegree = getAverageDegree();		double variance = 0;		for (Node node : world.getNodeSet()) {			variance += Math.pow((averageDegree - node.getLinkSet().size()), 2);		}		variance /= world.size();		return Math.sqrt(variance);	}	/**	 * å¨æ¬¡æ°ã®åºéã«ãããæ¬¡æ°å?¸??åªæ?°ãè¿ãã?	 * 	 * @return åªæ?°	 */	public double getPowerIndex() {		return getPowerIndex(1, getMaxDegree());	}	/**	 * æ¬¡æ°ã?fromâ¦kâ¦to)ã®åºéã«ãããæ¬¡æ°å?¸??åªæ?°ãè¿ãã?	 * 	 * @param from	 * @param to	 * @return åªæ?°	 */	public double getPowerIndex(int from, int to) {		int elementNum = 0;		double sigma_x = 0, sigma_y = 0;		double sigma_xx = 0, sigma_xy = 0;		/* æ?°ï¼ä¹æ³ãå©ç¨ãã?æ¬¡æ°å?¸??ä¸¡å¯¾æ°è¡¨ç¤ºã«ãããå¾ããè¨ç®ãã¦è¿ãã?*/		for (Double logNum : logarithmMap.keySet()) {			if (logNum >= Math.log10(from) && logNum <= Math.log10(to)) {				elementNum++;				sigma_x += logNum;				sigma_y += logarithmMap.get(logNum);				sigma_xy += logNum * logarithmMap.get(logNum);				sigma_xx += logNum * logNum;			}		}		if(elementNum * sigma_xx - sigma_x * sigma_x == 0){			return 0;		}		return (elementNum * sigma_xy - sigma_x * sigma_y) / (elementNum * sigma_xx - sigma_x * sigma_x);	}	/**	 * å¨æ¬¡æ°ã®åºéã«ãããè¿ä¼¼ç´ç·ã?å?ãè¿ãã?	 * 	 * @return åªæ?°	 */	public double getIntercept() {		return getIntercept(1, getMaxDegree());	}	/**	 * æ¬¡æ°ã?fromâ¦kâ¦to)ã®åºéã«ãããè¿ä¼¼ç´ç·ã?å?ãè¿ãã?	 * 	 * @param from	 * @param to	 * @return è¿ä¼¼ç´ç·ã?å?	 */	public double getIntercept(int from, int to) {		int elementNum = 0;		double sigma_x = 0, sigma_y = 0;		double sigma_xx = 0, sigma_xy = 0;		/* æ?°ï¼ä¹æ³ãå©ç¨ãã?æ¬¡æ°å?¸??ä¸¡å¯¾æ°è¡¨ç¤ºã«ãããå¾ããè¨ç®ãã¦è¿ãã?*/		for (Double logNum : logarithmMap.keySet()) {			if (logNum >= Math.log10(from) && logNum <= Math.log10(to)) {				elementNum++;				sigma_x += logNum;				sigma_y += logarithmMap.get(logNum);				sigma_xy += logNum * logarithmMap.get(logNum);				sigma_xx += logNum * logNum;			}		}		double logScaleIntercept = (sigma_y-getPowerIndex(from, to)*sigma_x)/elementNum;		//double logScaleIntercept = (sigma_xx * sigma_y - sigma_x * sigma_xy) / (elementNum * sigma_xx - sigma_x * sigma_x);		//return Math.pow(10, logScaleIntercept);		return logScaleIntercept;	}	/**	 * ãã?Mapã®ã­ã¼ã®Setãè¿ã	 * @return	 */	public Set<Integer> keySet() {		return degreeMap.keySet();	}	/**	 * æ±ºå®ä¿æ°ãè¿ã	 * @return	 */	public double getR2() {				double gamma = getPowerIndex();		double intercept = getIntercept();		double average = 0;		for(double key:logarithmMap.keySet()){			if(key == 0){				continue;			}			average +=logarithmMap.get(key);		}		average /= logarithmMap.size();				double numerator = 0;		double denominator = 0;		for(double key:logarithmMap.keySet()){			if(/*key == 0 || */Double.isInfinite(key)){				continue;			}			double f = intercept+key*gamma;			double y = logarithmMap.get(key);			numerator += Math.pow(y-f, 2);			denominator += Math.pow(y-average, 2);						//System.out.printf("%f\t%f\t%f\t(%f,%f)\n", key, f, y, gamma,intercept);		}		//System.out.printf("%.3f\t%.3f\n", gamma, intercept);				if(denominator == 0){			return 0;		}				return 1.0-(numerator/denominator);	}}