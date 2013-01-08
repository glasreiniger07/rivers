package com.skaringa.riversystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Waterways {

	private Map<Long, String> id2Basin = new HashMap<Long, String>();
	
	private Map<Integer, Long> index2Id = new HashMap<Integer, Long>();
	
	private Map<Long, Integer> id2Index = new HashMap<Long, Integer>();
	
	private long[][] nodes;
	
	public static Waterways loadFromJson(File jsonFile) throws JSONException, IOException {
		Waterways waterways = new Waterways();
		
		FileInputStream in = new FileInputStream(jsonFile);
		try {
			waterways.load(in);
		} finally {
			in.close();
		}
		
		return waterways;
	}
	
	public Map<Long, String> getId2Basin() {
		return id2Basin;
	}
	
	public void explore() {
		List<Long> testIds = new LinkedList<Long>(id2Basin.keySet());
		do {
			List<Long> newIds = new LinkedList<Long>();
			for (Long id : testIds) {
				newIds.addAll(exploreNodes(id));
			}
			testIds = newIds;
			System.out.printf("Found %d new ways%n", testIds.size());
		} while (! testIds.isEmpty());
	}
	
	private List<Long> exploreNodes(Long wayid) {
		List<Long> newIds = new LinkedList<Long>();
		int refWayIndex = id2Index.get(wayid);
		String basin = id2Basin.get(wayid);
		long[] refway = nodes[refWayIndex];
		for (long refNodeId : refway) {
			for (int i = 0; i < nodes.length; ++i) {
				Long id = index2Id.get(i);
				if (id2Basin.containsKey(id)) {
					// already assigned
					continue;
				}
				if (Arrays.binarySearch(nodes[i], refNodeId) >= 0) {
					// found
					id2Basin.put(id, basin);
					newIds.add(id);
					continue;
				}
			}
		}
		return newIds;
	}

	private void load(InputStream in) throws JSONException {
		JSONTokener tokener = new JSONTokener(in);
		JSONArray wwayArray = new JSONArray(tokener);
		int wwayCount = wwayArray.length();
		nodes = new long[wwayCount][];
		for (int i = 0; i < wwayCount; ++i) {
			JSONObject wway = wwayArray.getJSONObject(i);
			long id = wway.getLong("id");
			index2Id.put(i, id);
			id2Index.put(id, i);
			nodes[i] = toSortedNodeList(wway.getJSONObject("nodes"));
			String basin = WellknownRivers.getBasin(id);
			if (basin != null) {
				id2Basin.put(id, basin);
			}
		}
	}

	private long[] toSortedNodeList(JSONObject nodes) throws JSONException {
		int nodeCount = nodes.getInt("length");
		long[] nodeArray = new long[nodeCount];
		for (int i = 0; i < nodeCount; ++i) {
			nodeArray[i] = nodes.getLong(String.valueOf(i));
		}
		Arrays.sort(nodeArray);
		return nodeArray;
	}

}