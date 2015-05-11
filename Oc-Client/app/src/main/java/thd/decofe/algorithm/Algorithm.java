package thd.decofe.algorithm;

import android.util.Log;

import java.util.ArrayList;
import java.util.Random;


public class Algorithm {
    String TAG = "Algorithm";

    public String longFibonacci(int n){

        long timeIt = System.currentTimeMillis();
        int value = calculateFibonacci(n);
        timeIt = System.currentTimeMillis() - timeIt;

        return "longFibonacci run in "+timeIt+" ms with value:" + value;
    }

    public String longBubbleSort(int listSize){
        Random r = new Random();
        ArrayList<Integer> randomArray = new ArrayList<>(listSize);
        for (int i = 0; i < listSize; i++) {
            randomArray.add(r.nextInt(listSize));
        }

        Log.d(TAG,"before sort:"+randomArray);

        long timeIt = System.currentTimeMillis();
        randomArray = bublesort(randomArray);
        timeIt = System.currentTimeMillis() - timeIt;

        Log.d(TAG,"after sort:"+randomArray);
        return "longBubbleSort run in "+timeIt+" ms with array of size:" + listSize;

    }

    private int calculateFibonacci(int n){

        if(n == 0)
            return 0;
        if(n == 1)
            return 1;

        return calculateFibonacci(n-1) + calculateFibonacci(n-2);
    }

    private ArrayList<Integer> bublesort(ArrayList<Integer> list){
        boolean swapped = true;
        while (swapped == true){
            swapped = false;
            for (int i = 0; i < list.size()-1; i++){
                if(list.get(i) > list.get(i+1)){
                    int temp = list.get(i);
                    list.set(i,list.get(i+1));
                    list.set(i+1,temp);
                    swapped = true;
                }
            }
        }

        return list;
    }

}
