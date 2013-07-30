/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.toolkit.demo.sentiment.utils;

/**
* This class stores the result of searching MPQA dictionary
* @author Xiang Ji
* @version 1.0 07.24.2013
* @email xiangji2010@gmail.com
*/
public class DictionaryLabel {
    public int priorPolarity; //1: positive, -1: negative, 0: neutral
    public int type; //1: weaksubj 2: strongsubj
    public boolean isSet = false; //default is false
}
