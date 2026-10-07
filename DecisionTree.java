import java.io.*;
import java.util.*;

public class DecisionTree {

    // Represents one row from the CSV file
    static class DataRow {
        String[] values;

        DataRow(String[] values) {
            this.values = values;
        }
    }

    // Represents a node in the decision tree
    static class TreeNode {
        String feature;
        String prediction;

        Map<String, TreeNode> children;

        TreeNode() {
            children = new LinkedHashMap<>();
        }
        
        //Leaf constructor
        TreeNode(String prediction){
        	this();
        	this.prediction = prediction;
        }

        boolean isLeaf() {
            return prediction != null;
        }
    }

 
    // Read CSV
    public static List<DataRow> readCSV(String filename) throws IOException {

        List<DataRow> data = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(filename));

        String line;

        // Read header
        String headerLine = br.readLine();

        while ((line = br.readLine()) != null) {

            if (line.trim().isEmpty()) {
                continue;
            }

            String[] values = line.split(",");

            data.add(new DataRow(values));
        }

        br.close();

        return data;
    }

    // Calculate entropy
    public static double entropy(List<DataRow> data, int targetIndex) {

        // TODO:
        // 1. Count the occurrences of each target value
        // 2. Calculate:
    	
    	
        //
        // entropy = - SUM(p * log2(p))
        //
    	if(data.isEmpty()) {
    		return 0.0;
    	}
    	
    	//Getting the number of occurrences of each target value
    	Map<String, Integer> counts = new HashMap<>();
    	
    	for(DataRow row : data) {
    		String target = row.values[targetIndex];
    		counts.put(target, counts.getOrDefault(target, 0) + 1);
    	}
    	
    	double entropy = 0.0;
    	
    	for(int  i : counts.values()) {
    		
    		double p =  (double) i / data.size();
    		
    		entropy -= p * (Math.log(p) / Math.log(2));
    	}
    	
        // Return the entropy.

        return entropy;
    }

    // Calculate information gain
    public static double informationGain(
            List<DataRow> data,
            int featureIndex,
            int targetIndex) {

        // TODO:
        //
        // Information Gain =
        // Entropy(parent)
        // -
    	// weighted entropy(children)
        //
        // Return the information gain
    	
    	if(data.isEmpty()) {
    		return 0.0;
    	}
    	
    	double entropy = entropy(data, targetIndex);
    	
    	//Split the rows by feature value
    	Map<String, List<DataRow>> groups = new HashMap<>();
    	
    	List<DataRow> zeros = new ArrayList<>();
    	List<DataRow>ones = new ArrayList<>();
    	
    	//Adding to the lists of the 0 and 1 lists
    	for(DataRow row : data) {
    		groups.computeIfAbsent(row.values[featureIndex], k -> new ArrayList<>()).add(row);
    	}
    	
    	//Gain
    	double weightedentropy = 0.0;
    	
    	for(List<DataRow>sub : groups.values()) {
    		double weight = (double) sub.size() / data.size();
    		weightedentropy += weight * entropy(sub, targetIndex);
    	}

        return entropy + weightedentropy;
    }

    // Find the majority class
    public static String majorityClass(
            List<DataRow> data,
            int targetIndex) {

        // TODO:
        // Return the most common target value.
    	//Count each target value
    	Map<String, Integer> counts = new HashMap<>();
    	
    	for(DataRow row : data) {
    		String target = row.values[targetIndex];
    		counts.put(target, counts.getOrDefault(target, 0) + 1);
    	}
    	
    	String best = null;
    	int bestCount = -1;
    	
    	for(Map.Entry<String,Integer> entry : counts.entrySet()) {
    		if(entry.getValue() > bestCount) {
    			bestCount = entry.getValue();
    			best= entry.getKey();
    		}
    	}

        return best;
    }

    // Check whether all rows have the same target
    public static boolean allSameClass(
            List<DataRow> data,
            int targetIndex) {

        // TODO:
        // Return true if all rows have the same target value.
    	if(data.isEmpty()) {
    		return true;
    	}
    	
    	String first = data.get(0).values[targetIndex];
    	
    	for(DataRow row :  data) {
    		if(!row.values[targetIndex].equals(first)) {
    			return false;
    		}
    	}

        return true;
    }

    // Build decision tree
    public static TreeNode buildTree(
            List<DataRow> data,
            List<Integer> availableFeatures,
            String[] header,
            int targetIndex) {

        // TODO:
        //
        // 1. If all examples have the same class:
        //       create a leaf
    	if(allSameClass(data,targetIndex)) {
    		return new TreeNode(data.get(0).values[targetIndex]);
    	}
    	
        //
        // 2. If there are no features left:
        //       return majority class
        //
    	if(availableFeatures.isEmpty()) {
    		return new TreeNode(majorityClass(data, targetIndex) );
    	}
        // 3. Find feature with highest information gain
        //
    	int bestFeature = -1;
    	double bestGain = -1;
    	
    	for(int feature : availableFeatures) {
    		double gain = informationGain(data, feature, targetIndex);
    		
    		if(gain > bestGain) {
    			bestFeature = feature;
    			bestGain = gain;
    		}
    	}
        // 4. Create a decision node
        //
    	
    	TreeNode node = new TreeNode();
    	node.feature = header[bestFeature];
        // 5. Split data according to selected feature
        //
    	
    	Map<String, List<DataRow>> groups = new LinkedHashMap<>();
    	for(DataRow row : data) {
    		groups.computeIfAbsent(row.values[bestFeature], k->new ArrayList<>()).add(row);
    	}
        // 6. Recursively build child trees
        //
    	List<Integer> remaining = new ArrayList<>(availableFeatures);
    	remaining.remove(Integer.valueOf(bestFeature));
    	
    	for(Map.Entry<String, List<DataRow>> entry :groups.entrySet()) {
    		node.children.put(entry.getKey(), buildTree(entry.getValue(),remaining, header, targetIndex));
    	}
        // 7. Return root node

        return node;
    }

    // Convert tree to JSON
    public static String toJSON(TreeNode node) {

        // TODO:
        // Convert the tree recursively into JSON.
    	return toJSON(node,"");

        
    }
    
    private static String toJSON(TreeNode node, String indent) {

        if (node.isLeaf()) {
            return "{ \"prediction\": \"" + escape(node.prediction) + "\" }";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append(indent).append("  \"feature\": \"").append(escape(node.feature)).append("\",\n");
        sb.append(indent).append("  \"children\": {\n");

        int i = 0;
        for (Map.Entry<String, TreeNode> entry : node.children.entrySet()) {
            sb.append(indent).append("    \"").append(escape(entry.getKey())).append("\": ");
            sb.append(toJSON(entry.getValue(), indent + "    "));

            if (i < node.children.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
            i++;
        }

        sb.append(indent).append("  }\n");
        sb.append(indent).append("}");
        return sb.toString();
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // Main
    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.out.println("Usage: java DecisionTree <csv-file>");
            return;
        }

        String filename = args[0];

        List<DataRow> data = readCSV(filename);

        // Example header
        // In the final implementation this should be read
        // from the CSV file rather than hard-coded.

        String[] header = {
            "Example",
            "Not Heavy",
            "Smelly",
            "Spotted",
            "Smooth",
            "Edible"
        };

        int targetIndex = header.length - 1;

        List<Integer> features = new ArrayList<>();

        // Do not use target column as a feature
        for (int i = 1; i < targetIndex; i++) {
            features.add(i);
        }

        TreeNode root =
                buildTree(data, features, header, targetIndex);

        System.out.println(toJSON(root));
    }
}