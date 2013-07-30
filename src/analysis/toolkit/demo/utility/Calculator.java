/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.toolkit.demo.utility;

import java.util.*;
public class Calculator {
    public static int sum (List<Double> a){
        if (a.size() > 0) {
            int sum = 0;
            for (Double i : a) {
                sum += i;
            }
            return sum;
        }
        return 0;
    }
    public static double mean (List<Double> a){
        int sum = sum(a);
        double mean = 0;
        mean = sum / (a.size() * 1.0);
        return mean;
    }
    public static double median (List<Double> a){
        int middle = a.size()/2;
 
        if (a.size() % 2 == 1) {
            return a.get(middle);
        } else {
           return (a.get(middle-1) + a.get(middle)) / 2.0;
        }
    }
}
