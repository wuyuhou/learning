package test.com;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.tools.utility.api.IOUtil;
import com.tools.utility.api.ReflectUtil;
import com.tools.utility.spi.format.NumberFormatter;

public class Shuangseqiu {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		InputStream in = null;
		try {
			in = new FileInputStream("d:/download/双色球.csv");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String line = null;
			ArrayList<int[]> haomaList = new ArrayList<int[]>();
			LinkedHashMap<String, Integer> haomaMap = new LinkedHashMap<String, Integer>();
			double sum = 0;
			while((line = reader.readLine()) != null) {
				try {
					if (line == null || line.trim().length() == 0) {
						continue;
					}
					String[] eles = line.split(",");
					if (eles.length == 0 || eles.length != 4) {
						System.out.println("WARN:" + line);
						continue;
					}
					ArrayList<Integer> haomaArray = new ArrayList<Integer>();
					if (haomaMap.get(eles[2]) != null) {
						haomaMap.put(eles[2], haomaMap.get(eles[2]) + 1);
					} else {
						haomaMap.put(eles[2], 1);
					}
					for (String haoma : eles[2].split(" ")) {
						haomaArray.add(ReflectUtil.cast(haoma, Integer.class));
					}
					haomaList.add(ReflectUtil.cast(haomaArray, int[].class));
					sum++;
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			System.out.println("===============统计期数====================" + sum);
			//蓝号排名
			HashMap<Integer, Haoma> lanhaoMap = new HashMap<Integer, Haoma>();
			//红号排名
			HashMap<Integer, Haoma> honghaoMap = new HashMap<Integer, Haoma>();
			{
				//蓝号概率统计
				Haoma[] lanhaoArray = new Haoma[17];
				for (int[] haomaArray : haomaList) {
					Haoma haoma = lanhaoArray[haomaArray[6]];
					if (haoma == null) {
						haoma = new Haoma();
						lanhaoArray[haomaArray[6]] = haoma;
					}
					haoma.haoma = haomaArray[6]; 
					haoma.cishu++;
				}
				
				for (Haoma haoma : lanhaoArray) {
					if (haoma == null) {
						continue;
					}
					haoma.gailv = haoma.cishu / sum;
				}
				ReflectUtil.sort(lanhaoArray, "cishu", null, true);
				for (int i = 0; i < lanhaoArray.length; i++) {
					if (lanhaoArray[i] == null) {
						continue;
					}
					lanhaoArray[i].index = i;
					lanhaoMap.put(lanhaoArray[i].haoma, lanhaoArray[i]);
				}	
				System.out.println("===============蓝号概率统计====================");
				for (Haoma haoma : lanhaoArray) {
					System.out.println(haoma);
				}
			}
			
			{
				//红号统计
				Haoma[] honghaoArray = new Haoma[34];
				for (int[] haomaArray : haomaList) {
					for (int i = 0; i < 6; i++) {
						Haoma haoma = honghaoArray[haomaArray[i]];
						if (haoma == null) {
							haoma = new Haoma();
							honghaoArray[haomaArray[i]] = haoma;
						}
						haoma.haoma = haomaArray[i]; 
						haoma.cishu++;
					}					
				}
				
				for (Haoma haoma : honghaoArray) {
					if (haoma == null) {
						continue;
					}
					haoma.gailv = haoma.cishu / sum;
				}
				ReflectUtil.sort(honghaoArray, "cishu", null, true);
				for (int i = 0; i < honghaoArray.length; i++) {
					if (honghaoArray[i] == null) {
						continue;
					}
					honghaoArray[i].index = i;
					honghaoMap.put(honghaoArray[i].haoma, honghaoArray[i]);
				}	
				System.out.println("===============红号概率统计====================");
				for (Haoma haoma : honghaoArray) {
					System.out.println(haoma);
				}
			}
//			IOUtil.write(new File("d:/download/shuangseqiu.txt"), ReflectUtil.cast(haomaMap.keySet().toArray(), String.class).getBytes(), false);
			System.out.println("===============中奖统计====================");
			for (String haomaString : haomaMap.keySet()) {
				System.out.print(haomaString);
				String[] haomaArray = haomaString.split(" ");
				int[] haomaIntArray = new int[haomaArray.length];
				for (int i = 0; i < haomaArray.length; i++) {
					haomaIntArray[i] = ReflectUtil.cast(haomaArray[i], int.class);
				}
				
				{					
					int total = 0;
					System.out.print("===");
					for (int i = 0; i < haomaIntArray.length; i++) {
						if (i > 5) {
							continue;
						}
						total += haomaIntArray[i];
					}
					System.out.print(total < 100 ? "0" + total : total);
				}
				{
					int total = 0;
					System.out.print("===");
					for (int i = 0; i < haomaIntArray.length; i++) {
						total += haomaIntArray[i];
					}
					System.out.print(total < 100 ? "0" + total : total);
				}
				NumberFormatter formatter = new NumberFormatter().setPattern("#.000");
				{					
					double totalGailv = 0;
					System.out.print("===");
					for (int i = 0; i < haomaIntArray.length; i++) {
						if (i > 5) {
							continue;
						}
						totalGailv += honghaoMap.get(haomaIntArray[i]).gailv;
					}
					System.out.print(formatter.format(totalGailv));
				}
				{
					double totalGailv = 0;
					System.out.print("===");
					for (int i = 0; i < haomaIntArray.length; i++) {
						if (i > 5) {
							totalGailv += lanhaoMap.get(haomaIntArray[i]).gailv;
						} else {
							totalGailv += honghaoMap.get(haomaIntArray[i]).gailv;
						}
					}
					System.out.println(formatter.format(totalGailv));
				}
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			IOUtil.closeQuietly(in);
		}
	}
	
	private static class Haoma {
		int haoma = 0;
		int cishu = 0;
		double gailv = 0;
		int index = 0;
		
		@Override
		public String toString() {
			return (haoma >= 10 ? haoma : "0" + haoma) + " : " + gailv + " : " + cishu;
		}
	}
}
/*
 * $Log$ 
 */