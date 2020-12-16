import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javafx.util.Pair;

public class NaiveBayes {
	
	private static ArrayList<String> classifications = new ArrayList<>();
	private static ArrayList<String> computedClassifications = new ArrayList<>();
	private static ArrayList<ArrayList<String>> meta = new ArrayList<>();
	private static ArrayList<ArrayList<Pair<String, Integer>>> bayes = new ArrayList<ArrayList<Pair<String, Integer>>>();

	public static void main(String[] args) {
		Scanner keyboard = new Scanner(System.in);
		int command = 0;
		String file1;
		String file2;

		while (command != 4) {
			System.out.println("Please enter the number corresponding to the following command that you wish to run.");
			System.out.println("1 - Enter new training files.");
			System.out.println("2 - Classify data.");
			System.out.println("3 - Determine accuracy of data labels.");
			System.out.println("4 - Quit.");
			command = keyboard.nextInt();
			keyboard.nextLine();

			switch (command) {
			case 1:
				
				System.out.println("Enter the name of the metadata file: ");
				file1 = keyboard.nextLine();
				System.out.println("Enter the name of the data file: ");
				file2 = keyboard.nextLine();
				train(file1, file2);
				break;

			case 2:
				if(meta.isEmpty())
				{
					System.out.println("Please train the system before trying to classify data.");
					break;
				}
				System.out.println("Enter the name of the input file: ");
				file1 = keyboard.nextLine();
				System.out.println("Enter the name of the output file: ");
				file2 = keyboard.nextLine();
				classify(file1, file2);
				break;

			case 3:
				if(meta.isEmpty())
				{
					System.out.println("Please train the system before trying to determine its accuracy.");
					break;
				}
				System.out.println("Enter the name of the data file: ");
				file1 = keyboard.nextLine();
				calcAccuracy(file1);
				break;
			}
		}

		keyboard.close();
	}

	private static void train(String mFile, String dFile)
	{
		System.out.println("Meta file name is: " + mFile);
		System.out.println("Training file name is: " + dFile);

		BufferedReader mbr = null;
		BufferedReader dbr = null;
		meta.clear();
		bayes.clear();
		String line = "";
		try {
			mbr = new BufferedReader(new FileReader(mFile));
			dbr = new BufferedReader(new FileReader(dFile));
			int count = 0;
			//stores data from meta file
			while ((line = mbr.readLine()) != null) 
			{
				meta.add(new ArrayList<String>());
				String[] data = line.split(",|\\:");
				for(int j = 1; j < data.length; j++)
					meta.get(count).add(data[j]);	
				count++;
			}
			//sets up storage for naive bayes calculations
			for(int i = 0; i < meta.size(); i++)
			{
				bayes.add(new ArrayList<Pair<String, Integer>>());
				if(i != meta.size() - 1)
				{
					for(int j = 0; j < meta.get(meta.size() - 1).size(); j++)
					{
						Pair<String, Integer> p = null;
						for(int k = 0; k < meta.get(i).size(); k++)
						{
							p = new Pair<String, Integer>(meta.get(i).get(k) + "/", 0);
							bayes.get(i).add(p);
						}
					}
				}
				if(i == meta.size() - 1)
				{
					for(int j = 0; j < meta.get(i).size(); j++)
					{
						Pair<String, Integer> p = new Pair<String, Integer>(meta.get(i).get(j), 0);
						bayes.get(i).add(p);
					}
				}
			}
			for(int i = 0; i < bayes.size() - 1; i++)
			{
				for(int j = 0; j < bayes.get(i).size(); j++)
				{
					Pair<String, Integer> p = new Pair<String, Integer>(bayes.get(i).get(j).getKey() + meta.get(meta.size() - 1).get(j/meta.get(i).size()), 0);
					bayes.get(i).set(j, p);
				}
			}
		
			//stores counts from training file
			count = 0;
			while ((line = dbr.readLine()) != null) 
			{
				String[] data = line.split(",");
				for(int j = 0; j < data.length; j++)
				{
					if(j != data.length - 1)
					{
						String checkKey = data[j] + "/" + data[data.length - 1];
						for(int i = 0; i < bayes.get(j).size(); i++)
						{
		
							if(bayes.get(j).get(i).getKey().equals(checkKey))
							{
								Pair<String, Integer> p = new Pair<String, Integer>(bayes.get(j).get(i).getKey(), bayes.get(j).get(i).getValue() + 1);
								bayes.get(j).set(i, p);
							}
						}
					}
					else
					{
						String checkKey = data[j];
						for(int i = 0; i < bayes.get(bayes.size() - 1).size(); i++)
						{
							if(bayes.get(bayes.size() - 1).get(i).getKey().equals(checkKey))
							{
								Pair<String, Integer> p = new Pair<String, Integer>(bayes.get(bayes.size() - 1).get(i).getKey(), bayes.get(bayes.size() - 1).get(i).getValue() + 1);
								bayes.get(bayes.size() - 1).set(i, p);
							}
						}
					}
				}
				count++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (mbr != null && dbr != null) {
				try {
					mbr.close();
					dbr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("System has been trained.");
	}

	private static void classify(String inF, String outF) {
		System.out.println("Input file name is: " + inF);
		System.out.println("Output file name is: " + outF);

		BufferedReader br = null;
		FileWriter fw = null;
		String line = "";
		String classification;
		try {
			br = new BufferedReader(new FileReader(inF));
			fw = new FileWriter(outF);
			while ((line = br.readLine()) != null) {
				String[] data = line.split(",");
				// prints everything but classification
				for (int i = 0; i < data.length - 1; i++) {
					fw.write(data[i] + ",");
				}
				
				//use data to get a classification
				classification=findClassification(data);
				fw.write(classification + "\n");
				
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null && fw != null) {
				try {
					br.close();
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Data has been classified.");
	}

	private static void calcAccuracy(String inF) {
		System.out.println("Data file name is: " + inF);

		classifications.clear();
		computedClassifications.clear();

		BufferedReader br = null;
		String line = "";
		int compare = 0;
		String classify;

		try {
			br = new BufferedReader(new FileReader(inF));
			while ((line = br.readLine()) != null) {
				String[] data = line.split(",");
				classifications.add(data[data.length - 1]);
				// classify instance
				classify=findClassification(data);
				computedClassifications.add(classify);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		for (int i = 0; i < classifications.size(); i++) {
			if (classifications.get(i).equals(computedClassifications.get(i)))
				compare++;
		}
		double percent;
		percent = ((double)compare/computedClassifications.size())*100; 
		System.out.println("Accuracy has been calculated: " + compare + " computed classifications out of "
				+ computedClassifications.size() + " are correct.");
		String formatted = String.format("%.2f", percent);
		System.out.println(formatted+"% correct.");
	}

	private static String findClassification(String[] data) {
		
		ArrayList<ArrayList<Double>> matrix = new ArrayList<>();
		int size = meta.size();
		//num of possible classifications
		int classes = meta.get(size-1).size();
	
		int total=0;
		String tempKey;
		
		int top;
		double bottom;
		double probability;
		
		//populate total
		for (int i=0;i<classes;i++) {
			total=total+bayes.get(size-1).get(i).getValue();
		}
		
		//for each element in data except the classification create a probability for each possible classification
		for(int i =0 ; i< data.length-1;i++) {
			ArrayList<Double> probabilities = new ArrayList<>();
			//for each classification set temp key
			for(int k=0; k<classes; k++) {
				tempKey=data[i] + "/" + meta.get(size-1).get(k);
				//for every element in bayes check if tempKey matches
				for(int j=0; j<bayes.get(i).size(); j++) {
					if(tempKey.equals(bayes.get(i).get(j).getKey())) {
						top=bayes.get(i).get(j).getValue();
						bottom=bayes.get(size-1).get(k).getValue();
						probability=top/bottom;
						probabilities.add(probability);
					}
				}

			}
			matrix.add(probabilities);
			//probabilities.clear();
		}
		ArrayList<Double> probabilities = new ArrayList<>();
		for (int i=0;i<classes;i++) {
			//add probability for classification
			top=bayes.get(size-1).get(i).getValue();
			bottom=total;
			probability=top/bottom;
			probabilities.add(probability);
		}
		matrix.add(probabilities);
		

		double totalProb=0;
		ArrayList<Double> totalProbs = new ArrayList<>();
		
		for(int k = 0 ; k<classes;k++) {
			for (int i =0; i<matrix.size();i++) {
				//if i=0 add element to totalProb
				if(i==0) {
					totalProb=matrix.get(i).get(k);
				}
				else {
					totalProb=totalProb*matrix.get(i).get(k);
				}
				
			}
			totalProbs.add(totalProb);
		}

		int largestIndex=0;
		double tempLargest=0;
		
		//get largest of totalProb
		for(int i =0; i<totalProbs.size();i++) {
			if (totalProbs.get(i)>tempLargest) {
				tempLargest=totalProbs.get(i);
				largestIndex=i;
			}
		}
		
		return meta.get(size-1).get(largestIndex);
	}
}