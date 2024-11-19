package org.example.drivernoticebot;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        String[] a = {"a", "b", "c", "d"};
        int[] b = {1, 0, 1, 1};
        int k = 3;
        //System.out.println(kthDistinct(a, k));
        //System.out.println(Arrays.toString(countBits(5)));
        //System.out.println(divisorGame(3));
        System.out.println(isSubsequence("astd", "afrds"));

    }

    public static boolean isSubsequence(String s, String t) {
        List<Character> chS = new ArrayList<>();
        List<Character> chT = new ArrayList<>();
        if (s.length() > t.length()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            chS.add(s.charAt(i));
        }
        for (int i = 0; i < t.length(); i++) {
            chT.add(t.charAt(i));
            chS.remove(chT.get(i));
        }
        return chS.isEmpty();
    }

   /* public static boolean divisorGame(int n) {
        return n % 2 == 0;
    }*/

    /*public static int[] countBits(int n) {
        int k, sum = 0;
        List<Integer> list = new ArrayList<>();
        List<Integer> l = new ArrayList<>();
        while (n >= 0) {
            k = n;
            while (k >= 1) {
                sum += k % 2;
                k /= 2;
            }
            n--;
            list.add(sum);
            sum = 0;
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            l.add(list.get(i));
        }
        return l.stream().mapToInt(i -> i).toArray();
    }*/

   /* public int numberOfEmployeesWhoMetTarget(int[] hours, int target) {
        int k = 0;
        for (int hour : hours) {
            if (hour >= target) {
                k++;
            }
        }
        return k;
    }*/

   /* public static String kthDistinct(String[] arr, int k) {
        List<String> l = new ArrayList<>(Arrays.asList(arr));
        List<String> list = new ArrayList<>();
        int p = 0;
        for (int i = 0; i < arr.length; i++) {
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[i].equals(arr[j])) {
                    p++;
                }
            }
            if (p != 0) {
                list.add(arr[i]);
            }
            p = 0;
        }
        l.removeAll(list);
        return l.size() >= k ? l.get(k - 1) : "";
    }*/
}
